package servermail;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;

public class Log implements Serializable{
    private StringProperty mes=new SimpleStringProperty();
    private StringProperty client=new SimpleStringProperty();
    private StringProperty date= new SimpleStringProperty();
    private StringProperty ipClient= new SimpleStringProperty();

    //Property messaggio log
    public  StringProperty mesProperty() {
        return this.mes;
    }
    public String getMes() {
        return this.mesProperty().get();
    }
    public void setMes (String m) {
        this.mesProperty().set(m);
    }

    //Property client log
    public StringProperty clientProperty() {
        return this.client;
    }
    public String getClient() {
        return this.clientProperty().get();
    }
    public  void setClient (String c) {
        this.clientProperty().set(c);
    }

    //Property ip client log
    public StringProperty ipClientProperty() {
        return this.ipClient;
    }
    public String getIpClient() {
        return this.ipClientProperty().get();
    }
    public  void setIpClient (String s) {
        this.ipClientProperty().set(s);
    }

    //Property data log
    public StringProperty dateProperty() {
        return this.date;
    }
    public String getDate() {
        return this.dateProperty().get();
    }
    public  void setDate (String d) {
        this.dateProperty().set(d);
    }


    //Costruttore
    public Log(String mes, String client, String date, String ipClient) {
        setMes (mes);
        setClient(client);
        setDate(date);
        setIpClient(ipClient);
    }

    @Override
    public String toString() {
        return getDate()+ ";"+getMes()+";"+getClient()+";"+getIpClient()+";";
    }
}








