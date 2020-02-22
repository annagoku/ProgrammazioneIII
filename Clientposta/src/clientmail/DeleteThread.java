package clientmail;

import commons.EMail;
import commons.Utilities;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class DeleteThread extends Thread {
    private EMail mailDelete;
    private ClientModel model;
    public enum Selection {ARRIVED, SENT};

    private Selection selection = null;
    private String prefixLog = getClass().getName();

    public DeleteThread (Selection s, ClientModel model, EMail mail){

        this.model=model;
        this.mailDelete=mail;
        this.selection = s;
        System.out.println(prefixLog+": deleting email "+mail.getId()+ " from "+selection);
    }

    public void run() {
        EMail mail;
        Socket s;
        Boolean found=false;
        File f = null;

            try {
                System.out.println(prefixLog+": connecting to server...");

                s = new Socket(model.host, model.port);
                try {
                    OutputStream out = s.getOutputStream();
                    InputStream in = s.getInputStream();
                    ObjectOutputStream clientObjOut = new ObjectOutputStream(out);
                    ObjectInputStream clientObjIn = new ObjectInputStream(in);
                    Scanner clientIn = new Scanner(in);
                    PrintWriter clientPrint = new PrintWriter(out, true);
                    // comunico la cancellazione al server
                    clientObjOut.writeObject(model.getAccount());

                    String serverAnswer = clientIn.nextLine();
                    System.out.println(prefixLog+": Server says" +serverAnswer);

                    if (serverAnswer.equals("Ready")) {
                        System.out.println(prefixLog+": connected. Sending delete command..");
                        clientPrint.println("Delete "+selection.toString());
                        clientObjOut.writeObject(mailDelete);
                        String res = clientIn.nextLine();
                        clientPrint.println("QUIT");

                        if(res != null && res.equals("Done")) {

                            switch (selection) {
                                case ARRIVED:
                                    //Utilities.removeFromListAndSaveFile(model.getMailArrived(), "./data/"+model.getCasella()+"_arrived.csv", mailDelete);
                                    Platform.runLater(() -> {
                                        //model.getMailArrived().remove(mailDelete);
                                        try {
                                            Utilities.removeFromListAndSaveFile(model.getMailArrived(), "./data/"+model.getCasella()+"_arrived.csv", mailDelete);
                                            //Utilities.saveEmailCsvToFile(model.getMailArrived(), "./data/"+model.getCasella()+"_arrived.csv");
                                        } catch (Exception e) {
                                            Alert alert = new Alert(Alert.AlertType.ERROR);
                                            alert.setContentText("Cannot update file mail arrived: "+e.getMessage());
                                            alert.show();
                                        }

                                    });
                                    break;
                                case SENT:
                                    Utilities.removeFromListAndSaveFile(model.getMailSent(), "./data/"+model.getCasella()+"_sent.csv", mailDelete);
                                    break;
                                default:
                                    throw new RuntimeException("shouldn't be here");
                            }
                        }
                        else {
                            Platform.runLater(
                                    () -> {
                                        Alert alert = new Alert(Alert.AlertType.ERROR);
                                        alert.setContentText("Cannot delete mail: "+res);
                                        alert.show();
                                    }
                            );
                        }
                    }
                    s.close();
            } catch (Exception  e) {
                    e.printStackTrace();
                    //TODO gestire meglio le eccezioni
            }
        } catch(IOException e){
                e.printStackTrace();
        }
    }


}
