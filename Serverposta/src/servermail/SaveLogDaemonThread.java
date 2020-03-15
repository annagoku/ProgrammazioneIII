package servermail;

import commons.SystemLogger;
import java.io.IOException;


public class SaveLogDaemonThread extends Thread {
    private ServerModel model;
    private static SystemLogger LOGGER = new SystemLogger(SaveLogDaemonThread.class);

    public SaveLogDaemonThread(ServerModel model) {
        setDaemon(true);
        this.model = model;
    }

    public void run() {
        try {
            while (true) {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    LOGGER.log("interrupted: "+e.getMessage());
                }
                model.saveLogs();
            }

        } catch (IOException e) {
            LOGGER.log(e.getMessage());
            e.printStackTrace();
        }
    }
}

