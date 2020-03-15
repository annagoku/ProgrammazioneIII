package clientmail;

import commons.EMail;
import commons.SystemLogger;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SendThread extends Thread {
    private EMail mailSend;
    private ClientModel model;
    static Pattern patternDone = Pattern.compile("Done\\s+(.*)");

    private static SystemLogger LOGGER = new SystemLogger(SendThread.class);

    public SendThread(ClientModel model, EMail m) {
        this.model = model;
        this.mailSend = m;

    }


    public void run() {
        Socket s;
        try{
            sleep (2000);
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        try {
            LOGGER.log("connection to "+model.host+":"+model.port);
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
                LOGGER.log("Server says '" +serverAnswer+"'");

                if (serverAnswer.equals("Ready")) {

                    LOGGER.log("sending email... "+mailSend.toString());
                    clientPrint.println("Send");
                    clientPrint.println(mailSend.toString());

                    String res = null;

                    try {
                        res = clientIn.nextLine();
                        clientPrint.println("QUIT OK");

                    }catch (Exception e) {
                        clientPrint.println("QUIT KO");
                        throw e;
                    }

                    final String messageFromServer = res;

                    Matcher m = patternDone.matcher(res);
                    LOGGER.log("Server answer ->" +res);

                    if(m.matches()) {
                        //Aggiunge alla mail l'id fornito dal server
                        String mailID = m.group(1);
                        mailSend.setId(mailID);
                        //Aggiorna il file della posta inviata
                        model.getFileSent().add(mailSend);
                        //Aggiorna la table view sulla base di un aggiornamento in accesso esclusivo dell' observable list
                        model.sem.acquire();
                        model.getMailSent().add(mailSend);
                    }
                    else {
                        Platform.runLater(
                                () -> {

                                        Alert alert = new Alert(Alert.AlertType.ERROR);
                                        alert.setHeaderText("Cannot send mail");
                                        alert.setContentText(messageFromServer);
                                        alert.show();
                                    }
                        );
                    }
                }
                s.close();
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(
                        () -> {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setHeaderText("Cannot send mail");
                                alert.setContentText(e.getMessage());
                                alert.show();
                            }
                );
            }
            finally{
                model.sem.release();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(
                    () -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setHeaderText("Cannot send mail");
                            alert.setContentText(e.getMessage());
                            alert.show();
                        }
            );
        }
        finally {
            Platform.runLater(
                    () -> {
                            model.setClientOperation("");
                        }
            );
        }
    }
}


