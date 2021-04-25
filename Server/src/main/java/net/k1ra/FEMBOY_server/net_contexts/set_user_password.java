package net.k1ra.FEMBOY_server.net_contexts;

import com.sun.net.httpserver.HttpExchange;
import net.k1ra.FEMBOY_server.libFEMBOY.DatabaseAbstractionLayer;
import net.k1ra.FEMBOY_server.HANDLER_BASE;
import net.k1ra.FEMBOY_server.PageParams;

import java.io.IOException;

public class set_user_password  extends HANDLER_BASE {

    public set_user_password(final boolean verify_session, final boolean admin, final boolean verify_feature_level) {
        super(verify_session, admin, verify_feature_level);
    }

    @Override
    public void handle(final HttpExchange t) throws IOException {
        //Handle parameter parsing. If connection was closed, do nothing
        final PageParams p = new PageParams();
        prep(t, p);
        if (p.closed) {return;}

        try {
            DatabaseAbstractionLayer.set_user_password(p.params.get("target_uid"), p.params.get("password"));
            success(p);

        } catch (Exception e){
            handle_error(t, e);
            return;
        }

        done(t, p);
    }

}
