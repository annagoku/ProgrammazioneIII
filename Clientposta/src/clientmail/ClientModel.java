package clientmail;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import commons.EMail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ClientModel {
    private ObservableList<EMail> mailArrived= FXCollections.observableArrayList();
    private ObservableList<EMail> mailSent=FXCollections.observableArrayList();
    private String casella;
    public String host;
    public Socket socket;

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
    //Costruttore model
    public ClientModel (){

        this.loadMailArrived();
        this.loadMailSent();

    }


    public ObservableList<EMail> getMailArrived(){
        return mailArrived;
    }
    public ObservableList<EMail> getMailSent(){
        return mailSent;
    }

    public String getCasella() {
        return casella;
    }

    public void setCasella(String casella) {
        this.casella = casella;
    }

    //caricamento all'avvio di mail ricevute e inviate salvate in txt
    public void loadMailArrived()  {
        try{
            File f=new File("./data/diana_rossi_arrived.csv");
            if (f.length()!=0) {
                Scanner mailIn = new Scanner(f); //parametrizzare il nome del file
                while (mailIn.hasNextLine()) {
                    String tmp = mailIn.nextLine();
                    Scanner l = new Scanner(tmp).useDelimiter("\\s*;\\s*");
                    EMail mailA = new EMail(l.next(), l.next(), l.next(), l.next(), l.next(), l.next());
                    synchronized (mailArrived) {
                        mailArrived.add(mailA);
                    }
                    l.close();
                    mailIn.close();
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void loadMailSent() {
        try{
            File f=new File("./data/diana_rossi_sent.csv");
            if(f.length()!=0) {
                Scanner mailOut = new Scanner(f); //parametrizzare il nome del file
                while (mailOut.hasNextLine()) {
                    String tmp = mailOut.nextLine();
                    Scanner l = new Scanner(tmp).useDelimiter("\\s*;\\s*");
                    EMail mailS = new EMail(l.next(), l.next(), l.next(), l.next(), l.next(), l.next());
                    synchronized (mailSent) {
                        mailSent.add(mailS);
                    }
                    l.close();
                    mailOut.close();

                }
            }
        }catch (IOException  e){
            e.printStackTrace();
        }
    }



    public synchronized boolean removeArrivedMail (EMail mail){
        return getMailArrived().remove(mail);
    }
    public synchronized boolean removeSentMail (EMail mail){
        return getMailSent().remove(mail);
    }

    }



