package net.k1ra.FEMBOY_server.net_contexts;

import com.sun.net.httpserver.HttpExchange;
import net.k1ra.FEMBOY_server.ConfigDatabase;
import net.k1ra.FEMBOY_server.HANDLER_BASE;
import net.k1ra.FEMBOY_server.LDAPUtils;
import net.k1ra.FEMBOY_server.PageParams;
import org.ldaptive.LdapEntry;

import java.io.IOException;
import java.util.List;

public class run_ldap_user_test extends HANDLER_BASE {

    public run_ldap_user_test(final boolean verify_session, final boolean admin, final boolean verify_feature_level) {
        super(verify_session, admin, verify_feature_level);
    }

    @Override
    public void handle(final HttpExchange t) throws IOException {
        //Handle parameter parsing. If connection was closed, do nothing
        final PageParams p = new PageParams();
        prep(t, p);
        if (p.closed) {return;}

        LDAPUtils.get_user(p.params.get("user"), list -> {
            try {
                StringBuilder s = new StringBuilder();
                for (LdapEntry e : list)
                    s.append(e.toString()).append("\n").append("\n");

                p.out.put("data",s.toString());
                success(p);
                done(t, p);
            } catch (Exception e) {
                handle_error(t, e);
            }
        });
    }

}