package commons;

import java.io.Serializable;


public class Account implements Serializable {

    private String name;
    private String surname;
    private String email;

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
