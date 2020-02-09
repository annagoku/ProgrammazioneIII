package servermail;

import javafx.collections.ObservableList;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import commons.Account;

public class SaveLogDaemonThread extends Thread {
    private ObservableList<Log> listLog;
    private ServerModel model;
    private PrintWriter saveLog;
    private int lastPos = 0;

    public SaveLogDaemonThread(ObservableList<Log> l, ServerModel model) {
        setDaemon(true);
        this.listLog = l;
        this.model = model;
    }

    public void run() {

        try {
            saveLog = new PrintWriter(new FileWriter("./data/Log.csv", true), true);


            while (true) {
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                   System.out.println("SaveLogDaemon interrupted: "+e.getMessage());
                }

                System.out.println("SaveLogDaemon: saving logs");
                synchronized (listLog) {
                    int i = 0;
                    for (i=lastPos; i<listLog.size(); i++) {

                        saveLog.println(listLog.get(i).toString());
                    }
                    lastPos=i;
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }



    }
}

