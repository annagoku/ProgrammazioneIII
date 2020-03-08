package servermail;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import commons.*;

/*
 Client Handler
 */
public class ClientHandlerThread extends Thread{

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
            LOGGER.info("running for client "+ip);
            Log connectLog= new Log ("Connection from client", email,ServerModel.dateToString(), ip  );
            model.addLog(connectLog);

            in = listening.getInputStream();
            out = listening.getOutputStream();
            inputLine = new Scanner(in);
            serverAnswer = new PrintWriter(out, true);

            serverObjOut = new ObjectOutputStream(out);
            serverObjIn = new ObjectInputStream(in);

            LOGGER.info("waiting for account..");
            client= (Account)serverObjIn.readObject();
            email = client.getEmail();
            LOGGER.info("account: "+client);

            Account localAccount = model.accounts.get(email);



            if (localAccount == null){
                serverAnswer.println("Account not registered");
                Log log_1= new Log ("Rejecting connection from account not registered", client.getEmail(),ServerModel.dateToString(), ip  );
                model.addLog(log_1);
                model.prec.remove(this);

            }else{
                model.addLog(
                        new Log ("Account connected successfully", client.getEmail(),ServerModel.dateToString(), ip  ));


                serverAnswer.println("Ready");


                boolean stop = false;

                while (!stop) {
                    String s =inputLine.nextLine();
                    LOGGER.debug("string received: '"+s+"'");

                    Matcher m = pattern.matcher(s);

                    if(m.matches()) {
                        command = m.group(1).toLowerCase();
                        String param=m.group(2);
                        model.addLog(
                                new Log ("Received command ("+s+")", client.getEmail(),ServerModel.dateToString(), ip  ));

                        switch (command){
                            case "receive":
                                LOGGER.debug("---------------receive case: ");
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
                                    LOGGER.debug("sending mail list to client: "+arrived);

                                    serverObjOut.writeObject(arrived);
                                    model.addLog(
                                            new Log ("Sent email list (size "+arrived.size()+") to client", client.getEmail(),ServerModel.dateToString(), ip  ));

                                }
                                catch (Exception e) {
                                    serverAnswer.println("Error: "+e.getMessage());
                                }
                                break;
                            case "send":
                                LOGGER.debug("---------------send case: ");
                                EMail mailToSend;
                                String receiver;
                                String tpm;
                                PrintWriter saveMailArrived;
                                PrintWriter saveMailSent;
                                try{
                                    LOGGER.debug("reading mail to send from client... ");
                                    String mailString = inputLine.nextLine();
                                    LOGGER.debug("parsing -> "+mailString);

                                    mailToSend=EMail.parseEmail(mailString);
                                    LOGGER.debug("parsed  -> "+mailToSend.toString());
                                    tpm=mailToSend.getRecipients();
                                    Scanner l =new Scanner(tpm).useDelimiter(("\\s*,\\s*"));
                                    String mailID = model.nextId();
                                    mailToSend.setId(mailID);
                                    LOGGER.debug("sent answer: Done "+mailID);
                                    serverAnswer.println("Done "+mailID);
                                    model.addLog(
                                            new Log ("Sent response (Done "+mailID+") to client", client.getEmail(),ServerModel.dateToString(), ip  ));

                                    while (l.hasNext()){
                                        receiver=l.next();
                                        Account receiverAccount = model.accounts.get(receiver);
                                        LOGGER.debug("checking receiver: "+receiver);
                                        if (receiverAccount != null){
                                            LOGGER.debug("updating arrived file for "+receiver);

                                            FileHandler receiverArrivedFile = model.arrivedFileHandler.get(receiver);
                                            receiverArrivedFile.add(mailToSend);
                                        }else{
                                            String textReply= "Receiver \""+receiver+"\" does not exist";
                                            LOGGER.debug("receiver not found "+receiver);
                                            LOGGER.debug("sending notification back to "+mailToSend.getSender());
                                            EMail errorReply = new EMail(mailID, Utilities.dateString(), ServerModel.MAIL_SERVER, mailToSend.getSender(), "Message not delivered", textReply + Utilities.getReplyText(mailToSend, "Original message", "> "));

                                            FileHandler senderArrivedFile = model.arrivedFileHandler.get(localAccount.getEmail());
                                            senderArrivedFile.add(errorReply);
                                            LOGGER.debug("notification sent to "+mailToSend.getSender());
                                        }

                                    }

                                    LOGGER.debug("updating file sent of "+localAccount.getEmail());

                                    FileHandler senderSentFile = model.sentFileHandler.get(localAccount.getEmail());
                                    senderSentFile.add(mailToSend);
                                }catch (Exception e) {
                                    e.printStackTrace();
                                    serverAnswer.println("Error: "+e.getMessage());
                                }
                                break;

                            case "delete":
                                LOGGER.debug("---------------delete case: ");

                                String mailToDelete;
                                List<EMail> list=new ArrayList<>();
                                String selection = param.toUpperCase();
                                LOGGER.debug("delete selection: '"+selection+"'");
                                try{
                                    mailToDelete= inputLine.nextLine();
                                    LOGGER.debug("mail to delete: "+mailToDelete);

                                    switch (selection){
                                        case "ARRIVED":
                                            model.arrivedFileHandler.get(localAccount.getEmail()).remove(mailToDelete);
                                            break;
                                        case "SENT":
                                            model.sentFileHandler.get(localAccount.getEmail()).remove(mailToDelete);

                                            break;
                                        default:
                                            LOGGER.debug("selection not allowed '"+selection+"'");
                                            throw new IllegalStateException("Param required for delete (ARRIVED or SENT)");

                                    }
                                    LOGGER.debug("sending Done to client");
                                    serverAnswer.println("Done");
                                    model.addLog(
                                            new Log ("Sent response (Done) to client", client.getEmail(),ServerModel.dateToString(), ip  ));
                                    LOGGER.debug("end delete");
                                }
                                catch(Exception e){
                                    e.printStackTrace();
                                    serverAnswer.println("Error: "+e.getMessage());
                                }
                                break;

                            case "quit":
                                LOGGER.debug("---------------quit case: ");
                                stop = true;

                                break;

                            default:
                                LOGGER.debug("sending invalid command to client");

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
            LOGGER.error("I/O error: "+details);
            model.addLog(
                    new Log ("Connection Error ("+details+")", email,ServerModel.dateToString(), ip  ));
        }
        finally {
            LOGGER.info("Closing connection... ");
            model.prec.remove(this);
            try {
                listening.close();
                model.addLog(
                        new Log ("Connection closed", email,ServerModel.dateToString(), ip  ));
                LOGGER.info("Closing connection... OK");
            } catch (IOException e) {
                LOGGER.error("Error on closing connection" +e.getMessage());
                model.addLog(
                        new Log ("Error on closing connection ("+e.getMessage()+")", email,ServerModel.dateToString(), ip  ));
                e.printStackTrace();
            }
        }
    }
}
