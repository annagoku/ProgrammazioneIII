package servermail;

import commons.SystemLogger;
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
    private static SystemLogger LOGGER = new SystemLogger(SaveLogDaemonThread.class);

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
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    LOGGER.warn("interrupted: "+e.getMessage());
                }

                LOGGER.info("saving logs");

                int i = 0;
                for (i=lastPos; i<listLog.size(); i++) {

                    saveLog.println(listLog.get(i).toString());
                }
                lastPos=i;

            }


        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }



    }
}

