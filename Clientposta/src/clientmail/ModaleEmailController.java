package clientmail;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import commons.EMail;


public class ModaleEmailController {
    ModelLista model = null;

    Stage stage = null;

    @FXML
    private Label valueMittente ;
    @FXML
    private TextField valueDestinatari;
    @FXML
    private  TextField valueOggetto;
    @FXML
    private TextArea valueTestoEmail;


    public void initModel(ModelLista m, Stage s)  {
        this.model = m;
        this.stage = s;
        valueMittente.textProperty().setValue(m.getCasella());
    }

    @FXML
    public void invia(ActionEvent event){
        //TODO logica per inviare la mail
        model.getmaillist().add(new EMail(model.getCasella(), valueDestinatari.getText(), valueOggetto.getText(), valueTestoEmail.getText()));
        this.stage.close();
    }

    @FXML
    public void annulla(ActionEvent event){
        this.stage.close();
    }


}
