package clientmail;

import commons.Account;
import commons.Utilities;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import commons.EMail;

import java.io.*;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;

public class ClientModel {
    private Properties props = null;
    private ObservableList<EMail> mailArrived= FXCollections.observableArrayList();
    private ObservableList<EMail> mailSent=FXCollections.observableArrayList();
    private Account casella;
    private String timestamp;
    public String host;
    public int port;
    public int count; // contatore nuove mail


    public Socket socket;
    public final Object lockReceive = new Object();

    //Property label connection
    private StringProperty connection=new SimpleStringProperty();
    public StringProperty connectionProperty() {
        return this.connection;
    }
    public String getConnection() {
        return this.connectionProperty().get();
    }
    public  void setConnection (String d) {
        this.connectionProperty().set(d);
    }

    //Property label contatore nuove mail
    private StringProperty countNewMail=new SimpleStringProperty("no new mail");
    public StringProperty countNewMailProperty() {
        return this.countNewMail;
    }
    public String getCountNewMail() {
        return this.countNewMailProperty().get();
    }
    public  void setCountNewMail (String d) {
        this.countNewMailProperty().set(d);
    }


    //Costruttore model
    public ClientModel (Properties props){
        //carico le properties e inizializzo
        this.props = props;
        //leggo l'account
        setCountNewMail("No new mail");
        this.casella = new Account(props.getProperty("account.name"),props.getProperty("account.surname"),props.getProperty("account.email"));
        this.host = props.getProperty("server.host");
        this.port = Integer.valueOf(props.getProperty("server.port"));

        this.loadMailArrived();
        this.loadMailSent();

        new ReceiveThread(this, true).start();


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

    //caricamento all'avvio di mail ricevute e inviate salvate in txt
    public void loadMailArrived()  {
        try{

            synchronized (mailArrived) {
                mailArrived.addAll(Utilities.loadMailFromCSV("./data/"+casella.getEmail()+"_arrived.csv"));
            }

        }catch (Exception e){
            e.printStackTrace();

        }
    }

    public void loadMailSent() {
        try{

            synchronized (mailSent) {
                mailSent.addAll(Utilities.loadMailFromCSV("./data/"+casella.getEmail()+"_sent.csv"));
            }

        }catch (Exception  e){
            e.printStackTrace();
        }
    }

}



