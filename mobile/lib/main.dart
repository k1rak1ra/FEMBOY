import 'package:flutter/material.dart';
import 'home.dart';
import 'database.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    initDatabase();
    print("database init");

    return MaterialApp(
      home: Home(),
    );
  }
}
