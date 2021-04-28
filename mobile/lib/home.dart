import 'dart:convert';
import 'dart:io';
import 'package:FEMBOY/database.dart';
import 'package:FEMBOY/settings.dart';
import 'package:FEMBOY/tagging.dart';
import 'package:animations/animations.dart';
import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
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

List<int> recievedImages = List.empty();
List<int> selectedImages = List.empty(growable: true);
List<int> savedImages = List.empty();
List<String> allTags = List.empty();
final List<TagListChip> filterTags = List.empty(growable: true);
bool gettingImages = false;
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
    return Chip(
        deleteIcon: Icon(Icons.close_outlined),
        backgroundColor: Colors.blue[400],
        label: Text(this.tag),
        onDeleted: () => onDeleted(this, setState));
  }
}

class HomeState extends State<Home> {
  double progress = 0;

  void getImagesAndTags(BuildContext context) async {
    if (!gettingImages) {
      gettingImages = true;
      if (localMode) {
        recievedImages =
            await getImagesWithTags(filterTags.map((e) => e.tag).toList());
        allTags = await getAllTags();

        setState(
          () {
            gettingImages = false;
          },
        );
      } else {
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

            setState(
              () {
                recievedImages = recievedImagesNew;
                allTags = allTagsNew;
                gettingImages = false;
                hideLoading();
              },
            );
          }, null, context);
        }, null, context);
      }
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
    //TODO may need iOS fix https://pub.dev/packages/file_picker
    FilePickerResult result = await FilePicker.platform.pickFiles(
        allowMultiple: true,
        type: FileType.custom,
        allowedExtensions: ['jpg', 'jpeg', 'png']);

    if (result != null) {
      List<File> files = result.paths.map((path) => File(path)).toList();
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

  void launchTask(BuildContext context) async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    PackageInfo packageInfo = await PackageInfo.fromPlatform();
    session =
        SessionInfo(int.parse(packageInfo.buildNumber), packageInfo.version);
    await session.updateUserInfo();
    await setServer();
    documentDirectory = await getApplicationDocumentsDirectory();
    if (prefs.getBool("setup_done") != null && prefs.getBool("setup_done")) {
      localMode = prefs.getBool("local_mode");
      getImagesAndTags(context);
    } else {
      showIntroDialog(context, () => setState(() => {}));
    }
  }

  Widget gridImage(int id, BuildContext context, int numBoxes, int shrink) {
    return localMode
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
          var response = await http.get(
              server + "/image/" + selectedImages[progress.round()].toString());
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
              closeModel();
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
                //delet
              }, () {
                for (int i in selectedImages) {
                  if (savedImages.contains(i)) {
                    selectedImages.remove(i);
                  }
                }

                if (selectedImages.length == 0) {
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
                                vibrateDevice();
                                if (!selectedImages
                                    .contains(recievedImages[index])) {
                                  selectedImages.add(recievedImages[index]);
                                  setState(() {});
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
                                savedImages.contains(recievedImages[index])
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
