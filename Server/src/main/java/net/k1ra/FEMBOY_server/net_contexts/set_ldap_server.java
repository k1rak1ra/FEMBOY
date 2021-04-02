package net.k1ra.FEMBOY_server.net_contexts;

import com.sun.net.httpserver.HttpExchange;
import net.k1ra.FEMBOY_server.ConfigDatabase;
import net.k1ra.FEMBOY_server.HANDLER_BASE;
import net.k1ra.FEMBOY_server.LDAPUtils;
import net.k1ra.FEMBOY_server.PageParams;
import org.json.JSONObject;

import java.io.IOException;

public class set_ldap_server extends HANDLER_BASE {

    public set_ldap_server(final boolean verify_session, final boolean admin, final boolean verify_feature_level) {
        super(verify_session, admin, verify_feature_level);
    }

    @Override
    public void handle(final HttpExchange t) throws IOException {
        //Handle parameter parsing. If connection was closed, do nothing
        final PageParams p = new PageParams();
        prep(t, p);
        if (p.closed) {return;}

        try {
            if (ConfigDatabase.get("ldap_server") == null)
                LDAPUtils.set_defaults();

            ConfigDatabase.put("ldap_server", p.params.get("ldap_server"));
            ConfigDatabase.put("ldap_tls", p.params.get("ldap_tls"));
            ConfigDatabase.put("ldap_bind_user", p.params.get("ldap_bind_user"));
            ConfigDatabase.put("ldap_bind_password", p.params.get("ldap_bind_password"));
            String error;

            if ((error = LDAPUtils.get_connection_error()) == null) {
                success(p);
            } else {
                p.out.put("data", error);
                error(1, p);
            }
        } catch (Exception e){
            handle_error(t, e);
            return;
        }

        done(t, p);
    }

}