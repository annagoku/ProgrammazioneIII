package servermail;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import commons.Account;
import commons.EMail;
import commons.Utilities;

/*
 Client Handler
 */
public class ClientHandlerThread extends Thread{

    // i comandi da dare al server sono fatti da una parola + uno o piu' spazi + un eventuale parametro
    // questo pattern controlla questo formato e raggruppa in modo che il primo gruppo sia il comando
    // e il secondo gruppo sia l'eventuale parametro
    private static Pattern pattern = Pattern.compile("(\\w+)\\s*(.*)");

    private String ip;
    private Socket listening = null;
    private ServerModel model;
    private InputStream in=null;
    private OutputStream out=null;
    private Scanner inputLine=null;
    private PrintWriter serverAnswer=null;
    private ObjectInputStream serverObjIn=null;
    private ObjectOutputStream serverObjOut=null;
    private String command;
    private Account client;


    public ClientHandlerThread(Socket socket, ServerModel model) {
        this.listening = socket;
        this.model = model;
        this.ip = listening.getInetAddress().getHostAddress();


    }

    @Override
    public void run() {
        String email = "";
        try {
            System.out.println(Thread.currentThread().getName()+" running for client "+ip);
            Log connectLog= new Log ("Connection from client", email,ServerModel.dateToString(), ip  );
            model.logHistory.add(connectLog);

            in = listening.getInputStream();
            out = listening.getOutputStream();
            inputLine = new Scanner(in);
            serverAnswer = new PrintWriter(out, true);

            serverObjOut = new ObjectOutputStream(out);
            serverObjIn = new ObjectInputStream(in);

            System.out.println(Thread.currentThread().getName()+" ("+ip+") waiting for account: ");
            client= (Account)serverObjIn.readObject();
            email = client.getEmail();
            System.out.println(Thread.currentThread().getName()+" ("+ip+") account: "+client);

            Account localAccount = model.accounts.get(email);

            if (localAccount == null){
                serverAnswer.println("Account not registered");
                Log log_1= new Log ("Rejecting connection from account not registered", client.getEmail(),ServerModel.dateToString(), ip  );
                model.logHistory.add(log_1);
                model.prec.remove(this);

            }else{
                model.logHistory.add(
                        new Log ("Account connected successfully", client.getEmail(),ServerModel.dateToString(), ip  ));


                serverAnswer.println("Ready");


                boolean stop = false;

                while (!stop) {
                    String s =inputLine.nextLine();
                    System.out.println(Thread.currentThread().getName()+" ("+ip+") string received: "+s);

                    Matcher m = pattern.matcher(s);

                    if(m.matches()) {
                        command = m.group(1).toLowerCase();
                        String param=m.group(2);
                        model.logHistory.add(
                                new Log ("Received command ("+s+")", client.getEmail(),ServerModel.dateToString(), ip  ));

                        switch (command){
                            case "receive":
                                String timestamp=param;
                                List<EMail> arrived=new ArrayList<>();
                                try {
                                    if("".equals(param))
                                        arrived = Utilities.loadMailFromCSV("./data/"+client.getEmail()+"/"+client.getEmail()+"_arrived.csv");
                                    else
                                        arrived = Utilities.loadMailFromCSVTimestamp("./data/"+client.getEmail()+"/"+client.getEmail()+"_arrived.csv", timestamp);

                                    serverAnswer.println("Done");
                                    System.out.println(Thread.currentThread().getName()+" ("+ip+") sending: "+arrived);
                                    serverObjOut.writeObject(arrived);
                                }
                                catch (Exception e) {
                                    serverAnswer.println("Error: "+e.getMessage());
                                }
                                break;
                            case "send":
                                EMail mailToSend;
                                String receiver;
                                String tpm;
                                PrintWriter saveMailArrived;

                                try{
                                    mailToSend=(EMail)serverObjIn.readObject();
                                    tpm=mailToSend.getRecipients();
                                    Scanner l =new Scanner(tpm).useDelimiter(("\\s*,\\s*"));
                                    String mailID = model.nextId();
                                    serverAnswer.println("Done "+mailID);

                                    while (l.hasNext()){
                                        receiver=l.next();
                                        Account receiverAccount = model.accounts.get(receiver);
                                        if (receiverAccount != null){
                                            mailToSend.setId(mailID);
                                            receiverAccount.getWriteFileArrived().lock();
                                            saveMailArrived = new PrintWriter(new FileWriter("./data/"+receiver+"/"+receiver+"_arrived.csv", true), true);
                                            saveMailArrived.println(mailToSend.toString());
                                            receiverAccount.getWriteFileArrived().lock();
                                        }else{
                                            String textReply= "Receiver does not exist";
                                            EMail errorReply = new EMail(mailID, Utilities.dateString(), ServerModel.MAIL_SERVER, mailToSend.getSender(), "Message not delivered", textReply + "\n\n\n"+mailToSend.getText());
                                            localAccount.getWriteFileArrived().lock();
                                            saveMailArrived = new PrintWriter(new FileWriter("./data/"+localAccount.getEmail()+"/"+localAccount.getEmail()+"_arrived.csv", true), true);
                                            saveMailArrived.println(errorReply.toString());
                                            localAccount.getWriteFileArrived().unlock();
                                        }
                                    }
                                }catch (Exception e) {
                                    serverAnswer.println("Error: "+e.getMessage());
                                }
                                break;

                            case "delete":
                                EMail mailToDelete;
                                List<EMail> list=new ArrayList<>();
                                String selection = param.toUpperCase();
                                System.out.println(Thread.currentThread().getName()+" ("+ip+") delete selection: "+selection);
                                try{
                                    mailToDelete=(EMail) serverObjIn.readObject();
                                    System.out.println(Thread.currentThread().getName()+" ("+ip+") mail to delete: "+mailToDelete.getId());

                                    switch (selection){
                                        case "ARRIVED":
                                            localAccount.getWriteFileArrived().lock();
                                            System.out.println(Thread.currentThread().getName()+" ("+ip+") locked file arrived: "+localAccount.getEmail());

                                            list=Utilities.loadMailFromCSV("./data/"+client.getEmail()+"/"+client.getEmail()+"_arrived.csv");
                                            Utilities.removeFromListAndSaveFile(list,"./data/"+client.getEmail()+"/"+client.getEmail()+"_arrived.csv" , mailToDelete);
                                            localAccount.getWriteFileArrived().unlock();
                                            break;
                                        case "SENT":
                                            localAccount.getWriteFileSent().lock();
                                            File fs=new File("./data/"+client.getEmail()+"/"+client.getEmail()+"_sent.csv");
                                            list=Utilities.loadMailFromCSV("./data/"+client.getEmail()+"/"+client.getEmail()+"_sent.csv");
                                            Utilities.removeFromListAndSaveFile(list,"./data/"+client.getEmail()+"/"+client.getEmail()+"_sent.csv" , mailToDelete);
                                            localAccount.getWriteFileSent().unlock();
                                            break;
                                        default:
                                            throw new IllegalStateException("Param required for delete (ARRIVED or SENT)");

                                    }
                                    serverAnswer.println("Done");
                                    System.out.println(Thread.currentThread().getName()+" ("+ip+") end delete");
                                }
                                catch(Exception e){
                                    serverAnswer.println("Error: "+e.getMessage());
                                }
                                break;

                            case "quit":
                                stop = true;

                                break;

                            default:

                                serverAnswer.println("Error: invalid command "+command);
                                stop = true;
                                break;
                        }
                    }
                    else {
                        serverAnswer.println("Command not found");
                        model.logHistory.add(
                                new Log ("Command not found ("+s+")", client.getEmail(),ServerModel.dateToString(), ip  ));

                    }
                }


            }

        } catch (ClassNotFoundException e) {
            String details = e.getClass().getName()+" "+e.getMessage();
            System.out.println(Thread.currentThread().getName()+" ("+ip+") Bad request format: "+details);
            model.logHistory.add(
                    new Log ("Bad request format ("+e.getMessage()+")", email, ServerModel.dateToString(), ip  ));
        }
        catch (Exception e){
            String details = e.getClass().getName()+" "+e.getMessage();
            System.out.println(Thread.currentThread().getName()+" ("+ip+") I/O error: "+details);
            model.logHistory.add(
                    new Log ("Connection Error ("+details+")", email,ServerModel.dateToString(), ip  ));
        }
        finally {
            System.out.println(Thread.currentThread().getName()+" ("+ip+") Closing connection... ");
            model.prec.remove(this);
            try {
                listening.close();
                model.logHistory.add(
                        new Log ("Connection closed", email,ServerModel.dateToString(), ip  ));
                System.out.println(Thread.currentThread().getName()+" ("+ip+") Closing connection... OK");
            } catch (IOException e) {
                System.out.println(Thread.currentThread().getName()+" ("+ip+") Error on closing connection" +e.getMessage());
                model.logHistory.add(
                        new Log ("Error on closing connection ("+e.getMessage()+")", email,ServerModel.dateToString(), ip  ));
                e.printStackTrace();
            }
        }
    }
}
