package clientmail;

import commons.EMail;
import commons.SystemLogger;
import commons.Utilities;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class ReceiveThread extends Thread {
    private ClientModel model;
    private boolean demon;
    private boolean automatic= false;
    private static SystemLogger LOGGER = new SystemLogger(ReceiveThread.class);
    private String prefixLog = "";

    public ReceiveThread (ClientModel model, boolean demon, boolean automatic){
        this.model=model;
        this.demon = demon;
        this.automatic = automatic;
        prefixLog = (automatic? "auto " : "manual ");
        if(demon)
            prefixLog+=" (demon) ";
    }

    public void run() {
        LOGGER.info(prefixLog+" running..");
        if(demon) {
            while(true) {
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                }
                Platform.runLater(() -> {
                    model.setClientOperation("Loading mail from server....");
                });
                new ReceiveThread(model, false, true).start();
            }
        }
        else {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            receiveLogic();
        }
    }


    private void receiveLogic() {


        Socket s = null;
        try {
            LOGGER.debug(prefixLog+"connection to "+model.host+":"+model.port);
            s = new Socket(model.host, model.port);
            try {
                OutputStream out = s.getOutputStream();
                ObjectOutputStream clientObjOut = new ObjectOutputStream(out);
                InputStream in = s.getInputStream();
                ObjectInputStream clientObjIn = new ObjectInputStream(in);
                Scanner clientIn = new Scanner(in);
                PrintWriter clientPrint = new PrintWriter(out, true);

                clientObjOut.writeObject(model.getAccount());
                String serverAnswer = clientIn.nextLine();
                LOGGER.info(prefixLog+"Server says '"+serverAnswer+"'");

                if (serverAnswer.equals("Ready")) {
                    String timestamp = "";
                    if(!model.getMailArrived().isEmpty()){
                        timestamp=(model.getMailArrived().get(model.getMailArrived().size()-1)).getTime();

                    }
                    clientPrint.println("Receive "+timestamp);
                    serverAnswer = clientIn.nextLine();
                    LOGGER.info(prefixLog+"server answer -> "+serverAnswer);
                    if(serverAnswer.equals("Done")) {
                        List<String> stringList = null;

                        try {
                            stringList =(List<String>)clientObjIn.readObject();
                            clientPrint.println("Quit OK");
                        }
                        catch (Exception e) {
                            clientPrint.println("Quit KO");
                            throw e;
                        }



                        List<EMail> list = Utilities.readNewEmailsFromStringList(stringList);


                        //aggiorna il file
                        model.getFileArrived().addAll(list);

                        Platform.runLater(
                                () -> {
                                    try {
                                        model.sem.acquire();
                                        model.getMailArrived().addAll(list);
                                        if(list.size()>0) {
                                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                            alert.setHeaderText(list.size() + " new mail"+(list.size()>1 ? "s!":"!"));
                                            alert.show();
                                        }
                                        else if(!automatic) {
                                            // We show alert for no mail only if the thread is started manually
                                            // (from receive button)
                                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                            alert.setHeaderText("No new mail");
                                            alert.show();

                                        }
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    finally {
                                        model.sem.release();
                                    }
                                }
                        );
                    }
                }
                else {
                    LOGGER.error(prefixLog+"error on receiving. Server says "+serverAnswer);
                }


            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setHeaderText("Error on receive mail");
                        alert.setContentText(e.getMessage());
                        alert.show();

                });
            }
            finally {
                if(s != null)
                    s.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> {

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("Cannot connect to server");
                    alert.setContentText(e.getMessage());
                    alert.show();

            });

        }
        finally {
            Platform.runLater(() -> {

                    model.setClientOperation("");                }
            );
        }


    }
}




