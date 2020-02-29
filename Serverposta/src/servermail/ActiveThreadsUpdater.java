package servermail;

import javafx.application.Platform;

public class ActiveThreadsUpdater extends  Thread {
    ServerModel model;

    public ActiveThreadsUpdater(ServerModel model) {
        super();
        this.model = model;
    }

    @Override
    public void run() {
        Platform.runLater(() -> {
            model.setActiveThread(model.prec.size()+ " active threads");
        });
    }
}
