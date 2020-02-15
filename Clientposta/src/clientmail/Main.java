package clientmail;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import commons.EMail;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        String casella = "diana.rossi@mymail.com";

        BorderPane root= new BorderPane();
        FXMLLoader listLoader = new FXMLLoader(getClass().getResource("ClientGraphicalInterface.fxml"));
        root.setCenter(listLoader.load());
        MainGuiController mainGuiController = listLoader.getController();

        ClientModel model=new ClientModel(casella);

        mainGuiController.initModel(model, primaryStage);
        primaryStage.setTitle("Client posta "+casella);
        primaryStage.setScene(new Scene(root, 800, 700));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
