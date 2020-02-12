package servermail;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import commons.Account;

public class ServerModel {

   // variabili gestione connessione
    public ServerSocket s = null;
    public Socket listening = null;

    //Thread di connessione
    public Connessione connect;

    //Thread di salvataggioLog
    SaveLogDaemonThread daemonLog=null;

    //Lista di connessioni client attive
    List<ClientHandlerThread> prec = new ArrayList<>();


    //gestione binding loglist_tableView
    public ObservableList<Log> logHistory = FXCollections.observableArrayList();
    public ObservableList<Log> getLog() {
        return logHistory;
    }

    ArrayList<Account> accounts=new ArrayList<>();

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
            System.out.println("Provo a connettere");
            try {
                s = new ServerSocket(8089);
                while (true) {
                    System.out.println("Rimango in attesa");
                    listening = s.accept();
                    System.out.println("accettata nuova connessione... creo clientHandler");
                    ClientHandlerThread h = new ClientHandlerThread(listening, model);
                    prec.add(h);
                    h.start();


                }
            }catch (IOException e) {
                System.out.println("Interruzione del server ("+e.getMessage()+")");
            }

            finally {
                try {
                    System.out.println("sto disconnettendo");
                    if(listening != null)
                        listening.close();
                    System.out.println("disconnesso");
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
            accounts.add(new Account(dr.next(), dr.next(), dr.next()));
            dr.close();
        }
        rr.close();
        //System.out.println(accounts.toString());
    }
}









