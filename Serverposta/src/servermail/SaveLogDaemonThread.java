package servermail;

import commons.SystemLogger;
import javafx.collections.ObservableList;

import java.io.IOException;


public class SaveLogDaemonThread extends Thread {
    private ServerModel model;
    private static SystemLogger LOGGER = new SystemLogger(SaveLogDaemonThread.class);

    public SaveLogDaemonThread(ObservableList<Log> l, ServerModel model) {
        setDaemon(true);
        this.model = model;
    }

    public void run() {

        try {

            while (true) {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    LOGGER.warn("interrupted: "+e.getMessage());
                }
                model.saveLogs();
            }

        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }
}

