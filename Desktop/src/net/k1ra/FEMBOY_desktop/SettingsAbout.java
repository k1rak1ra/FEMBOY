package net.k1ra.FEMBOY_desktop;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import net.k1ra.FEMBOY_desktop.libFEMBOY.DatabaseAbstractionLayer;

import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class SettingsAbout implements Initializable {
    @FXML AnchorPane ap;
    @FXML Text version;
    @FXML Text feature_level;
    @FXML Text server;
    @FXML Text user;
    @FXML Button btn_logout;
    @FXML Button btn_logout_all;
    @FXML Button btn_reset;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ap.setBackground(new Background(new BackgroundFill(Color.web("#2f3136"), CornerRadii.EMPTY, Insets.EMPTY)));
        btn_logout.setStyle("-fx-background-color: #202225");
        btn_logout_all.setStyle("-fx-background-color: #202225");
        btn_reset.setStyle("-fx-background-color: #ff0000");

        version.setText("Version "+Main.version);
        feature_level.setText("Feature level "+Main.feature_level);

        if (DatabaseAbstractionLayer.get_mode() == 1) {
            server.setText("Local mode");
            user.setText("");
            user.setFont(Font.font(2));

            logout_hide();
        } else {
            server.setText("Connected to server at: " + NetworkRequest.server_url);

            if (!Controller.logged_in.getValue()) {
                logout_hide();
                user.setText("Not logged in...");
            } else {
                user.setText("Logged in as user: " + DatabaseAbstractionLayer.client_get_name());
            }
        }

        btn_logout.setOnMouseClicked(event -> {
            NetworkResponse out = new NetworkResponse();
            NetworkRequest.make_POST(out, "/log_out", new Pair[]{ Pair.create("all", "0") }, ()->{
                DatabaseAbstractionLayer.client_logout();
                Controller.logged_in.setValue(false);
                logout_hide();
            }, ()->{}, true);
        });

        btn_logout_all.setOnMouseClicked(event -> {
            NetworkResponse out = new NetworkResponse();
            NetworkRequest.make_POST(out, "/log_out", new Pair[]{ Pair.create("all", "1") }, ()->{
                DatabaseAbstractionLayer.client_logout();
                Controller.logged_in.setValue(false);
                logout_hide();
            }, ()->{}, true);
        });

        btn_reset.setOnMouseClicked(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Nuke confirmation");
            alert.setHeaderText("Are you sure you want to delete all locally stored application data and reset? " +
                    "If you are connected to a server, data on the server will be unaffected. If you click OK, the application will delete the data and then close. " +
                    "Next time you launch it, you will be guided through setup again.");

            alert.showAndWait().ifPresent((btnType) -> {
                if (btnType.equals(ButtonType.OK)) {
                    try {
                        DatabaseAbstractionLayer.conn.close();
                        Utils.delete_folder(Paths.get(Utils.get_local_storage_dir()).toFile());
                        Platform.exit();
                        System.exit(0);
                    } catch (Exception e) {
                        Utils.handle_error(e.toString());
                    }
                }
            });
        });
    }

    void logout_hide() {
        btn_logout.setDisable(true);
        btn_logout.setVisible(false);
        btn_logout_all.setDisable(true);
        btn_logout_all.setVisible(false);
    }
}
