package net.k1ra.FEMBOY_server;

import com.sun.net.httpserver.HttpServer;
import net.k1ra.FEMBOY_server.libFEMBOY.DatabaseAbstractionLayer;
import net.k1ra.FEMBOY_server.libFEMBOY.TagAbstractionLayer;
import net.k1ra.FEMBOY_server.net_contexts.*;

import java.io.File;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;

public class Main {

    public static boolean is_set_up = false;
    public static int server_feature_level = 1;
    static int port = 5000;

    public static void main(String[] args) throws Exception {
        System.out.println("FEMBOY server feature level "+server_feature_level+" stating up.");

        //check for data/folder and create if does not exit
        if (!Files.exists(Paths.get(Utils.get_local_storage_dir()))) {
            System.out.println("Application data directory does not exist. Creating it at "+Utils.get_local_storage_dir());
            new File(Utils.get_local_storage_dir()).mkdir();
            new File(Utils.get_local_storage_dir()+"images/").mkdir();

            //copy model and tags to directory
            InputStream is = Main.class.getResourceAsStream("DD_data/DD-model.zip");
            Utils.copy_stream(is, new File(Utils.get_local_storage_dir()+"DD-model.zip"));
            is = Main.class.getResourceAsStream("DD_data/DD-tags.txt");
            Utils.copy_stream(is, new File(Utils.get_local_storage_dir()+"DD-tags.txt"));
            is = Main.class.getResourceAsStream("DD_data/DD-characters.txt");
            Utils.copy_stream(is, new File(Utils.get_local_storage_dir()+"DD-characters.txt"));
        }

        //init database with config params
        System.out.println("Loading config");
        ConfigDatabase.init();

        //load DD model
        System.out.println("Loading DeepDanbooru model into RAM");
        TagAbstractionLayer.init();
        Utils.populate_chara_tags();
        System.out.println("DeepDanbooru model loaded!");

        //init HTTP server
        System.out.println("HTTP server init");
        //init HTTPserver
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newCachedThreadPool()); // use as many threads as needed
        server.start();

        //create info and setup contexts
        server.createContext("/info", new info(false, false, false));
        server.createContext("/do_setup", new do_setup(false, false, true));

        //check if DB set up
        if (ConfigDatabase.get("setup_done") == null) {
            System.out.println("SQL not set up, server is in setup mode!");
        } else {
            System.out.println("SQL is set up, connecting...");
            is_set_up = true;
            Utils.connect_mysql();
        }

        //create regular contexts
        server.createContext("/login", new login(false, false, false));
        server.createContext("/upload_image", new upload_image(true, false, true));
        server.createContext("/image", new image());
        server.createContext("/get_img_tags", new get_img_tags(true, false, true));
        server.createContext("/set_img_tags", new set_img_tags(true, false, true));
        server.createContext("/get_images_with_tags", new get_images_with_tags(true, false, true));
        server.createContext("/get_all_tags", new get_all_tags(true, false, true));
        server.createContext("/remove_image", new remove_image(true, false, true));
        server.createContext("/log_out", new log_out(true, false, true));

        //create admin contexts
        server.createContext("/get_users", new get_users(true, true, true));
        server.createContext("/create_user", new create_user(true, true, true));
        server.createContext("/set_user_password", new set_user_password(true, true, true));
        server.createContext("/set_user_name", new set_user_name(true, true, true));
        server.createContext("/set_user_admin", new set_user_admin(true, true, true));
        server.createContext("/delete_user", new delete_user(true, true, true));
        server.createContext("/get_ldap_status", new get_ldap_status(true, true, true));
        server.createContext("/set_ldap_server", new set_ldap_server(true, true, true));
        server.createContext("/set_ldap_user_settings", new set_ldap_user_settings(true, true, true));
        server.createContext("/set_ldap_group_settings", new set_ldap_group_settings(true, true, true));
        server.createContext("/run_ldap_user_test", new run_ldap_user_test(true, true, true));
        server.createContext("/run_ldap_group_test", new run_ldap_group_test(true, true, true));

        System.out.println("Done! Server running");

        //keep-alive task
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                DatabaseAbstractionLayer.get_users();
            }
        }, 0, 2 * 60 * 1000);

        //global tag list update task to prune unused tags
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                List<String> tags = new ArrayList<>();
                List<DatabaseAbstractionLayer.ImageIDAspect> images = DatabaseAbstractionLayer.get_images_with_tags(new ArrayList<>());

                for (DatabaseAbstractionLayer.ImageIDAspect image : images) {
                    List<String> img_tags = DatabaseAbstractionLayer.get_img_tags(image.id);
                    for (String tag : img_tags)
                        if (!tags.contains(tag))
                            tags.add(tag);

                }
                DatabaseAbstractionLayer.purge_global_tag_list();
                DatabaseAbstractionLayer.update_global_tag_list(tags);
                System.out.println("Hourly all_tag list tag recheck");
            }
        }, 0, 60 * 60 * 1000);
    }

}
