package servermail;

import commons.FileHandler;
import commons.SystemLogger;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import commons.Account;

public class ServerModel {

    public final static String MAIL_SERVER = "mail.server@mymail.com";
    private static SystemLogger LOGGER = new SystemLogger(ServerModel.class);

   // variabili gestione connessione
    public ServerSocket s = null;
    public Socket listening = null;

    //Thread di connessione
    public Connessione connect;

    //Thread di salvataggioLog
    SaveLogDaemonThread daemonLog=null;

    //Lista di connessioni client attive
    List<ClientHandlerThread> prec = new ArrayList<>();

    //Property Numero di Thread attivi
    private StringProperty activeThread=new SimpleStringProperty("No Thread active");
    public StringProperty activeThreadProperty() {
        return this.activeThread;
    }
    public String getActiveThread() {
        return this.activeThreadProperty().get();
    }
    public  void setActiveThread (String d) {
        this.activeThreadProperty().set(d);
    }


    //gestione binding loglist_tableView
    public ObservableList<Log> logHistory = FXCollections.observableArrayList();
    public ObservableList<Log> getLog() {
        return logHistory;
    }

    Map<String, Account> accounts=new HashMap<>();
    Map<String, FileHandler> arrivedFileHandler=new HashMap<String, FileHandler>();
    Map<String, FileHandler> sentFileHandler=new HashMap<String, FileHandler>();



    //Generazione ID mail univoci in mutua esclusione
    private AtomicInteger countId;


    //metodi per avviare e terminare la connessione del server
    public void startconnect(){
        Connessione connect=new Connessione(this);
        connect.start();
    }

    public void endconnect()  throws  IOException {
        if(s != null && !s.isClosed()) {
            s.close();
        }
    }

    //costruttore ServerModel
    public ServerModel(){
        daemonLog=new SaveLogDaemonThread(logHistory,this);
        daemonLog.start();
    }

    // gestione connessione
    public class Connessione extends Thread {
        ServerModel model = null;

        public Connessione (ServerModel m) {
            super();
            this.model = m;
        }

        public void run() {
            LOGGER.info("Connecting");
            try {
                s = new ServerSocket(8089);
                while (true) {
                    LOGGER.info("waiting for connections");
                    listening = s.accept();
                    LOGGER.info("new connection accepted... creating clientHandler");
                    ClientHandlerThread h = new ClientHandlerThread(listening, model);
                    prec.add(h);
                    new ActiveThreadsUpdater(model).start();
                    h.start();


                }
            }catch (IOException e) {
                LOGGER.error("Interruzione del server ("+e.getMessage()+")");
            }

            finally {
                try {
                    LOGGER.info("disconnecting...");
                    if(listening != null)
                        listening.close();
                    LOGGER.info("disconnected");
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }
    }

    //metodo statico per convertire il timestamp in String
    public static String dateToString() {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate = dateFormat.format(date);
        return strDate;
    }



    //Caricamento elenco account all'avvio

    public void loadAccounts() throws FileNotFoundException, Exception {
        File f=new File("./data/accounts.csv");
        Scanner rr = new Scanner(f);
        String s = null;
        while( rr.hasNextLine()) {
            s = rr.nextLine();
            Scanner dr = new Scanner(s);
            dr.useDelimiter("\\s*;\\s*");
            Account acc = new Account(dr.next(), dr.next(), dr.next());
            accounts.put(acc.getEmail(), acc);
            arrivedFileHandler.put(acc.getEmail(), new FileHandler("./data/"+acc.getEmail()+"/"+acc.getEmail()+"_arrived.csv"));
            sentFileHandler.put(acc.getEmail(), new FileHandler("./data/"+acc.getEmail()+"/"+acc.getEmail()+"_sent.csv"));
            dr.close();
        }
        rr.close();
    }

    public void loadNextId()throws FileNotFoundException, Exception  {
        File f=new File("./data/generateId.csv");

        if(f.exists()){
            if(f.length()==0) {
                countId = new AtomicInteger();
            }
            else{
                Scanner rr = new Scanner(f);
                while( rr.hasNextLine()) {
                    countId=new AtomicInteger(Integer.parseInt(rr.nextLine()));
                }
                rr.close();
            }
        }

    }


    public void saveId()throws FileNotFoundException, Exception{
        PrintWriter saveId =new PrintWriter (new FileWriter("./data/generateId.csv"));
        saveId.println(countId.get());
        saveId.close();


    }


    public String nextId() {
        return ""+ countId.getAndIncrement();
    }
}









