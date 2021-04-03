package net.k1ra.FEMBOY_desktop;

import com.jfoenix.controls.*;
import com.jfoenix.controls.cells.editors.TextFieldEditorBuilder;
import com.jfoenix.controls.cells.editors.base.GenericEditableTreeTableCell;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import net.k1ra.FEMBOY_desktop.libFEMBOY.DatabaseAbstractionLayer;
import org.json.JSONArray;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsUsers implements Initializable {
    @FXML AnchorPane ap;
    @FXML JFXTreeTableView<User> table;
    @FXML Text status;
    @FXML Button btn_new;
    @FXML AnchorPane cover_pane;
    @FXML AnchorPane password_pane;
    @FXML AnchorPane password_modal;
    @FXML JFXPasswordField pw_1;
    @FXML JFXPasswordField pw_2;
    @FXML Button btn_pw_cancel;
    @FXML Button btn_pw_confirm;
    @FXML AnchorPane new_user_pane;
    @FXML AnchorPane new_user_modal;
    @FXML JFXTextField new_name;
    @FXML JFXPasswordField new_pw_1;
    @FXML JFXPasswordField new_pw_2;
    @FXML Button btn_user_cancel;
    @FXML Button btn_user_confirm;
    @FXML JFXCheckBox new_is_admin;

    Scene scene;
    ObservableList<User> list = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ap.setBackground(new Background(new BackgroundFill(Color.web("#202225"), CornerRadii.EMPTY, Insets.EMPTY)));
        table.getStylesheets().add(this.getClass().getResource("CSS/tree_table.css").toExternalForm());
        btn_new.setStyle("-fx-background-color: #40444b");
        cover_pane.setBackground(new Background(new BackgroundFill(Color.web("#000000"), CornerRadii.EMPTY, Insets.EMPTY)));
        password_pane.setBackground(Background.EMPTY);
        password_modal.setBackground(new Background(new BackgroundFill(Color.web("#2f3136"), new CornerRadii(25), Insets.EMPTY)));
        pw_1.getStylesheets().add(this.getClass().getResource("CSS/text_field.css").toExternalForm());
        pw_2.getStylesheets().add(this.getClass().getResource("CSS/text_field.css").toExternalForm());
        btn_pw_cancel.setStyle("-fx-background-color: #40444b");
        btn_pw_confirm.setStyle("-fx-background-color: #40444b");
        new_user_pane.setBackground(Background.EMPTY);
        new_user_modal.setBackground(new Background(new BackgroundFill(Color.web("#2f3136"), new CornerRadii(25), Insets.EMPTY)));
        new_name.getStylesheets().add(this.getClass().getResource("CSS/text_field.css").toExternalForm());
        new_pw_1.getStylesheets().add(this.getClass().getResource("CSS/text_field.css").toExternalForm());
        new_pw_2.getStylesheets().add(this.getClass().getResource("CSS/text_field.css").toExternalForm());
        btn_user_cancel.setStyle("-fx-background-color: #40444b");
        btn_user_confirm.setStyle("-fx-background-color: #40444b");

        hide_all_modals();
        hide_modal_bg();

        JFXTreeTableColumn<User, Integer> col_uid = new JFXTreeTableColumn<>("ID");
        col_uid.setMaxWidth(50);
        col_uid.setMinWidth(50);
        col_uid.setCellValueFactory((TreeTableColumn.CellDataFeatures<User, Integer> param) ->{
            if(col_uid.validateValue(param)) return param.getValue().getValue().uid.asObject();
            else return col_uid.getComputedValue(param);
        });

        JFXTreeTableColumn<User, String> col_name = new JFXTreeTableColumn<>("Name");
        col_name.setPrefWidth(300);
        col_name.setCellValueFactory((TreeTableColumn.CellDataFeatures<User, String> param) ->{
            if(col_name.validateValue(param)) return param.getValue().getValue().name;
            else return col_name.getComputedValue(param);
        });

        JFXTreeTableColumn<User, Boolean> col_admin = new JFXTreeTableColumn<>("Is admin");
        col_admin.setMinWidth(75);
        col_admin.setMaxWidth(75);
        col_admin.setCellValueFactory((TreeTableColumn.CellDataFeatures<User, Boolean> param) ->{
            if(col_admin.validateValue(param)) return param.getValue().getValue().admin;
            else return col_admin.getComputedValue(param);
        });

        JFXTreeTableColumn<User, Integer> col_pwd = new JFXTreeTableColumn<>();
        col_pwd.setMinWidth(150);
        col_pwd.setMaxWidth(150);
        col_pwd.setCellValueFactory((TreeTableColumn.CellDataFeatures<User, Integer> param) ->{
            if(col_pwd.validateValue(param)) return param.getValue().getValue().uid.asObject();
            else return col_pwd.getComputedValue(param);
        });

        JFXTreeTableColumn<User, Integer> col_delet = new JFXTreeTableColumn<>();
        col_delet.setMinWidth(120);
        col_delet.setMaxWidth(120);
        col_delet.setCellValueFactory((TreeTableColumn.CellDataFeatures<User, Integer> param) ->{
            if(col_delet.validateValue(param)) return param.getValue().getValue().uid.asObject();
            else return col_delet.getComputedValue(param);
        });

        col_name.setCellFactory((TreeTableColumn<User, String> param) ->
                new GenericEditableTreeTableCell<>(new TextFieldEditorBuilder()));
        col_name.setOnEditCommit((TreeTableColumn.CellEditEvent<User, String> t)->{
            if (t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow()).getValue().uid.getValue() != 1) {
                final NetworkResponse out = new NetworkResponse();
                NetworkRequest.make_POST(out, "/set_user_name", new Pair[]{
                        Pair.create("target_uid", String.valueOf(t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow()).getValue().uid.getValue())),
                        Pair.create("name", t.getNewValue())}, ()->{
                    t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow()).getValue().name.setValue(t.getNewValue());
                }, ()->{
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Another account already uses this name");
                    alert.showAndWait();
                    String tmp = t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow()).getValue().name.get();
                    t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow()).getValue().name.setValue(t.getNewValue());
                    t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow()).getValue().name.setValue(tmp);
                }, true);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Cannot rename root user");
                alert.showAndWait();
                t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow()).getValue().name.setValue(t.getNewValue());
                t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow()).getValue().name.setValue("root");
            }
        });

        col_admin.setCellFactory(param -> new CheckBoxCell());
        col_admin.setOnEditCommit((TreeTableColumn.CellEditEvent<User, Boolean> t) -> {
            if (t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow()).getValue().uid.getValue() == 1) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Root must be admin");
                alert.showAndWait();
                t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow()).getValue().admin.setValue(false);
                t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow()).getValue().admin.setValue(true);
            } else if (t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow()).getValue().uid.getValue() == Integer.parseInt(DatabaseAbstractionLayer.client_get_uid())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Cannot demote the account you are currently logged into");
                alert.showAndWait();
                t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow()).getValue().admin.setValue(false);
                t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow()).getValue().admin.setValue(true);
            } else {
                t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow()).getValue().admin.setValue(t.getNewValue());
                final NetworkResponse out = new NetworkResponse();
                NetworkRequest.make_POST(out, "/set_user_admin", new Pair[]{
                        Pair.create("target_uid", String.valueOf(t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow()).getValue().uid.getValue())),
                        Pair.create("admin", t.getNewValue()? "1":"0")
                }, ()->{}, ()->{}, true);
            }
        });

        col_pwd.setCellFactory(param -> new PasswordButtonCell());

        col_delet.setCellFactory(param -> new DeleteButtonCell());
        col_delet.setOnEditCommit((TreeTableColumn.CellEditEvent<User, Integer> t) -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete user "+t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow()).getValue().name);
            alert.setHeaderText("Are you sure you want to delete the user "+t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow()).getValue().name.getValue()+"?");
            alert.showAndWait().ifPresent((btnType) -> {
                if (btnType.equals(ButtonType.OK)) {
                    final NetworkResponse out = new NetworkResponse();
                    NetworkRequest.make_POST(out, "/delete_user", new Pair[]{
                            Pair.create("target_uid", String.valueOf(t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow()).getValue().uid.getValue()))
                    }, ()-> list.remove(t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow()).getValue()), ()->{}, true);
                }
            });
        });

        list.addListener((ListChangeListener<User>) c -> status.setText(list.size() +" users on this server (excluding LDAP)"));
        fetch_users();

        final TreeItem<User> root = new RecursiveTreeItem<>(list, RecursiveTreeObject::getChildren);
        table.setRoot(root);
        table.setShowRoot(false);
        table.setEditable(true);
        table.getColumns().setAll(col_uid, col_name, col_admin, col_pwd, col_delet);
        table.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);

        Settings.scene.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> {
            col_name.setMinWidth(newSceneWidth.doubleValue()-col_uid.getWidth()-col_admin.getWidth()-col_pwd.getWidth()-col_delet.getWidth()-2);
            col_name.setMaxWidth(newSceneWidth.doubleValue()-col_uid.getWidth()-col_admin.getWidth()-col_pwd.getWidth()-col_delet.getWidth()-2);
        });

        btn_new.setOnMouseClicked(event -> new_user_modal());
    }

    void fetch_users() {
        if (!Controller.offline.getValue() && Controller.logged_in.getValue()) {
            final NetworkResponse out = new NetworkResponse();
            NetworkRequest.make_POST(out, "/get_users", new Pair[]{}, () -> {
                JSONArray arr = out.obj.getJSONArray("data");
                list.clear();

                for (int i = 0; i < arr.length(); i++)
                    list.add(new User(arr.getJSONObject(i).getInt("uid"), arr.getJSONObject(i).getString("name"), arr.getJSONObject(i).getInt("admin")));

            }, () -> {}, true);
        }
    }

    void show_modal_bg() {
        cover_pane.setVisible(true);
        cover_pane.setMouseTransparent(false);
    }

    void hide_modal_bg() {
        cover_pane.setVisible(false);
        cover_pane.setMouseTransparent(true);
    }

    void hide_all_modals() {
        password_pane.setVisible(false);
        password_pane.setMouseTransparent(true);

        new_user_pane.setVisible(false);
        new_user_pane.setMouseTransparent(true);
    }

    void new_user_modal() {
        show_modal_bg();
        new_user_pane.setVisible(true);
        new_user_pane.setMouseTransparent(false);

        new_name.setText("");
        new_pw_1.setText("");
        new_pw_2.setText("");

        btn_user_cancel.setOnMouseClicked(event -> {
            hide_all_modals();
            hide_modal_bg();
        });

        btn_user_confirm.setOnMouseClicked(event -> {
            if (new_pw_1.getText().isEmpty() && new_name.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Password and name cannot be blank");
                alert.showAndWait();
            } else if (new_pw_1.getText().equals(new_pw_2.getText())) {
                final NetworkResponse out = new NetworkResponse();
                NetworkRequest.make_POST(out, "/create_user", new Pair[]{Pair.create("name", new_name.getText()),
                        Pair.create("password", new_pw_1.getText()),
                        Pair.create("admin", new_is_admin.isSelected()? "1":"0")}, ()->{
                    hide_modal_bg();
                    hide_all_modals();
                    fetch_users();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText("User \""+new_name.getText()+ "\" created successfully!");
                    alert.showAndWait();
                }, ()->{
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("A user with this name already exists");
                    alert.showAndWait();
                }, true);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Passwords do not match. Please re-enter them");
                alert.showAndWait();
            }
        });
    }

    void password_change_modal(int uid) {
        show_modal_bg();
        password_pane.setVisible(true);
        password_pane.setMouseTransparent(false);

        pw_1.setText("");
        pw_2.setText("");

        btn_pw_cancel.setOnMouseClicked(event -> {
            hide_all_modals();
            hide_modal_bg();
        });

        btn_pw_confirm.setOnMouseClicked(event -> {
            if (pw_1.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Password cannot be blank");
                alert.showAndWait();
            } else if (pw_1.getText().equals(pw_2.getText())) {
                final NetworkResponse out = new NetworkResponse();
                NetworkRequest.make_POST(out, "/set_user_password", new Pair[]{Pair.create("target_uid", String.valueOf(uid)),
                        Pair.create("password", pw_1.getText())}, ()->{
                    hide_modal_bg();
                    hide_all_modals();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText("Password changed successfully!");
                    alert.showAndWait();
                }, ()->{}, true);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Passwords do not match. Please re-enter them");
                alert.showAndWait();
            }
        });
    }


    class DeleteButtonCell extends TreeTableCell<User, Integer> {
        Button btn = new Button("Delete user");
        int uid;

        public DeleteButtonCell(){
            HBox container = new HBox();
            container.setAlignment(Pos.CENTER);
            container.getChildren().add(btn);

            btn.setOnMouseClicked(event -> commitEdit(uid));
            btn.setStyle("-fx-background-color: #ff0000");
            btn.setTextFill(Color.WHITE);

            this.graphicProperty().bind(Bindings.when(this.emptyProperty()).then((Node) null).otherwise(container));
        }

        @Override
        public void commitEdit(Integer item) {
            if (!isEditing()) {
                TreeTableView<User> table = getTreeTableView();
                if (table != null) {
                    TreeTableColumn<User, Integer> column = getTableColumn();
                    TreeTableColumn.CellEditEvent<User, Integer> event = new TreeTableColumn.CellEditEvent<>(
                            table, new TreeTablePosition<>(table, getIndex(), column),
                            TreeTableColumn.editCommitEvent(), item
                    );
                    Event.fireEvent(column, event);
                }
            }

            super.commitEdit(item);
        }

        @Override
        protected void updateItem(Integer item, boolean empty) {
            super.updateItem(item, empty);

            if (item != null) {
                uid = item;

                if (uid == 1 || uid == Integer.parseInt(DatabaseAbstractionLayer.client_get_uid())) {
                    btn.setVisible(false);
                    btn.setDisable(true);
                }
            }
        }
    }

    class PasswordButtonCell extends TreeTableCell<User, Integer> {
        Button btn = new Button("Change password");
        int uid;

        public PasswordButtonCell(){
            HBox container = new HBox();
            container.setAlignment(Pos.CENTER);
            container.getChildren().add(btn);

            btn.setOnMouseClicked(event -> password_change_modal(uid));
            btn.setStyle("-fx-background-color: #202225");
            btn.setTextFill(Color.WHITE);

            this.graphicProperty().bind(Bindings.when(this.emptyProperty()).then((Node) null).otherwise(container));
        }

        @Override
        protected void updateItem(Integer item, boolean empty) {
            super.updateItem(item, empty);

            if (item != null)
                uid = item;
        }
    }

    class CheckBoxCell extends TreeTableCell<User, Boolean> {
        JFXCheckBox box = new JFXCheckBox();

        public CheckBoxCell(){
            HBox container = new HBox();
            container.setAlignment(Pos.CENTER);
            container.getChildren().add(box);

            box.setOnMouseClicked(event -> commitEdit(box.isSelected()));

            this.graphicProperty().bind(Bindings.when(this.emptyProperty()).then((Node) null).otherwise(container));
        }

        @Override
        public void commitEdit(Boolean item) {
            if (!isEditing() && !item.equals(getItem())) {
                TreeTableView<User> table = getTreeTableView();
                if (table != null) {
                    TreeTableColumn<User, Boolean> column = getTableColumn();
                    TreeTableColumn.CellEditEvent<User, Boolean> event = new TreeTableColumn.CellEditEvent<>(
                            table, new TreeTablePosition<>(table, getIndex(), column),
                            TreeTableColumn.editCommitEvent(), item
                    );
                    Event.fireEvent(column, event);
                }
            }

            super.commitEdit(item);
        }


        @Override
        protected void updateItem(Boolean item, boolean empty) {
            super.updateItem(item, empty);

            if (item != null)
                box.setSelected(item);
        }
    }

    public static class User extends RecursiveTreeObject<User> {
        public IntegerProperty uid;
        public StringProperty name;
        public BooleanProperty admin;

        public User(int uid, String name, int admin) {
            this.uid = new SimpleIntegerProperty(uid);
            this.name = new SimpleStringProperty(name);
            this.admin = new SimpleBooleanProperty(admin==1);
        }
    }

}