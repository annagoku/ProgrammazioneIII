package clientmail;

import commons.EMail;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class SendThread extends Thread {
    private EMail mailSend;
    private ClientModel model;


    public SendThread(ClientModel model, EMail m) {
        this.model = model;
        this.mailSend = m;

    }


    public void run() {
        String host;
        Socket s;

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

                clientPrint.println(model.getCasella());
                if (clientIn.next().equals("Ready")) {
                    clientPrint.println("Send");
                    clientObjOut.writeObject(mailSend);

                    String res = clientIn.nextLine();
                    if(res != null && res.equals("Done")) {
                        synchronized (model.getMailSent()) {
                            model.getMailSent().add(mailSend);
                            ClientModel.saveCsvToFile(model.getMailSent(), "./data/"+model.getCasella()+"_sent.csv");
                        }

                    }
                    else {
                        //TODO gestire eccezione (il server non ha mandato la mail)

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


