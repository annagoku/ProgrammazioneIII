package clientmail;

import commons.EMail;
import commons.SystemLogger;
import commons.Utilities;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.*;
import java.net.InetAddress;
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
            sleep (3000);
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        try {
            LOGGER.debug("connection to "+model.host+":"+model.port);
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
                LOGGER.debug("Server says" +serverAnswer);

                if (serverAnswer.equals("Ready")) {

                    LOGGER.debug("sending email... "+mailSend.toString());
                    clientPrint.println("Send");
                    clientPrint.println(mailSend.toString());

                    String res = clientIn.nextLine();
                    clientPrint.println("QUIT");

                    Matcher m = patternDone.matcher(res);
                    LOGGER.debug("Server answer ->" +res);

                    if(m.matches()) {
                        String mailID = m.group(1);
                        mailSend.setId(mailID);
                        model.getFileSent().add(mailSend);

                        model.sem.acquire();
                        model.getMailSent().add(mailSend);

                        model.sem.release();
                    }
                    else {
                        Platform.runLater(
                                () -> {

                                        Alert alert = new Alert(Alert.AlertType.ERROR);
                                        alert.setHeaderText("Cannot send mail");
                                        alert.setContentText(res);
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


