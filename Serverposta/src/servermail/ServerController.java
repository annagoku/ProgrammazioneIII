package servermail;

import commons.SystemLogger;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.WindowEvent;


import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class ServerController implements Initializable {
    private static SystemLogger LOGGER = new SystemLogger(ServerController.class);

    @FXML
    private TableView<Log> logHistory;
    @FXML
    private TableColumn<Log, String> date;
    @FXML
    private TableColumn<Log, String> message;
    @FXML
    private TableColumn<Log, String> client;
    @FXML
    private TableColumn<Log, String> ipClient;
    @FXML
    private Label state;



    @FXML
    private Button connect;
    @FXML
    private Button disconnect;

    private ServerModel model;

    public void init(ServerModel m){
        if (this.model!=null){
            throw new IllegalStateException("Il modello può essere inizializzato una sola volta");
        }
        this.model=m;
        //aggancio l'observable list alla tabella
        logHistory.setItems(model.getLog());


        try {
            //carica gli account
            model.loadAccounts();
            model.loadLogs();
        } catch (FileNotFoundException e) {
            state.setText("Error: accounts file not found");
            state.setTextFill(Color.RED);
            connect.setDisable(true);
        }
        catch (Exception e) {
            e.printStackTrace();
            state.setText("generic Error on load accounts"+e.getMessage());
            state.setTextFill(Color.RED);
            connect.setDisable(true);
        }

        try {
            //carica il file generatore di ID
            model.loadNextId();
        } catch (FileNotFoundException e) {
            state.setText("Error: generateId file not found");
            state.setTextFill(Color.RED);
            connect.setDisable(true);
        }catch (Exception e) {
            state.setText("Error: on reading generateId from file");
            state.setTextFill(Color.RED);
            connect.setDisable(true);
        }



    }







    public void handleconnection(ActionEvent e) {
        LOGGER.debug("pressed Connect button");
        model.startconnect();
        state.setText("Connected");
        state.setTextFill(Color.GREEN);
        connect.setDisable(true);
        disconnect.setDisable(false);

    }

    public void handleclose(ActionEvent e) {
        LOGGER.debug("pressed Close button");
        model.exec.shutdown();


        /*if(model.prec.size()>0) {
            for (Thread t : model.prec) {
                try {
                    t.join();
                }catch (InterruptedException j) {
                    j.printStackTrace();
                }
            }
        }*/
        try {
            model.endconnect();
            state.setText("Disconnect");
            state.setTextFill(Color.RED);
            disconnect.setDisable(true);
            connect.setDisable(false);
            model.exec.awaitTermination(30, TimeUnit.SECONDS);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
            ex.printStackTrace();
            state.setText("Disconnect error: "+ex.getMessage());
            state.setTextFill(Color.RED);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            LOGGER.error(ex.getMessage());
            state.setText("Timeout in stopping clients: "+ex.getMessage());
            state.setTextFill(Color.RED);
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        state.setText("Disconnect");
        state.setTextFill(Color.RED);
        disconnect.setDisable(true);
        logHistory.setPlaceholder(new Label("No logs to display"));

        //Binding tra observable list di Log e tableview
        date.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        message.setCellValueFactory(cellData -> cellData.getValue().mesProperty());
        client.setCellValueFactory(cellData -> cellData.getValue().clientProperty());
        ipClient.setCellValueFactory(cellData -> cellData.getValue().ipClientProperty());


    }
}



