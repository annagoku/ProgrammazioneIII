package commons;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.util.*;

public class EMail implements Serializable {

    public static String EMAIL_PATTERN =  "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";

    private StringProperty stateNewMail= new SimpleStringProperty();
    private StringProperty id=new SimpleStringProperty();
    private StringProperty sender=new SimpleStringProperty();
    private StringProperty recipients=new SimpleStringProperty();
    private StringProperty subject =new SimpleStringProperty();
    private StringProperty text= new SimpleStringProperty();
    private StringProperty time= new SimpleStringProperty();


    //Property stateNewMail
    public  StringProperty stateNewMailProperty() {
        return this.stateNewMail;
    }
    public String getStateNewMail() {
        return this.stateNewMailProperty().get();
    }
    public void setStateNewMail (String m) {
        this.stateNewMailProperty().set(m);
    }


    //Property id
    public  StringProperty idProperty() {
        return this.id;
    }
    public String getId() {
        return this.idProperty().get();
    }
    public void setId (String m) {
        this.idProperty().set(m);
    }


    //Property sender
    public StringProperty senderProperty() {
        return this.sender;
    }
    public String getSender() {
        return this.senderProperty().get();
    }
    public  void setSender (String c) {
        this.senderProperty().set(c);
    }


    //Property recipients
    public StringProperty recipientsProperty() {
        return this.recipients;
    }
    public String getRecipients() {
        return this.recipients.get();
    }
    public  void setRecipients (String s) {
        this.recipientsProperty().set(s);
    }


    //Property object
    public StringProperty subjectProperty() {
        return this.subject;
    }
    public String getSubject() {
        return this.subjectProperty().get();
    }
    public  void setSubject(String d) {
        this.subjectProperty().set(d);
    }


    //Property text
    public StringProperty textProperty() {
        return this.text;
    }
    public String getText() {
        return this.textProperty().get();
    }
    public  void setText (String d) {
        this.textProperty().set(d);
    }


    //Property time
    public StringProperty timeProperty() {
        return this.time;
    }
    public String getTime() {
        return this.timeProperty().get();
    }
    public  void setTime (String d) {
        this.timeProperty().set(d);
    }


    //Costruttore
    public EMail(String id, String time, String sender, String recipients, String subject, String text ) {
        setId (id);
        setTime(time);
        setSender(sender);
        setRecipients(recipients);
        setSubject(subject);
        setText(text);

    }

    @Override
    public String toString() {
        return getId()+";"+ getTime()+";"+getSender()+";"+getRecipients()+";"+ getSubject()+";"+getText();
    }

    /**
     * Controlla se un indirizzo email e' valido (
     * @param email
     * @return
     */
    public static boolean isValid(String email) {
        return email != null && email.matches(EMAIL_PATTERN);
    }

    public static boolean recipientsValid(String rec) {
        if(rec!= null) {
            Scanner s = new Scanner(rec).useDelimiter("\\s*,\\s*");
            boolean valid = true;
            while(s.hasNext() && valid) {
                valid = isValid(s.next());
            }
            return valid;
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj!=null && obj instanceof EMail){
            EMail mail=(EMail) obj;
            if(this.getId()==mail.getId()){
                return true;
            }
            return false;
        }
        return super.equals(obj);
    }
}
