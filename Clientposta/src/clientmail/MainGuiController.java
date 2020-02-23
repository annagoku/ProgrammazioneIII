package clientmail;

import commons.Utilities;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.ResourceBundle;

import commons.EMail;
import javafx.stage.WindowEvent;

public class MainGuiController implements Initializable {

    //Configure Table
    @FXML
    private TableView<EMail> tableArrived;
    @FXML
    private TableColumn<EMail, String> stateMail;
    @FXML
    private TableColumn<EMail, String> dateArrived;
    @FXML
    private TableColumn<EMail, String> sender;
    @FXML
    private TableColumn<EMail, String> objectArrived;

    @FXML
    private TableView<EMail> tableSent;
    @FXML
    private TableColumn<EMail, String> dateSent;
    @FXML
    private TableColumn<EMail, String> recipients;
    @FXML
    private TableColumn<EMail, String> objectSent;

    //Configure Buttons
    @FXML
    private Button newMail;
    @FXML
    private Button replayMail;
    @FXML
    private Button replayAllMail;
    @FXML
    private Button forwardMail;
    @FXML
    private Button deleteMail;
    @FXML
    private Button receiveMail;
    @FXML
    private Button openSelec;

    //Configure Label
    @FXML
    private Label countNewMail;
    @FXML
    private Label stateConnection;

    //Preview mail
    @FXML
    private Pane panelEmailDetail;

    @FXML
    private TextArea valueTextMail;
    @FXML
    private Label valueSender;
    @FXML
    private Label valueRecipients;
    @FXML
    private Label valueObject;

    //variabili locali del controller
    private EMail selectedEmail;
    private String commandEvent;

    private ClientModel mail;
    Stage primaryStage = null;



    //metodo di inizializzazione model/controller
    public void initModel(ClientModel model, Stage pm) {
        if (this.mail != null) {
            throw new IllegalStateException("Model può essere inizializzato una sola volta");
        }
        this.mail = model;
        this.primaryStage = pm;
        //aggancio l'observable list alla tabella
        tableArrived.setItems(model.getMailArrived());
        tableSent.setItems(model.getMailSent());

        //Bindings

        countNewMail.textProperty().bind(mail.countNewMailProperty());

        // Binding tableArrived
        stateMail.setCellValueFactory(cellData -> cellData.getValue().stateNewMailProperty());
        dateArrived.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        sender.setCellValueFactory(cellData -> cellData.getValue().senderProperty());
        objectArrived.setCellValueFactory(cellData -> cellData.getValue().subjectProperty());


        // Binding tableSent
        dateSent.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        recipients.setCellValueFactory(cellData -> cellData.getValue().recipientsProperty());
        objectSent.setCellValueFactory(cellData -> cellData.getValue().subjectProperty());


        // Listen for selection changes and show the mail details when changed.
        tableArrived.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showMailDetails(newValue));

        tableSent.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showMailDetails(newValue));

        // handles window close -> shutdown application
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {

                Platform.exit();
                System.exit(0);

            }
        });
    }

    //Ricezione mail su evento button Receive
    @FXML
    public void handleReceive(){
        mail.setCountNewMail("Loading mail from server....");
        new ReceiveThread(mail, false).start();

    }

    //Cancellazione mail da client e Server
    @FXML
    public void handleDelete() {
        // 0 - arrived, 1 - sent
        int tabIndex = ((TabPane)tableArrived.getParent().getParent().getParent()).getSelectionModel().getSelectedIndex();

        if (selectedEmail != null){
            if(tabIndex == 0)
                new DeleteThread(DeleteThread.Selection.ARRIVED, mail, selectedEmail).start();
            else
                new DeleteThread(DeleteThread.Selection.SENT, mail, selectedEmail).start();
        }
    }



    //Selezione mail su click su riga arrived/sent
    public void showMailDetails(EMail mail){
        if (mail != null) {
            selectedEmail=mail;
            panelEmailDetail.setVisible(true);
            replayMail.setDisable(false);
            replayAllMail.setDisable(false);
            forwardMail.setDisable(false);
            deleteMail.setDisable(false);
            valueSender.setText(mail.getSender());
            valueRecipients.setText(mail.getRecipients());
            valueObject.setText(mail.getSubject());
            valueTextMail.setText(mail.getText());
        }
    }


    //Apertura nuova finestra per reply/replyall/forward
    @FXML
    public void handleAction(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ModalEmail.fxml"));
        loader.load();
        Parent root = loader.getRoot();
        Stage modal_dialog = new Stage(StageStyle.DECORATED);
        modal_dialog.initModality(Modality.WINDOW_MODAL);
        modal_dialog.initOwner(primaryStage);
        Scene scene = new Scene(root);
        modal_dialog.setTitle("Client posta "+mail.getCasella());

        ModalEmailController mc = (ModalEmailController) loader.getController();
        commandEvent=((Button)event.getSource()).getText();
        mc.initModel(this.mail,selectedEmail, commandEvent, modal_dialog);
        modal_dialog.setScene(scene);
        modal_dialog.show();
    }

    //Apertura mail con 2-click mouse
    @FXML
    public void handleActionMouse (MouseEvent event)throws IOException{
        if(event.getClickCount()==2){
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ModalEmail.fxml"));
            loader.load();
            Parent root = loader.getRoot();
            Stage modal_dialog = new Stage(StageStyle.DECORATED);
            modal_dialog.initModality(Modality.WINDOW_MODAL);
            modal_dialog.initOwner(primaryStage);
            Scene scene = new Scene(root);
            //modal_dialog.setTitle("Nuovo messaggio");
            modal_dialog.setTitle("Client posta "+mail.getCasella());
            ModalEmailController mc = (ModalEmailController) loader.getController();
            commandEvent="MOUSEEVENT";
            mc.initModel(this.mail,selectedEmail, commandEvent, modal_dialog);
            modal_dialog.setScene(scene);
            modal_dialog.show();
        }
    }




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)  {
        panelEmailDetail.setVisible(false);
        tableArrived.setPlaceholder(new Label("No mail to display"));
        tableSent.setPlaceholder(new Label("No mail to display"));
        countNewMail.setText("No new mail");
        replayMail.setDisable(true);
        replayAllMail.setDisable(true);
        deleteMail.setDisable(true);
        forwardMail.setDisable(true);
    }

}