package clientmail;

import commons.EMail;
import commons.SystemLogger;
import commons.Utilities;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class DeleteThread extends Thread {
    private EMail mailDelete;
    private ClientModel model;
    public enum Selection {ARRIVED, SENT};
    private static SystemLogger LOGGER = new SystemLogger(DeleteThread.class);

    private Selection selection = null;

    public DeleteThread (Selection s, ClientModel model, EMail mail){

        this.model=model;
        this.mailDelete=mail;
        this.selection = s;
        LOGGER.info("deleting email "+mail.getId()+ " from "+selection);
    }

    public void run() {
        EMail mail;
        Socket s;
        Boolean found=false;
        File f = null;
        try{
            sleep (3000);
        }catch(InterruptedException e){
            e.printStackTrace();
        }

            try {
                LOGGER.info("connecting to server...");

                s = new Socket(model.host, model.port);
                try {
                    OutputStream out = s.getOutputStream();
                    InputStream in = s.getInputStream();
                    ObjectOutputStream clientObjOut = new ObjectOutputStream(out);
                    ObjectInputStream clientObjIn = new ObjectInputStream(in);
                    Scanner clientIn = new Scanner(in);
                    PrintWriter clientPrint = new PrintWriter(out, true);
                    // comunico la cancellazione al server
                    clientObjOut.writeObject(model.getAccount());

                    String serverAnswer = clientIn.nextLine();
                    LOGGER.debug("Server says" +serverAnswer);

                    if (serverAnswer.equals("Ready")) {
                        LOGGER.debug("connected. Sending delete command.."+" mailId "+mailDelete.getId());
                        clientPrint.println("Delete "+selection.toString() );
                        clientPrint.println(mailDelete.getId());
                        String res = clientIn.nextLine();
                        clientPrint.println("QUIT");

                        if(res != null && res.equals("Done")) {

                            switch (selection) {
                                case ARRIVED:
                                    model.getFileArrived().remove(mailDelete.getId());

                                    Platform.runLater(() -> {
                                        try {
                                            model.sem.acquire();
                                            model.getMailArrived().removeIf(eMail -> eMail.getId().equals(mailDelete.getId()));

                                        } catch (Exception e) {
//                                            synchronized ((model.lock)) {
                                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                                alert.setHeaderText("Cannot update file mail arrived");
                                                alert.setContentText( e.getMessage());
                                                alert.show();
//                                            }
                                        }finally {
                                            model.sem.release();
                                        }
                                    });
                                    break;
                                case SENT:
                                    model.getFileSent().remove(mailDelete.getId());

                                    Platform.runLater(() -> {
                                        try {
                                            model.sem.acquire();
                                            model.getMailSent().removeIf(eMail -> eMail.getId().equals(mailDelete.getId()));
                                            model.sem.release();

                                        } catch (Exception e) {
                                            synchronized ((model.lock)) {
                                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                                alert.setHeaderText("Cannot update file mail sent");
                                                alert.setContentText( e.getMessage());
                                                alert.show();
                                            }
                                        }
                                    });
                                    break;
                                default:
                                    throw new RuntimeException("shouldn't be here");
                            }
                        }
                        else {
                            Platform.runLater(
                                    () -> {
                                        synchronized (model.lock) {
                                            Alert alert = new Alert(Alert.AlertType.ERROR);
                                            alert.setHeaderText("Cannot delete mail");
                                            alert.setContentText( res);
                                            alert.show();
                                        }
                                    }
                            );
                        }
                        }

                    s.close();
            } catch (Exception  e) {
                    e.printStackTrace();
                    Platform.runLater(
                            () -> {
                                synchronized (model.lock) {
                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                    alert.setHeaderText("Cannot delete mail");
                                    alert.setContentText( e.getMessage());
                                    alert.show();
                                }
                            }
                    );
            }
        } catch(IOException e){
                e.printStackTrace();
                Platform.runLater(
                        () -> {
                            synchronized (model.lock) {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setHeaderText("Cannot delete mail");
                                alert.setContentText( e.getMessage());
                                alert.show();
                            }
                        }
                );
        }
        finally {
            Platform.runLater(() -> {
                synchronized (model.lock) {
                    model.setClientOperation("");                }
            });
        }
    }


}
