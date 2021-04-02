package net.k1ra.FEMBOY_server.net_contexts;

import com.sun.net.httpserver.HttpExchange;
import net.k1ra.FEMBOY_server.libFEMBOY.DatabaseAbstractionLayer;
import net.k1ra.FEMBOY_server.HANDLER_BASE;
import net.k1ra.FEMBOY_server.PageParams;
import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class set_img_tags extends HANDLER_BASE {

    public set_img_tags(final boolean verify_session, final boolean admin, final boolean verify_feature_level) {
        super(verify_session, admin, verify_feature_level);
    }

    @Override
    public void handle(final HttpExchange t) throws IOException {
        //Handle parameter parsing. If connection was closed, do nothing
        final PageParams p = new PageParams();
        prep(t, p);
        if (p.closed) {return;}

        try {

            List<String> tags = new ArrayList<String>();
            JSONArray arr = new JSONArray(p.params.get("tags"));

            for (int i = 0; i < arr.length(); i++)
                tags.add(arr.getString(i));

            DatabaseAbstractionLayer.update_img_tags(Integer.parseInt(p.params.get("id")), tags);
            success(p);

        } catch (Exception e){
            handle_error(t, e);
            return;
        }

        done(t, p);
    }

}
