package net.k1ra.FEMBOY_server;

import org.ldaptive.*;
import org.ldaptive.auth.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LDAPUtils {
    public static ConnectionConfig get_ldap_connection() {
        if (ConfigDatabase.get("ldap_server") == null || ConfigDatabase.get("ldap_server").isEmpty())
            return null;
        else if (ConfigDatabase.get("ldap_bind_user") == null || ConfigDatabase.get("ldap_bind_user").isEmpty())
            return ConnectionConfig.builder()
                    .url(ConfigDatabase.get("ldap_server"))
                    .useStartTLS(ConfigDatabase.get("ldap_tls").equals("1"))
                    .build();
        else
            return ConnectionConfig.builder()
                    .url(ConfigDatabase.get("ldap_server"))
                    .useStartTLS(ConfigDatabase.get("ldap_tls").equals("1"))
                    .connectionInitializers(BindConnectionInitializer.builder()
                            .dn(ConfigDatabase.get("ldap_bind_user"))
                            .credential(ConfigDatabase.get("ldap_bind_password"))
                            .build())
                    .build();
    }

    public static void set_defaults() {
        ConfigDatabase.put("ldap_admin_group_name", "admins");
        ConfigDatabase.put("ldap_group_dn", "cn=groups,cn=compat,dc=k1ra,dc=local");
        ConfigDatabase.put("ldap_group_filter", "(&(objectClass=posixGroup)(memberUid={user}))");
        ConfigDatabase.put("ldap_group_name_attribute", "cn");

        ConfigDatabase.put("ldap_user_dn", "cn=users,cn=accounts,dc=k1ra,dc=local");
        ConfigDatabase.put("ldap_user_filter", "uid={user}");
        ConfigDatabase.put("ldap_user_uid_attribute", "uidNumber");
    }

    public static String get_connection_error() {
        try {
            SearchOperation search = new SearchOperation(
                    new DefaultConnectionFactory(get_ldap_connection()), "");
            SearchResponse response = search.execute("(uid=*)");
        } catch (Exception e) {
            ConfigDatabase.put("ldap_server","");
            return Utils.get_root_cause(e).getMessage();
        }
        return null;
    }


    public static void get_user(String user, Consumer<List<LdapEntry>> runnable) {
        List<LdapEntry> list = new ArrayList<>();
        try {
            SearchOperation user_search = SearchOperation.builder()
                    .factory(new DefaultConnectionFactory(get_ldap_connection()))
                    .onEntry(entry -> {
                        list.add(entry);
                        return entry;
                    }).onResult(result -> runnable.accept(list)).build();
            user_search.send(SearchRequest.builder()
                    .dn(ConfigDatabase.get("ldap_user_dn"))
                    .filter(ConfigDatabase.get("ldap_user_filter").replace("{user}", user))
                    .build());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void get_groups(String user, Consumer<List<String>> runnable) {
        List<String> list = new ArrayList<>();
        try {
            SearchOperation group_search = SearchOperation.builder()
                    .factory(new DefaultConnectionFactory(get_ldap_connection()))
                    .onEntry(entry -> {
                        list.add(entry.getAttribute(ConfigDatabase.get("ldap_group_name_attribute")).getStringValue());
                        return entry;
                    }).onResult(result->runnable.accept(list)).build();
            group_search.send(SearchRequest.builder()
                    .dn(ConfigDatabase.get("ldap_group_dn"))
                    .filter(ConfigDatabase.get("ldap_group_filter").replace("{user}", user))
                    .returnAttributes(ConfigDatabase.get("ldap_group_name_attribute"))
                    .build());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void do_login(String user, String password, Runnable on_success, Runnable on_fail) {
        try {
            SearchDnResolver auth_resolver = SearchDnResolver.builder()
                    .factory(new DefaultConnectionFactory(get_ldap_connection()))
                    .dn(ConfigDatabase.get("ldap_user_dn"))
                    .filter(ConfigDatabase.get("ldap_user_filter"))
                    .build();

            SimpleBindAuthenticationHandler auth_handler = new SimpleBindAuthenticationHandler(new DefaultConnectionFactory(get_ldap_connection()));
            Authenticator auth = new Authenticator(auth_resolver, auth_handler);
            AuthenticationResponse response = auth.authenticate(new AuthenticationRequest(user, new Credential(password)));
            if (response.isSuccess())
               on_success.run();
            else
                on_fail.run();
        } catch (Exception e) {
            on_fail.run();
        }
    }

}
