import 'dart:io';
import 'dart:typed_data';
import 'package:image/image.dart' as img;
import 'package:tflite/tflite.dart';
import 'package:flutter/services.dart' show rootBundle;
import 'package:path/path.dart';
import 'network_requests.dart';

List<String> characters = List.empty(growable: true);

Future<bool> loadCharacters() async {
  characters.clear();
  String text = await rootBundle.loadString('assets/DD-characters.txt');
  List<String> list = text.split("\n");
  for (String s in list) {
    if (s.length > 2) {
      characters.add(s.substring(0, s.length - 1));
    }
  }

  print(characters);
  print(characters.length);
  return true;
}

Future<String> loadModel() async {
  return await Tflite.loadModel(
      model: join(documentDirectory.path, "model.tflite").toString(),
      labels: join(documentDirectory.path, "DD-tags.txt").toString(),
      numThreads: 1, // defaults to 1
      isAsset:
          false, // defaults to true, set to false to load resources outside assets
      useGpuDelegate: true // defaults to false, set to true to use GPU delegate
      );
}

void closeModel() async {
  Tflite.close();
}

Future<List<String>> tagImage(File image) async {
  List<String> tags = List.empty(growable: true);
  var recognitions = await Tflite.runModelOnImage(
      path: image.path, // required
      imageMean: 0.0, // defaults to 117.0
      imageStd: 255.0, // defaults to 1.0
      numResults: 1000, // defaults to 5
      threshold: 0.5, // defaults to 0.1
      asynch: true // defaults to true
      );

  print(recognitions);
  for (Map<dynamic, dynamic> item in recognitions) {
    tags.add(item['label'] as String);
  }
  return tags;
}
