package net.k1ra.FEMBOY_desktop;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsLibraries implements Initializable {
    @FXML AnchorPane ap;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ap.setBackground(new Background(new BackgroundFill(Color.web("#2f3136"), CornerRadii.EMPTY, Insets.EMPTY)));
    }
}
