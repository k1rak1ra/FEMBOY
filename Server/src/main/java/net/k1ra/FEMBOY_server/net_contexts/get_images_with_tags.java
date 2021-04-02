package net.k1ra.FEMBOY_server.net_contexts;

import com.sun.net.httpserver.HttpExchange;
import net.k1ra.FEMBOY_server.libFEMBOY.DatabaseAbstractionLayer;
import net.k1ra.FEMBOY_server.HANDLER_BASE;
import net.k1ra.FEMBOY_server.PageParams;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class get_images_with_tags extends HANDLER_BASE {

    public get_images_with_tags(final boolean verify_session, final boolean admin, final boolean verify_feature_level) {
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

            List<DatabaseAbstractionLayer.ImageIDAspect> list = DatabaseAbstractionLayer.get_images_with_tags(tags);
            JSONObject data = new JSONObject();

            data.put("ids", new JSONArray(new JSONArray(list.stream().map(DatabaseAbstractionLayer.ImageIDAspect::id).collect(Collectors.toList()))));
            data.put("aspects", new JSONArray(new JSONArray(list.stream().map(DatabaseAbstractionLayer.ImageIDAspect::aspect).collect(Collectors.toList()))));

            p.out.put("data", data);
            success(p);

        } catch (Exception e){
            handle_error(t, e);
            return;
        }

        done(t, p);
    }

}