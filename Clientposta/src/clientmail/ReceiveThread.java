package clientmail;

import commons.EMail;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class ReceiveThread extends Thread {
    private String timestamp;
    private String accountMail;
    private ClientModel model;


    public ReceiveThread (ClientModel model, String time){
        this.model=model;
        this.timestamp=time;
    }

    public void run() {
        EMail mail;
        String host;
        Socket s;
            synchronized (model.lockReceive) {
                try {
                    host = InetAddress.getLocalHost().getHostName();
                    s = new Socket("host", 8089);
                    try {
                        OutputStream out = s.getOutputStream();
                        InputStream in = s.getInputStream();
                        ObjectOutputStream clientObjOut = new ObjectOutputStream(out);
                        ObjectInputStream clientObjIn = new ObjectInputStream(in);
                        Scanner clientIn = new Scanner(in);
                        PrintWriter clientPrint = new PrintWriter(out);
                        PrintWriter filePrint = new PrintWriter(new FileWriter(model.getCasella()+"_arrived.csv", true));

                        clientPrint.println(model.getCasella());

                        if (clientIn.next().equals("Ready")) {
                            clientPrint.println("Receive");
                            clientPrint.println(timestamp);

                            String ctr = "ongoing";
                            synchronized (model.getMailArrived()) {
                                while (ctr.equals("ongoing")) {
                                    mail = (EMail) clientObjIn.readObject();
                                    model.getMailArrived().add(mail);
                                    filePrint.println(mail.toString());
                                    ctr = clientIn.nextLine();
                                }
                                ClientModel.saveCsvToFile(model.getMailArrived(), "./data/"+model.getCasella()+"_arrived.csv");
                            }
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




