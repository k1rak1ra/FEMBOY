package net.k1ra.FEMBOY_server;

import com.sleepycat.je.*;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class ConfigDatabase {

    static Database config_db;

    static void init() {
        try {
            DatabaseConfig bdb_conf = new DatabaseConfig();
            EnvironmentConfig env_conf = new EnvironmentConfig();
            env_conf.setAllowCreate(true);
            env_conf.setTransactional(true);
            Environment env = new Environment(new File(Utils.get_local_storage_dir()), env_conf);
            bdb_conf.setAllowCreate(true);
            bdb_conf.setTransactional(true);
            config_db = env.openDatabase(null, "config", bdb_conf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void put(final String key, final String value) {
        try {
            config_db.put(null, new DatabaseEntry(key.getBytes(StandardCharsets.UTF_8)), new DatabaseEntry(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String get(final String key) {
        try {
            DatabaseEntry out = new DatabaseEntry();
            config_db.get(null, new DatabaseEntry(key.getBytes(StandardCharsets.UTF_8)), out, null);

            if (out.getData() != null)
                return new String(out.getData(), StandardCharsets.UTF_8);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
