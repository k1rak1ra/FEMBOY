package net.k1ra.FEMBOY_server.net_contexts;

import com.sun.net.httpserver.HttpExchange;
import net.k1ra.FEMBOY_server.libFEMBOY.DatabaseAbstractionLayer;
import net.k1ra.FEMBOY_server.HANDLER_BASE;
import net.k1ra.FEMBOY_server.PageParams;

import java.io.IOException;

public class log_out  extends HANDLER_BASE {

    public log_out(final boolean verify_session, final boolean admin, final boolean verify_feature_level) {
        super(verify_session, admin, verify_feature_level);
    }

    @Override
    public void handle(final HttpExchange t) throws IOException {
        //Handle parameter parsing. If connection was closed, do nothing
        final PageParams p = new PageParams();
        prep(t, p);
        if (p.closed) {return;}

        try {
            if (p.params.get("all").equals("1")) {
                DatabaseAbstractionLayer.logout_all(p.params.get("uid"));
            } else {
                DatabaseAbstractionLayer.logout(p.params.get("token"), p.params.get("uid"));
            }

            success(p);

        } catch (Exception e){
            handle_error(t, e);
            return;
        }

        done(t, p);
    }

}
