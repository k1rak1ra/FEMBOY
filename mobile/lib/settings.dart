import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'network_requests.dart';

List<User> userList = List.empty(growable: true);
bool ldapSetUp = false;
bool ldapTls;

final controllerNewUserName = TextEditingController();
final controllerNewUserPassword1 = TextEditingController();
final controllerNewUserPassword2 = TextEditingController();
final controllerLdapServer = TextEditingController();
final controllerLdapBindUser = TextEditingController();
final controllerLdapBindPassword = TextEditingController();
final controllerLdapUserDn = TextEditingController();
final controllerLdapUserFilter = TextEditingController();
final controllerLdapUserUidAttribute = TextEditingController();
final controllerLdapGroupDn = TextEditingController();
final controllerLdapGroupFilter = TextEditingController();
final controllerLdapGroupNameAttribute = TextEditingController();
final controllerLdapAdminGroupName = TextEditingController();
final controllerUserTest = TextEditingController();
final controllerGroupTest = TextEditingController();

class User {
  int uid;
  String name;
  bool admin;
  User(this.uid, this.name, this.admin);
}

class Settings extends StatefulWidget {
  @override
  SettingsState createState() => SettingsState();
}

class SettingsState extends State<Settings> {
  bool launchTaskRan = false;

  void refreshInfo(BuildContext context) {
    showLoading(context);
    userList.clear();
    makeNetworkRequest({}, "/get_users", (json) {
      List<dynamic> users = json['data'] as List<dynamic>;
      for (Map<String, dynamic> user in users) {
        userList.add(User(user['uid'] as int, user['name'] as String,
            user['admin'] as int == 1));
      }
      makeNetworkRequest({}, "/get_ldap_status", (json) {
        hideLoading();
        Map<String, dynamic> data = json['data'] as Map<String, dynamic>;
        ldapSetUp = data['set_up'] as int == 1;
        ldapTls = false;

        if (ldapSetUp) {
          ldapTls = data['ldap_tls'] as String == "1";
          controllerLdapBindUser.text = data['ldap_bind_user'] as String;
          controllerLdapBindPassword.text =
              data['ldap_bind_password'] as String;
          controllerLdapUserDn.text = data['ldap_user_dn'] as String;
          controllerLdapUserFilter.text = data['ldap_user_filter'] as String;
          controllerLdapUserUidAttribute.text =
              data['ldap_user_uid_attribute'] as String;
          controllerLdapGroupDn.text = data['ldap_group_dn'] as String;
          controllerLdapGroupFilter.text = data['ldap_group_filter'] as String;
          controllerLdapGroupNameAttribute.text =
              data['ldap_group_name_attribute'] as String;
          controllerLdapAdminGroupName.text =
              data['ldap_admin_group_name'] as String;
          controllerLdapServer.text = data['ldap_server'] as String;
        }

        setState(() {});
      }, (json) {
        hideLoading();
        errorDialog(context, json['data'] as String);
        ldapSetUp = false;
        setState(() {});
      }, context);
    }, null, context);
  }

  List<Widget> getTabTitles() {
    if (session.admin && !localMode && !offline) {
      return <Widget>[
        Tab(
          text: "About",
        ),
        Tab(
          text: "Users",
        ),
        Tab(
          text: "LDAP",
        ),
        Tab(
          text: "Libraries used",
        ),
      ];
    } else {
      return <Widget>[
        Tab(
          text: "About",
        ),
        Tab(
          text: "Libraries used",
        ),
      ];
    }
  }

  void userEditDialog(BuildContext context, User user) {
    controllerNewUserName.text = user.name;
    bool admin = user.admin;
    showDialog(
        context: context,
        builder: (BuildContext context2) {
          return StatefulBuilder(builder: (context2, setState) {
            return AlertDialog(
              shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.all(Radius.circular(20.0))),
              backgroundColor: Color(0xff2f3136),
              content: Container(
                child: ListView(
                  shrinkWrap: true,
                  children: <Widget>[
                    Text(
                      "Edit user:",
                      textAlign: TextAlign.center,
                      style: TextStyle(fontSize: 20.0, color: Colors.white),
                    ),
                    SizedBox(
                      height: 5,
                    ),
                    (user.uid != 1)
                        ? TextField(
                            style:
                                TextStyle(fontSize: 16.0, color: Colors.white),
                            minLines: 1,
                            maxLines: 1,
                            controller: controllerNewUserName,
                            decoration: InputDecoration(
                                contentPadding: EdgeInsets.all(0.0),
                                isDense: true,
                                hintStyle: TextStyle(
                                    fontSize: 16.0, color: Colors.white60),
                                enabledBorder: UnderlineInputBorder(
                                  borderSide: BorderSide(color: Colors.white),
                                ),
                                hintText: 'Username'),
                          )
                        : Container(
                            height: 0,
                          ),
                    SizedBox(
                      height: 7,
                    ),
                    TextField(
                      style: TextStyle(fontSize: 16.0, color: Colors.white),
                      minLines: 1,
                      maxLines: 1,
                      obscureText: true,
                      controller: controllerNewUserPassword1,
                      decoration: InputDecoration(
                          contentPadding: EdgeInsets.all(0.0),
                          isDense: true,
                          hintStyle:
                              TextStyle(fontSize: 16.0, color: Colors.white60),
                          enabledBorder: UnderlineInputBorder(
                            borderSide: BorderSide(color: Colors.white),
                          ),
                          hintText: 'Password (blank for unchanged)'),
                    ),
                    SizedBox(
                      height: 7,
                    ),
                    TextField(
                      style: TextStyle(fontSize: 16.0, color: Colors.white),
                      minLines: 1,
                      maxLines: 1,
                      obscureText: true,
                      controller: controllerNewUserPassword2,
                      decoration: InputDecoration(
                          contentPadding: EdgeInsets.all(0.0),
                          isDense: true,
                          hintStyle:
                              TextStyle(fontSize: 16.0, color: Colors.white60),
                          enabledBorder: UnderlineInputBorder(
                            borderSide: BorderSide(color: Colors.white),
                          ),
                          hintText: 'Again (blank for unchanged)'),
                    ),
                    SizedBox(
                      height: 3,
                    ),
                    (user.uid != 1 && user.uid != int.parse(session.uid))
                        ? Row(
                            children: [
                              Switch(
                                  value: admin,
                                  onChanged: (value) {
                                    admin = value;
                                    setState(() {});
                                  }),
                              Text(
                                "User is admin",
                                textAlign: TextAlign.center,
                                style: TextStyle(
                                    fontSize: 15.0, color: Colors.white),
                              )
                            ],
                          )
                        : Container(
                            height: 0,
                          ),
                    ElevatedButton(
                      style: ButtonStyle(
                        backgroundColor:
                            MaterialStateProperty.resolveWith<Color>(
                          (Set<MaterialState> states) {
                            return Color(0xff40444b);
                          },
                        ),
                      ),
                      child: Container(child: Text("Save")),
                      onPressed: () async {
                        if (controllerNewUserPassword1.text !=
                            controllerNewUserPassword2.text) {
                          errorDialog(context,
                              "Passwords do not match. Please re-enter them");
                        } else {
                          bool goOn = true;
                          showLoading(context);
                          if (controllerNewUserName.text != user.name) {
                            await makeNetworkRequest({
                              "name": controllerNewUserName.text,
                              "target_uid": user.uid.toString()
                            }, "/set_user_name", (json) {}, (json) {
                              hideLoading();
                              goOn = false;
                              errorDialog(context,
                                  "A user with this name already exists");
                            }, context);
                          }
                          if (goOn) {
                            makeNetworkRequest({
                              "admin": admin ? "1" : "0",
                              "target_uid": user.uid.toString()
                            }, "/set_user_admin", (json) {
                              if (controllerNewUserPassword1.text.isEmpty) {
                                hideLoading();
                                Navigator.pop(context2);
                                controllerNewUserName.clear();
                                controllerNewUserPassword1.clear();
                                controllerNewUserPassword2.clear();
                                refreshInfo(context);
                              } else {
                                makeNetworkRequest({
                                  "password": controllerNewUserPassword1.text,
                                  "target_uid": user.uid.toString()
                                }, "/set_user_password", (json) {
                                  hideLoading();
                                  Navigator.pop(context2);
                                  controllerNewUserName.clear();
                                  controllerNewUserPassword1.clear();
                                  controllerNewUserPassword2.clear();
                                  refreshInfo(context);
                                }, null, context);
                              }
                            }, null, context);
                          }
                        }
                      },
                    ),
                    SizedBox(
                      height: 10,
                    ),
                    (user.uid != 1 && user.uid != int.parse(session.uid))
                        ? ElevatedButton(
                            style: ButtonStyle(
                              backgroundColor:
                                  MaterialStateProperty.resolveWith<Color>(
                                (Set<MaterialState> states) {
                                  return Colors.red;
                                },
                              ),
                            ),
                            child: Container(child: Text("Delete user")),
                            onPressed: () {
                              showDialog(
                                context: context,
                                builder: (BuildContext context2) {
                                  return AlertDialog(
                                    shape: RoundedRectangleBorder(
                                        borderRadius: BorderRadius.all(
                                            Radius.circular(20.0))),
                                    backgroundColor: Color(0xff2f3136),
                                    content: Container(
                                      child: ListView(
                                        shrinkWrap: true,
                                        children: <Widget>[
                                          Text(
                                            "Are you sure you want to delete this user?",
                                            textAlign: TextAlign.center,
                                            style: TextStyle(
                                                fontSize: 20.0,
                                                color: Colors.white),
                                          ),
                                          SizedBox(
                                            height: 5,
                                          ),
                                          ElevatedButton(
                                            style: ButtonStyle(
                                              backgroundColor:
                                                  MaterialStateProperty
                                                      .resolveWith<Color>(
                                                (Set<MaterialState> states) {
                                                  return Color(0xff40444b);
                                                },
                                              ),
                                            ),
                                            child: Container(
                                                child:
                                                    Text("No, don't delete")),
                                            onPressed: () {
                                              Navigator.pop(context2);
                                            },
                                          ),
                                          SizedBox(
                                            height: 5,
                                          ),
                                          ElevatedButton(
                                            style: ButtonStyle(
                                              backgroundColor:
                                                  MaterialStateProperty
                                                      .resolveWith<Color>(
                                                (Set<MaterialState> states) {
                                                  return Colors.red;
                                                },
                                              ),
                                            ),
                                            child: Container(
                                                child: Text("Yes, delete")),
                                            onPressed: () {
                                              showLoading(context2);
                                              makeNetworkRequest({
                                                "target_uid":
                                                    user.uid.toString()
                                              }, "/delete_user", (json) {
                                                hideLoading();
                                                Navigator.pop(context2);
                                                Navigator.pop(context);
                                                refreshInfo(context);
                                              }, null, context2);
                                            },
                                          )
                                        ],
                                      ),
                                    ),
                                  );
                                },
                              );
                            },
                          )
                        : Container(
                            height: 0,
                          ),
                  ],
                ),
              ),
            );
          });
        });
  }

  void createUserDialog(BuildContext context) {
    bool admin = false;
    controllerNewUserName.clear();
    showDialog(
        context: context,
        builder: (BuildContext context2) {
          return StatefulBuilder(builder: (context2, setState) {
            return AlertDialog(
              shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.all(Radius.circular(20.0))),
              backgroundColor: Color(0xff2f3136),
              content: Container(
                child: ListView(
                  shrinkWrap: true,
                  children: <Widget>[
                    Text(
                      "Create a new user:",
                      textAlign: TextAlign.center,
                      style: TextStyle(fontSize: 20.0, color: Colors.white),
                    ),
                    SizedBox(
                      height: 5,
                    ),
                    TextField(
                      style: TextStyle(fontSize: 16.0, color: Colors.white),
                      minLines: 1,
                      maxLines: 1,
                      controller: controllerNewUserName,
                      decoration: InputDecoration(
                          contentPadding: EdgeInsets.all(0.0),
                          isDense: true,
                          hintStyle:
                              TextStyle(fontSize: 16.0, color: Colors.white60),
                          enabledBorder: UnderlineInputBorder(
                            borderSide: BorderSide(color: Colors.white),
                          ),
                          hintText: 'Username'),
                    ),
                    SizedBox(
                      height: 7,
                    ),
                    TextField(
                      style: TextStyle(fontSize: 16.0, color: Colors.white),
                      minLines: 1,
                      maxLines: 1,
                      obscureText: true,
                      controller: controllerNewUserPassword1,
                      decoration: InputDecoration(
                          contentPadding: EdgeInsets.all(0.0),
                          isDense: true,
                          hintStyle:
                              TextStyle(fontSize: 16.0, color: Colors.white60),
                          enabledBorder: UnderlineInputBorder(
                            borderSide: BorderSide(color: Colors.white),
                          ),
                          hintText: 'Password'),
                    ),
                    SizedBox(
                      height: 7,
                    ),
                    TextField(
                      style: TextStyle(fontSize: 16.0, color: Colors.white),
                      minLines: 1,
                      maxLines: 1,
                      obscureText: true,
                      controller: controllerNewUserPassword2,
                      decoration: InputDecoration(
                          contentPadding: EdgeInsets.all(0.0),
                          isDense: true,
                          hintStyle:
                              TextStyle(fontSize: 16.0, color: Colors.white60),
                          enabledBorder: UnderlineInputBorder(
                            borderSide: BorderSide(color: Colors.white),
                          ),
                          hintText: 'Password again'),
                    ),
                    SizedBox(
                      height: 3,
                    ),
                    Row(
                      children: [
                        Switch(
                            value: admin,
                            onChanged: (value) {
                              admin = value;
                              setState(() {});
                            }),
                        Text(
                          "User is admin",
                          textAlign: TextAlign.center,
                          style: TextStyle(fontSize: 15.0, color: Colors.white),
                        )
                      ],
                    ),
                    ElevatedButton(
                      style: ButtonStyle(
                        backgroundColor:
                            MaterialStateProperty.resolveWith<Color>(
                          (Set<MaterialState> states) {
                            return Color(0xff40444b);
                          },
                        ),
                      ),
                      child: Container(child: Text("Create")),
                      onPressed: () {
                        if (controllerNewUserPassword1.text.isEmpty ||
                            controllerNewUserName.text.isEmpty) {
                          errorDialog(
                              context, "Password and name cannot be blank");
                        } else if (controllerNewUserPassword1.text !=
                            controllerNewUserPassword2.text) {
                          errorDialog(context,
                              "Passwords do not match. Please re-enter them");
                        } else {
                          showLoading(context);
                          makeNetworkRequest({
                            "name": controllerNewUserName.text,
                            "password": controllerNewUserPassword1.text,
                            "admin": admin ? "1" : "0"
                          }, "/create_user", (json) {
                            hideLoading();
                            Navigator.pop(context2);
                            controllerNewUserName.clear();
                            controllerNewUserPassword1.clear();
                            controllerNewUserPassword2.clear();
                            refreshInfo(context);
                          }, (json) {
                            hideLoading();
                            errorDialog(context,
                                "A user with this name already exists");
                          }, context);
                        }
                      },
                    )
                  ],
                ),
              ),
            );
          });
        });
  }

  List<Widget> getUsers(BuildContext context) {
    List<Widget> list = List.empty(growable: true);
    for (User user in userList) {
      list.add(ElevatedButton(
        style: ButtonStyle(
          backgroundColor: MaterialStateProperty.resolveWith<Color>(
            (Set<MaterialState> states) {
              return Color(0xff202225);
            },
          ),
        ),
        child: Container(child: Text(user.name)),
        onPressed: () => userEditDialog(context, user),
      ));
      list.add(SizedBox(
        height: 10,
      ));
    }
    list.add(ElevatedButton(
      style: ButtonStyle(
        backgroundColor: MaterialStateProperty.resolveWith<Color>(
          (Set<MaterialState> states) {
            return Colors.green;
          },
        ),
      ),
      child: Container(
          child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [Icon(Icons.add), Text("Add new user")],
      )),
      onPressed: () => createUserDialog(context),
    ));
    return list;
  }

  List<Widget> ldapTab(BuildContext context) {
    List<Widget> list = List.empty(growable: true);

    list.add(Container(
        decoration: BoxDecoration(
            borderRadius: BorderRadius.all(Radius.circular(20)),
            color: Color(0xff202225)),
        padding: EdgeInsets.all(10),
        child: Column(
          children: [
            Text(
              "Server setup:",
              textAlign: TextAlign.center,
              style: TextStyle(fontSize: 20.0, color: Colors.white),
            ),
            SizedBox(
              height: 5,
            ),
            TextField(
              style: TextStyle(fontSize: 16.0, color: Colors.white),
              minLines: 1,
              maxLines: 1,
              controller: controllerLdapServer,
              decoration: InputDecoration(
                  contentPadding: EdgeInsets.all(0.0),
                  isDense: true,
                  hintStyle: TextStyle(fontSize: 16.0, color: Colors.white60),
                  enabledBorder: UnderlineInputBorder(
                    borderSide: BorderSide(color: Colors.white),
                  ),
                  hintText: 'Server address (ldap://example.com)'),
            ),
            SizedBox(
              height: 7,
            ),
            TextField(
              style: TextStyle(fontSize: 16.0, color: Colors.white),
              minLines: 1,
              maxLines: 1,
              controller: controllerLdapBindUser,
              decoration: InputDecoration(
                  contentPadding: EdgeInsets.all(0.0),
                  isDense: true,
                  hintStyle: TextStyle(fontSize: 16.0, color: Colors.white60),
                  enabledBorder: UnderlineInputBorder(
                    borderSide: BorderSide(color: Colors.white),
                  ),
                  hintText: 'Bind user (Blank for none)'),
            ),
            SizedBox(
              height: 7,
            ),
            TextField(
              style: TextStyle(fontSize: 16.0, color: Colors.white),
              minLines: 1,
              maxLines: 1,
              obscureText: true,
              controller: controllerLdapBindPassword,
              decoration: InputDecoration(
                  contentPadding: EdgeInsets.all(0.0),
                  isDense: true,
                  hintStyle: TextStyle(fontSize: 16.0, color: Colors.white60),
                  enabledBorder: UnderlineInputBorder(
                    borderSide: BorderSide(color: Colors.white),
                  ),
                  hintText: 'Bind user\'s password (Blank for none)'),
            ),
            SizedBox(
              height: 3,
            ),
            Row(
              children: [
                Switch(
                    value: ldapTls,
                    onChanged: (value) {
                      ldapTls = value;
                      setState(() {});
                    }),
                Text(
                  "Use StartTLS",
                  textAlign: TextAlign.center,
                  style: TextStyle(fontSize: 15.0, color: Colors.white),
                )
              ],
            ),
            ElevatedButton(
              style: ButtonStyle(
                backgroundColor: MaterialStateProperty.resolveWith<Color>(
                  (Set<MaterialState> states) {
                    return Color(0xff40444b);
                  },
                ),
              ),
              child: Container(child: Text("Connect")),
              onPressed: () {
                showLoading(context);
                makeNetworkRequest({
                  "ldap_server": controllerLdapServer.text,
                  "ldap_tls": ldapTls ? "1" : "0",
                  "ldap_bind_user": controllerLdapBindUser.text,
                  "ldap_bind_password": controllerLdapBindPassword.text
                }, "/set_ldap_server", (json) {
                  hideLoading();
                  errorDialog(
                      context,
                      "Successfully connected to server and ran test query \n \nIf you have not yet set up user/group LDAP filters and DNs, you should do so now. " +
                          "Defaults are what the developer uses for his freeIPA server. If you use freeIPA, you should be able to just replace " +
                          "k1ra and local with you own base DN and be good to go. If you do not, then an LDAP directory explorer application " +
                          "and the filter testing buttons are your friends.");
                  refreshInfo(context);
                }, (json) {
                  hideLoading();
                  errorDialog(
                      context,
                      "Could not connect to LDAP server: \n" +
                          (json['data'] as String));
                  refreshInfo(context);
                }, context);
              },
            )
          ],
        )));

    if (ldapSetUp) {
      list.add(SizedBox(
        height: 20,
      ));
      list.add(Container(
          decoration: BoxDecoration(
              borderRadius: BorderRadius.all(Radius.circular(20)),
              color: Color(0xff202225)),
          padding: EdgeInsets.all(10),
          child: Column(
            children: [
              Text(
                "User setup:",
                textAlign: TextAlign.center,
                style: TextStyle(fontSize: 20.0, color: Colors.white),
              ),
              SizedBox(
                height: 5,
              ),
              TextField(
                style: TextStyle(fontSize: 16.0, color: Colors.white),
                minLines: 1,
                maxLines: 1,
                controller: controllerLdapUserDn,
                decoration: InputDecoration(
                    contentPadding: EdgeInsets.all(0.0),
                    isDense: true,
                    hintStyle: TextStyle(fontSize: 16.0, color: Colors.white60),
                    enabledBorder: UnderlineInputBorder(
                      borderSide: BorderSide(color: Colors.white),
                    ),
                    hintText: 'User DN'),
              ),
              SizedBox(
                height: 7,
              ),
              TextField(
                style: TextStyle(fontSize: 16.0, color: Colors.white),
                minLines: 1,
                maxLines: 1,
                controller: controllerLdapUserFilter,
                decoration: InputDecoration(
                    contentPadding: EdgeInsets.all(0.0),
                    isDense: true,
                    hintStyle: TextStyle(fontSize: 16.0, color: Colors.white60),
                    enabledBorder: UnderlineInputBorder(
                      borderSide: BorderSide(color: Colors.white),
                    ),
                    hintText: 'User filter'),
              ),
              SizedBox(
                height: 7,
              ),
              TextField(
                style: TextStyle(fontSize: 16.0, color: Colors.white),
                minLines: 1,
                maxLines: 1,
                controller: controllerLdapUserUidAttribute,
                decoration: InputDecoration(
                    contentPadding: EdgeInsets.all(0.0),
                    isDense: true,
                    hintStyle: TextStyle(fontSize: 16.0, color: Colors.white60),
                    enabledBorder: UnderlineInputBorder(
                      borderSide: BorderSide(color: Colors.white),
                    ),
                    hintText: 'User UID attribute'),
              ),
              SizedBox(
                height: 5,
              ),
              ElevatedButton(
                style: ButtonStyle(
                  backgroundColor: MaterialStateProperty.resolveWith<Color>(
                    (Set<MaterialState> states) {
                      return Color(0xff40444b);
                    },
                  ),
                ),
                child: Container(child: Text("Save")),
                onPressed: () {
                  showLoading(context);
                  makeNetworkRequest({
                    "ldap_user_dn": controllerLdapUserDn.text,
                    "ldap_user_filter": controllerLdapUserFilter.text,
                    "ldap_user_uid_attribute":
                        controllerLdapUserUidAttribute.text
                  }, "/set_ldap_user_settings", (json) {
                    hideLoading();
                    errorDialog(context,
                        "Saved user query details\n\nYou should run a test now");
                  }, null, context);
                },
              ),
              SizedBox(
                height: 5,
              ),
              Text(
                "Config test:",
                textAlign: TextAlign.center,
                style: TextStyle(fontSize: 20.0, color: Colors.white),
              ),
              SizedBox(
                height: 5,
              ),
              TextField(
                style: TextStyle(fontSize: 16.0, color: Colors.white),
                minLines: 1,
                maxLines: 1,
                controller: controllerUserTest,
                decoration: InputDecoration(
                    contentPadding: EdgeInsets.all(0.0),
                    isDense: true,
                    hintStyle: TextStyle(fontSize: 16.0, color: Colors.white60),
                    enabledBorder: UnderlineInputBorder(
                      borderSide: BorderSide(color: Colors.white),
                    ),
                    hintText: 'Enter a user'),
              ),
              SizedBox(
                height: 5,
              ),
              ElevatedButton(
                style: ButtonStyle(
                  backgroundColor: MaterialStateProperty.resolveWith<Color>(
                    (Set<MaterialState> states) {
                      return Color(0xff40444b);
                    },
                  ),
                ),
                child: Container(child: Text("Fetch details")),
                onPressed: () {
                  showLoading(context);
                  makeNetworkRequest(
                      {"user": controllerUserTest.text}, "/run_ldap_user_test",
                      (json) {
                    hideLoading();
                    errorDialog(context, json['data'] as String);
                  }, null, context);
                },
              )
            ],
          )));
      list.add(SizedBox(
        height: 20,
      ));
      list.add(Container(
          decoration: BoxDecoration(
              borderRadius: BorderRadius.all(Radius.circular(20)),
              color: Color(0xff202225)),
          padding: EdgeInsets.all(10),
          child: Column(
            children: [
              Text(
                "Group setup:",
                textAlign: TextAlign.center,
                style: TextStyle(fontSize: 20.0, color: Colors.white),
              ),
              SizedBox(
                height: 5,
              ),
              TextField(
                style: TextStyle(fontSize: 16.0, color: Colors.white),
                minLines: 1,
                maxLines: 1,
                controller: controllerLdapGroupDn,
                decoration: InputDecoration(
                    contentPadding: EdgeInsets.all(0.0),
                    isDense: true,
                    hintStyle: TextStyle(fontSize: 16.0, color: Colors.white60),
                    enabledBorder: UnderlineInputBorder(
                      borderSide: BorderSide(color: Colors.white),
                    ),
                    hintText: 'Group DN'),
              ),
              SizedBox(
                height: 7,
              ),
              TextField(
                style: TextStyle(fontSize: 16.0, color: Colors.white),
                minLines: 1,
                maxLines: 1,
                controller: controllerLdapGroupFilter,
                decoration: InputDecoration(
                    contentPadding: EdgeInsets.all(0.0),
                    isDense: true,
                    hintStyle: TextStyle(fontSize: 16.0, color: Colors.white60),
                    enabledBorder: UnderlineInputBorder(
                      borderSide: BorderSide(color: Colors.white),
                    ),
                    hintText: 'Group filter'),
              ),
              SizedBox(
                height: 7,
              ),
              TextField(
                style: TextStyle(fontSize: 16.0, color: Colors.white),
                minLines: 1,
                maxLines: 1,
                controller: controllerLdapGroupNameAttribute,
                decoration: InputDecoration(
                    contentPadding: EdgeInsets.all(0.0),
                    isDense: true,
                    hintStyle: TextStyle(fontSize: 16.0, color: Colors.white60),
                    enabledBorder: UnderlineInputBorder(
                      borderSide: BorderSide(color: Colors.white),
                    ),
                    hintText: 'Group name attribute'),
              ),
              SizedBox(
                height: 7,
              ),
              TextField(
                style: TextStyle(fontSize: 16.0, color: Colors.white),
                minLines: 1,
                maxLines: 1,
                controller: controllerLdapAdminGroupName,
                decoration: InputDecoration(
                    contentPadding: EdgeInsets.all(0.0),
                    isDense: true,
                    hintStyle: TextStyle(fontSize: 16.0, color: Colors.white60),
                    enabledBorder: UnderlineInputBorder(
                      borderSide: BorderSide(color: Colors.white),
                    ),
                    hintText: 'Admin group name'),
              ),
              SizedBox(
                height: 5,
              ),
              ElevatedButton(
                style: ButtonStyle(
                  backgroundColor: MaterialStateProperty.resolveWith<Color>(
                    (Set<MaterialState> states) {
                      return Color(0xff40444b);
                    },
                  ),
                ),
                child: Container(child: Text("Save")),
                onPressed: () {
                  showLoading(context);
                  makeNetworkRequest({
                    "ldap_group_dn": controllerLdapGroupDn.text,
                    "ldap_group_filter": controllerLdapGroupFilter.text,
                    "ldap_group_name_attribute":
                        controllerLdapGroupNameAttribute.text,
                    "ldap_admin_group_name": controllerLdapAdminGroupName.text
                  }, "/set_ldap_group_settings", (json) {
                    hideLoading();
                    errorDialog(context,
                        "Saved group query details\n\nYou should run a test now");
                  }, null, context);
                },
              ),
              SizedBox(
                height: 5,
              ),
              Text(
                "Config test:",
                textAlign: TextAlign.center,
                style: TextStyle(fontSize: 20.0, color: Colors.white),
              ),
              SizedBox(
                height: 5,
              ),
              TextField(
                style: TextStyle(fontSize: 16.0, color: Colors.white),
                minLines: 1,
                maxLines: 1,
                controller: controllerGroupTest,
                decoration: InputDecoration(
                    contentPadding: EdgeInsets.all(0.0),
                    isDense: true,
                    hintStyle: TextStyle(fontSize: 16.0, color: Colors.white60),
                    enabledBorder: UnderlineInputBorder(
                      borderSide: BorderSide(color: Colors.white),
                    ),
                    hintText: 'Enter a user'),
              ),
              SizedBox(
                height: 5,
              ),
              ElevatedButton(
                style: ButtonStyle(
                  backgroundColor: MaterialStateProperty.resolveWith<Color>(
                    (Set<MaterialState> states) {
                      return Color(0xff40444b);
                    },
                  ),
                ),
                child: Container(child: Text("Fetch details")),
                onPressed: () {
                  showLoading(context);
                  makeNetworkRequest({"user": controllerGroupTest.text},
                      "/run_ldap_group_test", (json) {
                    hideLoading();

                    errorDialog(
                        context,
                        "User is " +
                            ((((json['data']
                                            as Map<String, dynamic>)['is_admin']
                                        as int) ==
                                    1)
                                ? ""
                                : "not ") +
                            "an admin and belongs to these groups: \n" +
                            ((json['data'] as Map<String, dynamic>)['list']
                                as String));
                  }, null, context);
                },
              )
            ],
          )));
    }

    return list;
  }

  List<Widget> getTabs(BuildContext context) {
    if (session.admin && !localMode && !offline) {
      return <Widget>[
        about(),
        Center(
          child: Center(
            child: Container(
              color: Color(0xff36393f),
              child: ListView(
                  shrinkWrap: false,
                  padding: EdgeInsets.fromLTRB(20, 20, 20, 20),
                  children: getUsers(context)),
            ),
          ),
        ),
        Center(
          child: Center(
            child: Container(
              color: Color(0xff36393f),
              child: ListView(
                  shrinkWrap: false,
                  padding: EdgeInsets.fromLTRB(20, 20, 20, 20),
                  children: ldapTab(context)),
            ),
          ),
        ),
        libraries(),
      ];
    } else {
      return <Widget>[
        about(),
        libraries(),
      ];
    }
  }

  Widget about() {
    List<Widget> contents = <Widget>[
      Text(
        "Pocket F.E.M.B.O.Y.",
        textAlign: TextAlign.center,
        style: TextStyle(fontSize: 29.0, color: Colors.white),
      ),
      Text(
        "Version " + session.version,
        textAlign: TextAlign.center,
        style: TextStyle(fontSize: 19.0, color: Colors.white),
      ),
      Text(
        "Feature level " + session.featureLevel.toString(),
        textAlign: TextAlign.center,
        style: TextStyle(fontSize: 19.0, color: Colors.white),
      ),
      Text(
        "By k1ra, 2021",
        textAlign: TextAlign.center,
        style: TextStyle(fontSize: 20.0, color: Colors.white),
      ),
      SizedBox(
        height: 20,
      ),
    ];

    contents.addAll(localOrServerInfo());

    return Center(
      child: Container(
        color: Color(0xff36393f),
        child: ListView(
            shrinkWrap: false,
            padding: EdgeInsets.fromLTRB(10, 7, 10, 10),
            children: contents),
      ),
    );
  }

  List<Widget> localOrServerInfo() {
    if (localMode || offline) {
      return <Widget>[
        Text(
          offline
              ? (loggedOut ? "Logged out" : "Offline")
              : "Running in local mode",
          textAlign: TextAlign.center,
          style: TextStyle(fontSize: 20.0, color: Colors.white),
        )
      ];
    } else {
      return <Widget>[
        Text(
          "Connected to " + server,
          textAlign: TextAlign.center,
          style: TextStyle(fontSize: 20.0, color: Colors.white),
        ),
        Text(
          "Logged in as " + session.name,
          textAlign: TextAlign.center,
          style: TextStyle(fontSize: 20.0, color: Colors.white),
        ),
        SizedBox(
          height: 5,
        ),
        ElevatedButton(
          style: ButtonStyle(
            backgroundColor: MaterialStateProperty.resolveWith<Color>(
              (Set<MaterialState> states) {
                return Color(0xff202225);
              },
            ),
          ),
          child: Container(child: Text("Log out")),
          onPressed: () {
            showLoading(context);
            makeNetworkRequest({"all": "0"}, "/log_out", (json) {
              offlineListener(true);
              loggedOut = true;
              Navigator.pop(context);
              setState(() {});
            }, null, context);
          },
        )
      ];
    }
  }

  Widget libraries() {
    return Center(
      child: Container(
        color: Color(0xff36393f),
        child: ListView(
            shrinkWrap: false,
            padding: EdgeInsets.fromLTRB(20, 7, 20, 10),
            children: <Widget>[
              Text(
                "Pocket FEMBOY uses these Flutter packages:",
                textAlign: TextAlign.center,
                style: TextStyle(fontSize: 20.0, color: Colors.white),
              ),
              SizedBox(height: 5),
              Text(
                "- http",
                textAlign: TextAlign.left,
                style: TextStyle(fontSize: 15.0, color: Colors.white),
              ),
              Text(
                "- shared_preferences",
                textAlign: TextAlign.left,
                style: TextStyle(fontSize: 15.0, color: Colors.white),
              ),
              Text(
                "- vibration",
                textAlign: TextAlign.left,
                style: TextStyle(fontSize: 15.0, color: Colors.white),
              ),
              Text(
                "- package_info",
                textAlign: TextAlign.left,
                style: TextStyle(fontSize: 15.0, color: Colors.white),
              ),
              Text(
                "- flutter_typeahead",
                textAlign: TextAlign.left,
                style: TextStyle(fontSize: 15.0, color: Colors.white),
              ),
              Text(
                "- animations",
                textAlign: TextAlign.left,
                style: TextStyle(fontSize: 15.0, color: Colors.white),
              ),
              Text(
                "- photo_view",
                textAlign: TextAlign.left,
                style: TextStyle(fontSize: 15.0, color: Colors.white),
              ),
              Text(
                "- share",
                textAlign: TextAlign.left,
                style: TextStyle(fontSize: 15.0, color: Colors.white),
              ),
              Text(
                "- path_provider",
                textAlign: TextAlign.left,
                style: TextStyle(fontSize: 15.0, color: Colors.white),
              ),
              Text(
                "- mime",
                textAlign: TextAlign.left,
                style: TextStyle(fontSize: 15.0, color: Colors.white),
              ),
              Text(
                "- file_picker",
                textAlign: TextAlign.left,
                style: TextStyle(fontSize: 15.0, color: Colors.white),
              ),
              Text(
                "- sqflite",
                textAlign: TextAlign.left,
                style: TextStyle(fontSize: 15.0, color: Colors.white),
              ),
              Text(
                "- tflite",
                textAlign: TextAlign.left,
                style: TextStyle(fontSize: 15.0, color: Colors.white),
              ),
              Text(
                "- image",
                textAlign: TextAlign.left,
                style: TextStyle(fontSize: 15.0, color: Colors.white),
              ),
              Text(
                "- flutter_launcher_icons",
                textAlign: TextAlign.left,
                style: TextStyle(fontSize: 15.0, color: Colors.white),
              ),
            ]),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    if (!launchTaskRan && !localMode && !offline) {
      launchTaskRan = true;
      Future.delayed(Duration.zero, () => refreshInfo(context));
    }

    return DefaultTabController(
      initialIndex: 0,
      length: (session.admin && !offline) ? 4 : 2,
      child: Scaffold(
        appBar: AppBar(
          backgroundColor: Color(0xff202225),
          brightness: Brightness.dark,
          title: Text('Settings'),
          bottom: TabBar(
            tabs: getTabTitles(),
          ),
        ),
        body: TabBarView(
          children: getTabs(context),
        ),
      ),
    );
  }
}
