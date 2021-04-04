package net.k1ra.FEMBOY_desktop;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.k1ra.FEMBOY_desktop.libFEMBOY.DatabaseAbstractionLayer;

import java.io.File;
import java.io.InputStream;
import java.nio.file.*;

public class Main extends Application {

    static String version = "0.1.1 - BETA";
    static int feature_level = 1;

    @Override
    public void start(Stage primary_stage) throws Exception{
        //set database error handler to GUI message
        DatabaseAbstractionLayer.error_handler = e -> Platform.runLater(()-> { Utils.handle_error(e.toString()); e.printStackTrace();});

        //check for data/folder and create if does not exit
        if (!Files.exists(Paths.get(Utils.get_local_storage_dir()))) {
            new File(Utils.get_local_storage_dir()).mkdir();
            new File(Utils.get_local_storage_dir()+"images/").mkdir();

            //copy model and tags to directory
            InputStream is = Main.class.getResourceAsStream("DD_data/DD-model.zip");
            Utils.copy_stream(is, new File(Utils.get_local_storage_dir()+"DD-model.zip"));
            is = Main.class.getResourceAsStream("DD_data/DD-tags.txt");
            Utils.copy_stream(is, new File(Utils.get_local_storage_dir()+"DD-tags.txt"));
            is = Main.class.getResourceAsStream("DD_data/DD-characters.txt");
            Utils.copy_stream(is, new File(Utils.get_local_storage_dir()+"DD-characters.txt"));

            //if SQlite DB does not exist, create tables
            Utils.connect_db();
            DatabaseAbstractionLayer.create_tables();
            DatabaseAbstractionLayer.create_tables_client();
        } else {
            Utils.connect_db();
        }

        Utils.populate_chara_tags();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FXML/main.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();
        controller.stage = primary_stage;

        primary_stage.setTitle("F.E.M.B.O.Y. - desktop");
        primary_stage.setScene(new Scene(root, 600, 600));
        primary_stage.setMinHeight(600);
        primary_stage.setMinWidth(600);
        primary_stage.setMaximized(true);
        primary_stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
