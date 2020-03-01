package commons;

import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Account implements Serializable {

    private String name;
    private String surname;
    private String email;
    private transient ReadWriteLock fileArrived = new ReentrantReadWriteLock();
    private transient Lock readFileArrived=fileArrived.readLock();
    private transient Lock writeFileArrived=fileArrived.writeLock();
    private transient ReadWriteLock fileSent = new ReentrantReadWriteLock();
    private transient Lock readFileSent=fileSent.readLock();

    public Lock getReadFileArrived() {
        return readFileArrived;
    }

    public Lock getWriteFileArrived() {
        return writeFileArrived;
    }

    public Lock getReadFileSent() {
        return readFileSent;
    }

    public Lock getWriteFileSent() {
        return writeFileSent;
    }

    private transient Lock writeFileSent=fileSent.writeLock();

    public Account(String name, String surname, String email) {
        //per impedire che un oggetto di classe Account abbia la email vuota
        if(email == null || "".equals(email)) {
            throw new RuntimeException("email cannot be empty or null");
        }

        this.name = name;
        this.surname = surname;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getEmail() {
        return email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    // elimitato setter setEmail in quanto voglio che non sia modificabile dall'esterno

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    //Override del metodo equals
    @Override
    public boolean equals(Object obj) {
        if (obj!=null && obj instanceof Account){
            Account a=(Account) obj;
            return this.email.equals(a.email);
        }
        return false;
    }
}
