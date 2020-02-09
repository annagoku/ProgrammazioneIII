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
        String casella = "diana2013@mymail.com";

        BorderPane root= new BorderPane();
        FXMLLoader listLoader = new FXMLLoader(getClass().getResource("Interfacciaclient.fxml"));
        root.setCenter(listLoader.load());
        MainController tableController = listLoader.getController();

        ObservableList<EMail> list= FXCollections.observableArrayList();
        list.add(new EMail("july2016@mymail.com", casella,"ciao","prova1"));
        list.add(new EMail("anna1981@mymail.com", casella, "hello","prova2"));
        list.add(new EMail("laura1977@mymail.com", casella, "hola","prova3"));

        ModelLista model=new ModelLista(list);
        model.setCasella(casella);
        tableController.initModel(model, primaryStage);
        primaryStage.setTitle("Client posta "+casella);
        primaryStage.setScene(new Scene(root, 800, 700));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
