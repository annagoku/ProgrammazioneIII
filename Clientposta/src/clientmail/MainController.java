package clientmail;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

import commons.EMail;

public class MainController /*implements Initializable*/ {

    //Configure Table
    @FXML
    private TableView<EMail> table;
    @FXML
    private TableColumn<EMail, String> data;
    @FXML
    private TableColumn<EMail, String> mittente;
    @FXML
    private TableColumn<EMail, String> oggetto;

    //Preview mail
    @FXML
    private Pane panelDettaglioEmail;

    @FXML
    private TextArea valueTestoMail;
    @FXML
    private Label valueMittente;
    @FXML
    private Label valueDestinatari;
    @FXML
    private Label valueOggetto;

    //variabili locali del controller
    private EMail selectedEmail;

    private ModelLista mail;
    Stage primaryStage = null;

    private static DateTimeFormatter DATEFORMAT = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss");

    public void initModel(ModelLista model, Stage pm) {
        if (this.mail != null) {
            throw new IllegalStateException("Model può essere inizializzato una sola volta");
        }
        this.mail = model;
        this.primaryStage = pm;
        table.setItems(model.getmaillist());

        data.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<EMail, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<EMail, String> param) {
                return new ReadOnlyStringWrapper(param.getValue().time.format(DATEFORMAT));
            }
        });

        mittente.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<EMail, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<EMail, String> param) {
                return new ReadOnlyObjectWrapper<>(param.getValue().mittente);
            }
        });

        oggetto.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<EMail, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<EMail, String> param) {
                return new ReadOnlyStringWrapper(param.getValue().oggetto);
            }
        });


        // gestione click su riga
        table.setRowFactory(new Callback<TableView<EMail>, TableRow<EMail>>() {
            @Override
            public TableRow<EMail> call(TableView<EMail> param) {
                TableRow<EMail> row = new TableRow<>();
                row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (! row.isEmpty() && event.getButton()== MouseButton.PRIMARY) {

                            selectedEmail = row.getItem();
                            //System.out.println("selezionata riga "+selectedEmail.mittente);
                            panelDettaglioEmail.setVisible(true);
                            valueDestinatari.setText(selectedEmail.destinatario);
                            valueMittente.setText(selectedEmail.mittente);
                            valueOggetto.setText(selectedEmail.oggetto);
                            valueTestoMail.setText(selectedEmail.testo);
                        }
                    }
                });

                return row;
            }
        });
    }

    @FXML
    public void nuovaEmail(ActionEvent event) throws IOException {
        //carico la classe fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("modaleEmail.fxml"));

        loader.load();
        Parent root = loader.getRoot();
        Stage modal_dialog = new Stage(StageStyle.DECORATED);
        modal_dialog.initModality(Modality.WINDOW_MODAL);
        modal_dialog.initOwner(primaryStage);
        Scene scene = new Scene(root);
        modal_dialog.setTitle("Nuovo messaggio");

        ModaleEmailController mc = (ModaleEmailController) loader.getController();
        mc.initModel(this.mail, modal_dialog);
        modal_dialog.setScene(scene);
        modal_dialog.show();
    }

    @FXML
    public void eliminaMail(ActionEvent event) throws IOException {

        panelDettaglioEmail.setVisible(false);
        valueDestinatari.setText("");
        valueMittente.setText("");
        valueOggetto.setText("");
        valueTestoMail.setText("");

        mail.rimuoviMail(selectedEmail);

    }

}