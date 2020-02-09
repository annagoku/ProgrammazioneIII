package test;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import commons.*;

public class TestClient {

    enum Phases{
        LOGIN, RICEVI, RICEVI2, INVIO, ELIMINA;

    }

    public static void main(String [] args) {
        try {
            String host = InetAddress.getLocalHost().getHostName();
            System.out.println(host);
            Socket s = new Socket(host, 8089);
            System.out.println("Sto per inviare dei dati al Server");
            boolean stop = false;
            Phases f  = Phases.LOGIN;

            Account account = new Account("Luigi", "Bianchi", "luigi.bianchi@mymail.com");

            try {
                InputStream in = s.getInputStream();
                //ObjectInputStream rd = new ObjectInputStream(in);
                OutputStream out = s.getOutputStream();


                ObjectOutputStream st = new ObjectOutputStream(out);

                Scanner init = new Scanner(in);
                PrintWriter print = new PrintWriter(out, true);

                System.out.println("Inizio");

                while (!stop) {
                    String line = init.nextLine();
                    System.out.println("Server says: "+line);
                    if (line.equals("Ready")) {

                        switch (f) {
                            case LOGIN:
                                System.out.println("mando oggetto");
                                st.writeObject(account);
                                System.out.println("mandato oggetto");
                                Thread.sleep(5000);
                                f = Phases.RICEVI;
                                stop = true;
                                break;
                            default:
                                stop = true;
                                break;
                        }

                    }


                }
                print.println("QUIT");

            } catch (IOException e) {
                e.printStackTrace();

            } finally {
                s.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
