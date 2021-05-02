import 'package:FEMBOY/appbar.dart';
import 'package:FEMBOY/database.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:photo_view/photo_view.dart';
import 'home.dart';
import 'network_requests.dart';
import 'package:share/share.dart';
import 'package:mime/mime.dart';
import 'dart:io';
import 'package:http/http.dart' as http;
import 'package:path/path.dart';
import 'package:path_provider/path_provider.dart';

final controllerTagEdit = TextEditingController();
List<TagListChip> tagEditorList = List.empty(growable: true);
ScrollPhysics pageViewPhysics = ScrollPhysics();

class ImageViewer extends StatefulWidget {
  final int index;
  final Function setHomeState;
  ImageViewer(this.index, this.setHomeState);

  @override
  ImageViewerState createState() => ImageViewerState(index, setHomeState);
}

class ImageViewerState extends State<ImageViewer> {
  int index;
  final Function setHomeState;
  ImageViewerState(this.index, this.setHomeState);

  List<Widget> getPages() {
    List<Widget> list = List.empty(growable: true);
    for (int image in recievedImages) {
      list.add(PhotoView(
        loadingBuilder: (context, progress) => Center(
          child: Container(
            width: 30.0,
            height: 30.0,
            child: CircularProgressIndicator(),
          ),
        ),
        minScale: PhotoViewComputedScale.contained * 1.0,
        scaleStateChangedCallback: (scale) {
          print(scale);
          if (scale != PhotoViewScaleState.initial) {
            pageViewPhysics = NeverScrollableScrollPhysics();
          } else {
            pageViewPhysics = ScrollPhysics();
          }
          setState(() {});
        },
        imageProvider: localMode || offline
            ? Image.file(getLocalImage(image)).image
            : NetworkImage(server + "/image/" + image.toString()),
      ));
    }
    return list;
  }

  void editDialog(Map<String, dynamic> json, BuildContext context) async {
    bool modified = false;

    List<dynamic> tags = localMode || offline
        ? await getImgTags(recievedImages[index])
        : json['data'] as List<dynamic>;

    tagEditorList.clear();
    for (String tag in tags) {
      tagEditorList.add(TagListChip(tag, (item, setState) {
        if (!offline) {
          modified = true;
          tagEditorList.remove(item);
          setState();
        }
      }, () => setState(() {})));
    }

    hideLoading();
    showDialog(
        context: context,
        builder: (BuildContext context2) {
          return StatefulBuilder(
            builder: (context2, setState) {
              for (TagListChip chip in tagEditorList) {
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
                          offline
                              ? Container(
                                  width: 0,
                                  height: 0,
                                )
                              : TextField(
                                  style: TextStyle(
                                      fontSize: 16.0, color: Colors.white),
                                  minLines: 1,
                                  maxLines: 1,
                                  controller: controllerTagEdit,
                                  decoration: InputDecoration(
                                      contentPadding: EdgeInsets.all(0.0),
                                      isDense: true,
                                      hintStyle: TextStyle(
                                          fontSize: 16.0,
                                          color: Colors.white60),
                                      enabledBorder: UnderlineInputBorder(
                                        borderSide:
                                            BorderSide(color: Colors.white),
                                      ),
                                      hintText: 'Enter a tag')),
                          offline
                              ? Container(
                                  width: 0,
                                  height: 0,
                                )
                              : SizedBox(
                                  height: 5,
                                ),
                          offline
                              ? Container(
                                  width: 0,
                                  height: 0,
                                )
                              : ElevatedButton(
                                  style: ButtonStyle(
                                    backgroundColor: MaterialStateProperty
                                        .resolveWith<Color>(
                                      (Set<MaterialState> states) {
                                        return Color(0xff40444b);
                                      },
                                    ),
                                  ),
                                  child: Container(child: Text("Add")),
                                  onPressed: () {
                                    if (!tagEditorList
                                        .map((e) => e.tag)
                                        .toList()
                                        .contains(controllerTagEdit.text)) {
                                      modified = true;
                                      tagEditorList.add(
                                          TagListChip(controllerTagEdit.text,
                                              (item, setState) {
                                        tagEditorList.remove(item);
                                        setState();
                                      }, () => setState(() {})));
                                      setState(() {});
                                    }
                                    controllerTagEdit.clear();
                                  },
                                ),
                          offline
                              ? Container(
                                  width: 0,
                                  height: 0,
                                )
                              : SizedBox(
                                  height: 15,
                                ),
                          Wrap(
                              spacing: 8.0, // gap between adjacent chips
                              runSpacing: 4.0, // gap between lines
                              children:
                                  tagEditorList.map((e) => e.get()).toList()),
                          offline
                              ? Container(
                                  width: 0,
                                  height: 0,
                                )
                              : SizedBox(
                                  height: 15,
                                ),
                          offline
                              ? Container(
                                  width: 0,
                                  height: 0,
                                )
                              : ElevatedButton(
                                  style: ButtonStyle(
                                    backgroundColor: MaterialStateProperty
                                        .resolveWith<Color>(
                                      (Set<MaterialState> states) {
                                        return Color(0xff40444b);
                                      },
                                    ),
                                  ),
                                  child: Container(child: Text("Save")),
                                  onPressed: () {
                                    if (localMode) {
                                      updateImgTags(
                                          tagEditorList
                                              .map((e) => e.tag)
                                              .toList(),
                                          recievedImages[index]);
                                      Navigator.pop(context2);
                                    } else {
                                      showLoading(context);
                                      makeNetworkRequest({
                                        "tags": stringListToJSON(tagEditorList
                                            .map((e) => e.tag)
                                            .toList()),
                                        "id": recievedImages[index].toString()
                                      }, "/set_img_tags", (json) {
                                        Navigator.pop(context2);
                                        hideLoading();
                                      }, null, context);
                                    }
                                  },
                                ),
                        ],
                      ),
                    ),
                  ),
                  onWillPop: () {
                    if (modified) {
                      Future.delayed(
                          Duration.zero,
                          () => {
                                showDialog(
                                  context: context,
                                  builder: (BuildContext context) {
                                    return AlertDialog(
                                      shape: RoundedRectangleBorder(
                                          borderRadius: BorderRadius.all(
                                              Radius.circular(20.0))),
                                      backgroundColor: Color(0xff2f3136),
                                      content: Container(
                                        child: ListView(
                                          shrinkWrap: true,
                                          children: <Widget>[
                                            Text(
                                              "You have unsaved changes. Are you sure you want to close the tag editor?",
                                              textAlign: TextAlign.center,
                                              style: TextStyle(
                                                  fontSize: 20.0,
                                                  color: Colors.white),
                                            ),
                                            SizedBox(
                                              height: 5,
                                            ),
                                            ElevatedButton(
                                              style: ButtonStyle(
                                                backgroundColor:
                                                    MaterialStateProperty
                                                        .resolveWith<Color>(
                                                  (Set<MaterialState> states) {
                                                    return Color(0xff40444b);
                                                  },
                                                ),
                                              ),
                                              child: Container(
                                                  child: Text("Go back")),
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
                                                    MaterialStateProperty
                                                        .resolveWith<Color>(
                                                  (Set<MaterialState> states) {
                                                    return Colors.red;
                                                  },
                                                ),
                                              ),
                                              child: Container(
                                                  child: Text("Close it")),
                                              onPressed: () {
                                                Navigator.pop(context);
                                                Navigator.pop(context2);
                                              },
                                            )
                                          ],
                                        ),
                                      ),
                                    );
                                  },
                                )
                              });
                    }
                    return Future.value(!modified);
                  });
            },
          );
        });
  }

  void edit(BuildContext context) {
    if (localMode || offline) {
      editDialog(null, context);
    } else {
      showLoading(context);
      makeNetworkRequest(
          {"id": recievedImages[index].toString()}, "/get_img_tags", (json) {
        editDialog(json, context);
      }, null, context);
    }
  }

  void share(BuildContext context) async {
    if (localMode || offline) {
      Share.shareFiles([getLocalImage(recievedImages[index]).path]);
    } else {
      showLoading(context);
      var response = await http.get(
          Uri.parse(server + "/image/" + recievedImages[index].toString()));
      Directory documentDirectory = await getApplicationDocumentsDirectory();
      String ext = ".jpg";
      if (lookupMimeType(join(documentDirectory.path, ''), headerBytes: [
            response.bodyBytes.elementAt(0),
            response.bodyBytes.elementAt(1)
          ]) ==
          null) {
        ext = ".png";
      }
      File file = new File(join(documentDirectory.path, 'download' + ext));
      await file.writeAsBytes(response.bodyBytes);
      hideLoading();
      Share.shareFiles([join(documentDirectory.path, 'download' + ext)]);
    }
  }

  void delete(BuildContext context) {
    showDialog(
      context: context,
      builder: (BuildContext context2) {
        return AlertDialog(
          shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.all(Radius.circular(20.0))),
          backgroundColor: Color(0xff2f3136),
          content: Container(
            child: ListView(
              shrinkWrap: true,
              children: <Widget>[
                Text(
                  "Are you sure you want to delete this image?",
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
                  child: Container(child: Text("No, don't delete")),
                  onPressed: () {
                    Navigator.pop(context2);
                  },
                ),
                SizedBox(
                  height: 5,
                ),
                ElevatedButton(
                  style: ButtonStyle(
                    backgroundColor: MaterialStateProperty.resolveWith<Color>(
                      (Set<MaterialState> states) {
                        return Colors.red;
                      },
                    ),
                  ),
                  child: Container(child: Text("Yes, delete it")),
                  onPressed: () async {
                    if (localMode) {
                      await removeImage(recievedImages[index]);
                      getLocalImage(recievedImages[index]).delete();
                      Navigator.pop(context2);
                      recievedImages.removeAt(index);
                      Navigator.pop(context);
                      setHomeState();
                    } else {
                      showLoading(context2);
                      makeNetworkRequest(
                          {"id": recievedImages[index].toString()},
                          "/remove_image", (json) {
                        hideLoading();
                        Navigator.pop(context2);
                        recievedImages.removeAt(index);
                        Navigator.pop(context);
                        setHomeState();
                      }, null, context2);
                    }
                  },
                )
              ],
            ),
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    final PageController controller = PageController(initialPage: index);
    return Scaffold(
        backgroundColor: Colors.black,
        appBar: offline
            ? offlineImageViewerBar("Offline image viewer", () => edit(context),
                () => share(context))
            : imageViewerBar("Image viewer", () => edit(context),
                () => delete(context), () => share(context)),
        body: PageView(
          scrollDirection: Axis.horizontal,
          physics: pageViewPhysics,
          onPageChanged: (value) => index = value,
          controller: controller,
          children: getPages(),
        ));
  }
}
