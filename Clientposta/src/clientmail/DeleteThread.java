package clientmail;

import commons.EMail;
import commons.Utilities;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class DeleteThread extends Thread {
    private EMail mailDelete;
    private ClientModel model;
    public enum Selection {ARRIVED, SENT};

    private Selection selection = null;

    public DeleteThread (Selection s, ClientModel model, EMail mail){

        this.model=model;
        this.mailDelete=mail;
        this.selection = s;
        System.out.println("DeleteThread - rimuovo email "+mail.getId()+ " da "+selection);
    }

    public void run() {
        EMail mail;
        String host;
        Socket s;
        Boolean found=false;
        File f = null;

            try {
                System.out.println("DeleteThread - connessione al server...");
                host = InetAddress.getLocalHost().getHostName();
                s = new Socket(model.host, model.port);
                try {
                    OutputStream out = s.getOutputStream();
                    InputStream in = s.getInputStream();
                    ObjectOutputStream clientObjOut = new ObjectOutputStream(out);
                    ObjectInputStream clientObjIn = new ObjectInputStream(in);
                    Scanner clientIn = new Scanner(in);
                    PrintWriter clientPrint = new PrintWriter(out);
                    // comunico la cancellazione al server
                    clientObjOut.writeObject(model.getAccount());
                    if (clientIn.next().equals("Ready")) {
                        System.out.println("DeleteThread - connesso. Comunico la cancellazione...");
                        clientPrint.println("Delete");
                        clientObjOut.writeObject(mailDelete);
                        String res = clientIn.nextLine();
                        if(res != null && res.equals("Done")) {
                            switch (selection) {
                                case ARRIVED:
                                    removeFromListAndSaveFile(model.getMailArrived(), "./data/"+model.getCasella()+"_arrived.csv", mailDelete);
                                    break;
                                case SENT:
                                    removeFromListAndSaveFile(model.getMailSent(), "./data/"+model.getCasella()+"_sent.csv", mailDelete);
                                    break;
                                default:
                                    throw new RuntimeException("shouldn't be here");
                            }
                        }
                        else {
                            //TODO gestire eccezione (il server non ha cancellato la mail)
                        }
                    }

            } catch (Exception  e) {
                    e.printStackTrace();
                    //TODO gestire meglio le eccezioni
            }
        } catch(IOException e){
                e.printStackTrace();
        }
    }

    private static void removeFromListAndSaveFile (ObservableList<EMail> list, String filename, EMail selectedEmail) throws Exception {
        File f = new File(filename);
        synchronized (list) {
            list.remove(selectedEmail);
            Utilities.saveEmailCsvToFile(list, filename);
        }
    }
}
