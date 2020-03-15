package servermail;

import commons.Account;
import commons.FileHandler;
import commons.SystemLogger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerModel {

    public final static String MAIL_SERVER = "mail.server@mymail.com";
    private static SystemLogger LOGGER = new SystemLogger(ServerModel.class);
    private int lastPos = 0;

   // variabili gestione connessione
    public ServerSocket s = null;
    public Socket listening = null;

    //Thread di salvataggioLog
    SaveLogDaemonThread daemonLog=null;

    //Dichiarazione di un pool di Thread per la gestione delle richieste client
    ExecutorService exec = Executors.newFixedThreadPool(100);

    //Dichiarazione di una Observable list per i Log
    public ObservableList<Log> logHistory = FXCollections.observableArrayList();
    public ObservableList<Log> getLog() {
        return logHistory;
    }

    //Hash maps per mantenere la corrispondenza tra indirizzo mail, oggetto account e oggetti fileHandler
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

    //Costruttore ServerModel
    public ServerModel(){
        daemonLog=new SaveLogDaemonThread(this);
        daemonLog.start();
    }

    // Thread per la connessione
    public class Connessione extends Thread {
        ServerModel model = null;

        public Connessione (ServerModel m) {
            super();
            this.model = m;
        }

        public void run() {
            LOGGER.log("Connecting");
            try {
                s = new ServerSocket(8089);
                while (true) {
                    LOGGER.log("waiting for connections");
                    listening = s.accept();
                    LOGGER.log("new connection accepted... creating clientHandler");
                    ClientHandlerThread h = new ClientHandlerThread(listening, model);
                    exec.execute(h);
                }
            }catch (IOException e) {
                LOGGER.log("Interruzione del server ("+e.getMessage()+")");
            }
            finally {
                try {
                    LOGGER.log("disconnecting...");
                    if(listening != null)
                        listening.close();
                    LOGGER.log("disconnected");
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }
    }

    //Aggiornamento observable list Log in mutua esclusione
    public synchronized void addLog(Log l) {
        this.logHistory.add(l);
    }

    //Aggiornamento file Log in mutua esclusione
    public synchronized void saveLogs() throws IOException {
            PrintWriter saveLog = new PrintWriter(new FileWriter("./data/Log.csv", true), true);

            LOGGER.log("saving logs");
            int i = 0;
            for (i=lastPos; i<logHistory.size(); i++) {
                saveLog.println(logHistory.get(i).toString());
            }
            lastPos=i;
    }

    //metodo statico per convertire il timestamp in String
    public static String dateToString() {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate = dateFormat.format(date);
        return strDate;
    }


    //Caricamento elenco account all'avvio
    public void loadAccounts() throws Exception {
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

    //Caricamento log da file all'avvio
    public void loadLogs() throws Exception {
        File f=new File("./data/Log.csv");
        Scanner rr = new Scanner(f);
        while( rr.hasNextLine()) {
            logHistory.add(Log.parseLog(rr.nextLine()));
        }
        rr.close();
    }

    //Caricamento del progressivo mail id da file
    public void loadNextId()throws Exception  {
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

    //Salvataggio in sovrescrittura del progressivo ID in chiusura
    public void saveId()throws Exception{
        PrintWriter saveId =new PrintWriter (new FileWriter("./data/generateId.csv"));
        saveId.println(countId.get());
        saveId.close();
    }

    public String nextId() {
        return ""+ countId.getAndIncrement();
    }
}









