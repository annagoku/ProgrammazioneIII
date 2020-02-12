package clientmail;

import commons.EMail;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.UnknownHostException;
import java.util.Scanner;

public class ReceiveDaemonThread extends Thread {

    private String timestamp;
    private String accountMail;
    private ClientModel model;

    public ReceiveDaemonThread (ClientModel model, String time, String a){
        this.model=model;
        this.timestamp=time;
        this.accountMail=a;
        setDaemon(true);
    }

    public void run() {
        EMail mail;
        String host;
        Socket s;

       while(true) {
           try {
               Thread.sleep(20000);
           } catch (InterruptedException e) {
           }
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
                   PrintWriter filePrint = new PrintWriter(new FileWriter("./data/diana_rossi_arrived.csv", true));

                   clientPrint.println("accountMail");
                   if (clientIn.next().equals("Ready")) {
                       clientPrint.println("Receive");
                       clientPrint.println(timestamp);

                       String ctr = "ongoing";
                       while (ctr.equals("ongoing")) {
                           mail = (EMail) clientObjIn.readObject();
                           model.getMailArrived().add(mail);
                           filePrint.write(mail.toString());
                           ctr = clientIn.nextLine();
                       }
                       s.close();
                   }

               } catch (IOException | ClassNotFoundException e) {
               }
           } catch (IOException e) {
           }
       }

    }
}
