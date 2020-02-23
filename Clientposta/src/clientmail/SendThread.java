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

        try {
            LOGGER.debug("connection to "+model.host+":"+model.port);
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
                LOGGER.debug("Server says" +serverAnswer);

                if (serverAnswer.equals("Ready")) {

                    LOGGER.debug("sending email...");
                    clientPrint.println("Send");
                    clientObjOut.writeObject(mailSend);

                    String res = clientIn.nextLine();
                    clientPrint.println("QUIT");

                    Matcher m = patternDone.matcher(res);
                    LOGGER.debug("Server ansewer ->" +res);

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
                                    synchronized (model.lock) {
                                        Alert alert = new Alert(Alert.AlertType.ERROR);
                                        alert.setHeaderText("Cannot send mail");
                                        alert.setContentText(res);
                                        alert.show();
                                    }
                                }
                        );


                    }

                }
                s.close();
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(
                        () -> {
                            synchronized (model.lock) {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setHeaderText("Cannot send mail");
                                alert.setContentText(e.getMessage());
                                alert.show();
                            }
                        }
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(
                    () -> {
                        synchronized (model.lock) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setHeaderText("Cannot send mail");
                            alert.setContentText(e.getMessage());
                            alert.show();
                        }
                    }
            );
        }
    }
}


