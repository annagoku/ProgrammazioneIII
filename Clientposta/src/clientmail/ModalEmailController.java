package clientmail;

import commons.DateUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import commons.EMail;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;


public class ModalEmailController implements Initializable {

    ClientModel model = null;
    Stage stage = null;
    String command;
    EMail mailsel;

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


    public void initModel(ClientModel m, EMail mail, String b, Stage s) {
        this.model = m;
        this.command=b;
        this.stage = s;
        switch (command){
            case "NEW":
                valueSender.setText(model.getCasella());
                valueSender.setDisable(true);
                valueSender.setStyle("-fx-opacity: 1;");
                date.setText(DateUtils.dateString());
                break;
            case "REPLAY":
                if(mail!=null){
                    this.mailsel=mail;
                    valueSender.setText(model.getCasella());
                    valueSender.setDisable(true);
                    valueSender.setStyle("-fx-opacity: 1;");
                    valueObject.setText(("R: ".concat(mailsel.getSubject())));
                    date.setText(DateUtils.dateString());
                    valueRecipients.setText(mailsel.getSender());
                    String tmp=("\n"+"\n"+"From: "+mailsel.getSender()+ "\n" +"Sent: "+mailsel.getTime() +"\n"+"To: "+mailsel.getRecipients()+"\n"+
                            "Subject: "+ mailsel.getSubject());
                    valueText.setText(tmp.concat(mailsel.getText()));
                }
                break;
            case "REPLAY ALL":
                if(mail!=null){
                    this.mailsel=mail;
                    valueSender.setText(model.getCasella());
                    valueSender.setDisable(true);
                    valueSender.setStyle("-fx-opacity: 1;");
                    valueObject.setText(("R: ".concat(mailsel.getSubject())));
                    date.setText(DateUtils.dateString());
                    valueRecipients.setText(mailsel.getSender().concat("; "+mailsel.getRecipients()));
                    String tmp=("\n"+"\n"+"From: "+mailsel.getSender()+ "\n" +"Sent: "+mailsel.getTime() +"\n"+"To: "+mailsel.getRecipients()+"\n"+
                            "Subject: "+ mailsel.getSubject());
                    valueText.setText(tmp.concat(mailsel.getText()));
                }
                break;
            case "FORWARD":
                if(mail!=null){
                    this.mailsel=mail;
                    valueSender.setText(model.getCasella());
                    valueSender.setDisable(true);
                    valueSender.setStyle("-fx-opacity: 1;");
                    valueObject.setText(("F: ".concat(mailsel.getSubject())));
                    date.setText(DateUtils.dateString());
                    valueRecipients.setText("");
                    String tmp=("\n"+"\n"+"From: "+mailsel.getSender()+ "\n" +"Sent: "+mailsel.getTime() +"\n"+"To: "+mailsel.getRecipients()+"\n"+
                            "Subject: "+ mailsel.getSubject());
                    valueText.setText(tmp.concat("\n"+"\n"+"\n"+mailsel.getText()));
                }
                break;
        }

    }

    @FXML
    public void send(ActionEvent event){

        EMail email = new EMail(model.getCasella()+ UUID.randomUUID().toString(),
                model.getCasella(),
                valueRecipients.getText(),
                valueObject.getText(),
                valueText.getText(),
                DateUtils.dateString());

        //TODO aggiungere logica per inviare la mail


        model.getMailSent().add(email);
        this.stage.close();
    }

    @FXML
    public void cancel(ActionEvent event){
        this.stage.close();
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


    }
}
