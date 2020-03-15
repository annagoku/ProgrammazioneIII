package clientmail;

import commons.Account;
import commons.EMail;
import commons.FileHandler;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Properties;
import java.util.concurrent.Semaphore;

public class ClientModel {
    private Properties props = null;
    //Dichiarazione Observable List per le mail in arrivo ed inviate
    private ObservableList<EMail> mailArrived= FXCollections.observableArrayList();
    private ObservableList<EMail> mailSent=FXCollections.observableArrayList();
    private FileHandler fileSent;
    private FileHandler fileArrived;
    private Account casella;
    public String host;
    public int port;
    // Strumento di sincronizzazione per l'accesso esclusivo alle Observable List
    public Semaphore sem= new Semaphore(1);


    //Property label client action
    private StringProperty clientOperation=new SimpleStringProperty("");
    public StringProperty clientOperationProperty() {
        return this.clientOperation;
    }
    public String getClientOperation() {
        return this.clientOperationProperty().get();
    }
    public  void setClientOperation (String d) {
        this.clientOperationProperty().set(d);
    }


    //Costruttore model
    public ClientModel (Properties props){
        //carico le properties e inizializzo
        this.props = props;
        //leggo l'account
        this.casella = new Account(props.getProperty("account.name"),props.getProperty("account.surname"),props.getProperty("account.email"));
        this.host = props.getProperty("server.host");
        this.port = Integer.valueOf(props.getProperty("server.port"));
        this.fileArrived = new FileHandler("./data/"+casella.getEmail()+"/"+casella.getEmail()+"_arrived.csv");
        this.fileSent = new FileHandler("./data/"+casella.getEmail()+"/"+casella.getEmail()+"_sent.csv");

        this.loadMailArrived();
        this.loadMailSent();

        //Thread Daemon per la ricezione periodica automatica di mail
        new ReceiveThread(this, true, true).start();
    }


    public FileHandler getFileSent() {
        return fileSent;
    }

    public FileHandler getFileArrived() {
        return fileArrived;
    }

    public ObservableList<EMail> getMailArrived(){
        return mailArrived;
    }
    public ObservableList<EMail> getMailSent(){
        return mailSent;
    }

    public Account getAccount() {
        return casella;
    }
    public String getCasella() {
        return casella.getEmail();
    }

    //Caricamento all'avvio di mail ricevute e inviate  salvate in csv nelle rispettive liste
    public void loadMailArrived()  {
        try{
            mailArrived.addAll(fileArrived.readList());

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void loadMailSent() {
        try{
            mailSent.addAll(fileSent.readList());

        }catch (Exception  e){
            e.printStackTrace();
        }
    }

}



