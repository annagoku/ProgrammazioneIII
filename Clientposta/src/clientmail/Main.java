package clientmail;

import commons.SystemLogger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import commons.EMail;
import javafx.stage.WindowEvent;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

public class Main extends Application {
    private static SystemLogger LOGGER = new SystemLogger(Main.class);

    @Override
    public void start(Stage primaryStage) throws Exception{



        BorderPane root= new BorderPane();
        LOGGER.info("Loading UI");
        FXMLLoader listLoader = new FXMLLoader(getClass().getResource("ClientGraphicalInterface.fxml"));
        root.setCenter(listLoader.load());
        MainGuiController mainGuiController = listLoader.getController();

        //Reading mail folder
        Parameters args = getParameters();
        String folder = args.getRaw().get(0);
        LOGGER.info("Account folder: "+folder);

        LOGGER.info("Loading properties");
        Properties props = new Properties();
        props.load(new FileInputStream(("./data/"+folder+"/config.properties")));



        ClientModel model=new ClientModel(props);
        LOGGER.info("Initializing model");

        mainGuiController.initModel(model, primaryStage);
        primaryStage.setTitle("Client posta "+model.getCasella());
        primaryStage.setScene(new Scene(root, 800, 700));
        primaryStage.show();

        LOGGER.info("ClientPosta started");




    }


    public static void main(String[] args) {
        Locale.setDefault(Locale.ENGLISH);
        launch(args);
    }
}
