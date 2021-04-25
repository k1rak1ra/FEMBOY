import 'dart:convert';
import 'dart:io';
import 'package:FEMBOY/network_requests.dart';
import 'package:path_provider/path_provider.dart';
import 'package:path/path.dart';
import 'package:sqflite/sqflite.dart';

Database db;

void initDatabase() async {
  Directory documentDirectory = await getApplicationDocumentsDirectory();
  db = await openDatabase(join(documentDirectory.path, "FEMBOY.db"), version: 1,
      onCreate: (Database db, int version) async {
    // When creating the db, create the tables
    await db.execute("CREATE TABLE tags (\n" +
        "\tid INTEGER PRIMARY KEY,\n" +
        "\ttags LONGTEXT NOT NULL,\n" +
        "\tlatest_id INTEGER\n" +
        ");");
    await db.execute("INSERT INTO tags (id, tags, latest_id)  \n" +
        "VALUES (1, \"[]\", 1);");
    await db.execute("CREATE TABLE images (\n" +
        "\tid INTEGER PRIMARY KEY,\n" +
        "\ttags TEXT NOT NULL,\n" +
        "\taspect VARCHAR(500) NULL\n" +
        ");");
  });
}

Future<int> getImgIndex() async {
  List<Map> list = await db.rawQuery('SELECT * FROM tags WHERE id = 1');
  return list[0]['latest_id'] as int;
}

Future<bool> uploadImage(List<String> tags, int id) async {
  await db.transaction((txn) async {
    int id1 = await txn.rawInsert(
        "INSERT INTO images (id, tags, aspect)  \n" + "VALUES (?, ?, 1);",
        [id.toString(), stringListToJSON(tags)]);
    print('inserted: $id1');
  });
  return true;
}

Future<bool> putImgIndex(int index) async {
  await db.rawUpdate(
      'UPDATE tags SET latest_id = ?  WHERE id = 1;', [index.toString()]);
  return true;
}

Future<bool> updateGlobalTagList(List<String> tags) async {
  List<Map> list = await db.rawQuery('SELECT * FROM tags WHERE id = 1');
  List<dynamic> json = jsonDecode(list[0]['tags'] as String);
  for (String s in json) {
    if (!tags.contains(s)) {
      tags.add(s);
    }
  }
  await db.rawUpdate(
      'UPDATE tags SET tags = ?  WHERE id = 1;', [stringListToJSON(tags)]);
  return true;
}

Future<List<int>> getImagesWithTags(List<String> tags) async {
  List<int> images = List.empty(growable: true);
  String query = "tags LIKE '%'";
  for (String tag in tags) {
    query += "AND tags LIKE '%" + tag + "%'";
  }
  List<Map> list = await db
      .rawQuery("SELECT * FROM images WHERE " + query + " ORDER BY id DESC");

  for (Map<dynamic, dynamic> item in list) {
    images.add(item['id'] as int);
  }
  return images;
}

Future<List<String>> getAllTags() async {
  List<String> tags = List.empty(growable: true);
  List<Map> list = await db.rawQuery('SELECT * FROM tags WHERE id = 1');
  List<dynamic> json = jsonDecode(list[0]['tags'] as String);
  for (String s in json) {
    tags.add(s);
  }
  return tags;
}

Future<List<String>> getImgTags(int id) async {
  List<String> tags = List.empty(growable: true);
  List<Map> list =
      await db.rawQuery('SELECT * FROM images WHERE id = ?', [id.toString()]);
  List<dynamic> json = jsonDecode(list[0]['tags'] as String);
  for (String s in json) {
    tags.add(s);
  }
  return tags;
}

Future<bool> updateImgTags(List<String> tags, int id) async {
  await updateGlobalTagList(tags);
  await db.rawUpdate('UPDATE images SET tags = ?  WHERE id = ?;',
      [stringListToJSON(tags), id.toString()]);
  return true;
}

Future<bool> removeImage(int id) async {
  await db.rawDelete('DELETE FROM images WHERE id = ?;', [id.toString()]);
  return true;
}
