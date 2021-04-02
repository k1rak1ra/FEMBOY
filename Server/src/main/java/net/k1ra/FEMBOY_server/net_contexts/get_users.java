package net.k1ra.FEMBOY_server.net_contexts;

import com.sun.net.httpserver.HttpExchange;
import net.k1ra.FEMBOY_server.libFEMBOY.DatabaseAbstractionLayer;
import net.k1ra.FEMBOY_server.HANDLER_BASE;
import net.k1ra.FEMBOY_server.PageParams;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class get_users extends HANDLER_BASE {

    public get_users(final boolean verify_session, final boolean admin, final boolean verify_feature_level) {
        super(verify_session, admin, verify_feature_level);
    }

    @Override
    public void handle(final HttpExchange t) throws IOException {
        //Handle parameter parsing. If connection was closed, do nothing
        final PageParams p = new PageParams();
        prep(t, p);
        if (p.closed) {return;}

        try {

            List<DatabaseAbstractionLayer.User> list = DatabaseAbstractionLayer.get_users();
            JSONArray arr = new JSONArray();

            for (DatabaseAbstractionLayer.User user : list) {
                JSONObject obj = new JSONObject();
                obj.put("uid", user.uid);
                obj.put("name", user.name);
                obj.put("admin", user.admin);
                arr.put(obj);
            }

            p.out.put("data", arr);
            success(p);

        } catch (Exception e){
            handle_error(t, e);
            return;
        }

        done(t, p);
    }

}
