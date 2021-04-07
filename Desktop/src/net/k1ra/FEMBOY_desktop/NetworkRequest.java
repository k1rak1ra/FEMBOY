package net.k1ra.FEMBOY_desktop;

import javafx.application.Platform;
import net.k1ra.FEMBOY_desktop.libFEMBOY.DatabaseAbstractionLayer;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public abstract class NetworkRequest {
    public static String server_url = "";

    public static Runnable loading = null;
    public static Runnable done_loading = null;
    public static Runnable no_internet = null;

    public static void make_POST(final NetworkResponse out, final String p, final Pair[] params,
                                 final Runnable onSuccess, final Runnable onError, final boolean send_token, final boolean... man_debug)
    {
        final boolean debug = man_debug.length > 0 || true;

        if (loading != null)
            loading.run();

        Thread worker = new Thread() {
            public void run() {
                try {
                    final URL url = new URL(server_url+p);
                    if (debug) { System.out.println(url.toString()); }
                    final HttpURLConnection client = (HttpURLConnection) url.openConnection();

                    client.setReadTimeout(100000);
                    client.setConnectTimeout(150000);
                    client.setRequestMethod("POST");
                    client.setDoInput(true);
                    client.setDoOutput(true);

                    StringBuilder parameters = new StringBuilder();
                    parameters.append("feature_level").append("=").append(URLEncoder.encode(String.valueOf(Main.feature_level), "UTF-8"));

                    if (params != null)
                        for (int i = 0; i < params.length; i++) {
                            parameters.append("&").append(params[i].first).append("=").append(URLEncoder.encode(params[i].second, "UTF-8"));
                            if (man_debug.length > 1){ System.out.println(params[i].first+"  -  "+params[i].second); }
                        }

                    if (send_token) {
                        parameters.append("&").append("uid").append("=").append(URLEncoder.encode(DatabaseAbstractionLayer.client_get_uid(), "UTF-8"));
                        parameters.append("&").append("token").append("=").append(URLEncoder.encode(DatabaseAbstractionLayer.client_get_token(), "UTF-8"));
                    }


                    final OutputStream os = client.getOutputStream();
                    final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
                    writer.write(parameters.toString());
                    writer.flush();
                    writer.close();
                    os.close();

                    out.sCode = client.getResponseCode();
                    if (debug) { System.out.println("CODE: "+out.sCode); }
                    client.connect();

                    final InputStream in = new BufferedInputStream(client.getInputStream());
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8), 8);
                    final StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }

                    in.close();
                    client.disconnect();
                    if (debug) { System.out.println("OUT: "+sb.toString()); }

                    if (done_loading != null)
                        done_loading.run();


                    out.obj = new JSONObject(sb.toString());
                    if (out.obj.getInt("success") == 1) {
                        Controller.offline.setValue(false);
                        Controller.logged_in.setValue(true);
                        Platform.runLater(onSuccess);
                    } else {
                        out.error = out.obj.getInt("error");
                        if (out.error == 0)
                            Controller.logged_in.setValue(false);

                        Platform.runLater(onError);
                    }


                } catch (Exception e) {
                   e.printStackTrace();

                    if (done_loading != null)
                        done_loading.run();

                    if (p.equals("/info") || p.equals("/do_setup") || p.equals("/login") || p.equals("/upload_image")) {
                        out.error = -1;
                        Platform.runLater(onError);
                    } else
                        Platform.runLater(no_internet);
                }
            }
        };
        worker.start();
    }
}
