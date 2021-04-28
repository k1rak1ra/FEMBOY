import 'dart:convert';
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import 'package:path/path.dart';

String server = "";
BuildContext loadingContext;
SessionInfo session;
bool localMode;
Directory documentDirectory;

class SessionInfo {
  String token = "";
  String uid = "";
  String name = "";
  int featureLevel;
  String version;
  bool admin = false;
  SessionInfo(this.featureLevel, this.version);

  Future<bool> updateUserInfo() async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    if (prefs.getString("token") != null) {
      token = prefs.getString("token");
      name = prefs.getString("name");
      uid = prefs.getString("uid");
      admin = prefs.getBool("admin");
    }
    return true;
  }

  Map<String, String> get() {
    return {
      "token": token,
      "uid": uid,
      "feature_level": featureLevel.toString()
    };
  }
}

Future<bool> setServer() async {
  final SharedPreferences prefs = await SharedPreferences.getInstance();
  if (prefs.getString("server") != null) {
    server = prefs.getString("server");
  }
  return true;
}

String stringListToJSON(List<String> list) {
  List<String> out = List.empty(growable: true);
  for (String s in list) {
    out.add("\"" + s + "\"");
  }

  return out.toString();
}

Map<String, String> headers = {
  "Accept": "application/json",
  "Content-Type": "application/x-www-form-urlencoded"
};

Future<bool> makeNetworkRequest(
    Map<String, String> params,
    String url,
    Function(Map<String, dynamic>) onSuccess,
    Function(Map<String, dynamic>) onFail,
    BuildContext context) async {
  //delayed so loading has time to show
  await Future.delayed(Duration(milliseconds: 100), () async {
    try {
      print(server + url);
      Map<String, String> body = {};
      body.addAll(session.get());
      body.addAll(params);
      final response = await http.post(server + url,
          headers: headers, body: body, encoding: Encoding.getByName("utf-8"));

      if (response.statusCode == 200) {
        print(response.body);
        Map<String, dynamic> json = jsonDecode(response.body);
        if (json['success'] as int == 1) {
          await onSuccess(json);
        } else if (json['error'] as int == 0) {
          //logout
        } else if (onFail != null) {
          await onFail(json);
        }
        return;
      }
    } catch (e) {
      print(e);
    }

    if (url == "/info" || url == "/upload_image") {
      await onFail(null);
    } else {
      hideLoading();
      errorDialog(context,
          "Could not connect to the server. Check your internet connection and the server.");
    }
  });
  return true;
}

void hideLoading() {
  if (loadingContext != null) {
    Navigator.pop(loadingContext);
    loadingContext = null;
  }
}

void showLoading(BuildContext context) {
  showDialog(
    barrierDismissible: false,
    context: context,
    builder: (BuildContext dialogContext) {
      loadingContext = dialogContext;
      return WillPopScope(
          child: AlertDialog(
            shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.all(Radius.circular(20.0))),
            backgroundColor: Color(0x00ffffff),
            content: Row(
              children: <Widget>[
                Spacer(),
                Container(
                  width: 75,
                  height: 75,
                  child: CircularProgressIndicator(),
                ),
                Spacer()
              ],
            ),
          ),
          onWillPop: () => Future.value(false));
    },
  );
}

void errorDialog(BuildContext context, String text) {
  showDialog(
    context: context,
    builder: (BuildContext context) {
      return AlertDialog(
        shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.all(Radius.circular(20.0))),
        backgroundColor: Color(0xff2f3136),
        content: Container(
          child: ListView(
            shrinkWrap: true,
            children: <Widget>[
              Text(
                text,
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
                child: Container(child: Text("Okay")),
                onPressed: () {
                  Navigator.pop(context);
                },
              )
            ],
          ),
        ),
      );
    },
  );
}

File getLocalImage(int id) {
  File image = File(join(documentDirectory.path, id.toString() + ".png"));
  if (!image.existsSync()) {
    image = File(join(documentDirectory.path, id.toString() + ".jpg"));
  }
  if (!image.existsSync()) {
    image = File(join(documentDirectory.path, id.toString() + ".jpeg"));
  }
  return image;
}
