package sample;

import java.time.LocalDateTime;
import java.util.*;

public class EMail {
    private String ID;
    public String mittente;
    public String destinatario;
    public String oggetto;
    public String testo;
    LocalDateTime time;

    public EMail(String mitt, String dest, String ogg, String test){
        UUID newid = UUID.randomUUID();
        ID=newid.toString();
        mittente=mitt;
        time= LocalDateTime.now();
        destinatario=dest;
        oggetto =ogg;
        testo=test;
    }

    public String getID() {
        return ID;
    }
}
