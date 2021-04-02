package net.k1ra.FEMBOY_desktop;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javafx.scene.control.Alert;
import net.k1ra.FEMBOY_desktop.libFEMBOY.DatabaseAbstractionLayer;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

public class Utils {
    public static List<String> character_tags = new ArrayList<String>();

    public static void delete_folder(File file){
        for (File subFile : file.listFiles()) {
            if(subFile.isDirectory()) {
                delete_folder(subFile);
            } else {
                subFile.delete();
            }
        }
        file.delete();
    }

    public static String base64_encode(File file) {
        String b64_img = "";
        try (FileInputStream file_in = new FileInputStream(file)) {
            // Reading a Image file from file system
            byte[] image_data = new byte[(int) file.length()];
            file_in.read(image_data);
            b64_img = Base64.getEncoder().encodeToString(image_data);
        } catch (FileNotFoundException e) {
            System.out.println("Image not found" + e);
        } catch (IOException ioe) {
            System.out.println("Exception while reading the Image " + ioe);
        }
        return b64_img;
    }

    public static void populate_chara_tags() {
        try {
            Scanner s = new Scanner(new File(Utils.get_local_storage_dir() + "DD-characters.txt"));
            while (s.hasNext())
                character_tags.add(s.next());
        } catch (Exception e) {
            handle_error(e.toString());
        }
    }

    public static String get_local_storage_dir() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win"))
            return System.getenv("APPDATA") + "/FEMBOY/";
        else if (os.contains("mac"))
            return System.getProperty("user.home") + "/Library/Application Support/FEMBOY/";
        else if (os.contains("nux"))
            return System.getProperty("user.home") + "/.FEMBOY/";
        else
            return System.getProperty("user.dir") + "/.FEMBOY/";
    }

    public static String get_sqlite_db() {
        return get_local_storage_dir()+"db.db";
    }

    public static void connect_db() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setPoolName("FEMBOY");
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:"+get_sqlite_db());
        config.setConnectionTestQuery("SELECT 1");
        config.setMaxLifetime(60000); // 60 Sec
        config.setIdleTimeout(45000); // 45 Sec
        config.setMaximumPoolSize(50); // 50 Connections (including idle connections)
        DatabaseAbstractionLayer.conn = new HikariDataSource(config).getConnection();
    }

    public static String get_extension(File in) {
        return in.getAbsolutePath().split("\\.")[in.getAbsolutePath().split("\\.").length-1];
    }

    public static void copy_image(File in, int id) {
        copy_image(in, new File(get_local_storage_dir()+"images/"+id+"."+get_extension(in)));
    }

    public static void copy_image(File in, File out) {
        try {
            InputStream is = null;
            OutputStream os = null;
            try {
                is = new FileInputStream(in);
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
            handle_error(e.toString());
        }
    }

    public static void copy_stream(InputStream is, File out) {
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
            handle_error(e.toString());
        }
    }

    public static void delete_image(int id, String extension) {
        try {
            File img = new File(Utils.get_local_storage_dir() + "images/" + id + extension);
            img.delete();
        } catch (Exception e) {
            handle_error(e.toString());
        }
    }

    public static void handle_error(String trace) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(trace);
        alert.showAndWait();
    }
}
