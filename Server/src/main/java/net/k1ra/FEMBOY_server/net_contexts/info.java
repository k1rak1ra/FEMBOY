package net.k1ra.FEMBOY_server.net_contexts;

import com.sun.net.httpserver.HttpExchange;
import net.k1ra.FEMBOY_server.HANDLER_BASE;
import net.k1ra.FEMBOY_server.PageParams;
import org.json.JSONObject;

import java.io.IOException;

import static net.k1ra.FEMBOY_server.Main.is_set_up;
import static net.k1ra.FEMBOY_server.Main.server_feature_level;

public class info extends HANDLER_BASE {

    public info(final boolean verify_session, final boolean admin, final boolean verify_feature_level) {
        super(verify_session, admin, verify_feature_level);
    }

    @Override
    public void handle(final HttpExchange t) throws IOException {
        //Handle parameter parsing. If connection was closed, do nothing
        final PageParams p = new PageParams();
        prep(t, p);
        if (p.closed) {return;}

        try {
            success(p);
            final JSONObject data = new JSONObject();
            data.put("setup_complete", is_set_up? 1:0);
            data.put("server_feature_level", server_feature_level);

            p.out.put("data", data);
        } catch (Exception e){
            handle_error(t, e);
            return;
        }

        done(t, p);
    }

}
