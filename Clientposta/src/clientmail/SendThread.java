package clientmail;

import commons.EMail;
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

    private String prefixLog = getClass().getName();

    public SendThread(ClientModel model, EMail m) {
        this.model = model;
        this.mailSend = m;

    }


    public void run() {
        Socket s;

        try {
            System.out.println(prefixLog+": Connecting to "+model.host+":"+  model.port);
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
                System.out.println(prefixLog+": Server says" +serverAnswer);

                if (serverAnswer.equals("Ready")) {

                    System.out.println(prefixLog+": sending email...");
                    clientPrint.println("Send");
                    clientObjOut.writeObject(mailSend);

                    String res = clientIn.nextLine();
                    clientPrint.println("QUIT");

                    Matcher m = patternDone.matcher(res);
                    System.out.println(prefixLog+": Server ansewer ->" +res);

                    if(m.matches()) {
                        String mailID = m.group(1);
                        mailSend.setId(mailID);
                        synchronized (model.getMailSent()) {
                            model.getMailSent().add(mailSend);
                            Utilities.saveEmailCsvToFile(model.getMailSent(), "./data/"+model.getCasella()+"_sent.csv");
                        }

                    }
                    else {
                        Platform.runLater(
                                () -> {
                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                    alert.setContentText("Cannot send mail: "+res);
                                    alert.show();
                                }
                        );
                    }

                }
                s.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


