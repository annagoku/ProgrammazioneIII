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

            if (!model.accounts.contains(client)){
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
                                List<EMail> tosend=new ArrayList<>();


                                try {
                                    if("".equals(param))
                                        tosend = Utilities.loadMailFromCSV("./data/"+client.getEmail()+"/"+client.getEmail()+"_arrived.csv");
                                    else
                                        tosend = Utilities.loadMailFromCSVTimestamp("./data/"+client.getEmail()+"/"+client.getEmail()+"_arrived.csv", timestamp);

                                    serverAnswer.println("Done");
                                    System.out.println(Thread.currentThread().getName()+" ("+ip+") sending: "+tosend);
                                    serverObjOut.writeObject(tosend);
                                }
                                catch (Exception e) {
                                    serverAnswer.println("Error: "+e.getMessage());
                                }



                                break;
                            case "invio":
                                break;

                            case "elimina":
                                break;

                            case "quit":
                                stop = true;
                                break;

                            default:
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
