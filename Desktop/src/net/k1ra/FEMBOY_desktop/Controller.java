package net.k1ra.FEMBOY_desktop;

import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.k1ra.FEMBOY_desktop.libFEMBOY.DatabaseAbstractionLayer;
import net.k1ra.FEMBOY_desktop.libFEMBOY.TagAbstractionLayer;
import org.json.JSONArray;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class Controller implements Initializable {
    public Stage stage;
    JFXAutoCompletePopup<String> autocomplete;
    List<String> filter_tags;

    @FXML JFXMasonryPane mp;
    @FXML ScrollPane sp;
    @FXML AnchorPane ap;
    @FXML AnchorPane ap_search;
    @FXML AnchorPane ap_tag_list;
    @FXML Button btn_upload;
    @FXML Button btn_settings;
    @FXML Button btn_add_tag;
    @FXML JFXTextField search_field;
    @FXML JFXListView tag_list;
    @FXML Text empty_msg;
    @FXML JFXSpinner loading;
    @FXML AnchorPane intro_cover_pane;
    @FXML AnchorPane step_1_modal_container;
    @FXML AnchorPane step_2_modal_container;
    @FXML AnchorPane loading_modal_container;
    @FXML AnchorPane sql_setup_modal_container;
    @FXML AnchorPane done_modal_container;
    @FXML AnchorPane login_modal_container;
    @FXML AnchorPane step_1_modal;
    @FXML AnchorPane step_2_modal;
    @FXML AnchorPane loading_modal;
    @FXML AnchorPane login_modal;
    @FXML AnchorPane sql_setup_modal;
    @FXML AnchorPane done_modal;
    @FXML Button btn_local;
    @FXML Button btn_server;
    @FXML Button btn_step2_connect;
    @FXML JFXTextField step2_server_field;
    @FXML Button btn_sql_continue;
    @FXML JFXTextField sql_address;
    @FXML JFXTextField sql_dbname;
    @FXML JFXTextField sql_user;
    @FXML JFXPasswordField sql_password;
    @FXML JFXPasswordField root_password_1;
    @FXML JFXPasswordField root_password_2;
    @FXML Button btn_setup_done;
    @FXML JFXTextField login_user;
    @FXML JFXPasswordField login_password;
    @FXML Button btn_login;
    @FXML ProgressBar upload_progress;
    @FXML AnchorPane status_pane;
    @FXML Text status_line;
    @FXML Button btn_status_action;
    @FXML Text result_num_line;

    volatile boolean upload_tread_done = false;
    volatile boolean inhibit_mp_reload = false;
    static BooleanProperty offline = new SimpleBooleanProperty(false);
    static BooleanProperty logged_in = new SimpleBooleanProperty(true);
    static BooleanProperty is_loading = new SimpleBooleanProperty(false);
    boolean showing_no_internet_dialog = false;
    int status = 0;
    List<Image> images = new ArrayList<>();
    int mp_index = 0;
    List<Integer> mp_ids = new ArrayList<>();
    List<Float> mp_aspects = new ArrayList<>();
    Boolean sp_bottom_flag = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //set pane background colors
        sp.setStyle("-fx-background: #36393f; -fx-border-color: #36393f");
        ap.setBackground(new Background(new BackgroundFill(Color.web("#2f3136"), CornerRadii.EMPTY, Insets.EMPTY)));
        ap_search.setBackground(new Background(new BackgroundFill(Color.web("#202225"), new CornerRadii(20), Insets.EMPTY)));
        ap_tag_list.setBackground(new Background(new BackgroundFill(Color.web("#292b2f"), new CornerRadii(20), Insets.EMPTY)));
        intro_cover_pane.setBackground(new Background(new BackgroundFill(Color.web("#000000"), CornerRadii.EMPTY, Insets.EMPTY)));
        step_1_modal.setBackground(new Background(new BackgroundFill(Color.web("#2f3136"), new CornerRadii(50), Insets.EMPTY)));
        step_2_modal.setBackground(new Background(new BackgroundFill(Color.web("#2f3136"), new CornerRadii(50), Insets.EMPTY)));
        loading_modal.setBackground(new Background(new BackgroundFill(Color.web("#2f3136"), new CornerRadii(50), Insets.EMPTY)));
        sql_setup_modal.setBackground(new Background(new BackgroundFill(Color.web("#2f3136"), new CornerRadii(50), Insets.EMPTY)));
        login_modal.setBackground(new Background(new BackgroundFill(Color.web("#2f3136"), new CornerRadii(50), Insets.EMPTY)));
        done_modal.setBackground(new Background(new BackgroundFill(Color.web("#2f3136"), new CornerRadii(50), Insets.EMPTY)));
        tag_list.setBackground(new Background(new BackgroundFill(Color.web("#292b2f"), new CornerRadii(20), Insets.EMPTY)));
        status_pane.setBackground(new Background(new BackgroundFill(Color.web("#202225"), new CornerRadii(20), Insets.EMPTY)));

        //set colors for items such as buttons
        btn_upload.setStyle("-fx-background-color: #40444b");
        btn_settings.setStyle("-fx-background-color: #40444b");
        btn_add_tag.setStyle("-fx-background-color: #40444b");
        btn_local.setStyle("-fx-background-color: #40444b");
        btn_server.setStyle("-fx-background-color: #40444b");
        btn_step2_connect.setStyle("-fx-background-color: #40444b");
        btn_sql_continue.setStyle("-fx-background-color: #40444b");
        btn_setup_done.setStyle("-fx-background-color: #40444b");
        btn_login.setStyle("-fx-background-color: #40444b");
        btn_status_action.setStyle("-fx-background-color: #40444b");

        //scroll pane scrollbar settings
        sp.getStylesheets().add(this.getClass().getResource("CSS/scroll_main.css").toExternalForm());
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        //text field CSS
        search_field.getStylesheets().add(this.getClass().getResource("CSS/text_field.css").toExternalForm());
        step2_server_field.getStylesheets().add(this.getClass().getResource("CSS/text_field.css").toExternalForm());
        sql_address.getStylesheets().add(this.getClass().getResource("CSS/text_field.css").toExternalForm());
        sql_dbname.getStylesheets().add(this.getClass().getResource("CSS/text_field.css").toExternalForm());
        sql_user.getStylesheets().add(this.getClass().getResource("CSS/text_field.css").toExternalForm());
        sql_password.getStylesheets().add(this.getClass().getResource("CSS/text_field.css").toExternalForm());
        root_password_1.getStylesheets().add(this.getClass().getResource("CSS/text_field.css").toExternalForm());
        root_password_2.getStylesheets().add(this.getClass().getResource("CSS/text_field.css").toExternalForm());
        login_user.getStylesheets().add(this.getClass().getResource("CSS/text_field.css").toExternalForm());
        login_password.getStylesheets().add(this.getClass().getResource("CSS/text_field.css").toExternalForm());

        //listview css
        tag_list.getStylesheets().add(this.getClass().getResource("CSS/taglist.css").toExternalForm());

        //progressbar css
        upload_progress.getStylesheets().add(this.getClass().getResource("CSS/progress_bar.css").toExternalForm());

        //autocomplete setup
        autocomplete = new JFXAutoCompletePopup<>();
        autocomplete.setSelectionHandler(event -> search_field.setText(event.getObject()));
        filter_tags = new ArrayList<>();

        //hide uploading spinner
        loading.setVisible(false);

        // filtering options
        search_field.textProperty().addListener(observable -> {
            autocomplete.filter(string -> string.toLowerCase().contains(search_field.getText().toLowerCase()));
            if (autocomplete.getFilteredSuggestions().isEmpty() || search_field.getText().isEmpty())
                autocomplete.hide();
            else
                autocomplete.show(search_field);
        });

        //settings button
        btn_settings.setOnMouseClicked(event -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("FXML/settings.fxml"));
                    Pane pane = loader.load();
                    Settings settings = loader.getController();
                    Scene scene = new Scene(pane);
                    settings.scene = scene;
                    Stage stage = new Stage();
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setTitle("Settings");
                    stage.setMinHeight(700);
                    stage.setMinWidth(700);
                    stage.setScene(scene);
                    stage.show();
                    settings.init();
                } catch (Exception e) {
                    Utils.handle_error(e.toString());
                }
        });

        //upload button actions
        btn_upload.setOnMouseClicked(event -> {
                //Creating a File chooser
                FileChooser file_chooser = new FileChooser();
                file_chooser.setTitle("Select images to upload");
                file_chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Images", "*.jpg","*.jpeg", "*.png"));
                List<File> files = file_chooser.showOpenMultipleDialog(stage);

                List<String> images = new ArrayList<>();
                List<Integer> ids = new ArrayList<>();

                if (files != null) {
                    loading.setVisible(true);
                    btn_upload.setDisable(true);
                }

                new Thread(() -> {
                        if (files != null) {

                            //upload images locally if local mode, upload to server if server mode
                            if (DatabaseAbstractionLayer.get_mode() == 1) {
                                int current_index = DatabaseAbstractionLayer.get_img_index();
                                Platform.runLater(() -> upload_progress.setProgress(0));

                                for (int i = 0; i < files.size(); i++) {
                                    final double i_f = i;
                                    current_index++;
                                    Utils.copy_image(files.get(i), current_index);
                                    Image img = new Image(files.get(i).toURI().toString());
                                    TagAbstractionLayer.tag_image(files.get(i), current_index, (float)img.getHeight()/(float)img.getWidth());

                                    ids.add(current_index);
                                    images.add(Utils.get_local_storage_dir() + "images/" + current_index + "." + Utils.get_extension(files.get(i)));
                                    Platform.runLater(() ->  upload_progress.setProgress(i_f/(double)files.size()));
                                }

                                Platform.runLater(() -> upload_progress.setProgress(0));
                                DatabaseAbstractionLayer.put_img_index(current_index);
                            } else {
                                NetworkRequest.loading = null;
                                NetworkRequest.done_loading = null;
                                Platform.runLater(() -> upload_progress.setProgress(0));

                                for (final IntegerProperty i = new SimpleIntegerProperty(0); i.get() < files.size(); i.set(i.get()+1)) {
                                    final NetworkResponse out = new NetworkResponse();
                                    upload_tread_done = false;

                                    Image img = new Image(files.get(i.get()).toURI().toString());
                                    NetworkRequest.make_POST(out, "/upload_image", new Pair[] {
                                            Pair.create("image", Utils.base64_encode(files.get(i.get()))),
                                            Pair.create("aspect", String.valueOf(img.getHeight()/img.getWidth()))}, () ->{
                                        upload_progress.setProgress(i.get()/(double)files.size());
                                        ids.add(out.obj.getInt("id"));
                                        upload_tread_done = true;
                                    }, ()-> {
                                        if (out.error == -1) {
                                            Alert alert = new Alert(Alert.AlertType.ERROR);
                                            alert.setTitle("Error");
                                            alert.setHeaderText("Server connection issue.");
                                            alert.setContentText("Click OK to try resuming the image upload, next to skip the current image, or just cancel.");
                                            alert.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.NEXT);
                                            Optional<ButtonType> result = alert.showAndWait();
                                            if (result.get() == ButtonType.OK)
                                                i.set(i.get() - 1);
                                            else if (result.get() == ButtonType.CANCEL)
                                                i.set(files.size());
                                        }
                                        upload_tread_done = true;
                                    }, true);

                                    //wait until upload is done, do one by one
                                    while (!upload_tread_done)
                                        Thread.onSpinWait();
                                }

                                Platform.runLater(() -> upload_progress.setProgress(0));

                            }

                            Platform.runLater(() ->{
                                    reload_image_pane();
                                    refresh_auto_suggestions();
                                    loading.setVisible(false);
                                    btn_upload.setDisable(false);
                                    set_standard_loading();

                                    if (ids.size() > 0) {
                                        try {
                                            FXMLLoader loader = new FXMLLoader(getClass().getResource("FXML/edit.fxml"));
                                            Pane pane = loader.load();
                                            Stage stage = new Stage();
                                            Scene scene = new Scene(pane);

                                            Edit edit = loader.getController();
                                            edit.multi(ids, images, scene, stage, () -> {
                                                refresh_auto_suggestions();
                                                if (filter_tags.size() > 0) {
                                                    reload_image_pane();
                                                }
                                            });

                                            stage.initModality(Modality.APPLICATION_MODAL);
                                            stage.setMaximized(true);
                                            stage.setTitle("Tag editor");
                                            stage.setMinHeight(600);
                                            stage.setMinWidth(600);
                                            stage.setScene(scene);
                                            stage.show();
                                        } catch (Exception e) {
                                            Utils.handle_error(e.toString());
                                        }
                                    }
                            });
                        }
                }).start();
        });

        //tag adding button
        btn_add_tag.setOnMouseClicked(event -> {
                String text = search_field.getText();
                if (!search_field.getText().isEmpty() && !filter_tags.contains(text)) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("FXML/tag_cell.fxml"));
                        Parent root = loader.load();
                        TagCell cell = loader.getController();
                        tag_list.getItems().add(root);
                        cell.populate(text, tag_list, root, () -> { filter_tags.remove(text); reload_image_pane(); }, true);
                        filter_tags.add(text);
                        search_field.setText("");
                        reload_image_pane();
                    } catch (Exception e) { Utils.handle_error(e.toString()); }
                } else if (filter_tags.contains(search_field.getText())) {
                    search_field.setText("");
                }
        });

        set_standard_loading();

        NetworkRequest.no_internet = () -> {
            if (!showing_no_internet_dialog) {
                showing_no_internet_dialog = true;
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Could not connect to the server. Check your internet connection and the server.");
                offline.setValue(true);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK)
                    showing_no_internet_dialog = false;
            }
        };

        update_status_line();
        offline.addListener(l->Platform.runLater(this::update_status_line));
        logged_in.addListener(l->Platform.runLater(this::update_status_line));
        logged_in.setValue(DatabaseAbstractionLayer.client_get_uid() != null);

        if (DatabaseAbstractionLayer.get_setup_status() == 0) {
            show_intro_pane();
            hide_all_step_modals();
            first_intro_pane();
            show_intro_pane();
        } else {
            NetworkRequest.server_url = DatabaseAbstractionLayer.client_get_server_setting();
            hide_all_step_modals();
            hide_intro_pane();
            reload_image_pane();
            refresh_auto_suggestions();
            refresh_saved_images();
        }
    }

    void update_status_line() {
        if (DatabaseAbstractionLayer.get_mode() == 1) {
            status_line.setText("Local mode");
            btn_status_action.setText("Refresh");

            if (!loading.isVisible())
                btn_upload.setDisable(false);

            btn_status_action.setOnMouseClicked(event -> {
                reload_image_pane();
                refresh_auto_suggestions();
            });
            if (status != 0)
                status = 0;
        } else if (offline.getValue()) {
            status_line.setText("No server connection - Offline");
            btn_status_action.setText("Try connecting");
            btn_upload.setDisable(true);

            btn_status_action.setOnMouseClicked(event -> {
                inhibit_mp_reload = false;
                offline.setValue(false);
                reload_image_pane();
                refresh_auto_suggestions();
                status = 3;
            });

            if (status != 1) {
                status = 1;
                inhibit_mp_reload = false;
                reload_image_pane();
                refresh_auto_suggestions();
            }
        } else if (!logged_in.getValue()) {
            status_line.setText("Logged out");
            btn_status_action.setText("Log in");
            btn_upload.setDisable(true);

            btn_status_action.setOnMouseClicked(event -> {
                inhibit_mp_reload = false;
                DatabaseAbstractionLayer.client_logout();
                show_intro_pane();
                login_modal();
            });

            if (status != 2) {
                status = 2;
                inhibit_mp_reload = false;
                reload_image_pane();
                refresh_auto_suggestions();
            }
        } else {
            status_line.setText("Connected to server");
            btn_status_action.setText("Refresh and resync");

            if (!loading.isVisible())
                btn_upload.setDisable(false);

            btn_status_action.setOnMouseClicked(event -> {
                reload_image_pane();
                refresh_auto_suggestions();
                refresh_saved_images();
            });

            if (status != 3)
                status = 3;
        }
    }

    void refresh_saved_images() {
        if (DatabaseAbstractionLayer.get_mode() == 0) {
            List<DatabaseAbstractionLayer.ImageIDAspect> items = DatabaseAbstractionLayer.get_images_with_tags(new ArrayList<>());

            for (DatabaseAbstractionLayer.ImageIDAspect item : items) {
                final NetworkResponse out = new NetworkResponse();
                NetworkRequest.make_POST(out, "/get_img_tags", new Pair[]{Pair.create("id", String.valueOf(item.id))}, () -> {
                    List<String> tags = new ArrayList<>();
                    JSONArray arr = out.obj.getJSONArray("data");

                    for (int i = 0; i < arr.length(); i++)
                        tags.add(arr.getString(i));

                    DatabaseAbstractionLayer.update_img_tags(item.id, tags);
                }, () -> {
                    DatabaseAbstractionLayer.remove_image(item.id);
                    Utils.delete_image(item.id, ".png");
                }, true);
            }
        }
    }

    void set_standard_loading() {
        NetworkRequest.loading = this::show_loading;
        NetworkRequest.done_loading = this::hide_loading;
    }

    void show_loading() {
        hide_all_step_modals();
        show_intro_pane();
        is_loading.setValue(true);
        loading_modal_container.setVisible(true);
        loading_modal_container.setMouseTransparent(false);
    }

    void hide_loading() {
        is_loading.setValue(false);
        intro_cover_pane.setMouseTransparent(true);
        intro_cover_pane.setVisible(false);
        loading_modal_container.setVisible(false);
        loading_modal_container.setMouseTransparent(true);
    }

    void first_intro_pane() {
        step_1_modal.setMouseTransparent(false);
        step_1_modal.setVisible(true);
        step_1_modal_container.setVisible(true);
        show_intro_pane();

        btn_local.setOnMouseClicked(event -> {
            DatabaseAbstractionLayer.setup_done();
            hide_all_step_modals();
            hide_intro_pane();
            reload_image_pane();
            refresh_auto_suggestions();
        });

        btn_server.setOnMouseClicked(event -> {
            hide_all_step_modals();
            second_intro_pane();
        });
    }

    void second_intro_pane() {
        step_2_modal.setMouseTransparent(false);
        step_2_modal.setVisible(true);
        step_2_modal_container.setVisible(true);
        show_intro_pane();

        btn_step2_connect.setOnMouseClicked(event -> {
            NetworkRequest.server_url = step2_server_field.getText();
            if (NetworkRequest.server_url.length() > 0 && NetworkRequest.server_url.charAt(NetworkRequest.server_url.length() - 1) == '/')
                NetworkRequest.server_url = NetworkRequest.server_url.substring(0, NetworkRequest.server_url.length()-1);
            DatabaseAbstractionLayer.client_save_server_setting(NetworkRequest.server_url);


            final NetworkResponse out = new NetworkResponse();
            NetworkRequest.make_POST(out, "/info", null, () -> {
                step_2_modal.setMouseTransparent(false);
                step_2_modal.setVisible(true);
                step_2_modal_container.setVisible(true);
                show_intro_pane();

                if (out.obj.getJSONObject("data").getInt("server_feature_level") != Main.feature_level) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Server/client version mismatch");
                    alert.setContentText("Please make sure that both server and client are updated to the latest version");
                    alert.showAndWait();
                } else if (out.obj.getJSONObject("data").getInt("setup_complete") == 0){
                    hide_all_step_modals();
                    sql_setup_modal();
                } else {
                    hide_all_step_modals();
                    login_modal();
                }
            }, () -> {
                step_2_modal.setMouseTransparent(false);
                step_2_modal.setVisible(true);
                step_2_modal_container.setVisible(true);
                show_intro_pane();

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Could not connect to that address");
                alert.showAndWait();
            }, false);
        });

    }

    void sql_setup_modal() {
        sql_setup_modal.setMouseTransparent(false);
        sql_setup_modal.setVisible(true);
        sql_setup_modal_container.setVisible(true);
        show_intro_pane();

        btn_sql_continue.setOnMouseClicked(event -> {
            if (root_password_1.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Root password cannot be blank");
                alert.showAndWait();
            } else if (!root_password_1.getText().equals(root_password_2.getText())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Root passwords do not match. Please re-enter them");
                alert.showAndWait();
            } else {
                final NetworkResponse out = new NetworkResponse();
                NetworkRequest.make_POST(out, "/do_setup", new Pair[]{ Pair.create("sql_address",sql_address.getText()),
                        Pair.create("sql_dbname", sql_dbname.getText()), Pair.create("sql_user", sql_user.getText()),
                        Pair.create("sql_password", sql_password.getText()), Pair.create("root_password", root_password_1.getText())}, ()->{

                    DatabaseAbstractionLayer.client_login(1, "root", out.obj.getJSONObject("data").getString("session"), 1);
                    DatabaseAbstractionLayer.set_server_mode();
                    DatabaseAbstractionLayer.setup_done();
                    done_modal();
                }, ()->{
                    sql_setup_modal.setMouseTransparent(false);
                    sql_setup_modal.setVisible(true);
                    sql_setup_modal_container.setVisible(true);
                    show_intro_pane();

                    if (out.error == 1) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Could not connect to mySQL server");
                        alert.setContentText(out.obj.getString("error_text"));
                        alert.showAndWait();
                    } else {
                        NetworkRequest.no_internet.run();
                    }
                }, false);
            }
        });
    }

    void done_modal() {
        done_modal.setMouseTransparent(false);
        done_modal.setVisible(true);
        done_modal_container.setVisible(true);
        show_intro_pane();

        btn_setup_done.setOnMouseClicked(event -> {
            hide_all_step_modals();
            hide_intro_pane();
            reload_image_pane();
            refresh_auto_suggestions();
            update_status_line();
        });

    }

    void login_modal() {
        login_modal.setMouseTransparent(false);
        login_modal.setVisible(true);
        login_modal_container.setVisible(true);
        show_intro_pane();

        btn_login.setOnMouseClicked(event -> {
            final NetworkResponse out = new NetworkResponse();
            NetworkRequest.make_POST(out, "/login", new Pair[]{ Pair.create("user", login_user.getText()), Pair.create("password", login_password.getText()) }, ()->{
                DatabaseAbstractionLayer.client_login(out.obj.getJSONObject("data").getInt("uid"), login_user.getText(),
                        out.obj.getJSONObject("data").getString("session"),
                        out.obj.getJSONObject("data").getInt("admin"));
                DatabaseAbstractionLayer.set_server_mode();
                DatabaseAbstractionLayer.setup_done();
                hide_all_step_modals();
                hide_intro_pane();
                reload_image_pane();
                refresh_auto_suggestions();
                update_status_line();

            }, ()->{
                login_modal.setMouseTransparent(false);
                login_modal.setVisible(true);
                login_modal_container.setVisible(true);
                show_intro_pane();

                if (out.error == 1) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Incorrect username or password");
                    alert.showAndWait();
                } if (out.error == 2) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Server/client version mismatch");
                    alert.setContentText("Please make sure that both server and client are updated to the latest version");
                    alert.showAndWait();
                }else {
                    NetworkRequest.no_internet.run();
                }
            }, false);
        });
    }

    void hide_all_step_modals() {
        step_1_modal.setMouseTransparent(true);
        step_1_modal.setVisible(false);
        step_1_modal_container.setVisible(false);

        step_2_modal.setMouseTransparent(true);
        step_2_modal.setVisible(false);
        step_2_modal_container.setVisible(false);

        sql_setup_modal.setMouseTransparent(true);
        sql_setup_modal.setVisible(false);
        sql_setup_modal_container.setVisible(false);

        done_modal.setMouseTransparent(true);
        done_modal.setVisible(false);
        done_modal_container.setVisible(false);

        login_modal.setMouseTransparent(true);
        login_modal.setVisible(false);
        login_modal_container.setVisible(false);

        hide_loading();
    }

    void hide_intro_pane() {
        intro_cover_pane.setMouseTransparent(true);
        intro_cover_pane.setVisible(false);
        hide_all_step_modals();
        reload_image_pane();
    }

    void show_intro_pane() {
        intro_cover_pane.setMouseTransparent(false);
        intro_cover_pane.setVisible(true);
    }

    void refresh_auto_suggestions() {
        if (DatabaseAbstractionLayer.get_mode() == 1 || !logged_in.getValue() || offline.getValue()) {
            List<String> tags = DatabaseAbstractionLayer.get_all_tags();
            autocomplete.getSuggestions().clear();
            autocomplete.getSuggestions().addAll(tags);
        } else {
            final NetworkResponse out = new NetworkResponse();
            NetworkRequest.make_POST(out, "/get_all_tags", new Pair[]{}, ()->{
                List<String> tags = new ArrayList<>();
                JSONArray arr = out.obj.getJSONArray("data");
                for (int i = 0; i < arr.length(); i++)
                    tags.add(arr.getString(i));

                autocomplete.getSuggestions().clear();
                autocomplete.getSuggestions().addAll(tags);
            }, ()->{}, true);
        }
    }

    void reload_image_pane() {
        if (!inhibit_mp_reload) {
            mp.getChildren().clear();
            inhibit_mp_reload = true;

            sp.onScrollProperty().unbind();
            sp.setOnScroll(scrollEvent -> {
                if (sp.getVvalue() == 1.0 && !sp_bottom_flag) {
                    Platform.runLater(this::show_next_100);
                    sp_bottom_flag = true;
                } else if (sp.getVvalue() < 1.0)
                    sp_bottom_flag = false;
            });

            for (Image i : images)
                i.cancel();

            images.clear();

            if (DatabaseAbstractionLayer.get_mode() == 1 || !logged_in.getValue() || offline.getValue()) {
                List<DatabaseAbstractionLayer.ImageIDAspect> items = DatabaseAbstractionLayer.get_images_with_tags(filter_tags);
                mp.setLimitRow(items.size() + 10);
                set_result_count(items.size());
                mp_index = 0;
                mp_ids.clear();
                mp_aspects.clear();

                for (final DatabaseAbstractionLayer.ImageIDAspect item : items) {
                    mp_ids.add(item.id);
                    mp_aspects.add(item.aspect);
                }

                show_next_100();
                pane_empty_msg();
            } else {
                final JSONArray arr = new JSONArray();
                for (String tag : filter_tags)
                    arr.put(tag);

                final NetworkResponse out = new NetworkResponse();
                NetworkRequest.make_POST(out, "/get_images_with_tags", new Pair[]{Pair.create("tags", arr.toString())}, () -> {
                    JSONArray ids = out.obj.getJSONObject("data").getJSONArray("ids");
                    JSONArray aspects = out.obj.getJSONObject("data").getJSONArray("aspects");
                    mp.setLimitRow(ids.length() + 10);
                    set_result_count(ids.length());
                    mp_index = 0;
                    mp_ids.clear();
                    mp_aspects.clear();

                    for (int i = 0; i < ids.length(); i++) {
                        mp_ids.add(ids.getInt(i));
                        mp_aspects.add(aspects.getBigDecimal(i).floatValue());
                    }

                    show_next_100();
                    pane_empty_msg();
                }, () -> { }, true);
            }
        }
    }

    void show_next_100(){
        final int index = mp_index;
        for (int i = index; i < Math.min(mp_ids.size(), index + 100); i++) {
            put_image_into_pane(mp_ids.get(i), mp_aspects.get(i));
            mp_index++;
        }
    }

    void set_result_count(int num) {
        result_num_line.setText(num + " results");
    }

    void pane_empty_msg() {
        inhibit_mp_reload = false;
        if (mp.getChildren().size() == 0) {
            empty_msg.setVisible(true);
            if (filter_tags.size() == 0)
                empty_msg.setText("There are no images in your library. Add some!");
            else
                empty_msg.setText("No images were found with the specific tag(s)");
        } else {
            empty_msg.setVisible(false);
        }
    }

    void put_image_into_pane(Integer id, float aspect) {
        String file_path;
        String file_extension = null;

        if (DatabaseAbstractionLayer.get_mode() == 1 || !logged_in.getValue() || offline.getValue()) {
            //find image extension
            if (Files.exists(Paths.get(Utils.get_local_storage_dir() + "images/" + id + ".jpg")))
                file_extension = ".jpg";
            else if (Files.exists(Paths.get(Utils.get_local_storage_dir() + "images/" + id + ".jpeg")))
                file_extension = ".jpeg";
            else if (Files.exists(Paths.get(Utils.get_local_storage_dir() + "images/" + id + ".png")))
                file_extension = ".png";
        }

        if (DatabaseAbstractionLayer.get_mode() == 0)
            file_extension = ".png";

        //inner class shit, make final version of file extension
        final String file_extension_final = file_extension;

        if (file_extension != null || DatabaseAbstractionLayer.get_mode() == 0) {
            final Image[] img = {null};
            final File[] img_file = {null};
            ImageView image = new ImageView();
            image.setFitWidth(185);
            image.setFitHeight(185*aspect);
            image.setPreserveRatio(true);

            if (DatabaseAbstractionLayer.get_mode() == 1 || !logged_in.getValue() || offline.getValue()) {
                file_path = Utils.get_local_storage_dir() + "images/" + id + file_extension;
                img_file[0] = new File(file_path);
            }

            if (DatabaseAbstractionLayer.get_mode() == 1 || !logged_in.getValue() || offline.getValue())
                img[0] = new Image(img_file[0].toURI().toString(), 185, 185*aspect, true, false, true);
            else
                img[0] = new Image(NetworkRequest.server_url+"/image/"+id, 185, 185*aspect, true, false, true);

            images.add(img[0]);
            image.setImage(img[0]);


            final Label l = new Label(null, image);
            l.setOnMouseClicked(event -> {
                new Thread(() -> {
                Platform.runLater(this::show_loading);
                try {
                    if (DatabaseAbstractionLayer.get_mode() == 0) {
                        if (DatabaseAbstractionLayer.get_img_tags(id) == null) {
                            URL url = new URL(NetworkRequest.server_url + "/image/" + id);
                            BufferedImage buf_img = ImageIO.read(url);
                            img_file[0] = new File(Utils.get_local_storage_dir() + "temp.png");
                            ImageIO.write(buf_img, "png", img_file[0]);
                        } else {
                            img_file[0] = new File(Utils.get_local_storage_dir() + "images/" + id + ".png");
                        }
                    }

                    Platform.runLater(() ->{
                       try {
                           hide_loading();
                           if (event.getButton() == MouseButton.SECONDARY) {
                               //show context menu with options
                               ContextMenu context_menu = new ContextMenu();
                               MenuItem clipboard = new MenuItem("Copy to clipboard");
                               MenuItem save = new MenuItem("Save as");
                               MenuItem edit_tags = new MenuItem("Edit tags");
                               MenuItem delete = new MenuItem("Delete");
                               CheckMenuItem offline = new CheckMenuItem("Available offline");

                               clipboard.setOnAction(event1 -> {
                                   final Clipboard clipboard1 = Clipboard.getSystemClipboard();
                                   final ClipboardContent content = new ClipboardContent();

                                   content.putImage(new Image(img_file[0].toURI().toString()));
                                   clipboard1.setContent(content);
                               });

                               save.setOnAction(event12 -> {
                                   FileChooser file_chooser = new FileChooser();
                                   FileChooser.ExtensionFilter ext_filter = new FileChooser.ExtensionFilter("Image file", "*" + file_extension_final);
                                   file_chooser.getExtensionFilters().add(ext_filter);

                                   //Show save file dialog
                                   File file1 = file_chooser.showSaveDialog(stage);

                                   if (file1 != null) {
                                       File out = new File(file1.getAbsolutePath() + file_extension_final);
                                       Utils.copy_image(img_file[0], out);
                                   }
                               });

                               edit_tags.setOnAction(event2 -> {
                                   try {
                                       FXMLLoader loader = new FXMLLoader(getClass().getResource("FXML/edit.fxml"));
                                       Pane pane = loader.load();
                                       Stage stage = new Stage();
                                       Scene scene = new Scene(pane);

                                       Edit edit = loader.getController();
                                       edit.populate(id, (DatabaseAbstractionLayer.get_mode() == 1 || Controller.offline.getValue() || !Controller.logged_in.getValue())?
                                                       img_file[0].getAbsolutePath() : null,
                                               scene, stage, true, true, () -> {
                                                   refresh_auto_suggestions();
                                                   if (filter_tags.size() > 0) {reload_image_pane();}
                                               });

                                       stage.initModality(Modality.APPLICATION_MODAL);
                                       stage.setScene(scene);
                                       stage.setMaximized(true);
                                       stage.setTitle("Tag editor");
                                       stage.setMinHeight(600);
                                       stage.setMinWidth(600);
                                       stage.show();
                                   } catch (Exception e) {
                                       Utils.handle_error(e.toString());
                                   }
                               });

                               delete.setOnAction(event13 -> {
                                   Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                   alert.setTitle("Delete image");
                                   alert.setHeaderText("Are you sure you want to delete this image?");

                                   alert.showAndWait().ifPresent((btnType) -> {
                                       if (btnType.equals(ButtonType.OK)) {
                                           if (DatabaseAbstractionLayer.get_mode() == 1) {
                                               DatabaseAbstractionLayer.remove_image(id);
                                               Utils.delete_image(id, file_extension_final);
                                               mp.getChildren().remove(l);
                                           } else {
                                               final NetworkResponse out = new NetworkResponse();
                                               NetworkRequest.make_POST(out, "/remove_image", new Pair[] {Pair.create("id", String.valueOf(id))},
                                                       ()->mp.getChildren().remove(l), ()->{}, true);
                                           }
                                       }
                                   });
                               });

                               offline.setSelected(DatabaseAbstractionLayer.get_img_tags(id) != null);
                               offline.setOnAction(event14 -> {
                                   if (offline.isSelected()) {
                                       final NetworkResponse out = new NetworkResponse();
                                       NetworkRequest.make_POST(out, "/get_img_tags", new Pair[] { Pair.create("id", String.valueOf(id)) }, () -> {
                                           List<String> tags = new ArrayList<>();
                                           JSONArray arr = out.obj.getJSONArray("data");

                                           for (int i = 0; i < arr.length(); i++)
                                               tags.add(arr.getString(i));

                                           Utils.copy_image(img_file[0], id);
                                           Image img1 = new Image(img_file[0].toURI().toString());
                                           DatabaseAbstractionLayer.upload_image(id, tags, (float) img1.getHeight()/(float) img1.getWidth());
                                           DatabaseAbstractionLayer.update_global_tag_list(tags);
                                       }, ()->{}, true);
                                   } else {
                                       DatabaseAbstractionLayer.remove_image(id);
                                       Utils.delete_image(id, ".png");
                                   }
                               });

                               if (DatabaseAbstractionLayer.get_mode() == 0 && !(!logged_in.getValue() || Controller.offline.getValue()))
                                   context_menu.getItems().add(offline);

                               context_menu.getItems().add(clipboard);
                               context_menu.getItems().add(save);
                               context_menu.getItems().add(edit_tags);

                               if (!(!logged_in.getValue() || Controller.offline.getValue()) || DatabaseAbstractionLayer.get_mode() == 1)
                                   context_menu.getItems().add(delete);

                               Point mouse = java.awt.MouseInfo.getPointerInfo().getLocation();
                               context_menu.show(stage, mouse.x, mouse.y);
                           } else {
                               //open image in default image viewer
                               new Thread(() -> {
                                   try {
                                       Desktop.getDesktop().open(img_file[0]);
                                   } catch (Exception e) {
                                       Utils.handle_error(e.toString());
                                   }
                               }).start();
                           }
                       } catch (Exception e) {
                           Utils.handle_error(e.toString());
                       }
                    });
                } catch (Exception e) {
                    Utils.handle_error(e.toString());
                }

            }).start();});


            mp.getChildren().add(l);
        }
    }

}
