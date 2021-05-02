import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'network_requests.dart';

Widget selectModeBar(
    String title, Function closeAll, Function delete, Function save) {
  return AppBar(
    backgroundColor: Color(0xff202225),
    brightness: Brightness.dark,
    title: Text(title),
    leading: Padding(
      padding: const EdgeInsets.all(8.0),
      child: IconButton(
        icon: Icon(Icons.close),
        onPressed: () => closeAll(),
      ),
    ),
    actions: <Widget>[
      localMode
          ? Container(
              width: 0,
              height: 0,
            )
          : IconButton(
              icon: Icon(Icons.save),
              onPressed: () => save(),
            ),
      IconButton(
        icon: Icon(Icons.delete),
        onPressed: () => delete(),
      ),
    ],
  );
}

Widget FEMBOYBar(String title, Function refresh, Function search,
    Function uploadFile, Function settings) {
  return AppBar(
    backgroundColor: Color(0xff202225),
    brightness: Brightness.dark,
    title: Text(title),
    actions: <Widget>[
      IconButton(
        icon: Icon(Icons.refresh),
        onPressed: () => refresh(),
      ),
      IconButton(
        icon: Icon(Icons.search),
        onPressed: () => search(),
      ),
      (uploadFile == null)
          ? Container(
              height: 0,
              width: 0,
            )
          : IconButton(
              icon: Icon(Icons.upload_file),
              onPressed: () => uploadFile(),
            ),
      IconButton(
        icon: Icon(Icons.settings),
        onPressed: () => settings(),
      )
    ],
  );
}

Widget offlineFEMBOYBar(
    String title, Function refresh, Function search, Function settings) {
  return AppBar(
    backgroundColor: Color(0xff202225),
    brightness: Brightness.dark,
    title: Text(title),
    actions: <Widget>[
      IconButton(
        icon: Icon(Icons.refresh),
        onPressed: () => refresh(),
      ),
      IconButton(
        icon: Icon(Icons.search),
        onPressed: () => search(),
      ),
      IconButton(
        icon: Icon(Icons.settings),
        onPressed: () => settings(),
      )
    ],
  );
}

Widget imageViewerBar(
    String title, Function edit, Function delete, Function share) {
  return AppBar(
    backgroundColor: Color(0xff202225),
    brightness: Brightness.dark,
    title: Text(title),
    actions: <Widget>[
      IconButton(
        icon: Icon(Icons.edit),
        onPressed: () => edit(),
      ),
      IconButton(
        icon: Icon(Icons.delete),
        onPressed: () => delete(),
      ),
      IconButton(
        icon: Icon(Icons.share),
        onPressed: () => share(),
      )
    ],
  );
}

Widget offlineImageViewerBar(String title, Function edit, Function share) {
  return AppBar(
    backgroundColor: Color(0xff202225),
    brightness: Brightness.dark,
    title: Text(title),
    actions: <Widget>[
      IconButton(
        icon: Icon(Icons.edit),
        onPressed: () => edit(),
      ),
      IconButton(
        icon: Icon(Icons.share),
        onPressed: () => share(),
      )
    ],
  );
}
