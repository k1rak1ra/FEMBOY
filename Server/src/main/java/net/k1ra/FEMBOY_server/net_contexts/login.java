package net.k1ra.FEMBOY_server.net_contexts;

import com.sun.net.httpserver.HttpExchange;
import net.k1ra.FEMBOY_server.*;
import net.k1ra.FEMBOY_server.libFEMBOY.DatabaseAbstractionLayer;
import org.json.JSONObject;

import java.io.IOException;

public class login extends HANDLER_BASE {

    public login(final boolean verify_session, final boolean admin, final boolean verify_feature_level) {
        super(verify_session, admin, verify_feature_level);
    }

    @Override
    public void handle(final HttpExchange t) throws IOException {
        //Handle parameter parsing. If connection was closed, do nothing
        final PageParams p = new PageParams();
        prep(t, p);
        if (p.closed) {return;}

        if (!p.params.get("feature_level").equals(String.valueOf(Main.server_feature_level))) {
            error(2, p);
            done(t, p);
            return;
        }

        try {
            String token = DatabaseAbstractionLayer.user_login(p.params.get("user"), p.params.get("password"));

            if (token == null) {
                LDAPUtils.do_login(p.params.get("user"), p.params.get("password"), ()->{
                    LDAPUtils.get_groups(p.params.get("user"), group_list -> {
                        try {
                            StringBuilder s = new StringBuilder();
                            for (String e : group_list)
                                s.append(e).append("\n");

                            final boolean is_admin = s.toString().contains(ConfigDatabase.get("ldap_admin_group_name"));

                            LDAPUtils.get_user(p.params.get("user"), user_list -> {
                                try {
                                    final String uid = user_list.get(0).getAttribute(ConfigDatabase.get("ldap_user_uid_attribute")).getStringValue();

                                    success(p);
                                    final JSONObject data = new JSONObject();
                                    data.put("session", DatabaseAbstractionLayer.make_token(uid, is_admin? 1:0));
                                    data.put("uid", uid);
                                    data.put("admin", is_admin? 1:0);
                                    p.out.put("data", data);
                                    done(t, p);
                                } catch (Exception e) {
                                    handle_error(t, e);
                                }
                            });
                        } catch (Exception e) {
                            handle_error(t, e);
                        }
                    });
                }, ()->{
                    try {
                        error(1, p);
                        done(t, p);
                    } catch (IOException e) {
                        handle_error(t, e);
                    }
                });
            } else {
                success(p);
                final JSONObject data = new JSONObject();
                data.put("session", token);
                data.put("uid", DatabaseAbstractionLayer.user_uid(p.params.get("user")));
                data.put("admin", DatabaseAbstractionLayer.user_admin(p.params.get("user")));
                p.out.put("data", data);
                done(t, p);
            }

        } catch (Exception e){
            handle_error(t, e);
        }
    }

}
