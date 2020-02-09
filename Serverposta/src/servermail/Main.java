package servermail;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        BorderPane root= new BorderPane();
        FXMLLoader serverLoader = new FXMLLoader(getClass().getResource("serverfinestra.fxml"));
        root.setCenter(serverLoader.load());
        ServerController serverController = serverLoader.getController();
        primaryStage.setTitle("ServerMail");
        primaryStage.setScene(new Scene(root, 900, 700));
        primaryStage.show();
        ServerModel model= new ServerModel();
        serverController.init(model);


    }


    public static void main(String[] args) {

        launch(args);
    }
}
