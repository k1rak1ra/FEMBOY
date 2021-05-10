import 'dart:convert';
import 'dart:io';
import 'package:FEMBOY/database.dart';
import 'package:FEMBOY/settings.dart';
import 'package:FEMBOY/tagging.dart';
import 'package:animations/animations.dart';
import 'package:file_picker/file_picker.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:path_provider/path_provider.dart';
import 'package:vibration/vibration.dart';
import 'package:package_info/package_info.dart';
import 'package:flutter_typeahead/flutter_typeahead.dart';
import 'appbar.dart';
import 'network_requests.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'setup.dart';
import 'image_viewer.dart';
import 'package:path/path.dart';
import 'package:mime/mime.dart';
import 'package:http/http.dart' as http;
import 'package:firebase_storage/firebase_storage.dart' as firebase_storage;

List<int> recievedImages = List.empty();
List<int> selectedImages = List.empty(growable: true);
List<int> savedImages = List.empty();
List<String> allTags = List.empty();
final List<TagListChip> filterTags = List.empty(growable: true);
bool launchTaskRan = false;
final controllerSearchTag = TextEditingController();

class Home extends StatefulWidget {
  @override
  HomeState createState() => HomeState();
}

class TagListChip {
  String tag;
  Function(TagListChip, Function) onDeleted;
  Function setState;

  TagListChip(this.tag, this.onDeleted, this.setState);

  Widget get() {
    Color color = Colors.red;
    if (characters.contains(tag)) {
      color = Colors.blue[400];
    } else if (tag.length > 8 && tag.substring(0, 7) == "rating:") {
      color = Colors.green;
    }
    return Chip(
        deleteIcon: Icon(
          Icons.close_outlined,
          color: Colors.white,
        ),
        backgroundColor: color,
        label: Text(
          this.tag,
          style: TextStyle(color: Colors.white),
        ),
        onDeleted: () => onDeleted(this, setState));
  }
}

class HomeState extends State<Home> {
  double progress = 0;

  void updateLocal(BuildContext context) async {
    for (int i in savedImages) {
      if (!offline) {
        await makeNetworkRequest({"id": i.toString()}, "/get_img_tags",
            (json) async {
          List<dynamic> tagsJson = json['data'] as List<dynamic>;
          List<String> tags = List.empty(growable: true);
          for (String tag in tagsJson) {
            tags.add(tag);
          }

          await updateImgTags(tags, i);
          progress++;
        }, null, context);
      }
    }
  }

  void getImagesAndTags(BuildContext context) async {
    if (localMode || (offline && !offlineRetry)) {
      recievedImages =
          await getImagesWithTags(filterTags.map((e) => e.tag).toList());
      allTags = await getAllTags();

      setState(() {});
    } else {
      offlineRetry = false;
      showLoading(context);
      List<int> recievedImagesNew = List.empty(growable: true);
      List<String> allTagsNew = List.empty(growable: true);
      makeNetworkRequest(
          {"tags": stringListToJSON(filterTags.map((e) => e.tag).toList())},
          "/get_images_with_tags", (json) {
        List<dynamic> ids =
            (json['data'] as Map<String, dynamic>)['ids'] as List<dynamic>;
        for (int id in ids) {
          recievedImagesNew.add(id);
        }

        makeNetworkRequest({}, "/get_all_tags", (json) async {
          List<dynamic> tags = json['data'] as List<dynamic>;
          for (String tag in tags) {
            allTagsNew.add(tag);
          }

          savedImages = await getImagesWithTags(List.empty());
          updateLocal(context);

          setState(
            () {
              recievedImages = recievedImagesNew;
              allTags = allTagsNew;
              hideLoading();
            },
          );
        }, null, context);
      }, null, context);
    }
  }

  void search(BuildContext context) {
    showDialog(
        context: context,
        builder: (BuildContext context2) {
          return StatefulBuilder(
            builder: (context2, setState) {
              for (TagListChip chip in filterTags) {
                chip.setState = () => setState(() {});
              }

              return WillPopScope(
                  child: AlertDialog(
                    shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.all(Radius.circular(20.0))),
                    backgroundColor: Color(0xff2f3136),
                    content: Container(
                      width: 300,
                      child: ListView(
                        shrinkWrap: true,
                        children: <Widget>[
                          TypeAheadField(
                              textFieldConfiguration: TextFieldConfiguration(
                                style: TextStyle(
                                    fontSize: 16.0, color: Colors.white),
                                minLines: 1,
                                maxLines: 1,
                                controller: controllerSearchTag,
                                decoration: InputDecoration(
                                    contentPadding: EdgeInsets.all(0.0),
                                    isDense: true,
                                    hintStyle: TextStyle(
                                        fontSize: 16.0, color: Colors.white60),
                                    enabledBorder: UnderlineInputBorder(
                                      borderSide:
                                          BorderSide(color: Colors.white),
                                    ),
                                    hintText: 'Enter a tag'),
                              ),
                              suggestionsCallback: (pattern) => allTags
                                  .where((element) => element.contains(pattern))
                                  .toList(),
                              itemBuilder: (context, suggestion) {
                                return Container(
                                  height: 25,
                                  alignment: Alignment.centerLeft,
                                  child: Text(suggestion),
                                );
                              },
                              onSuggestionSelected: (suggestion) =>
                                  controllerSearchTag.text = suggestion),
                          SizedBox(
                            height: 5,
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
                            child: Container(child: Text("Add")),
                            onPressed: () {
                              if (!filterTags
                                  .map((e) => e.tag)
                                  .toList()
                                  .contains(controllerSearchTag.text)) {
                                filterTags.add(TagListChip(
                                    controllerSearchTag.text, (item, setState) {
                                  filterTags.remove(item);
                                  setState();
                                }, () => setState(() {})));
                                setState(() {});
                              }
                              controllerSearchTag.clear();
                            },
                          ),
                          SizedBox(
                            height: 15,
                          ),
                          Wrap(
                              spacing: 8.0, // gap between adjacent chips
                              runSpacing: 4.0, // gap between lines
                              children: filterTags.map((e) => e.get()).toList())
                        ],
                      ),
                    ),
                  ),
                  onWillPop: () {
                    Future.delayed(
                        Duration.zero, () => getImagesAndTags(context));
                    return Future.value(true);
                  });
            },
          );
        });
  }

  void uploadLoop(Function update, List<File> files, BuildContext context,
      BuildContext context2) async {
    int index;
    if (localMode) {
      await loadModel();
      index = await getImgIndex();
    }

    bool inUpload = false;
    while (progress < files.length) {
      if (!inUpload) {
        inUpload = true;
        File f = files[progress.round()];
        var image = await decodeImageFromList(f.readAsBytesSync());
        double aspect = image.height / image.width;

        if (localMode) {
          index++;
          List<String> tags = await tagImage(f);
          await uploadImage(tags, index);
          await putImgIndex(index);
          await updateGlobalTagList(tags);

          String ext = basename(f.path.split('.').last);
          File out =
              File(join(documentDirectory.path, index.toString() + "." + ext));
          out.writeAsBytesSync(f.readAsBytesSync());

          progress++;
          inUpload = false;
          update();
          if (progress == files.length) {
            closeModel();
            Navigator.pop(context2);
            getImagesAndTags(context);
          }
        } else {
          await makeNetworkRequest({
            "image": base64Encode(f.readAsBytesSync()),
            "aspect": aspect.toString()
          }, "/upload_image", (json) {
            progress++;
            inUpload = false;
            update();
            if (progress == files.length) {
              Navigator.pop(context2);
              getImagesAndTags(context);
            }
          }, (json) {
            showDialog(
              context: context,
              builder: (BuildContext context) {
                return WillPopScope(
                    child: AlertDialog(
                      shape: RoundedRectangleBorder(
                          borderRadius:
                              BorderRadius.all(Radius.circular(20.0))),
                      backgroundColor: Color(0xff2f3136),
                      content: Container(
                        width: 300,
                        child: ListView(
                          shrinkWrap: true,
                          children: <Widget>[
                            Text(
                              "A problem occured while uploading the image",
                              textAlign: TextAlign.center,
                              style: TextStyle(
                                  fontSize: 20.0, color: Colors.white),
                            ),
                            SizedBox(
                              height: 5,
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
                              child: Container(
                                  child: Text("Try this image again")),
                              onPressed: () {
                                Navigator.pop(context);
                                inUpload = false;
                              },
                            ),
                            SizedBox(
                              height: 5,
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
                              child: Container(
                                  child: Text("Skip to the next image")),
                              onPressed: () {
                                Navigator.pop(context);
                                progress++;
                                inUpload = false;
                              },
                            ),
                            SizedBox(
                              height: 5,
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
                              child:
                                  Container(child: Text("Cancel the upload")),
                              onPressed: () {
                                Navigator.pop(context);
                                progress = files.length / 1.0;
                                inUpload = false;
                              },
                            )
                          ],
                        ),
                      ),
                    ),
                    onWillPop: () => Future.value(false));
              },
            );
          }, context);
        }
      }
    }
  }

  void uploadFile(BuildContext context) async {
    FilePickerResult result;

    if (Platform.isIOS) {
      result = await FilePicker.platform
          .pickFiles(allowMultiple: true, type: FileType.image);
    } else {
      result = await FilePicker.platform.pickFiles(
          allowMultiple: true,
          type: FileType.custom,
          allowedExtensions: ['jpg', 'jpeg', 'png']);
    }

    if (result != null) {
      List<File> files = result.paths.map((path) => File(path)).toList();
      for (File f in files) {
        if (f.path.split(".").last != "jpg" &&
            f.path.split(".").last != "jpeg" &&
            f.path.split(".").last != "png") {
          files.remove(f);
        }
      }

      print(files);
      print(files.length.toString() + " files selected");
      progress = 0;
      bool uploadLoopLaunched = false;

      showDialog(
          barrierDismissible: false,
          context: context,
          builder: (BuildContext context) {
            return StatefulBuilder(builder: (context2, setState) {
              if (!uploadLoopLaunched) {
                uploadLoopLaunched = true;
                uploadLoop(() => setState(() {}), files, context, context2);
              }
              return WillPopScope(
                  child: AlertDialog(
                    shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.all(Radius.circular(20.0))),
                    backgroundColor: Color(0xff2f3136),
                    content: Container(
                      width: 300,
                      child: ListView(
                        shrinkWrap: true,
                        children: <Widget>[
                          Text(
                            "Please wait, uploading...",
                            textAlign: TextAlign.center,
                            style:
                                TextStyle(fontSize: 20.0, color: Colors.white),
                          ),
                          SizedBox(
                            height: 15,
                          ),
                          LinearProgressIndicator(
                            value: progress / files.length,
                            semanticsLabel: 'Progress indicator',
                          ),
                        ],
                      ),
                    ),
                  ),
                  onWillPop: () => Future.value(false));
            });
          });
    }
  }

  void vibrateDevice() async {
    if (await Vibration.hasVibrator()) {
      if (await Vibration.hasCustomVibrationsSupport()) {
        Vibration.vibrate(duration: 100);
      }
    }
  }

  void addListeners(BuildContext context) {
    offlineListener = (val) {
      if (offline != val) {
        offline = val;
        if (offline) {
          getImagesAndTags(context);
        }
      }
    };

    mainSetState = () => {setState(() {})};
  }

  void launchTask(BuildContext context) async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    PackageInfo packageInfo = await PackageInfo.fromPlatform();
    session = SessionInfo(packageInfo.version);
    await session.updateUserInfo();
    await setServer();
    await loadCharacters();
    documentDirectory = await getApplicationDocumentsDirectory();
    if (prefs.getBool("setup_done") != null && prefs.getBool("setup_done")) {
      localMode = prefs.getBool("local_mode");
      getImagesAndTags(context);
      if (!localMode) {
        addListeners(context);
      } else if (!File(join(documentDirectory.path, "model.tflite"))
          .existsSync()) {
        downloadModel(context);
      }
    } else {
      showIntroDialog(context, () => setState(() => {}));
    }
  }

  void modelDownloadLogic(
      Function setState2, BuildContext context, BuildContext context2) async {
    await Firebase.initializeApp();
    String url = await firebase_storage.FirebaseStorage.instance
        .ref('/model.tflite')
        .getDownloadURL();
    final request = http.Request('GET', Uri.parse(url));
    final http.StreamedResponse response = await http.Client().send(request);
    final contentLength = response.contentLength;
    File temp = File(join(documentDirectory.path, "model-temp.tflite"));
    if (temp.existsSync()) {
      temp.deleteSync();
    }

    ByteData bytes = await rootBundle.load('assets/DD-tags.txt');
    File tags = File(join(documentDirectory.path, "DD-tags.txt"));
    final buffer = bytes.buffer;
    tags.writeAsBytesSync(
        buffer.asUint8List(bytes.offsetInBytes, bytes.lengthInBytes));

    response.stream.listen(
      (List<int> newBytes) async {
        temp.writeAsBytesSync(newBytes, mode: FileMode.append);
        int downloadedLength = await temp.length();
        progress = downloadedLength / contentLength;
        setState2();
      },
      onDone: () async {
        temp.renameSync(join(documentDirectory.path, "model.tflite"));
        Navigator.pop(context2);
      },
      onError: (e) {
        Navigator.pop(context2);
        showDialog(
          context: context,
          builder: (BuildContext context2) {
            return WillPopScope(
                child: AlertDialog(
                  shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.all(Radius.circular(20.0))),
                  backgroundColor: Color(0xff2f3136),
                  content: Container(
                    width: 300,
                    child: ListView(
                      shrinkWrap: true,
                      children: <Widget>[
                        Text(
                          e.toString(),
                          textAlign: TextAlign.center,
                          style: TextStyle(fontSize: 20.0, color: Colors.white),
                        ),
                        SizedBox(
                          height: 5,
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
                          child: Container(child: Text("Retry")),
                          onPressed: () {
                            Navigator.pop(context2);
                            launchTaskRan = false;
                            setState(() {});
                          },
                        )
                      ],
                    ),
                  ),
                ),
                onWillPop: () => Future.value(false));
          },
        );
      },
      cancelOnError: true,
    );
  }

  void downloadModel(BuildContext context) async {
    bool inDownload = false;
    progress = 0;
    showDialog(
        barrierDismissible: false,
        context: context,
        builder: (BuildContext context) {
          return StatefulBuilder(builder: (context2, setState) {
            if (!inDownload) {
              inDownload = true;
              modelDownloadLogic(() => setState(() {}), context, context2);
            }

            return WillPopScope(
                child: AlertDialog(
                  shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.all(Radius.circular(20.0))),
                  backgroundColor: Color(0xff2f3136),
                  content: Container(
                    width: 300,
                    child: ListView(
                      shrinkWrap: true,
                      children: <Widget>[
                        Text(
                          "Please wait, downloading image tagging model...",
                          textAlign: TextAlign.center,
                          style: TextStyle(fontSize: 20.0, color: Colors.white),
                        ),
                        SizedBox(
                          height: 15,
                        ),
                        LinearProgressIndicator(
                          value: progress,
                          semanticsLabel: 'Progress indicator',
                        ),
                      ],
                    ),
                  ),
                ),
                onWillPop: () => Future.value(false));
          });
        });
  }

  Widget gridImage(int id, BuildContext context, int numBoxes, int shrink) {
    return localMode || offline
        ? Image.file(getLocalImage(id),
            height: (MediaQuery.of(context).size.width / numBoxes) - 4 - shrink,
            width: (MediaQuery.of(context).size.width / numBoxes) - 4 - shrink,
            fit: BoxFit.cover)
        : Image(
            image: NetworkImage(server + "/image/" + id.toString()),
            height: (MediaQuery.of(context).size.width / numBoxes) - 4 - shrink,
            width: (MediaQuery.of(context).size.width / numBoxes) - 4 - shrink,
            fit: BoxFit.cover);
  }

  void downloadLoop(
      Function update, BuildContext context, BuildContext context2) async {
    bool inUpload = false;
    while (progress < selectedImages.length) {
      if (!inUpload) {
        inUpload = true;
        try {
          var response = await http.get(Uri.parse(server +
              "/image/" +
              selectedImages[progress.round()].toString()));
          Directory documentDirectory =
              await getApplicationDocumentsDirectory();
          String ext = ".jpg";
          if (lookupMimeType(join(documentDirectory.path, ''), headerBytes: [
                response.bodyBytes.elementAt(0),
                response.bodyBytes.elementAt(1)
              ]) ==
              null) {
            ext = ".png";
          }

          await makeNetworkRequest(
              {"id": selectedImages[progress.round()].toString()},
              "/get_img_tags", (json) async {
            List<dynamic> tagsJson = json['data'] as List<dynamic>;
            List<String> tags = List.empty(growable: true);
            for (String tag in tagsJson) {
              tags.add(tag);
            }

            File file = new File(join(documentDirectory.path,
                selectedImages[progress.round()].toString() + ext));
            await file.writeAsBytes(response.bodyBytes);
            await uploadImage(tags, selectedImages[progress.round()]);
            await updateGlobalTagList(tags);
            progress++;

            inUpload = false;
            update();
            if (progress == selectedImages.length) {
              Navigator.pop(context2);
              savedImages = await getImagesWithTags(List.empty());
              selectedImages.clear();
              setState(() {});
            }
          }, null, context);
        } catch (e) {
          print(e);
          Navigator.pop(context2);
          progress = selectedImages.length / 1.0;
          inUpload = false;
        }
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    if (!launchTaskRan) {
      launchTaskRan = true;
      Future.delayed(Duration.zero, () => launchTask(context));
    }

    int numBoxes = (MediaQuery.of(context).size.width / 130).round();

    return Scaffold(
        backgroundColor: Color(0xff36393f),
        appBar: (selectedImages.length > 0)
            ? selectModeBar(selectedImages.length.toString() + " selected", () {
                selectedImages.clear();
                setState(() {});
              }, () {
                bool savedImageSelected = false;
                for (int i in selectedImages) {
                  if (savedImages.contains(i)) {
                    savedImageSelected = true;
                    break;
                  }
                }

                showDialog(
                  context: context,
                  builder: (BuildContext context) {
                    return AlertDialog(
                      shape: RoundedRectangleBorder(
                          borderRadius:
                              BorderRadius.all(Radius.circular(20.0))),
                      backgroundColor: Color(0xff2f3136),
                      content: Container(
                        width: 300,
                        child: ListView(
                          shrinkWrap: true,
                          children: <Widget>[
                            Text(
                              savedImageSelected
                                  ? "Would you like to delete these images from the server, or would you like to just delete them from the local device?"
                                  : "Are you sure you want to delete these images? They will be lost permanently.",
                              textAlign: TextAlign.center,
                              style: TextStyle(
                                  fontSize: 20.0, color: Colors.white),
                            ),
                            SizedBox(
                              height: 5,
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
                              child: Container(child: Text("Cancel")),
                              onPressed: () {
                                Navigator.pop(context);
                              },
                            ),
                            SizedBox(
                              height: 5,
                            ),
                            ElevatedButton(
                              style: ButtonStyle(
                                backgroundColor:
                                    MaterialStateProperty.resolveWith<Color>(
                                  (Set<MaterialState> states) {
                                    return Colors.red;
                                  },
                                ),
                              ),
                              child: Container(
                                  child: Text(localMode
                                      ? "Yes, delete"
                                      : "Delete from server")),
                              onPressed: () async {
                                if (localMode) {
                                  for (int i in selectedImages) {
                                    await removeImage(i);
                                    getLocalImage(i).delete();
                                  }
                                } else {
                                  showLoading(context);
                                  for (int i in selectedImages) {
                                    if (savedImages.contains(i)) {
                                      await removeImage(i);
                                      getLocalImage(i).delete();
                                    }
                                    await makeNetworkRequest(
                                        {"id": i.toString()},
                                        "/remove_image",
                                        (json) {},
                                        null,
                                        context);
                                  }
                                  hideLoading();
                                }

                                Navigator.pop(context);
                                getImagesAndTags(context);
                                selectedImages.clear();
                                setState(() {});
                              },
                            ),
                            savedImageSelected
                                ? SizedBox(
                                    height: 5,
                                  )
                                : Container(
                                    width: 0,
                                    height: 0,
                                  ),
                            savedImageSelected
                                ? ElevatedButton(
                                    style: ButtonStyle(
                                      backgroundColor: MaterialStateProperty
                                          .resolveWith<Color>(
                                        (Set<MaterialState> states) {
                                          return Colors.yellow;
                                        },
                                      ),
                                    ),
                                    child: Container(
                                        child: Text(
                                      "Delete from local device",
                                      style: TextStyle(color: Colors.black),
                                    )),
                                    onPressed: () async {
                                      for (int i in selectedImages) {
                                        if (savedImages.contains(i)) {
                                          await removeImage(i);
                                          getLocalImage(i).delete();
                                        }
                                      }

                                      Navigator.pop(context);
                                      savedImages =
                                          await getImagesWithTags(List.empty());
                                      selectedImages.clear();
                                      setState(() {});
                                    },
                                  )
                                : Container(
                                    width: 0,
                                    height: 0,
                                  )
                          ],
                        ),
                      ),
                    );
                  },
                );
              }, () {
                List<int> trimmed = List.empty(growable: true);
                for (int i in selectedImages) {
                  if (!savedImages.contains(i)) {
                    trimmed.add(i);
                  }
                }
                selectedImages = trimmed;

                if (selectedImages.length == 0) {
                  print("done");
                  setState(() {});
                  return;
                }

                bool downloadLoopLaunched = false;
                progress = 0;

                showDialog(
                    barrierDismissible: false,
                    context: context,
                    builder: (BuildContext context) {
                      return StatefulBuilder(builder: (context2, setState) {
                        if (!downloadLoopLaunched) {
                          downloadLoopLaunched = true;
                          downloadLoop(
                              () => setState(() {}), context, context2);
                        }
                        return WillPopScope(
                            child: AlertDialog(
                              shape: RoundedRectangleBorder(
                                  borderRadius:
                                      BorderRadius.all(Radius.circular(20.0))),
                              backgroundColor: Color(0xff2f3136),
                              content: Container(
                                width: 300,
                                child: ListView(
                                  shrinkWrap: true,
                                  children: <Widget>[
                                    Text(
                                      "Please wait, downloading...",
                                      textAlign: TextAlign.center,
                                      style: TextStyle(
                                          fontSize: 20.0, color: Colors.white),
                                    ),
                                    SizedBox(
                                      height: 15,
                                    ),
                                    LinearProgressIndicator(
                                      value: progress / selectedImages.length,
                                      semanticsLabel: 'Progress indicator',
                                    ),
                                  ],
                                ),
                              ),
                            ),
                            onWillPop: () => Future.value(false));
                      });
                    });
              })
            : offline
                ? offlineFEMBOYBar(loggedOut ? "Logged out" : "Offline", () {
                    offlineRetry = true;
                    getImagesAndTags(context);
                  },
                    () => search(context),
                    () => Navigator.push(
                          context,
                          MaterialPageRoute(builder: (context) => Settings()),
                        ))
                : FEMBOYBar(
                    recievedImages.length.toString() + " results",
                    () => getImagesAndTags(context),
                    () => search(context),
                    () => uploadFile(context),
                    () => Navigator.push(
                          context,
                          MaterialPageRoute(builder: (context) => Settings()),
                        )),
        body: (recievedImages.length == 0 && filterTags.length == 0)
            ? Text(
                "\nYour image library is empty, add something to it!",
                textAlign: TextAlign.center,
                style: TextStyle(fontSize: 20.0, color: Colors.white),
              )
            : GridView.count(
                crossAxisCount: numBoxes,
                children: List.generate(recievedImages.length, (index) {
                  return Center(
                      child: Padding(
                          padding: const EdgeInsets.all(2),
                          child: GestureDetector(
                              onLongPress: () {
                                if (!offline) {
                                  vibrateDevice();
                                  if (!selectedImages
                                      .contains(recievedImages[index])) {
                                    selectedImages.add(recievedImages[index]);
                                    setState(() {});
                                  }
                                }
                              },
                              child: Stack(children: <Widget>[
                                Align(
                                    alignment: Alignment.center,
                                    child: OpenContainer(
                                      transitionType:
                                          ContainerTransitionType.fadeThrough,
                                      closedColor: Color(0xff40444b),
                                      closedElevation: 0.0,
                                      openElevation: 4.0,
                                      transitionDuration:
                                          Duration(milliseconds: 200),
                                      openBuilder: (BuildContext context,
                                              VoidCallback _) =>
                                          ImageViewer(
                                              index, () => setState(() {})),
                                      closedBuilder: (BuildContext _,
                                          VoidCallback openContainer) {
                                        return gridImage(
                                            recievedImages[index],
                                            context,
                                            numBoxes,
                                            (selectedImages.contains(
                                                    recievedImages[index])
                                                ? 20
                                                : 1));
                                      },
                                    )),
                                selectedImages.length > 0
                                    ? Container(
                                        width:
                                            (MediaQuery.of(context).size.width /
                                                    numBoxes) -
                                                4,
                                        height:
                                            (MediaQuery.of(context).size.width /
                                                    numBoxes) -
                                                4,
                                        child: GestureDetector(
                                          onTap: () {
                                            if (!selectedImages.contains(
                                                recievedImages[index])) {
                                              selectedImages
                                                  .add(recievedImages[index]);
                                            } else {
                                              selectedImages.remove(
                                                  recievedImages[index]);
                                            }
                                            setState(() {});
                                          },
                                        ),
                                      )
                                    : Container(
                                        height: 0,
                                        width: 0,
                                      ),
                                selectedImages.contains(recievedImages[index])
                                    ? Align(
                                        alignment: Alignment.topLeft,
                                        child: ClipOval(
                                          child: Material(
                                            color: Colors.white,
                                            child: Icon(Icons.check),
                                          ),
                                        ),
                                      )
                                    : Container(
                                        height: 0,
                                        width: 0,
                                      ),
                                savedImages.contains(recievedImages[index]) ||
                                        offline
                                    ? Align(
                                        alignment: Alignment.topRight,
                                        child: Container(
                                          padding: EdgeInsets.all(1),
                                          child: ClipOval(
                                            child: Material(
                                              color: Colors.white,
                                              child: Container(
                                                child: Icon(Icons.save),
                                                padding: EdgeInsets.all(1),
                                              ),
                                            ),
                                          ),
                                        ),
                                      )
                                    : Container(
                                        height: 0,
                                        width: 0,
                                      )
                              ]))));
                }),
              ));
  }
}
