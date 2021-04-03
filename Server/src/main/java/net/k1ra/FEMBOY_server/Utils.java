package net.k1ra.FEMBOY_server;

import com.sun.net.httpserver.HttpExchange;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.k1ra.FEMBOY_server.libFEMBOY.DatabaseAbstractionLayer;

import java.io.*;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

public class Utils {

    static List<String> character_tags = new ArrayList<String>();

    public static File base64_decode(String b64_img, int id) {
        String path = get_local_storage_dir()+"images/"+id;
        byte[] byte_array = Base64.getDecoder().decode(b64_img);
        InputStream is = new ByteArrayInputStream(byte_array);

        //Find out image type
        String mime_type = null;
        String file_extension = null;
        try {
            mime_type = URLConnection.guessContentTypeFromStream(is); //mimeType is something like "image/jpeg"
            String delimiter="[/]";
            String[] tokens = mime_type.split(delimiter);
            file_extension = tokens[1];
            is.close();
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }

        path += "."+file_extension;

        try (FileOutputStream out = new FileOutputStream(path)) {
            out.write(byte_array);

            return new File(path);
        } catch (FileNotFoundException e) {
            System.out.println("Image not found" + e);
        } catch (IOException ioe) {
            System.out.println("Exception while reading the Image " + ioe);
        }
        return null;
    }

    public static void populate_chara_tags() {
        try {
            Scanner s = new Scanner(new File(Utils.get_local_storage_dir() + "DD-characters.txt"));
            while (s.hasNext())
                character_tags.add(s.next());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String get_local_storage_dir() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win"))
            return System.getenv("APPDATA") + "/FEMBOY-server/";
        else if (os.contains("mac"))
            return System.getProperty("user.home") + "/Library/Application Support/FEMBOY-server/";
        else if (os.contains("nux"))
            return System.getProperty("user.home") + "/FEMBOY-server/";
        else
            return System.getProperty("user.dir") + "/FEMBOY-server/";
    }

    public static boolean connect_mysql(PageParams... p) {
        try {
            HikariConfig config = new HikariConfig();
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setJdbcUrl("jdbc:mysql://"+ConfigDatabase.get("sql_address")+"/"+ConfigDatabase.get("sql_dbname")+"?useUnicode=true&character_set_server=utf8mb4");
            config.setUsername(ConfigDatabase.get("sql_user"));
            config.setPassword(ConfigDatabase.get("sql_password"));
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("autoReconnect","true");
            config.addDataSourceProperty("characterEncoding","utf8");
            config.addDataSourceProperty("useUnicode","true");
            config.setMaximumPoolSize(5);
            config.setConnectionInitSql("SET NAMES 'utf8mb4'");
            config.setMaximumPoolSize(5);
            config.setMaxLifetime(1000*3);
            DatabaseAbstractionLayer.conn = new HikariDataSource(config).getConnection();
            return true;
        } catch (Exception e) { //if DB connection fails, cannot continue
            System.out.println("ERROR: cannot connect to DB");

            if (p.length > 0)
                p[0].out.put("error_text", get_root_cause(e).getMessage());

            e.printStackTrace();
            return false;
        }
    }

    static Throwable get_root_cause(Throwable e) {
        Throwable cause = null;
        Throwable result = e;

        while(null != (cause = result.getCause())  && (result != cause) ) {
            result = cause;
        }
        return result;
    }

    static void copy_stream(InputStream is, File out) {
        try {
            OutputStream os = null;
            try {
                os = new FileOutputStream(out);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            } finally {
                is.close();
                os.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //function to get user IP
    public static String get_IP(final HttpExchange t){
        return t.getRemoteAddress().getAddress().toString().replace("/","");
    }

    //function to get user-agent
    public static String get_user_agent(final HttpExchange t){
        return t.getRequestHeaders().getFirst("User-Agent");
    }

    public static void delete_image(int id, String extension) {
        try {
            File img = new File(Utils.get_local_storage_dir() + "images/" + id + extension);
            img.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
