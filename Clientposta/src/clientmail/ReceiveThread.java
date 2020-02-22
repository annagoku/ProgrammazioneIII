package clientmail;

import commons.EMail;
import commons.Utilities;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class ReceiveThread extends Thread {
    private ClientModel model;
    private boolean demon;

    private String prefixLog = getClass().getName();

    public ReceiveThread (ClientModel model, boolean demon){
        this.model=model;
        this.demon = demon;
        if(demon)
            prefixLog+="(demon)";
    }

    public void run() {
        if(demon) {
            while(true) {
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                }
                receiveLogic();
            }
        }
        else {
            receiveLogic();
        }
    }


    private void receiveLogic() {

        synchronized (model.lockReceive) {
            Platform.runLater(
                    () -> {
                        model.setCountNewMail("Loading mail from server....");
                        try{
                            sleep(4000);
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
                    }
            );

            Socket s = null;
            try {
                s = new Socket(model.host, model.port);
                try {
                    OutputStream out = s.getOutputStream();
                    InputStream in = s.getInputStream();
                    ObjectOutputStream clientObjOut = new ObjectOutputStream(out);
                    ObjectInputStream clientObjIn = new ObjectInputStream(in);
                    Scanner clientIn = new Scanner(in);
                    PrintWriter clientPrint = new PrintWriter(out, true);

                    clientObjOut.writeObject(model.getAccount());
                    String serverAnswer = clientIn.nextLine();
                    System.out.println(prefixLog+": Server says "+serverAnswer);

                    if (serverAnswer.equals("Ready")) {
                        String timestamp = "";
                        if(!model.getMailArrived().isEmpty()){
                            timestamp=(model.getMailArrived().get(model.getMailArrived().size()-1)).getTime();

                        }
                        clientPrint.println("Receive "+timestamp);
                        serverAnswer = clientIn.nextLine();
                        System.out.println(prefixLog+": server answer -> "+serverAnswer);
                        if(serverAnswer.equals("Done")) {
                            synchronized (model.getMailArrived()) {
                                List<EMail> list = (List<EMail>) clientObjIn.readObject();
                                if(list.size()!=0){
                                   Platform.runLater(
                                           () -> {
                                                model.setCountNewMail(Integer.toString(list.size()));
                                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                                alert.setContentText("Ci sono "+list.size()+" nuove mail");
                                                alert.show();
                                           }
                                    );


                                }else {
                                    Platform.runLater(
                                            () -> {
                                                model.setCountNewMail("No new mail");

                                          }
                                    );
                                }
                                model.getMailArrived().addAll(list);

                                Utilities.saveEmailCsvToFile(model.getMailArrived(), "./data/"+model.getCasella()+"_arrived.csv");
                            }
                        }
                        else {
                            System.out.println(prefixLog+": error on receiving. Server says "+serverAnswer);
                        }
                        clientPrint.println("Quit");

                        s.close();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}




