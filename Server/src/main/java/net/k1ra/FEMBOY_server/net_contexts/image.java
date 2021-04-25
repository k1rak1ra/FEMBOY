package net.k1ra.FEMBOY_server.net_contexts;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.k1ra.FEMBOY_server.Utils;

import java.io.*;
import java.net.URLConnection;
import java.nio.file.Files;

public class image implements HttpHandler {


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String filename = exchange.getRequestURI().getPath().split("/")[2].split("\\.")[0];
        File file = new File(Utils.get_local_storage_dir()+"images/"+filename+".png");

        if (!file.exists())
            file = new File(Utils.get_local_storage_dir()+"images/"+filename+".jpg");

        if (!file.exists())
            file = new File(Utils.get_local_storage_dir()+"images/"+filename+".jpeg");

        if (file.exists()) {
            InputStream is = new BufferedInputStream(new FileInputStream(file));
            exchange.getResponseHeaders().set("Content-Type", URLConnection.guessContentTypeFromStream(is));
            exchange.sendResponseHeaders(200, file.length());
            OutputStream output_stream = exchange.getResponseBody();
            Files.copy(file.toPath(), output_stream);
            output_stream.close();
            is.close();
        } else
            exchange.sendResponseHeaders(404,1);

        exchange.close();
    }
}
