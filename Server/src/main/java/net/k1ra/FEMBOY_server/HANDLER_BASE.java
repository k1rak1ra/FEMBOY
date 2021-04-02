package net.k1ra.FEMBOY_server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.k1ra.FEMBOY_server.libFEMBOY.DatabaseAbstractionLayer;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class HANDLER_BASE implements HttpHandler {

    public boolean verify_sess;
    public boolean admin = false;
    public boolean verify_feature_level;


    public HANDLER_BASE(final boolean verify_sess, final boolean admin, final boolean verify_feature_level){
        this.verify_sess = verify_sess;
        this.admin = admin;
        this.verify_feature_level = verify_feature_level;
    }

    @Override
    public void handle(final HttpExchange t) throws IOException {}

    //call to parse POST parameters and verify session/page keys
    public void prep(final HttpExchange t, final PageParams p) throws IOException {
        t.getResponseHeaders().set("Content-Type", "text/javascript; charset=UTF-8");
        p.user_IP = Utils.get_IP(t);

        //put POST params into a String
        String line;
        final StringBuilder in = new StringBuilder();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(t.getRequestBody(), StandardCharsets.UTF_8));
        while ((line = reader.readLine()) != null)
            in.append(line);
        final String qs = in.toString();

        //take the String with POST params and turn it into a HashMap
        int last = 0, next, l = qs.length();
        while (last < l) {
            next = qs.indexOf('&', last);
            if (next == -1)
                next = l;

            if (next > last) {
                int eqPos = qs.indexOf('=', last);
                try {
                    if (eqPos < 0 || eqPos > next)
                        p.params.put(URLDecoder.decode(qs.substring(last, next), "utf-8"), "");
                    else
                        p.params.put(URLDecoder.decode(qs.substring(last, eqPos), "utf-8"), URLDecoder.decode(qs.substring(eqPos + 1, next), "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e); // will never happen, utf-8 support is mandatory for java
                }
            }
            last = next + 1;
        }

        //verify client feature level
        if (verify_feature_level && !p.params.get("feature_level").equals(String.valueOf(Main.server_feature_level))){
            error(0, p);
            done(t, p);
            p.closed = true;
        }

        //verify user's session
        if (verify_sess && !DatabaseAbstractionLayer.verify_session(p.params.get("uid"), p.params.get("token"))) {
            error(0, p);
            done(t, p);
            p.closed = true;
        }

        //verify if admin
        if (admin && !DatabaseAbstractionLayer.verify_admin(p.params.get("uid"))) {
            error(0, p);
            done(t, p);
            p.closed = true;
        }

    }

    public void success(final PageParams p){
        p.out.put("success",1);
    }

    public void error(final int error, final PageParams p){
        p.out.put("success",0);
        p.out.put("error",error);
    }

    public void done(final HttpExchange t, final PageParams p) throws IOException{
        t.sendResponseHeaders(200, p.out.toString().getBytes(StandardCharsets.UTF_8).length);
        OutputStream os = t.getResponseBody();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, StandardCharsets.UTF_8));
        writer.write(p.out.toString());
        writer.flush();
        writer.close();
        os.close();
    }

    public void handle_error(final HttpExchange t, Exception e){
        try {
            e.printStackTrace();
            t.sendResponseHeaders(500, 0);

        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

}