package net.k1ra.FEMBOY_server.net_contexts;

import com.sun.net.httpserver.HttpExchange;
import net.k1ra.FEMBOY_server.*;
import net.k1ra.FEMBOY_server.libFEMBOY.DatabaseAbstractionLayer;
import org.json.JSONArray;

import java.io.IOException;

public class get_img_tags extends HANDLER_BASE {

    public get_img_tags(final boolean verify_session, final boolean admin, final boolean verify_feature_level) {
        super(verify_session, admin, verify_feature_level);
    }

    @Override
    public void handle(final HttpExchange t) throws IOException {
        //Handle parameter parsing. If connection was closed, do nothing
        final PageParams p = new PageParams();
        prep(t, p);
        if (p.closed) {return;}

        try {
            JSONArray tags = new JSONArray(DatabaseAbstractionLayer.get_img_tags(Integer.parseInt(p.params.get("id"))));
            p.out.put("data", tags);
            success(p);

        } catch (Exception e){
            error(1, p);
            return;
        }

        done(t, p);
    }

}