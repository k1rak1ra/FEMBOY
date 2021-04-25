package net.k1ra.FEMBOY_desktop;

import com.jfoenix.controls.JFXTabPane;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import net.k1ra.FEMBOY_desktop.libFEMBOY.DatabaseAbstractionLayer;

public class Settings{

@FXML JFXTabPane settings_tabpane;
@FXML AnchorPane cover_pane;
@FXML AnchorPane loading_modal;
@FXML AnchorPane loading_modal_container;

static Scene scene;
Tab users;
Tab ldap;

    public void init(){
        try {
            cover_pane.setBackground(new Background(new BackgroundFill(Color.web("#000000"), CornerRadii.EMPTY, Insets.EMPTY)));
            loading_modal.setBackground(new Background(new BackgroundFill(Color.web("#2f3136"), new CornerRadii(25), Insets.EMPTY)));
            settings_tabpane.getStylesheets().add(this.getClass().getResource("CSS/tab_pane.css").toExternalForm());

            settings_tabpane.getTabs().addAll((Tab) FXMLLoader.load(this.getClass().getResource("FXML/settings_about.fxml")));

            if (DatabaseAbstractionLayer.get_mode() == 0) {
                users = FXMLLoader.load(this.getClass().getResource("FXML/settings_users.fxml"));
                ldap = FXMLLoader.load(this.getClass().getResource("FXML/settings_ldap.fxml"));

                //if user is admin, put admin settings tabs
                if (DatabaseAbstractionLayer.client_is_admin() && !Controller.offline.getValue() && Controller.logged_in.getValue())
                    settings_tabpane.getTabs().addAll(users, ldap);

                Controller.logged_in.addListener(l -> remove_admin());
                Controller.offline.addListener(l -> remove_admin());
                Controller.is_loading.addListener(l -> settings_loading());
            }

            settings_loading();

            settings_tabpane.getTabs().addAll((Tab) FXMLLoader.load(this.getClass().getResource("FXML/settings_libraries.fxml")));
        } catch (Exception e) {
            Utils.handle_error(e.toString());
        }
    }

    void settings_loading() {
        cover_pane.setVisible(Controller.is_loading.getValue());
        cover_pane.setMouseTransparent(!Controller.is_loading.getValue());
        loading_modal_container.setVisible(Controller.is_loading.getValue());
        loading_modal_container.setMouseTransparent(!Controller.is_loading.getValue());
    }

    void remove_admin() {
        settings_tabpane.getTabs().remove(users);
        settings_tabpane.getTabs().remove(ldap);
    }
}
