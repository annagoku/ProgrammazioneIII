package clientmail;

import commons.EMail;
import javafx.application.Platform;
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
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainGuiController implements Initializable {

    //Configure Table arrived
    @FXML
    private TableView<EMail> tableArrived;
    @FXML
    private TableColumn<EMail, String> dateArrived;
    @FXML
    private TableColumn<EMail, String> sender;
    @FXML
    private TableColumn<EMail, String> objectArrived;

    //Configure Table sent
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
    private Button replayMail;
    @FXML
    private Button replayAllMail;
    @FXML
    private Button forwardMail;
    @FXML
    private Button deleteMail;

    //Configure Label current action Client
    @FXML
    private Label action;

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

    private ClientModel model;
    Stage primaryStage = null;



    //Metodo di inizializzazione Model/Controller
    public void initModel(ClientModel model, Stage pm) {
        if (this.model != null) {
            throw new IllegalStateException("Il Model può essere inizializzato una sola volta");
        }
        this.model = model;
        this.primaryStage = pm;

        //aggancio l'observable list alla tabella
        tableArrived.setItems(model.getMailArrived());

        tableSent.setItems(model.getMailSent());

        //Bindings per operation on going
        action.textProperty().bind(this.model.clientOperationProperty());

        // Binding tra colonne tableArrived e properties della classe EMail
        dateArrived.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        sender.setCellValueFactory(cellData -> cellData.getValue().senderProperty());
        objectArrived.setCellValueFactory(cellData -> cellData.getValue().subjectProperty());

        // Binding tra colonne tableArrived e properties della classe EMail
        dateSent.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        recipients.setCellValueFactory(cellData -> cellData.getValue().recipientsProperty());
        objectSent.setCellValueFactory(cellData -> cellData.getValue().subjectProperty());

        // Listen per selezione su riga e visializzazione dettaglio mail
        tableArrived.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showMailDetails(newValue));

        tableSent.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showMailDetails(newValue));

        // Gestione esplicita evento Windows close
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
        model.setClientOperation("Loading mail from server....");
        new ReceiveThread(model, false, false).start();
    }

    //Cancellazione mail da client e Server su evento button Delete
    @FXML
    public void handleDelete() {
        // 0 - arrived, 1 - sent
        model.setClientOperation("Deleting mail selected");

        int tabIndex = ((TabPane)tableArrived.getParent().getParent().getParent()).getSelectionModel().getSelectedIndex();

        if (selectedEmail != null){
            if(tabIndex == 0)
                new DeleteThread(DeleteThread.Selection.ARRIVED, model, selectedEmail).start();
            else
                new DeleteThread(DeleteThread.Selection.SENT, model, selectedEmail).start();
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
        modal_dialog.initModality(Modality.WINDOW_MODAL); //Def modalità Windows
        modal_dialog.initOwner(primaryStage);
        Scene scene = new Scene(root);
        modal_dialog.setTitle("Client posta "+ model.getCasella());

        ModalEmailController mc = loader.getController();
        commandEvent=((Button)event.getSource()).getText();
        mc.initModel(this.model,selectedEmail, commandEvent, modal_dialog, action);
        modal_dialog.setScene(scene);
        modal_dialog.show();
    }

    //Gestione doppio-click con mouse
    @FXML
    public void handleActionMouseArrived (MouseEvent event)throws IOException {
        EMail email =  tableArrived.getSelectionModel().selectedItemProperty().get();
        showMailDetails(email);
        if(event.getClickCount() == 2) {
            openMailDialog();
        }
    }

    @FXML
    public void handleActionMouseSent (MouseEvent event)throws IOException {
        EMail email =  tableSent.getSelectionModel().selectedItemProperty().get();
        showMailDetails(email);
        if(event.getClickCount() == 2) {
            openMailDialog();
        }
    }

    //Apertura nuova finestra di dialogo
    private void openMailDialog() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ModalEmail.fxml"));
        loader.load();
        Parent root = loader.getRoot();
        Stage modal_dialog = new Stage(StageStyle.DECORATED);
        modal_dialog.initModality(Modality.WINDOW_MODAL);
        modal_dialog.initOwner(primaryStage);
        Scene scene = new Scene(root);
        modal_dialog.setTitle("Client posta "+ model.getCasella());
        ModalEmailController mc = (ModalEmailController) loader.getController();
        commandEvent="MOUSEEVENT";
        mc.initModel(this.model,selectedEmail, commandEvent, modal_dialog, action);
        modal_dialog.setScene(scene);
        modal_dialog.show();

    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)  {
        panelEmailDetail.setVisible(false);
        tableArrived.setPlaceholder(new Label("No mail to display"));
        tableSent.setPlaceholder(new Label("No mail to display"));
        action.setText("");
        replayMail.setDisable(true);
        replayAllMail.setDisable(true);
        deleteMail.setDisable(true);
        forwardMail.setDisable(true);
    }

}