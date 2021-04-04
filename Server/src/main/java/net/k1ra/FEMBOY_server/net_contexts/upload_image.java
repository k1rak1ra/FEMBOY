package net.k1ra.FEMBOY_server.net_contexts;

import com.sun.net.httpserver.HttpExchange;
import net.k1ra.FEMBOY_server.*;
import net.k1ra.FEMBOY_server.libFEMBOY.DatabaseAbstractionLayer;
import net.k1ra.FEMBOY_server.libFEMBOY.TagAbstractionLayer;

import java.io.File;
import java.io.IOException;

public class upload_image extends HANDLER_BASE {

    public upload_image(final boolean verify_session, final boolean admin, final boolean verify_feature_level) {
        super(verify_session, admin, verify_feature_level);
    }

    @Override
    public void handle(final HttpExchange t) throws IOException {
        //Handle parameter parsing. If connection was closed, do nothing
        final PageParams p = new PageParams();
        prep(t, p);
        if (p.closed) {return;}

        try {

            int current_index = DatabaseAbstractionLayer.get_img_index();
            current_index++;
            DatabaseAbstractionLayer.put_img_index(current_index);
            File img = Utils.base64_decode(p.params.get("image"), current_index);
            TagAbstractionLayer.tag_image(img, current_index, Float.parseFloat(p.params.get("aspect")));

            p.out.put("id", current_index);

            success(p);

        } catch (Exception e){
            error(1, p);
        }

        done(t, p);
    }

}