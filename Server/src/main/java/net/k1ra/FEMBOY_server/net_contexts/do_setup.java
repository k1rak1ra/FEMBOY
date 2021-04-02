package net.k1ra.FEMBOY_server.net_contexts;

import com.sun.net.httpserver.HttpExchange;
import net.k1ra.FEMBOY_server.*;
import net.k1ra.FEMBOY_server.libFEMBOY.DatabaseAbstractionLayer;
import org.json.JSONObject;

import java.io.IOException;

import static net.k1ra.FEMBOY_server.Main.is_set_up;

public class do_setup extends HANDLER_BASE {

    public do_setup(final boolean verify_session, final boolean admin, final boolean verify_feature_level) {
        super(verify_session, admin, verify_feature_level);
    }

    @Override
    public void handle(final HttpExchange t) throws IOException {
        //Handle parameter parsing. If connection was closed, do nothing
        final PageParams p = new PageParams();
        prep(t, p);
        if (p.closed || is_set_up) {return;}

        try {
            ConfigDatabase.put("sql_address", p.params.get("sql_address"));
            ConfigDatabase.put("sql_dbname", p.params.get("sql_dbname"));
            ConfigDatabase.put("sql_user", p.params.get("sql_user"));
            ConfigDatabase.put("sql_password", p.params.get("sql_password"));

            if(!Utils.connect_mysql(p)) {
                error(1, p);
            } else {
                success(p);

                DatabaseAbstractionLayer.create_tables();
                DatabaseAbstractionLayer.create_tables_mysql();

                final JSONObject data = new JSONObject();
                data.put("session", DatabaseAbstractionLayer.create_user_and_login("root", p.params.get("root_password"), 1));
                is_set_up = true;
                ConfigDatabase.put("setup_done", "1");

                p.out.put("data", data);
            }
        } catch (Exception e){
            handle_error(t, e);
            return;
        }

        done(t, p);
    }

}
