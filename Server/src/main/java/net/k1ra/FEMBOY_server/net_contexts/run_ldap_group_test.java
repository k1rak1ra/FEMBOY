package net.k1ra.FEMBOY_server.net_contexts;

import com.sun.net.httpserver.HttpExchange;
import net.k1ra.FEMBOY_server.ConfigDatabase;
import net.k1ra.FEMBOY_server.HANDLER_BASE;
import net.k1ra.FEMBOY_server.LDAPUtils;
import net.k1ra.FEMBOY_server.PageParams;
import org.json.JSONObject;
import org.ldaptive.LdapEntry;

import java.io.IOException;

public class run_ldap_group_test extends HANDLER_BASE {

    public run_ldap_group_test(final boolean verify_session, final boolean admin, final boolean verify_feature_level) {
        super(verify_session, admin, verify_feature_level);
    }

    @Override
    public void handle(final HttpExchange t) throws IOException {
        //Handle parameter parsing. If connection was closed, do nothing
        final PageParams p = new PageParams();
        prep(t, p);
        if (p.closed) {return;}

        LDAPUtils.get_groups(p.params.get("user"), list -> {
            try {
                StringBuilder s = new StringBuilder();
                for (String e : list)
                    s.append(e).append("\n");

                JSONObject data = new JSONObject();
                data.put("list", s.toString());
                data.put("is_admin", s.toString().contains(ConfigDatabase.get("ldap_admin_group_name"))? 1:0);

                p.out.put("data",data);
                success(p);
                done(t, p);
            } catch (Exception e) {
                handle_error(t, e);
            }
        });
    }

}
