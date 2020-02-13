package clientmail;

import commons.EMail;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class SendThread extends Thread {
    private EMail mailSend;
    private String accountMail;
    private ClientModel model;


    public SendThread(ClientModel model, EMail m, String a) {
        this.model = model;
        this.mailSend = m;
        this.accountMail = a;
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
                PrintWriter filePrint = new PrintWriter(new FileWriter("C:\\Users\\annag\\Desktop\\UNITO\\programmazioneIII\\ProgettoProgIII\\Clientposta\\data\\dianarossisent.csv", true));

                clientPrint.println("accountMail");
                if (clientIn.next().equals("Ready")) {
                    clientPrint.println("Send");
                    clientObjOut.writeObject(mailSend);
                    synchronized (model.getMailSent()) {
                        model.getMailSent().add(mailSend);
                    }
                    filePrint.println(mailSend.toString());
                }
                s.close();
            } catch (IOException e) {
            }
        } catch (IOException e) {
        }
    }
}


