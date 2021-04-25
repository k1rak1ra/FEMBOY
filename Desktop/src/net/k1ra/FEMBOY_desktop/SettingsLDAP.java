package net.k1ra.FEMBOY_desktop;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import org.json.JSONObject;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsLDAP implements Initializable {
    @FXML AnchorPane ap;
    @FXML AnchorPane ap_general;
    @FXML JFXTextField ldap_server;
    @FXML JFXCheckBox ldap_tls;
    @FXML JFXTextField ldap_bind_user;
    @FXML JFXPasswordField ldap_bind_password;
    @FXML Button btn_ldap_connect;
    @FXML AnchorPane ap_user;
    @FXML AnchorPane ap_group;
    @FXML JFXTextField ldap_user_dn;
    @FXML JFXTextField ldap_user_filter;
    @FXML JFXTextField ldap_user_uid_attribute;
    @FXML JFXTextField ldap_group_dn;
    @FXML JFXTextField ldap_group_filter;
    @FXML JFXTextField ldap_group_name_attribute;
    @FXML JFXTextField ldap_admin_group_name;
    @FXML Button btn_user_save;
    @FXML Button btn_group_save;
    @FXML JFXTextField user_test;
    @FXML JFXTextField group_test;
    @FXML Button btn_user_test;
    @FXML Button btn_group_test;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ap.setBackground(new Background(new BackgroundFill(Color.web("#2f3136"), CornerRadii.EMPTY, Insets.EMPTY)));
        ap_general.setBackground(new Background(new BackgroundFill(Color.web("#202225"), new CornerRadii(20), Insets.EMPTY)));
        ldap_server.getStylesheets().add(this.getClass().getResource("CSS/text_field.css").toExternalForm());
        ldap_bind_user.getStylesheets().add(this.getClass().getResource("CSS/text_field.css").toExternalForm());
        ldap_bind_password.getStylesheets().add(this.getClass().getResource("CSS/text_field.css").toExternalForm());
        btn_ldap_connect.setStyle("-fx-background-color: #40444b");
        ap_user.setBackground(new Background(new BackgroundFill(Color.web("#202225"), new CornerRadii(20), Insets.EMPTY)));
        ap_group.setBackground(new Background(new BackgroundFill(Color.web("#202225"), new CornerRadii(20), Insets.EMPTY)));
        ldap_user_dn.getStylesheets().add(this.getClass().getResource("CSS/text_field.css").toExternalForm());
        ldap_user_filter.getStylesheets().add(this.getClass().getResource("CSS/text_field.css").toExternalForm());
        ldap_user_uid_attribute.getStylesheets().add(this.getClass().getResource("CSS/text_field.css").toExternalForm());
        ldap_group_dn.getStylesheets().add(this.getClass().getResource("CSS/text_field.css").toExternalForm());
        ldap_group_filter.getStylesheets().add(this.getClass().getResource("CSS/text_field.css").toExternalForm());
        ldap_group_name_attribute.getStylesheets().add(this.getClass().getResource("CSS/text_field.css").toExternalForm());
        ldap_admin_group_name.getStylesheets().add(this.getClass().getResource("CSS/text_field.css").toExternalForm());
        btn_user_save.setStyle("-fx-background-color: #40444b");
        btn_group_save.setStyle("-fx-background-color: #40444b");
        user_test.getStylesheets().add(this.getClass().getResource("CSS/text_field.css").toExternalForm());
        group_test.getStylesheets().add(this.getClass().getResource("CSS/text_field.css").toExternalForm());
        btn_group_test.setStyle("-fx-background-color: #40444b");
        btn_user_test.setStyle("-fx-background-color: #40444b");

        group_user_ap_control(false);
        get_ldap_status();

        btn_ldap_connect.setOnMouseClicked(event -> {
            final NetworkResponse out = new NetworkResponse();
            NetworkRequest.make_POST(out, "/set_ldap_server", new Pair[]{ Pair.create("ldap_server", ldap_server.getText()),
                Pair.create("ldap_tls", ldap_tls.isSelected()? "1":"0"),
                Pair.create("ldap_bind_user", ldap_bind_user.getText()),
                Pair.create("ldap_bind_password", ldap_bind_password.getText())}, ()->{
                get_ldap_status();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Successfully connected to server and ran test query");
                alert.setContentText("If you have not yet set up user/group LDAP filters and DNs, you should do so now. " +
                        "Defaults are what the developer uses for his freeIPA server. If you use freeIPA, you should be able to just replace " +
                        "k1ra and local with you own base DN and be good to go. If you do not, then an LDAP directory explorer application " +
                        "and the filter testing buttons are your friends.");
                alert.showAndWait();
            }, ()->{
                group_user_ap_control(false);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Could not connect to LDAP server");
                alert.setContentText(out.obj.getString("data"));
                alert.showAndWait();
            }, true);
        });

        btn_user_save.setOnMouseClicked(event -> {
            final NetworkResponse out = new NetworkResponse();
            NetworkRequest.make_POST(out, "/set_ldap_user_settings", new Pair[]{ Pair.create("ldap_user_dn", ldap_user_dn.getText()),
                                    Pair.create("ldap_user_filter", ldap_user_filter.getText()),
                                    Pair.create("ldap_user_uid_attribute", ldap_user_uid_attribute.getText())}, ()->{
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Saved user query details");
                alert.setContentText("You should run a test now");
                alert.showAndWait();
            }, ()->{}, true);
        });

        btn_group_save.setOnMouseClicked(event -> {
            final NetworkResponse out = new NetworkResponse();
            NetworkRequest.make_POST(out, "/set_ldap_group_settings", new Pair[]{ Pair.create("ldap_group_dn", ldap_group_dn.getText()),
                    Pair.create("ldap_group_filter", ldap_group_filter.getText()),
                    Pair.create("ldap_group_name_attribute", ldap_group_name_attribute.getText()),
                    Pair.create("ldap_admin_group_name", ldap_admin_group_name.getText())}, ()->{
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Saved group query details");
                alert.setContentText("You should run a test now");
                alert.showAndWait();
            }, ()->{}, true);
        });

        btn_user_test.setOnMouseClicked(event -> {
            final NetworkResponse out = new NetworkResponse();
            NetworkRequest.make_POST(out, "/run_ldap_user_test", new Pair[]{Pair.create("user", user_test.getText())}, ()->{
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Query results:");
                alert.setContentText(out.obj.getString("data"));
                alert.showAndWait();
            }, ()->{}, true);
        });

        btn_group_test.setOnMouseClicked(event -> {
            final NetworkResponse out = new NetworkResponse();
            NetworkRequest.make_POST(out, "/run_ldap_group_test", new Pair[]{Pair.create("user", group_test.getText())}, ()->{
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("User is "+((out.obj.getJSONObject("data").getInt("is_admin")==1)?"":"not ")+"an admin and belongs to these groups:");
                alert.setContentText(out.obj.getJSONObject("data").getString("list"));
                alert.showAndWait();
            }, ()->{}, true);
        });
    }

    void get_ldap_status() {
        final NetworkResponse out = new NetworkResponse();
        NetworkRequest.make_POST(out, "/get_ldap_status", new Pair[]{}, ()->{
            if (out.obj.getJSONObject("data").getInt("set_up") == 1) {
                group_user_ap_control(true);
                JSONObject data = out.obj.getJSONObject("data");

                ldap_server.setText(data.getString("ldap_server"));
                ldap_tls.setSelected(data.getString("ldap_tls").equals("1"));
                ldap_bind_user.setText(data.getString("ldap_bind_user"));
                ldap_bind_password.setText(data.getString("ldap_bind_password"));
                ldap_user_dn.setText(data.getString("ldap_user_dn"));
                ldap_user_filter.setText(data.getString("ldap_user_filter"));
                ldap_user_uid_attribute.setText(data.getString("ldap_user_uid_attribute"));
                ldap_group_dn.setText(data.getString("ldap_group_dn"));
                ldap_group_filter.setText(data.getString("ldap_group_filter"));
                ldap_group_name_attribute.setText(data.getString("ldap_group_name_attribute"));
                ldap_admin_group_name.setText(data.getString("ldap_admin_group_name"));
            } else
                group_user_ap_control(false);
        }, ()->{
            group_user_ap_control(false);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not connect to LDAP server");
            alert.setContentText(out.obj.getString("data"));
            alert.showAndWait();
        }, true);
    }

    void group_user_ap_control(boolean show) {
        ap_group.setVisible(show);
        ap_group.setMouseTransparent(!show);
        ap_user.setVisible(show);
        ap_user.setMouseTransparent(!show);
    }
}
