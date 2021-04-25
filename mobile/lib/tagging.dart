import 'dart:io';
import 'dart:typed_data';
import 'package:image/image.dart' as img;
import 'package:tflite/tflite.dart';
import 'package:flutter/services.dart' show rootBundle;

Future<String> loadCharacters() async {
  return await rootBundle.loadString('assets/DD-characters.txt');
}

Future<String> loadModel() async {
  return await Tflite.loadModel(
      model: "assets/model.tflite",
      labels: "assets/DD-tags.txt",
      numThreads: 1, // defaults to 1
      isAsset:
          true, // defaults to true, set to false to load resources outside assets
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
