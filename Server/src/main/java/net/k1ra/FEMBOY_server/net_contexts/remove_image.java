package net.k1ra.FEMBOY_server.net_contexts;

import com.sun.net.httpserver.HttpExchange;
import net.k1ra.FEMBOY_server.libFEMBOY.DatabaseAbstractionLayer;
import net.k1ra.FEMBOY_server.HANDLER_BASE;
import net.k1ra.FEMBOY_server.PageParams;
import net.k1ra.FEMBOY_server.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class remove_image extends HANDLER_BASE {

    public remove_image(final boolean verify_session, final boolean admin, final boolean verify_feature_level) {
        super(verify_session, admin, verify_feature_level);
    }

    @Override
    public void handle(final HttpExchange t) throws IOException {
        //Handle parameter parsing. If connection was closed, do nothing
        final PageParams p = new PageParams();
        prep(t, p);
        if (p.closed) {return;}

        try {
            String file_path;
            String file_extension = null;

            //find image extension
            if (Files.exists(Paths.get(Utils.get_local_storage_dir() + "images/" + p.params.get("id") + ".jpg")))
                file_extension = ".jpg";
            else if (Files.exists(Paths.get(Utils.get_local_storage_dir() + "images/" + p.params.get("id") + ".jpeg")))
                file_extension = ".jpeg";
            else if (Files.exists(Paths.get(Utils.get_local_storage_dir() + "images/" + p.params.get("id") + ".png")))
                file_extension = ".png";

            if (file_extension != null){
                DatabaseAbstractionLayer.remove_image(Integer.parseInt(p.params.get("id")));
                Utils.delete_image(Integer.parseInt(p.params.get("id")), file_extension);
                success(p);
            } else {
                error(1, p);
            }
        } catch (Exception e){
            handle_error(t, e);
            return;
        }

        done(t, p);
    }

}