package clientmail;

import commons.DateUtils;
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
    private String timestamp;
    public String host;
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
    //Costruttore model
    public ClientModel (){
        this.loadMailArrived();
        this.loadMailSent();
        if(mailArrived.isEmpty()){
            timestamp= DateUtils.dateString();
            new ReceiveDaemonThread(this, timestamp, casella).start();
        }else{
            timestamp=(mailArrived.get(mailArrived.size()-1)).getTime();
            new ReceiveDaemonThread(this, timestamp, casella).start();
        }

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
            File f=new File("./data/dianarossiarrived.csv");
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
                }
            mailIn.close();
            }
        }catch (Exception e){
            e.printStackTrace();
            e.getMessage();
        }
    }

    public void loadMailSent() {
        try{
            File f=new File("./data/dianarossisent.csv");
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
                }
                mailOut.close();
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



