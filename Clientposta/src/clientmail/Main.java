package clientmail;

import commons.SystemLogger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.util.Locale;
import java.util.Properties;

public class Main extends Application {
    private static SystemLogger LOGGER = new SystemLogger(Main.class);

    @Override
    public void start(Stage primaryStage) throws Exception{

        BorderPane root= new BorderPane();
        LOGGER.log("Loading UI");
        FXMLLoader listLoader = new FXMLLoader(getClass().getResource("ClientGraphicalInterface.fxml"));
        root.setCenter(listLoader.load());
        MainGuiController mainGuiController = listLoader.getController();

        //Lettura del folder passato come parametro RUN--> Edit Configuration
        Parameters args = getParameters();
        String folder = args.getRaw().get(0);
        LOGGER.log("Account folder: "+folder);

        //Lettura del file di properties dell'account
        LOGGER.log("Loading properties");
        Properties props = new Properties();
        props.load(new FileInputStream(("./data/"+folder+"/config.properties")));


        ClientModel model=new ClientModel(props);
        LOGGER.log("Initializing model");

        //Associazione tra Controller e Model
        mainGuiController.initModel(model, primaryStage);
        //Set scene
        primaryStage.setTitle("Client posta "+model.getCasella());
        primaryStage.setScene(new Scene(root, 800, 700));
        primaryStage.show();

        LOGGER.log("ClientPosta started");




    }


    public static void main(String[] args) {
        Locale.setDefault(Locale.ENGLISH);
        launch(args);
    }
}
