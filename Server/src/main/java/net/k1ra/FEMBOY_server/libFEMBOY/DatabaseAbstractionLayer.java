package net.k1ra.FEMBOY_server.libFEMBOY;

import net.k1ra.FEMBOY_server.Utils;
import net.k1ra.FEMBOY_server.libFEMBOY.PW_CRYPT;
import org.json.JSONArray;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class DatabaseAbstractionLayer {
    public static Connection conn;
    public static Consumer<Exception> error_handler = e -> {
        e.printStackTrace();
        Utils.connect_mysql();
    };

    public static void create_tables() {
        try {
            PreparedStatement smt = conn.prepareStatement("CREATE TABLE tags (\n" +
                    "\tid INTEGER PRIMARY KEY,\n" +
                    "\ttags TEXT NOT NULL,\n" +
                    "\tlatest_id INTEGER\n" +
                    ");");
            smt.executeUpdate();
            smt.close();

            smt = conn.prepareStatement("INSERT INTO tags (id, tags, latest_id)  \n" +
                    "VALUES (1, \"[]\", 1);");
            smt.executeUpdate();
            smt.close();

            smt = conn.prepareStatement("CREATE TABLE images (\n" +
                    "\tid INTEGER PRIMARY KEY,\n" +
                    "\ttags TEXT NOT NULL,\n" +
                    "\taspect VARCHAR(500) NULL\n" +
                    ");");
            smt.executeUpdate();
            smt.close();

            smt = conn.prepareStatement("INSERT INTO images (id, tags)  \n" +
                    "VALUES (0, \"[]\");");
            smt.executeUpdate();
            smt.close();

        } catch (Exception e) {
            error_handler.accept(e);
        }
    }

    public static void create_tables_client() {
        try {
            PreparedStatement smt = conn.prepareStatement("CREATE TABLE `session` (\n" +
                    "  `id` INTEGER PRIMARY KEY,\n" +
                    "  `uid` INT NOT NULL,\n" +
                    "  `admin` INT NULL,\n" +
                    "  `name` VARCHAR(500) NOT NULL,\n" +
                    "  `token` VARCHAR(500) NOT NULL);");
            smt.executeUpdate();
            smt.close();

            smt = conn.prepareStatement("CREATE TABLE `settings` (\n" +
                    "  `id` INTEGER PRIMARY KEY,\n" +
                    "  `server` VARCHAR(500) NOT NULL,\n" +
                    "  `local_mode` INT NOT NULL,\n" +
                    "  `setup_done` INT NOT NULL);");
            smt.executeUpdate();
            smt.close();

            smt = conn.prepareStatement("INSERT INTO settings (id, server, local_mode, setup_done)  \n" +
                    "VALUES (1, none, 1, 0);");
            smt.executeUpdate();
            smt.close();

        } catch (Exception e) {
            error_handler.accept(e);
        }
    }

    public static void create_tables_mysql() {
        try {
            PreparedStatement smt = conn.prepareStatement("CREATE TABLE `FEMBOY`.`user_sessions` (\n" +
                    "  `id` INT NOT NULL AUTO_INCREMENT,\n" +
                    "  `uid` INT NULL,\n" +
                    "  `admin` INT NULL,\n" +
                    "  `token` VARCHAR(500) NULL,\n" +
                    "  PRIMARY KEY (`id`));");
            smt.addBatch("CREATE TABLE `FEMBOY`.`users` (\n" +
                    "  `uid` INT NOT NULL AUTO_INCREMENT,\n" +
                    "  `name` VARCHAR(500) NULL,\n" +
                    "  `password` VARCHAR(500) NULL,\n" +
                    "  `admin` INT NULL,\n" +
                    "  PRIMARY KEY (`uid`));");
            smt.executeUpdate();
            smt.executeBatch();
            smt.close();

        } catch (Exception e) {
            error_handler.accept(e);
        }
    }

    public static class User {
        public int uid;
        public String name;
        public int admin;

        public User(int uid, String name, int admin) {
            this.uid = uid;
            this.name = name;
            this.admin = admin;
        }
    }

    public static void delete_user (String uid) {
        try {
            PreparedStatement smt = conn.prepareStatement("DELETE FROM users WHERE uid = ?;");
            smt.setString(1, uid);
            smt.executeUpdate();
            smt.close();

            logout_all(uid);

        } catch (Exception e) {
            error_handler.accept(e);
        }
    }

    public static void set_user_admin (String uid, String admin) {
        try {
            PreparedStatement smt = conn.prepareStatement("UPDATE users SET admin = ?  WHERE uid = ?;");
            smt.setString(1, admin);
            smt.setString(2, uid);
            smt.executeUpdate();
            smt.close();

            logout_all(uid);

        } catch (Exception e) {
            error_handler.accept(e);
        }
    }

    public static boolean set_user_name (String uid, String name) {
        try {
            PreparedStatement chk_smt = conn.prepareStatement("SELECT * FROM users WHERE name = ?");
            chk_smt.setString(1, name);
            ResultSet rs = chk_smt.executeQuery();

            if (!rs.next()) {
                rs.close();
                PreparedStatement smt = conn.prepareStatement("UPDATE users SET name = ?  WHERE uid = ?;");
                smt.setString(1, name);
                smt.setString(2, uid);
                smt.executeUpdate();
                smt.close();

                return true;
            }
            rs.close();
        } catch (Exception e) {
            error_handler.accept(e);
        }
        return false;
    }

    public static void set_user_password (String uid, String password) {
        try {
            PreparedStatement smt = conn.prepareStatement("UPDATE users SET password = ?  WHERE uid = ?;");
            smt.setString(1, PW_CRYPT.hash(password));
            smt.setString(2, uid);
            smt.executeUpdate();
            smt.close();

        } catch (Exception e) {
            error_handler.accept(e);
        }
    }

    public static List<User> get_users() {
        List<User> list = new ArrayList<User>();

        try {
            PreparedStatement smt = conn.prepareStatement("SELECT * FROM users;");
            ResultSet rs = smt.executeQuery();

            while (rs.next())
                list.add(new User(rs.getInt("uid"), rs.getString("name"), rs.getInt("admin")));

        } catch (Exception e) {
            error_handler.accept(e);
        }

        return list;
    }

    public static void logout_all(String uid) {
        try {
            PreparedStatement smt = conn.prepareStatement("DELETE FROM user_sessions WHERE uid = ?;");
            smt.setString(1, uid);
            smt.executeUpdate();
            smt.close();
        } catch (Exception e) {
            error_handler.accept(e);
        }
    }

    public static void logout(String token, String uid) {
        try {
            PreparedStatement smt = conn.prepareStatement("DELETE FROM user_sessions WHERE uid = ? AND token = ?;");
            smt.setString(1, uid);
            smt.setString(2, token);
            smt.executeUpdate();
            smt.close();
        } catch (Exception e) {
            error_handler.accept(e);
        }
    }

    public static boolean client_is_admin() {
        try {
            PreparedStatement smt = conn.prepareStatement("SELECT * FROM session WHERE id = 1;");
            ResultSet rs = smt.executeQuery();

            if (rs.next()) {
                int item = rs.getInt("admin");
                rs.close();
                return item==1;
            }

            rs.close();

        } catch (Exception e) {
            error_handler.accept(e);
        }
        return false;
    }

    public static String client_get_name() {
        try {
            PreparedStatement smt = conn.prepareStatement("SELECT * FROM session WHERE id = 1;");
            ResultSet rs = smt.executeQuery();

            if (rs.next()) {
                String item = rs.getString("name");
                rs.close();
                return item;
            }

            rs.close();

        } catch (Exception e) {
            error_handler.accept(e);
        }
        return null;
    }

    public static String client_get_server_setting() {
        try {
            PreparedStatement smt = conn.prepareStatement("SELECT * FROM settings WHERE id = 1;");
            ResultSet rs = smt.executeQuery();

            if (rs.next()) {
                String item = rs.getString("server");
                rs.close();
                return item;
            }

            rs.close();

        } catch (Exception e) {
            //error_handler.accept(e);
        }
        return null;
    }

    public static void client_save_server_setting(String server) {
        try {
            PreparedStatement smt = conn.prepareStatement("UPDATE settings SET server = ?  WHERE id = 1;");
            smt.setString(1, server);
            smt.executeUpdate();
            smt.close();

        } catch (Exception e) {
            error_handler.accept(e);
        }
    }

    public static String client_get_uid() {
        try {
            PreparedStatement smt = conn.prepareStatement("SELECT * FROM session WHERE id = 1;");
            ResultSet rs = smt.executeQuery();

            if (rs.next()) {
                String item = rs.getString("uid");
                rs.close();
                return item;
            }

            rs.close();

        } catch (Exception e) {
            error_handler.accept(e);
        }
        return null;
    }

    public static String client_get_token() {
        try {
            PreparedStatement smt = conn.prepareStatement("SELECT * FROM session WHERE id = 1;");
            ResultSet rs = smt.executeQuery();

            if (rs.next()) {
                String item = rs.getString("token");
                rs.close();
                return item;
            }

            rs.close();

        } catch (Exception e) {
            error_handler.accept(e);
        }
        return null;
    }

    public static void client_logout() {
        try {
            PreparedStatement smt = conn.prepareStatement("DELETE FROM session WHERE id = 1;");
            smt.executeUpdate();
            smt.close();
        } catch (Exception e) {
            error_handler.accept(e);
        }
    }

    public static void client_login(int uid, String name, String token, int admin) {
        try {

            PreparedStatement smt = conn.prepareStatement("INSERT INTO session (id, uid, admin, name, token)  \n" +
                    "VALUES (?, ?, ?, ?, ?);");
            smt.setInt(1, 1);
            smt.setInt(2, uid);
            smt.setInt(3, admin);
            smt.setString(4, name);
            smt.setString(5, token);
            smt.executeUpdate();
            smt.close();
        } catch (Exception e) {
            error_handler.accept(e);
        }
    }

    public static boolean verify_admin(String uid) {
        try {
            PreparedStatement chk_smt = conn.prepareStatement("SELECT * FROM user_sessions WHERE uid = ?");
            chk_smt.setString(1, uid);
            ResultSet chk_rs = chk_smt.executeQuery();

            if (chk_rs.next() && chk_rs.getInt("admin") == 1)
                return true;

        } catch (Exception e) {
            error_handler.accept(e);
        }
        return false;
    }

    public static boolean verify_session(String uid, String token) {
        try {
            PreparedStatement chk_smt = conn.prepareStatement("SELECT * FROM user_sessions WHERE token = ?");
            chk_smt.setString(1, token);
            ResultSet chk_rs = chk_smt.executeQuery();

            if (chk_rs.next() && chk_rs.getString("uid").equals(uid))
                return true;

        } catch (Exception e) {
            error_handler.accept(e);
        }
        return false;
    }

    public static String user_uid(String name) {
        try {
            PreparedStatement smt = conn.prepareStatement("SELECT * FROM users WHERE name = ?");
            smt.setString(1, name);
            ResultSet rs = smt.executeQuery();

            if (rs.next()) {
                String item = rs.getString("uid");
                rs.close();
                return item;
            }

            rs.close();

        } catch (Exception e) {
            error_handler.accept(e);
        }
        return null;
    }

    public static int user_admin(String name) {
        try {
            PreparedStatement smt = conn.prepareStatement("SELECT * FROM users WHERE name = ?");
            smt.setString(1, name);
            ResultSet rs = smt.executeQuery();

            if (rs.next()) {
                int item = rs.getInt("admin");
                rs.close();
                return item;
            }

            rs.close();

        } catch (Exception e) {
            error_handler.accept(e);
        }
        return 0;
    }

    public static String make_token(String uid, int admin) throws Exception {
        while (true) {
            String token = UUID.randomUUID().toString();
            PreparedStatement chk_smt = conn.prepareStatement("SELECT * FROM user_sessions WHERE token = ?");
            chk_smt.setString(1, token);
            ResultSet chk_rs = chk_smt.executeQuery();

            if (!chk_rs.next()) {
                PreparedStatement ins_smt = conn.prepareStatement("INSERT INTO user_sessions (uid, admin, token)  \n" +
                        "VALUES (?, ?, ?);");
                ins_smt.setString(1, uid);
                ins_smt.setInt(2, admin);
                ins_smt.setString(3, token);
                ins_smt.executeUpdate();
                ins_smt.close();

                chk_rs.close();
                return token;
            }
        }
    }

    public static String user_login(String name, String password) {
        try {
            PreparedStatement smt = conn.prepareStatement("SELECT * FROM users WHERE name = ?");
            smt.setString(1, name);
            ResultSet rs = smt.executeQuery();

            if (rs.next() && PW_CRYPT.verify(password, rs.getString("password"))) {
                String token = make_token(rs.getString("uid"), rs.getInt("admin"));
                rs.close();
                return token;
            }

            rs.close();

        } catch (Exception e) {
            error_handler.accept(e);
        }
        return null;
    }

    public static String create_user_and_login(String name, String password, int admin) {
        if (create_user(name, password, admin) != null)
            return user_login(name, password);

        return null;
    }

    public static String create_user(String name, String password, int admin) {
        try {
            PreparedStatement chk_smt = conn.prepareStatement("SELECT * FROM users WHERE name = ?");
            chk_smt.setString(1, name);
            ResultSet rs = chk_smt.executeQuery();

            if (!rs.next()) {
                rs.close();
                PreparedStatement smt = conn.prepareStatement("INSERT INTO users (name, password, admin)  \n" +
                        "VALUES (?, ?, ?);");
                smt.setString(1, name);
                smt.setString(2, PW_CRYPT.hash(password));
                smt.setInt(3, admin);
                smt.executeUpdate();
                smt.close();

                return user_uid(name);
            }

            rs.close();

        } catch (Exception e) {
            error_handler.accept(e);
        }

        return null;
    }

    static void setup_done() {
        try {
            PreparedStatement smt = conn.prepareStatement("UPDATE settings SET setup_done = 1  WHERE id = 1;");
            smt.executeUpdate();
            smt.close();

        } catch (Exception e) {
            error_handler.accept(e);
        }
    }

    static int get_setup_status() {
        try {
            ResultSet rs = conn.prepareStatement("SELECT * FROM settings WHERE id = 1").executeQuery();
            rs.next();
            int val = rs.getInt("setup_done");
            rs.close();
            return val;
        } catch (Exception e) {
            error_handler.accept(e);
            return 0;
        }
    }

    static int get_mode() {
        try {
            ResultSet rs = conn.prepareStatement("SELECT * FROM settings WHERE id = 1").executeQuery();
            rs.next();
            int val = rs.getInt("local_mode");
            rs.close();
            return val;
        } catch (Exception e) {
            error_handler.accept(e);
            return 0;
        }
    }

    static void set_server_mode() {
        try {
            PreparedStatement smt = conn.prepareStatement("UPDATE settings SET local_mode = 0  WHERE id = 1;");
            smt.executeUpdate();
            smt.close();

        } catch (Exception e) {
            error_handler.accept(e);
        }
    }

    public static int get_img_index() {
        try {
            ResultSet rs = conn.prepareStatement("SELECT * FROM tags WHERE id = 1").executeQuery();
            rs.next();
            int index = rs.getInt("latest_id");
            rs.close();
            return index;
        } catch (Exception e) {
            error_handler.accept(e);
            return 0;
        }
    }

    public static void upload_image(int id, List<String> tags, float aspect) {
        JSONArray obj = new JSONArray();

        for (String tag : tags)
            obj.put(tag);

        try {
            PreparedStatement smt = conn.prepareStatement("INSERT INTO images (id, tags, aspect)  \n" +
                    "VALUES (?, ?, ?);");
            smt.setInt(1, id);
            smt.setString(2, obj.toString());
            smt.setString(3, String.valueOf(aspect));
            smt.executeUpdate();
            smt.close();

        } catch (Exception e) {
            error_handler.accept(e);
        }
    }

    public static void remove_image(int id) {
        try {
            PreparedStatement smt = conn.prepareStatement("DELETE FROM images WHERE id = ?;");
            smt.setInt(1, id);
            smt.executeUpdate();
            smt.close();
        } catch (Exception e) {
            error_handler.accept(e);
        }
    }

    public static void put_img_index(int index) {
        try {
            PreparedStatement smt = conn.prepareStatement("UPDATE tags SET latest_id = ?  WHERE id = 1;");
            smt.setInt(1, index);
            smt.executeUpdate();
            smt.close();

        } catch (Exception e) {
            error_handler.accept(e);
        }
    }

    public static void purge_global_tag_list(){
        try {
            PreparedStatement smt = conn.prepareStatement("UPDATE tags SET tags = ?  WHERE id = 1;");
            smt.setString(1, new JSONArray().toString());
            smt.executeUpdate();
            smt.close();
        } catch (Exception e) {
            error_handler.accept(e);
        }
    }

    public static void update_global_tag_list(List<String> tags) {
        try {
            ResultSet rs = conn.prepareStatement("SELECT * FROM tags WHERE id = 1").executeQuery();
            rs.next();
            String json = rs.getString("tags");
            rs.close();

            JSONArray arr = new JSONArray(json);

            for (String tag : tags) {
                if (!json.contains("\""+tag+"\"")) {
                    arr.put(tag);
                }
            }

            PreparedStatement smt = conn.prepareStatement("UPDATE tags SET tags = ?  WHERE id = 1;");
            smt.setString(1, arr.toString());
            smt.executeUpdate();
            smt.close();


        } catch (SQLException e) {
            error_handler.accept(e);
        }
    }

    public static List<String> get_img_tags(int id) {
        try {
            PreparedStatement smt = conn.prepareStatement("SELECT * FROM images WHERE id = ?");
            smt.setInt(1, id);
            ResultSet rs = smt.executeQuery();
            if (rs.next()) {
                String json = rs.getString("tags");
                rs.close();
                smt.close();

                JSONArray arr = new JSONArray(json);

                List<String> out = new ArrayList<String>();

                for (int i = 0; i < arr.length(); i++)
                    out.add(arr.getString(i));

                return out;
            }
        } catch (SQLException e) {
            error_handler.accept(e);
        }
        return null;
    }

    public static void update_img_tags(int id, List<String> tags) {
        try {
            JSONArray arr = new JSONArray();

            for (String tag : tags)
                arr.put(tag);

            PreparedStatement smt = conn.prepareStatement("UPDATE images SET tags = ?  WHERE id = ?;");
            smt.setString(1, arr.toString());
            smt.setInt(2,id);
            smt.executeUpdate();
            smt.close();

            update_global_tag_list(tags);
        } catch (SQLException e) {
            error_handler.accept(e);
        }
    }

    public static List<String> get_all_tags() {
        try {
            PreparedStatement smt = conn.prepareStatement("SELECT * FROM tags WHERE id = 1");
            ResultSet rs = smt.executeQuery();
            rs.next();
            String json = rs.getString("tags");
            rs.close();

            JSONArray arr = new JSONArray(json);

            List<String> out = new ArrayList<String>();

            for (int i = 0; i < arr.length(); i++)
                out.add(arr.getString(i));

            rs.close();
            smt.close();
            return out;
        } catch (SQLException e) {
            error_handler.accept(e);
        }
        return null;
    }

    //class for image with id and aspect ratio
    public static class ImageIDAspect {
        public int id;
        public float aspect;

        public ImageIDAspect(int id, float aspect) {
            this.id = id;
            this.aspect = aspect;
        }

        public int id() {
            return id;
        }

        public float aspect() {
            return aspect;
        }
    }

    public static List<ImageIDAspect> get_images_with_tags(List<String> tags) {
        try {
            StringBuilder query = new StringBuilder("tags LIKE '%'");
            for (String tag : tags)
                query.append("AND tags LIKE '%").append(tag).append("%'");

            PreparedStatement smt = conn.prepareStatement("SELECT * FROM images WHERE "+query);
            ResultSet rs = smt.executeQuery();

            List<ImageIDAspect> list = new ArrayList<ImageIDAspect>();

            while(rs.next())
                if (rs.getInt("id") != 0)
                    list.add(new ImageIDAspect(rs.getInt("id"), rs.getFloat("aspect")));

            rs.close();
            smt.close();

            return list;
        } catch (SQLException e) {
            error_handler.accept(e);
        }
        return null;
    }
}