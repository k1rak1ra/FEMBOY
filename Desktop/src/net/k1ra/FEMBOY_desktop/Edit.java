package net.k1ra.FEMBOY_desktop;

import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import net.k1ra.FEMBOY_desktop.libFEMBOY.DatabaseAbstractionLayer;
import org.json.JSONArray;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class Edit {
    @FXML ImageView image;
    @FXML AnchorPane ap;
    @FXML JFXListView tag_list;
    @FXML AnchorPane ap_text_field;
    @FXML Button btn_add_tag;
    @FXML JFXTextField tag;
    @FXML Button close;
    @FXML Button save;
    @FXML Button next;
    @FXML Button previous;
    @FXML AnchorPane loading_pane;
    @FXML Text mimetype;
    @FXML Text dimensions;

    double scene_width = 1;
    double scene_height = 1;
    Image img;
    List<String> tags;
    boolean edited = false;
    int index = 0;
    boolean loading_tags = false;

    void multi(List<Integer> ids, List<String> images, Scene scene, Stage stage, Runnable on_save) {
        populate(ids.get(index), images.size() > 0? images.get(index):null, scene, stage, index == 0, index == ids.size()-1, on_save);

        next.setOnMouseClicked(event -> {
            if (edited) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Unsaved changes alert");
                alert.setHeaderText("Are you sure you want to go to the next image? You have unsaved changes!");

                alert.showAndWait().ifPresent((btnType) -> {
                    if (btnType.equals(ButtonType.OK)) {
                        index++;
                        edited = false;
                        populate(ids.get(index), images.size() > 0? images.get(index):null, scene, stage, index == 0, index == ids.size() - 1, on_save);
                    }
                });
            } else {
                index++;
                get_tags(ids.get(index));
                edited = false;
                populate(ids.get(index), images.size() > 0? images.get(index):null, scene, stage, index == 0, index == ids.size()-1, on_save);
            }
        });

        previous.setOnMouseClicked(event -> {
            if (edited) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Unsaved changes alert");
                alert.setHeaderText("Are you sure you want to go back to the last image? You have unsaved changes!");

                alert.showAndWait().ifPresent((btnType) -> {
                    if (btnType.equals(ButtonType.OK)) {
                        index--;
                        edited = false;
                        populate(ids.get(index), images.size() > 0? images.get(index):null, scene, stage, index == 0, index == ids.size() - 1, on_save);
                    }
                });
            } else {
                index--;
                get_tags(ids.get(index));
                edited = false;
                populate(ids.get(index), images.size() > 0? images.get(index):null, scene, stage, index == 0, index == ids.size()-1, on_save);
            }
        });
    }

    void populate(int id, String file_path, Scene scene, Stage stage, boolean first, boolean last, Runnable on_save) {
        if (file_path != null) {
            final File img_file = new File(file_path);
            img = new Image(img_file.toURI().toString());
            try {
                InputStream is = new BufferedInputStream(new FileInputStream(img_file));
                mimetype.setText(URLConnection.guessContentTypeFromStream(is));
                is.close();
            } catch (IOException e){
                Utils.handle_error(e.toString());
            }
        } else {
            try {
                URL img_url = new URL(NetworkRequest.server_url + "/image/" + id);
                img = new Image(img_url.toString());
                mimetype.setText(img_url.openConnection().getContentType());
            } catch (Exception e) {
                Utils.handle_error(e.toString());
            }
        }

        dimensions.setText("H:"+(int)img.getHeight() + " W:"+(int)img.getWidth());

        loading_pane.setVisible(false);
        resize_panes();

        //styling
        ap.setBackground(new Background(new BackgroundFill(Color.web("#2f3136"), CornerRadii.EMPTY, Insets.EMPTY)));
        tag_list.setBackground(new Background(new BackgroundFill(Color.web("#292b2f"), CornerRadii.EMPTY, Insets.EMPTY)));
        ap_text_field.setBackground(new Background(new BackgroundFill(Color.web("#202225"), CornerRadii.EMPTY, Insets.EMPTY)));
        tag_list.getStylesheets().add(this.getClass().getResource("CSS/taglist.css").toExternalForm());
        btn_add_tag.setStyle("-fx-background-color: #40444b");
        tag.getStylesheets().add(this.getClass().getResource("CSS/text_field.css").toExternalForm());
        close.setStyle("-fx-background-color: #40444b");
        save.setStyle("-fx-background-color: #40444b");
        next.setStyle("-fx-background-color: #40444b");
        previous.setStyle("-fx-background-color: #40444b");

        if (first) {
            previous.setVisible(false);
            previous.setDisable(true);
        } else {
            previous.setVisible(true);
            previous.setDisable(false);
        }

        if (last) {
            next.setVisible(false);
            next.setDisable(true);
        } else {
            next.setVisible(true);
            next.setDisable(false);
        }

        scene.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> {
            scene_width = newSceneWidth.doubleValue();
            resize_panes();
        });
        scene.heightProperty().addListener((observableValue, oldSceneHeight, newSceneHeight) -> {
            scene_height = newSceneHeight.doubleValue();
            resize_panes();
        });

        get_tags(id);

        btn_add_tag.setOnMouseClicked(event -> {
            String text = tag.getText();
            if (!text.isEmpty() && !tags.contains(text)) {
                tags.add(text);
                tag.setText("");
                edited = true;
                populate_tag_list();
            }
        });

        save.setOnMouseClicked(event -> {
            if (DatabaseAbstractionLayer.get_mode() == 1) {
                DatabaseAbstractionLayer.update_img_tags(id, tags);
                edited = false;
                update_save_button();
                on_save.run();
            } else {
                JSONArray arr = new JSONArray();
                for (String tag : tags)
                    arr.put(tag);

                final NetworkResponse out = new NetworkResponse();
                NetworkRequest.make_POST(out, "/set_img_tags", new Pair[] { Pair.create("id", String.valueOf(id)) , Pair.create("tags", arr.toString()) }, ()->{
                    edited = false;
                    update_save_button();
                    on_save.run();
                }, () -> {}, true);
            }
        });

        close.setOnMouseClicked(event -> {
            if (edited) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Unsaved changes alert");
                alert.setHeaderText("Are you sure you want to close this window? You have unsaved changes!");

                alert.showAndWait().ifPresent((btnType) -> {
                    if (btnType.equals(ButtonType.OK))
                        stage.close();
                });
            } else {
                stage.close();
            }
        });

        if ((Controller.offline.getValue() || !Controller.logged_in.getValue()) && DatabaseAbstractionLayer.get_mode() == 0) {
            btn_add_tag.setDisable(true);
            btn_add_tag.setText("Cannot add tags when offline");
        }
    }

    void get_tags(int id){
        if (!loading_tags) {
            loading_tags = true;
            if (DatabaseAbstractionLayer.get_mode() == 1 || Controller.offline.getValue() || !Controller.logged_in.getValue()) {
                tags = DatabaseAbstractionLayer.get_img_tags(id);
                loading_tags = false;
            } else {
                tags = new ArrayList<>();
                loading_pane.setVisible(true);

                final NetworkResponse out = new NetworkResponse();
                NetworkRequest.make_POST(out, "/get_img_tags", new Pair[]{Pair.create("id", String.valueOf(id))}, () -> {

                    JSONArray arr = out.obj.getJSONArray("data");

                    for (int i = 0; i < arr.length(); i++)
                        tags.add(arr.getString(i));

                    resize_panes();
                    loading_pane.setVisible(false);
                    loading_tags = false;
                }, () -> { }, true);
            }
        }
    }

    void update_save_button(){
        if (edited) {
            save.setText("Save");
            save.setDisable(false);
        } else {
            save.setText("Saved!");
            save.setDisable(true);
        }
    }

    void populate_tag_list() {
        update_save_button();
        tag_list.getItems().clear();

        if ((int)((scene_width-image.boundsInParentProperty().get().getWidth())/6) > 0) {
            for (String tag : tags) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("FXML/tag_cell.fxml"));
                    Parent root = loader.load();
                    TagCell cell = loader.getController();
                    tag_list.getItems().add(root);

                    cell.populate(tag, tag_list, root, () -> {
                        tags.remove(tag);
                        edited = true;
                        update_save_button();
                    }, !(!Controller.logged_in.getValue() || Controller.offline.getValue()) || DatabaseAbstractionLayer.get_mode() == 1);
                } catch (Exception e) {
                    Utils.handle_error(e.toString());
                }
            }
        }
    }

    void resize_panes() {
        double smallest_dimension = Math.min(scene_height, scene_width);
        image.setFitHeight(smallest_dimension*0.5);
        image.setFitWidth(smallest_dimension*0.5);
        image.setImage(img);

        tag_list.setPrefWidth(scene_width-image.boundsInParentProperty().get().getWidth());
        ap_text_field.setPrefWidth(image.boundsInParentProperty().get().getWidth());
        ap_text_field.setPrefHeight(scene_height - image.boundsInParentProperty().get().getHeight() - 50);

        populate_tag_list();
    }

}
