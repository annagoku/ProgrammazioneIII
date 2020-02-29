package clientmail;

import commons.Utilities;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import commons.EMail;

import java.net.URL;
import java.util.ResourceBundle;


public class ModalEmailController implements Initializable {

    ClientModel model = null;
    Stage stage = null;
    String command;
    EMail mailsel;
    Label action;

    @FXML
    private TextField valueSender;
    @FXML
    private TextField valueRecipients;
    @FXML
    private  TextField valueObject;
    @FXML
    private TextArea valueText;
    @FXML
    private Label date;
    @FXML
    private Button send;


    public void initModel(ClientModel m, EMail mail, String b, Stage s, Label action) {
        this.model = m;
        this.command=b;
        this.stage = s;
        this.action=action;
        switch (command){
            case "NEW":
                valueSender.setText(model.getCasella());
                valueSender.setDisable(true);
                valueSender.setStyle("-fx-opacity: 1;");
                date.setText(Utilities.dateString());
                break;
            case "REPLY":
                if(mail!=null){
                    this.mailsel=mail;
                    valueSender.setText(model.getCasella());
                    valueSender.setDisable(true);
                    valueSender.setStyle("-fx-opacity: 1;");
                    valueObject.setText(("R: ".concat(mailsel.getSubject())));
                    date.setText(Utilities.dateString());
                    valueRecipients.setText(mailsel.getSender());
                    valueText.setText(Utilities.getReplyText(mailsel));
                }
                break;
            case "REPLY ALL":
                if(mail!=null){
                    this.mailsel=mail;
                    valueSender.setText(model.getCasella());
                    valueSender.setDisable(true);
                    valueSender.setStyle("-fx-opacity: 1;");
                    valueObject.setText(("R: ".concat(mailsel.getSubject())));
                    date.setText(Utilities.dateString());
                    if(mailsel.getRecipients().trim().equals(model.getCasella())) {
                        valueRecipients.setText(mailsel.getSender());
                    }
                    else {
                        valueRecipients.setText(mailsel.getSender()+", "+Utilities.replyAllRecipients(mailsel.getRecipients(), model.getCasella()));
                    }

                    valueText.setText(Utilities.getReplyText(mailsel));
                }
                break;
            case "FORWARD":
                if(mail!=null){
                    this.mailsel=mail;
                    valueSender.setText(model.getCasella());
                    valueSender.setDisable(true);
                    valueSender.setStyle("-fx-opacity: 1;");
                    valueObject.setText(("F: ".concat(mailsel.getSubject())));
                    date.setText(Utilities.dateString());
                    valueRecipients.setText("");
                    valueText.setText(Utilities.getReplyText(mailsel));
                }
                break;
            case "MOUSEEVENT":
                if(mail!=null){
                    this.mailsel=mail;
                    valueSender.setText(mailsel.getSender());
                    valueSender.setDisable(true);
                    valueSender.setStyle("-fx-opacity: 1;");
                    valueObject.setText(mailsel.getSubject());
                    valueObject.setDisable(true);
                    valueObject.setStyle("-fx-opacity: 1;");
                    date.setText(mailsel.getTime());
                    date.setDisable(true);
                    date.setStyle("-fx-opacity: 1;");
                    valueRecipients.setText(mail.getRecipients());
                    valueRecipients.setDisable(true);
                    valueRecipients.setStyle("-fx-opacity: 1;");
                    valueText.setText(mailsel.getText());
                    valueText.setEditable(false);
                    valueText.setStyle("-fx-opacity: 1;");
                    send.setDisable(true);
                    break;
                }

        }

    }

    @FXML
    public void handleSend(ActionEvent event){

        //controllo correttezza indirizzo destinatario lato client

        if(!EMail.recipientsValid(valueRecipients.getText())){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Mail address not valid");
            alert.setContentText("Please check before to try again");
            alert.show();

        }
        else {
            EMail email = new EMail("", Utilities.dateString(),
                    model.getCasella(),
                    valueRecipients.getText(),
                    valueObject.getText(),
                    valueText.getText());


            model.setClientOperation("Sending mail");
            new SendThread(model,email).start();
            this.stage.close();
        }

    }

    @FXML
    public void cancel(ActionEvent event){
        this.stage.close();
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


    }
}
