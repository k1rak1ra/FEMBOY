package net.k1ra.FEMBOY_desktop;

import com.jfoenix.controls.JFXListView;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class TagCell{

    @FXML AnchorPane ap;
    @FXML Text booru_tag;
    @FXML ImageView btn_tag_del;

    JFXListView list;
    Parent root;


    void populate (final String tag_str, JFXListView list, Parent root, Runnable on_delete, boolean delete_enabled) {

        ap.widthProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
                String short_tag_str;
                double max_width = newSceneWidth.doubleValue()-24;

                //trim text if too long and determine right inset.
                booru_tag.setText(tag_str);
                short_tag_str = tag_str;
                while (booru_tag.getLayoutBounds().getWidth() > max_width-10) {
                    short_tag_str = short_tag_str.substring(0, short_tag_str.length()-1);
                    booru_tag.setText(short_tag_str + "...");
                }

                String color = "#ff0000";

                if (Utils.character_tags.contains(tag_str))
                    color = "#00bb00";
                else if (tag_str.startsWith("rating:"))
                    color = "#0000ff";

                //set color and insets depending on tag type and text length
                ap.setBackground(new Background(new BackgroundFill(Color.web(color), new CornerRadii(40),
                        new Insets(0,max_width-booru_tag.getLayoutBounds().getWidth()-15,0,0))));
            }
        });

        //pass reference to list
        this.list = list;
        this.root = root;

        if (delete_enabled) {
            //add listener for deletion
            btn_tag_del.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    list.getItems().remove(root);
                    if (on_delete != null)
                        on_delete.run();
                }
            });
        }
    }
}
