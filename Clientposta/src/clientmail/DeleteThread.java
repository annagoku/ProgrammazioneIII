package clientmail;

import commons.EMail;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class DeleteThread extends Thread {
    private EMail mailDelete;
    private String accountMail;
    private ClientModel model;

    public DeleteThread (ClientModel model, String accountMail, EMail mail){
        this.model=model;
        this.accountMail=accountMail;
        this.mailDelete=mail;
    }

    public void run() {
        EMail mail;
        String host;
        Socket s;
        Boolean found=false;

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
                    File fA = new File("C:\\Users\\annag\\Desktop\\UNITO\\programmazioneIII\\ProgettoProgIII\\Clientposta\\data\\dianarossiarrived.csv");
                    File fS = new File("C:\\Users\\annag\\Desktop\\UNITO\\programmazioneIII\\ProgettoProgIII\\Clientposta\\data\\dianarossisent.csv");
                    Scanner fileArrived = new Scanner(fA);
                    Scanner fileSent = new Scanner(fS);
                    PrintWriter filePrintArrived = new PrintWriter(fA);
                    PrintWriter filePrintSent = new PrintWriter(fS);

                    clientPrint.println("accountMail");
                    if (clientIn.next().equals("Ready")) {
                        clientPrint.println("Delete");
                        clientObjOut.writeObject(mailDelete);

                        if (model.getMailArrived().contains(mailDelete)) {
                            synchronized (model.getMailArrived()){
                                model.getMailArrived().remove(mailDelete);
                                while (fileArrived.hasNext()&& !found) {
                                    String line = fileArrived.nextLine();
                                    Scanner tpm = new Scanner(line);
                                    if (tpm.next().equals(mailDelete.getId())) {
                                        found=true;
                                        filePrintArrived.println("" + System.getProperty("line.separator"));
                                    }
                                }
                            }
                        }else if (model.getMailSent().contains(mailDelete)) {
                            synchronized (model.getMailSent()) {
                                model.getMailSent().remove(mailDelete);
                                while (fileSent.hasNext() && !found) {
                                    String line = fileSent.nextLine();
                                    Scanner tpm = new Scanner(line);
                                    if (tpm.next().equals(mailDelete.getId())) {
                                        filePrintSent.println("" + System.getProperty("line.separator"));
                                    }
                                }
                            }
                        }
                    }
                    s.close();
            } catch (IOException  e) {
            }
        } catch(IOException e){
        }
    }

}
