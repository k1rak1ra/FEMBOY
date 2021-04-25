import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

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
      IconButton(
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
