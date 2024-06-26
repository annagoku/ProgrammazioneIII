package clientmail;

import commons.EMail;
import commons.SystemLogger;
import commons.Utilities;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

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
        LOGGER.log(prefixLog+" running..");
        if(demon) {
            while(true) {
                try {
                    Thread.sleep(90000);
                } catch (InterruptedException e) {
                }
                //L'istruzione seguente si inserisce quando l'aggiornamento della UI è indicata in un
                //thread diverso da quello principale di Java FX. Viene messo in coda e gestito alla fine da UI Java FX Thread
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
            LOGGER.log(prefixLog+"connection to "+model.host+":"+model.port);
            s = new Socket(model.host, model.port);
            try {
                OutputStream out = s.getOutputStream();
                ObjectOutputStream clientObjOut = new ObjectOutputStream(out);
                InputStream in = s.getInputStream();
                ObjectInputStream clientObjIn = new ObjectInputStream(in);
                Scanner clientIn = new Scanner(in);
                PrintWriter clientPrint = new PrintWriter(out, true);

                //Comunica il proprio account identificativo al Server
                clientObjOut.writeObject(model.getAccount());
                String serverAnswer = clientIn.nextLine();
                LOGGER.log(prefixLog+"Server says '"+serverAnswer+"'");

                if (serverAnswer.equals("Ready")) {
                    String timestamp = "";
                    if(!model.getMailArrived().isEmpty()){
                        timestamp=(model.getMailArrived().get(model.getMailArrived().size()-1)).getTime();

                    }
                    clientPrint.println("Receive "+timestamp);
                    serverAnswer = clientIn.nextLine();
                    LOGGER.log(prefixLog+"server answer -> "+serverAnswer);
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


                        //Aggiorna il file della posta arrivata
                        model.getFileArrived().addAll(list);

                        //Aggiorna la table view sulla base di un aggiornamento in accesso esclusivo dell' observable list
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
                                            // L'alert viene visualizzato solo se il receive è manuale
                                            // (da receive button)
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
                    LOGGER.log(prefixLog+"error on receiving. Server says "+serverAnswer);
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




