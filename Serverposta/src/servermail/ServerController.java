package servermail;

import commons.SystemLogger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.FileNotFoundException;
import java.io.IOException;
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
    private Stage primaryStage;

    public void init(ServerModel m, Stage s){
        if (this.model!=null){
            throw new IllegalStateException("Il modello può essere inizializzato una sola volta");
        }
        this.model=m;
        this.primaryStage=s;
        //aggancio l'observable list alla table view
        logHistory.setItems(model.getLog());

        try {
            //carica gli account
            model.loadAccounts();
            //carica i log dal file logs
            model.loadLogs();
        } catch (FileNotFoundException e) {
            state.setText("Error: accounts file not found");
            state.setTextFill(Color.RED);
            connect.setDisable(true);
        }
        catch (Exception e) {
            e.printStackTrace();
            state.setText("Generic Error on load accounts"+e.getMessage());
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

        // Gestione esplicita evento Windows close
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                LOGGER.log("window close");
                try {
                    model.endconnect();
                    model.saveId();
                    model.saveLogs();
                } catch (Exception e) {
                    LOGGER.log(e.getMessage());
                    e.printStackTrace();
                }
                finally {
                    Platform.exit();
                    System.exit(0);
                }
            }
        });
    }


    @FXML
    public void handleconnection(ActionEvent e) {
        LOGGER.log("pressed Connect button");
        model.startconnect();
        state.setText("Connected");
        state.setTextFill(Color.GREEN);
        connect.setDisable(true);
        disconnect.setDisable(false);

    }

    @FXML
    public void handleclose(ActionEvent e) {
        LOGGER.log("pressed Close button");
        model.exec.shutdown();
        try {
            model.endconnect();
            state.setText("Disconnect");
            state.setTextFill(Color.RED);
            disconnect.setDisable(true);
            connect.setDisable(false);
            //attesa di max 30 secondi per far terminare tutti i thread del pool
            model.exec.awaitTermination(30, TimeUnit.SECONDS);
        } catch (IOException ex) {
            LOGGER.log(ex.getMessage());
            ex.printStackTrace();
            state.setText("Disconnect error: "+ex.getMessage());
            state.setTextFill(Color.RED);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            LOGGER.log(ex.getMessage());
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

        //Binding tra colonne tableview e properties observable list di Log
        date.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        message.setCellValueFactory(cellData -> cellData.getValue().mesProperty());
        client.setCellValueFactory(cellData -> cellData.getValue().clientProperty());
        ipClient.setCellValueFactory(cellData -> cellData.getValue().ipClientProperty());


    }
}



