package clientmail;

import commons.DateUtils;
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


import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import commons.EMail;

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
    private Label receivingState;
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

    private static DateTimeFormatter DATEFORMAT = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss");

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
    }

    //Ricezione mail su evento button Receive
    @FXML
    public void handleReceive(){
        if(mail.getMailArrived().isEmpty()){
            String timestamp= DateUtils.dateString();
            new ReceiveDaemonThread(mail, timestamp, mail.getCasella()).start();
        }else{
            String timestamp=(mail.getMailArrived().get(mail.getMailArrived().size()-1)).getTime();
            new ReceiveDaemonThread(mail, timestamp, mail.getCasella()).start();
        }
    }

    //Cancellazione mail da client e Server
    public void handleDelete() {
        if (selectedEmail != null){
            new DeleteThread(mail, mail.getCasella(), selectedEmail).start();
        }
    }

    //invio mail
    public void handleSend(){
        if(selectedEmail!=null) {
            new SendThread(mail,selectedEmail,mail.getCasella()).start();
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




    /*@FXML
    public void eliminaMail(ActionEvent event) throws IOException {

        panelEmailDetail.setVisible(false);
        valueRecipients.setText("");
        valueSender.setText("");
        valueObject.setText("");
        valueTextMail.setText("");

        mail.removeArrivedMail(selectedEmail);

    }*/

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        panelEmailDetail.setVisible(false);
        tableArrived.setPlaceholder(new Label("No mail to display"));
        tableSent.setPlaceholder(new Label("No mail to display"));
        receivingState.setText("No new mail");
        replayMail.setDisable(true);
        replayAllMail.setDisable(true);
        deleteMail.setDisable(true);
        forwardMail.setDisable(true);


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


       //binding label connection
       //ClientModel model=new ClientModel();


       //stateConnection.textProperty().bind(model.connectionProperty());



    }

}