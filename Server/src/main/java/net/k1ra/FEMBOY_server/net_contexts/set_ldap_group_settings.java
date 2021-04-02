package net.k1ra.FEMBOY_server.net_contexts;

import com.sun.net.httpserver.HttpExchange;
import net.k1ra.FEMBOY_server.ConfigDatabase;
import net.k1ra.FEMBOY_server.HANDLER_BASE;
import net.k1ra.FEMBOY_server.PageParams;

import java.io.IOException;

public class set_ldap_group_settings extends HANDLER_BASE {

    public set_ldap_group_settings(final boolean verify_session, final boolean admin, final boolean verify_feature_level) {
        super(verify_session, admin, verify_feature_level);
    }

    @Override
    public void handle(final HttpExchange t) throws IOException {
        //Handle parameter parsing. If connection was closed, do nothing
        final PageParams p = new PageParams();
        prep(t, p);
        if (p.closed) {return;}

        try {
            ConfigDatabase.put("ldap_group_dn", p.params.get("ldap_group_dn"));
            ConfigDatabase.put("ldap_group_filter", p.params.get("ldap_group_filter"));
            ConfigDatabase.put("ldap_group_name_attribute", p.params.get("ldap_group_name_attribute"));
            ConfigDatabase.put("ldap_admin_group_name", p.params.get("ldap_admin_group_name"));

            success(p);
        } catch (Exception e){
            handle_error(t, e);
            return;
        }

        done(t, p);
    }

}
