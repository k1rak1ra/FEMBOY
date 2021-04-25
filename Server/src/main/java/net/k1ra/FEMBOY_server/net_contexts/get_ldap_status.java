package net.k1ra.FEMBOY_server.net_contexts;

import com.sun.net.httpserver.HttpExchange;
import net.k1ra.FEMBOY_server.*;
import org.json.JSONObject;

import java.io.IOException;

public class get_ldap_status extends HANDLER_BASE {

    public get_ldap_status(final boolean verify_session, final boolean admin, final boolean verify_feature_level) {
        super(verify_session, admin, verify_feature_level);
    }

    @Override
    public void handle(final HttpExchange t) throws IOException {
        //Handle parameter parsing. If connection was closed, do nothing
        final PageParams p = new PageParams();
        prep(t, p);
        if (p.closed) {return;}

        try {
            JSONObject data = new JSONObject();

            if (LDAPUtils.get_ldap_connection() == null) {
                data.put("set_up", 0);
                p.out.put("data", data);
                success(p);
            } else {
                data.put("set_up", 1);
                String error;
                if ((error = LDAPUtils.get_connection_error()) == null) {
                    data.put("ldap_server", ConfigDatabase.get("ldap_server"));
                    data.put("ldap_tls", ConfigDatabase.get("ldap_tls"));
                    data.put("ldap_bind_user", ConfigDatabase.get("ldap_bind_user"));
                    data.put("ldap_bind_password", ConfigDatabase.get("ldap_bind_password"));
                    data.put("ldap_user_dn", ConfigDatabase.get("ldap_user_dn"));
                    data.put("ldap_user_filter", ConfigDatabase.get("ldap_user_filter"));
                    data.put("ldap_user_uid_attribute", ConfigDatabase.get("ldap_user_uid_attribute"));
                    data.put("ldap_group_dn", ConfigDatabase.get("ldap_group_dn"));
                    data.put("ldap_group_filter", ConfigDatabase.get("ldap_group_filter"));
                    data.put("ldap_group_name_attribute", ConfigDatabase.get("ldap_group_name_attribute"));
                    data.put("ldap_admin_group_name", ConfigDatabase.get("ldap_admin_group_name"));
                    p.out.put("data", data);
                    success(p);
                } else {
                    p.out.put("data", error);
                    error(1, p);
                }
            }
        } catch (Exception e){
            handle_error(t, e);
            return;
        }

        done(t, p);
    }

}