import 'dart:developer';
import 'dart:collection';
import 'package:FEMBOY/home.dart';
import 'package:FEMBOY/network_requests.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:shared_preferences/shared_preferences.dart';

final controllerServerAddress = TextEditingController();
final controllerLoginUser = TextEditingController();
final controllerLoginPassword = TextEditingController();
final controllerSqlAddress = TextEditingController();
final controllerSqlDbname = TextEditingController();
final controllerSqlUser = TextEditingController();
final controllerSqlPassword = TextEditingController();
final controllerRootPassword1 = TextEditingController();
final controllerRootPassword2 = TextEditingController();
final dialogStack = Stack<BuildContext>();

class Stack<T> {
  final _stack = Queue<T>();

  void push(T element) {
    _stack.addLast(element);
  }

  T pop() {
    final T lastElement = _stack.last;
    _stack.removeLast();
    return lastElement;
  }

  void clear() {
    _stack.clear();
  }

  bool get isEmpty => _stack.isEmpty;
}

void showIntroDialog(BuildContext context, Function setState) {
  showDialog(
    barrierDismissible: false,
    context: context,
    builder: (BuildContext context) {
      dialogStack.push(context);
      return WillPopScope(
          child: AlertDialog(
            shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.all(Radius.circular(20.0))),
            backgroundColor: Color(0xff2f3136),
            content: Container(
              child: ListView(
                shrinkWrap: true,
                children: <Widget>[
                  Text(
                    "WELCOME",
                    textAlign: TextAlign.center,
                    style: TextStyle(fontSize: 28.0, color: Colors.white),
                  ),
                  Text(
                    "Welcome to F.E.M.B.O.Y.",
                    textAlign: TextAlign.center,
                    style: TextStyle(fontSize: 18.0, color: Colors.white),
                  ),
                  Text(
                    "The better way to organize your images!",
                    textAlign: TextAlign.center,
                    style: TextStyle(fontSize: 18.0, color: Colors.white),
                  ),
                  SizedBox(
                    height: 20,
                  ),
                  Text(
                    "The program can operate by itself or connect to a server",
                    textAlign: TextAlign.center,
                    style: TextStyle(fontSize: 15.0, color: Colors.white),
                  ),
                  SizedBox(
                    height: 20,
                  ),
                  Text(
                    "If you have not set up a server or are unsure about which option to pick, you should probably choose to use the program by itself",
                    textAlign: TextAlign.center,
                    style: TextStyle(fontSize: 15.0, color: Colors.white),
                  ),
                  SizedBox(
                    height: 20,
                  ),
                  ElevatedButton(
                    style: ButtonStyle(
                      backgroundColor: MaterialStateProperty.resolveWith<Color>(
                        (Set<MaterialState> states) {
                          return Color(0xff40444b);
                        },
                      ),
                    ),
                    child: Container(
                      child: Text("Use the program by itself"),
                    ),
                    onPressed: () async {
                      final SharedPreferences prefs =
                          await SharedPreferences.getInstance();
                      await prefs.setBool("local_mode", true);
                      await prefs.setBool("setup_done", true);

                      launchTaskRan = false;
                      while (!dialogStack.isEmpty) {
                        Navigator.pop(dialogStack.pop());
                      }

                      setState();
                    },
                  ),
                  ElevatedButton(
                    style: ButtonStyle(
                      backgroundColor: MaterialStateProperty.resolveWith<Color>(
                        (Set<MaterialState> states) {
                          return Color(0xff40444b);
                        },
                      ),
                    ),
                    child: Container(child: Text("Connect to a server")),
                    onPressed: () =>
                        {showServerAddressDialog(context, setState)},
                  )
                ],
              ),
            ),
          ),
          onWillPop: () => Future.value(false));
    },
  );
}

void showServerAddressDialog(BuildContext context, Function setState) {
  showDialog(
    context: context,
    builder: (BuildContext context) {
      dialogStack.push(context);
      return WillPopScope(
          child: AlertDialog(
            shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.all(Radius.circular(20.0))),
            backgroundColor: Color(0xff2f3136),
            content: Container(
              child: ListView(
                shrinkWrap: true,
                children: <Widget>[
                  Text(
                    "Enter server address:",
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
                    controller: controllerServerAddress,
                    decoration: InputDecoration(
                        contentPadding: EdgeInsets.all(0.0),
                        isDense: true,
                        hintStyle:
                            TextStyle(fontSize: 16.0, color: Colors.white60),
                        enabledBorder: UnderlineInputBorder(
                          borderSide: BorderSide(color: Colors.white),
                        ),
                        hintText: 'http://example.com'),
                  ),
                  SizedBox(
                    height: 10,
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
                      server = controllerServerAddress.text;
                      showLoading(context);
                      makeNetworkRequest({}, "/info", (json) {
                        hideLoading();

                        Map<String, dynamic> data =
                            json['data'] as Map<String, dynamic>;

                        if (data['server_feature_level'] as int !=
                            session.featureLevel) {
                          errorDialog(context,
                              "Server/client version mismatch: please make sure that both server and client are updated to the latest version");
                        } else if (data['setup_complete'] as int == 0) {
                          serverSetup(context, setState);
                        } else {
                          login(context, setState);
                        }
                      }, (json) {
                        hideLoading();
                        errorDialog(
                            context, "Could not connect to that address");
                      }, context);
                    },
                  )
                ],
              ),
            ),
          ),
          onWillPop: () {
            dialogStack.pop();
            return Future.value(true);
          });
    },
  );
}

void login(BuildContext context, Function setState) {
  showDialog(
    context: context,
    builder: (BuildContext context) {
      dialogStack.push(context);
      return WillPopScope(
          child: AlertDialog(
            shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.all(Radius.circular(20.0))),
            backgroundColor: Color(0xff2f3136),
            content: Container(
              child: ListView(
                shrinkWrap: true,
                children: <Widget>[
                  Text(
                    "LOGIN",
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
                    controller: controllerLoginUser,
                    decoration: InputDecoration(
                        contentPadding: EdgeInsets.all(0.0),
                        isDense: true,
                        hintStyle:
                            TextStyle(fontSize: 16.0, color: Colors.white60),
                        enabledBorder: UnderlineInputBorder(
                          borderSide: BorderSide(color: Colors.white),
                        ),
                        hintText: 'username'),
                  ),
                  SizedBox(
                    height: 7,
                  ),
                  TextField(
                    style: TextStyle(fontSize: 16.0, color: Colors.white),
                    minLines: 1,
                    maxLines: 1,
                    obscureText: true,
                    controller: controllerLoginPassword,
                    decoration: InputDecoration(
                        contentPadding: EdgeInsets.all(0.0),
                        isDense: true,
                        hintStyle:
                            TextStyle(fontSize: 16.0, color: Colors.white60),
                        enabledBorder: UnderlineInputBorder(
                          borderSide: BorderSide(color: Colors.white),
                        ),
                        hintText: 'password'),
                  ),
                  SizedBox(
                    height: 10,
                  ),
                  ElevatedButton(
                    style: ButtonStyle(
                      backgroundColor: MaterialStateProperty.resolveWith<Color>(
                        (Set<MaterialState> states) {
                          return Color(0xff40444b);
                        },
                      ),
                    ),
                    child: Container(child: Text("Login")),
                    onPressed: () {
                      showLoading(context);
                      makeNetworkRequest({
                        "user": controllerLoginUser.text,
                        "password": controllerLoginPassword.text
                      }, "/login", (json) async {
                        hideLoading();

                        Map<String, dynamic> data =
                            json['data'] as Map<String, dynamic>;

                        final SharedPreferences prefs =
                            await SharedPreferences.getInstance();
                        await prefs.setString("token", data['session']);
                        await prefs.setString("name", controllerLoginUser.text);
                        await prefs.setString("uid", data['uid']);
                        await prefs.setBool("admin", data['admin'] as int == 1);
                        await prefs.setString("server", server);
                        await prefs.setBool("local_mode", false);
                        await prefs.setBool("setup_done", true);

                        launchTaskRan = false;
                        while (!dialogStack.isEmpty) {
                          Navigator.pop(dialogStack.pop());
                        }

                        setState();
                      }, (json) {
                        hideLoading();
                        if (json['error'] as int == 1) {
                          errorDialog(
                              context, "Incorrect username or password");
                        } else {
                          errorDialog(context,
                              "Server/client version mismatch: please make sure that both server and client are updated to the latest version");
                        }
                      }, context);
                    },
                  )
                ],
              ),
            ),
          ),
          onWillPop: () {
            dialogStack.pop();
            return Future.value(true);
          });
    },
  );
}

void serverSetup(BuildContext context, Function setState) {
  showDialog(
    context: context,
    builder: (BuildContext context) {
      dialogStack.push(context);
      return WillPopScope(
          child: AlertDialog(
            shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.all(Radius.circular(20.0))),
            backgroundColor: Color(0xff2f3136),
            content: Container(
              child: ListView(
                shrinkWrap: true,
                children: <Widget>[
                  Text(
                    "The server you connected to has not been set up yet. \nYou need to connect it to a mySQL server and create a password for the root account.",
                    textAlign: TextAlign.center,
                    style: TextStyle(fontSize: 20.0, color: Colors.white),
                  ),
                  SizedBox(
                    height: 10,
                  ),
                  Text(
                    "SQL setup:",
                    textAlign: TextAlign.left,
                    style: TextStyle(fontSize: 18.0, color: Colors.white),
                  ),
                  SizedBox(
                    height: 5,
                  ),
                  TextField(
                    style: TextStyle(fontSize: 16.0, color: Colors.white),
                    minLines: 1,
                    maxLines: 1,
                    controller: controllerSqlAddress,
                    decoration: InputDecoration(
                        contentPadding: EdgeInsets.all(0.0),
                        isDense: true,
                        hintStyle:
                            TextStyle(fontSize: 16.0, color: Colors.white60),
                        enabledBorder: UnderlineInputBorder(
                          borderSide: BorderSide(color: Colors.white),
                        ),
                        hintText: 'Address (example.com:3306)'),
                  ),
                  SizedBox(
                    height: 7,
                  ),
                  TextField(
                    style: TextStyle(fontSize: 16.0, color: Colors.white),
                    minLines: 1,
                    maxLines: 1,
                    controller: controllerSqlDbname,
                    decoration: InputDecoration(
                        contentPadding: EdgeInsets.all(0.0),
                        isDense: true,
                        hintStyle:
                            TextStyle(fontSize: 16.0, color: Colors.white60),
                        enabledBorder: UnderlineInputBorder(
                          borderSide: BorderSide(color: Colors.white),
                        ),
                        hintText: 'Database name'),
                  ),
                  SizedBox(
                    height: 7,
                  ),
                  TextField(
                    style: TextStyle(fontSize: 16.0, color: Colors.white),
                    minLines: 1,
                    maxLines: 1,
                    controller: controllerSqlUser,
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
                    controller: controllerSqlPassword,
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
                  Text(
                    "root password setup:",
                    textAlign: TextAlign.left,
                    style: TextStyle(fontSize: 18.0, color: Colors.white),
                  ),
                  SizedBox(
                    height: 5,
                  ),
                  TextField(
                    style: TextStyle(fontSize: 16.0, color: Colors.white),
                    minLines: 1,
                    maxLines: 1,
                    obscureText: true,
                    controller: controllerRootPassword1,
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
                    controller: controllerRootPassword2,
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
                    height: 10,
                  ),
                  ElevatedButton(
                    style: ButtonStyle(
                      backgroundColor: MaterialStateProperty.resolveWith<Color>(
                        (Set<MaterialState> states) {
                          return Color(0xff40444b);
                        },
                      ),
                    ),
                    child: Container(child: Text("Continue")),
                    onPressed: () {
                      if (controllerRootPassword1.text.isEmpty) {
                        errorDialog(context, "Root password cannot be blank");
                      } else if (controllerRootPassword1.text !=
                          controllerRootPassword2.text) {
                        errorDialog(context,
                            "Root passwords do not match. Please re-enter them");
                      } else {
                        showLoading(context);
                        makeNetworkRequest({
                          "sql_address": controllerSqlAddress.text,
                          "sql_dbname": controllerSqlDbname.text,
                          "sql_user": controllerSqlUser.text,
                          "sql_password": controllerSqlPassword.text,
                          "root_password": controllerRootPassword1.text
                        }, "/do_setup", (json) async {
                          hideLoading();

                          Map<String, dynamic> data =
                              json['data'] as Map<String, dynamic>;

                          final SharedPreferences prefs =
                              await SharedPreferences.getInstance();
                          await prefs.setString("token", data['session']);
                          await prefs.setString("name", "root");
                          await prefs.setString("uid", "1");
                          await prefs.setBool("admin", true);
                          await prefs.setString("server", server);
                          await prefs.setBool("local_mode", false);
                          await prefs.setBool("setup_done", true);

                          setupDone(context, setState);
                        }, (json) {
                          hideLoading();
                          if (json['error'] as int == 1) {
                            errorDialog(context, json['error_text']);
                          }
                        }, context);
                      }
                    },
                  )
                ],
              ),
            ),
          ),
          onWillPop: () {
            dialogStack.pop();
            return Future.value(true);
          });
    },
  );
}

void setupDone(BuildContext context, Function setState) {
  showDialog(
    context: context,
    barrierDismissible: false,
    builder: (BuildContext context) {
      dialogStack.push(context);
      return WillPopScope(
          child: AlertDialog(
            shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.all(Radius.circular(20.0))),
            backgroundColor: Color(0xff2f3136),
            content: Container(
              child: ListView(
                shrinkWrap: true,
                children: <Widget>[
                  Text(
                    "Your server has been set up and you are logged into the root account!",
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
                          return Color(0xff40444b);
                        },
                      ),
                    ),
                    child: Container(child: Text("Start using your FEMBOY")),
                    onPressed: () {
                      launchTaskRan = false;
                      while (!dialogStack.isEmpty) {
                        Navigator.pop(dialogStack.pop());
                      }

                      setState();
                    },
                  )
                ],
              ),
            ),
          ),
          onWillPop: () => Future.value(false));
    },
  );
}
