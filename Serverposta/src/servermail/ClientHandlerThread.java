package servermail;

import commons.*;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ClientHandlerThread implements Runnable{

    // i comandi da dare al server sono fatti da una parola + uno o piu' spazi + un eventuale parametro
    // questo pattern controlla questo formato e raggruppa in modo che il primo gruppo sia il comando
    // e il secondo gruppo sia l'eventuale parametro
    private static Pattern pattern = Pattern.compile("(\\w+)\\s*(.*)");
    private static SystemLogger LOGGER = new SystemLogger(ClientHandlerThread.class);

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
            Thread.sleep(1500);
            LOGGER.log("running for client "+ip);
            Log connectLog= new Log ("Connection from client", email,ServerModel.dateToString(), ip  );
            model.addLog(connectLog);

            in = listening.getInputStream();
            out = listening.getOutputStream();
            inputLine = new Scanner(in);
            serverAnswer = new PrintWriter(out, true);

            serverObjOut = new ObjectOutputStream(out);
            serverObjIn = new ObjectInputStream(in);

            LOGGER.log("waiting for account..");
            client= (Account)serverObjIn.readObject();
            email = client.getEmail();
            LOGGER.log("account: "+client);

            //Utilizzo l'hashmap per verificare l'esistenza dell'account
            Account localAccount = model.accounts.get(email);

            if (localAccount == null){
                serverAnswer.println("Account not registered");
                Log log_1= new Log ("Rejecting connection from account not registered", client.getEmail(),ServerModel.dateToString(), ip  );
                model.addLog(log_1);

            }else{
                model.addLog(
                        new Log ("Account connected successfully", client.getEmail(),ServerModel.dateToString(), ip  ));
                serverAnswer.println("Ready");
                boolean stop = false;
                while (!stop) {
                    String s =inputLine.nextLine();
                    LOGGER.log("string received: '"+s+"'");

                    Matcher m = pattern.matcher(s);

                    if(m.matches()) {
                        command = m.group(1).toLowerCase();
                        String param=m.group(2);
                        model.addLog(
                                new Log ("Received command ("+s+")", client.getEmail(),ServerModel.dateToString(), ip  ));

                        switch (command){
                            case "receive":
                                LOGGER.log("---------------receive case: ");
                                String timestamp=param;
                                List<String> arrived=null;
                                try {
                                    if("".equals(param))
                                        arrived = model.arrivedFileHandler.get(localAccount.getEmail()).readListString();
                                    else
                                        arrived = model.arrivedFileHandler.get(localAccount.getEmail()).readListString(timestamp);

                                    serverAnswer.println("Done");
                                    model.addLog(
                                            new Log ("Sent response (Done) to client", client.getEmail(),ServerModel.dateToString(), ip  ));
                                    LOGGER.log("sending mail list to client: "+arrived);
                                    //Restituisce al client una lista di mail vuota o in forma di stringa
                                    serverObjOut.writeObject(arrived);
                                    model.addLog(
                                            new Log ("Sent email list (size "+arrived.size()+") to client", client.getEmail(),ServerModel.dateToString(), ip  ));

                                }
                                catch (Exception e) {
                                    serverAnswer.println("Error: "+e.getMessage());
                                }
                                break;
                            case "send":
                                LOGGER.log("---------------send case: ");
                                EMail mailToSend;
                                String receiver;
                                String tpm;
                                try{
                                    LOGGER.log("reading mail to send from client... ");
                                    String mailString = inputLine.nextLine();
                                    LOGGER.log("parsing -> "+mailString);

                                    mailToSend=EMail.parseEmail(mailString);
                                    LOGGER.log("parsed  -> "+mailToSend.toString());
                                    tpm=mailToSend.getRecipients();
                                    Scanner l =new Scanner(tpm).useDelimiter(("\\s*,\\s*"));
                                    String mailID = model.nextId();
                                    mailToSend.setId(mailID);
                                    LOGGER.log("sent answer: Done "+mailID);
                                    serverAnswer.println("Done "+mailID);
                                    model.addLog(
                                            new Log ("Sent response (Done "+mailID+") to client", client.getEmail(),ServerModel.dateToString(), ip  ));
                                    //Individuare singolarmente tutti i destinatari
                                    while (l.hasNext()){
                                        receiver=l.next();
                                        Account receiverAccount = model.accounts.get(receiver);
                                        LOGGER.log("checking receiver: "+receiver);
                                        if (receiverAccount != null){
                                            LOGGER.log("updating arrived file for "+receiver);
                                            //Hashmap mantiene la corrispondenza tra indirizzo mail e file
                                            FileHandler receiverArrivedFile = model.arrivedFileHandler.get(receiver);
                                            receiverArrivedFile.add(mailToSend);
                                        }else{
                                            String textReply= "Receiver \""+receiver+"\" does not exist";
                                            LOGGER.log("receiver not found "+receiver);
                                            LOGGER.log("sending notification back to "+mailToSend.getSender());
                                            EMail errorReply = new EMail(mailID, Utilities.dateString(), ServerModel.MAIL_SERVER, mailToSend.getSender(), "Message not delivered", textReply + Utilities.getReplyText(mailToSend, "Original message"));

                                            FileHandler senderArrivedFile = model.arrivedFileHandler.get(localAccount.getEmail());
                                            senderArrivedFile.add(errorReply);
                                            LOGGER.log("notification sent to "+mailToSend.getSender());
                                        }

                                    }

                                    LOGGER.log("updating file sent of "+localAccount.getEmail());

                                    FileHandler senderSentFile = model.sentFileHandler.get(localAccount.getEmail());
                                    senderSentFile.add(mailToSend);
                                }catch (Exception e) {
                                    e.printStackTrace();
                                    serverAnswer.println("Error: "+e.getMessage());
                                }
                                break;

                            case "delete":
                                LOGGER.log("---------------delete case: ");

                                String mailToDelete;
                                String selection = param.toUpperCase();
                                LOGGER.log("delete selection: '"+selection+"'");
                                try{
                                    mailToDelete= inputLine.nextLine();
                                    LOGGER.log("mail to delete: "+mailToDelete);

                                    switch (selection){
                                        case "ARRIVED":
                                            model.arrivedFileHandler.get(localAccount.getEmail()).remove(mailToDelete);
                                            break;
                                        case "SENT":
                                            model.sentFileHandler.get(localAccount.getEmail()).remove(mailToDelete);

                                            break;
                                        default:
                                            LOGGER.log("selection not allowed '"+selection+"'");
                                            throw new IllegalStateException("Param required for delete (ARRIVED or SENT)");

                                    }
                                    LOGGER.log("sending Done to client");
                                    serverAnswer.println("Done");
                                    model.addLog(
                                            new Log ("Sent response (Done) to client", client.getEmail(),ServerModel.dateToString(), ip  ));
                                    LOGGER.log("end delete");
                                }
                                catch(Exception e){
                                    e.printStackTrace();
                                    serverAnswer.println("Error: "+e.getMessage());
                                }
                                break;

                            case "quit":
                                LOGGER.log("---------------quit case: ");
                                stop = true;
                                break;

                            default:
                                LOGGER.log("sending invalid command to client");
                                serverAnswer.println("Error: invalid command "+command);
                                model.addLog(
                                        new Log ("invalid command ("+s+")", client.getEmail(),ServerModel.dateToString(), ip  ));
                                stop = true;
                                break;
                        }
                    }
                    else {
                        serverAnswer.println("Command not found");
                        model.addLog(
                                new Log ("Command not found ("+s+")", client.getEmail(),ServerModel.dateToString(), ip  ));

                    }
                }
            }

        }
        catch (Exception e){
            e.printStackTrace();
            String details = e.getClass().getName()+" "+e.getMessage();
            LOGGER.log("I/O error: "+details);
            model.addLog(
                    new Log ("Connection Error ("+details+")", email,ServerModel.dateToString(), ip  ));
        }
        finally {
            LOGGER.log("Closing connection... ");
            try {
                listening.close();
                model.addLog(
                        new Log ("Connection closed", email,ServerModel.dateToString(), ip  ));
                LOGGER.log("Closing connection... OK");
            } catch (IOException e) {
                LOGGER.log("Error on closing connection" +e.getMessage());
                model.addLog(
                        new Log ("Error on closing connection ("+e.getMessage()+")", email,ServerModel.dateToString(), ip  ));
                e.printStackTrace();
            }
        }
    }
}
