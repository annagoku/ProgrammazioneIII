package sample;

import javafx.collections.ObservableList;

public class ModelLista {
    private ObservableList<EMail> lista;
    private String casella;

    public ModelLista (ObservableList<EMail> l){
        lista=l;
    }

    public ObservableList<EMail> getmaillist(){
        return lista;
    }

    public String getCasella() {
        return casella;
    }

    public void setCasella(String casella) {
        this.casella = casella;
    }

    public synchronized boolean rimuoviMail (EMail mail){
        return getmaillist().remove(mail);

    }


    }



