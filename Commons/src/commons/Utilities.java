package commons;


import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class Utilities {
    // FORMATI DATA
    public static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static DateFormat DATE_FORMAT_MILLIS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

      //Metodo che ritorna una  stringa corrispondente all'istante "now"
    public static String dateString() {
        return dateString(Calendar.getInstance().getTime());
    }


    //Metodo che ritorna una data in stringa secondo il formato indicato
    public static String dateString(Date date) {
        return DATE_FORMAT.format(date);
    }

    //Metodo che ritorna l'istante "now" in stringa secondo il formato indicato
    public static String dateStringMillis() {
        return DATE_FORMAT_MILLIS.format(Calendar.getInstance().getTime());
    }


    //Metodo per salvare una lista di oggetti EMail in un file csv
    public static void saveEmailCsvToFile(List<EMail> emailList, String filename) throws  Exception {
        if(emailList != null) {
            //File f = new File (filename);
            PrintWriter printWriter = new PrintWriter(new FileWriter(filename), true);
            if(emailList.size() > 0) {
                for (EMail e: emailList) {
                    printWriter.println(e.toString());
                }
            }
            else {
                printWriter.println("");
            }
            printWriter.close();
        }
    }

    //Metodo di ausilio al reply mail
    public static String getReplyText(EMail sourceMail, String header) {
        String h = "------------------------------";
        if(header != null & !"".equals(header))
          h = "-----"+header+"-----";

        return  "\n\n"+h+"\nFrom: "+sourceMail.getSender()+ "\n" +
                "Sent: "+sourceMail.getTime() +"\n"+
                "To: "+sourceMail.getRecipients()+"\n"+
                "Subject: "+ sourceMail.getSubject()+"\n\n"+
                sourceMail.getText();
    }


    public static String getReplyText(EMail sourceMail) {
        return  getReplyText(sourceMail, "");
    }

    //Metodo per rimuovere una mail da una lista e salvare in un file csv
    public static void removeFromListAndSaveFile (List<EMail> emailList, String filename, EMail selectedEmail) throws Exception {
        File f = new File(filename);
        synchronized (emailList) {
            emailList.removeIf(eMail -> eMail.getId().equals(selectedEmail.getId()));
            Utilities.saveEmailCsvToFile(emailList, filename);
        }
    }

    //Metodo di ausilio per filtrare i destinatari in replyall
    public static String replyAllRecipients(String recipients, String email) {
        if(recipients != null && email != null) {
            String filteredRecipients = "";
            Scanner s = new Scanner(recipients).useDelimiter("\\s*,\\s*");
            while(s.hasNext()) {
                String tmp = s.next();
                if(!tmp.equals(email)) {
                    filteredRecipients += tmp;
                    if(s.hasNext())
                        filteredRecipients +=",";
                }
            }
            return filteredRecipients;
        }
        else {
            throw new IllegalArgumentException("Arguments must be not null");
        }
    }

    //Metodo di ausilio per aggiungere un a capo
    public static String escapeText(final String text) {
        if(text == null)
            return null;
        return text.replaceAll("\\n", "\\\\n");
    }


    //Metodo di ausilio per eliminare un a capo
    public static String parseText(final String text) {
        if(text == null)
            return null;
        return text.replaceAll("\\\\n", "\n");
    }


    //Metodo per convertire una lista di mail espresse in stringhe in una lista di oggetti EMail
    // (usato per inviare liste di mail tra client e server
    public static List<EMail> readNewEmailsFromStringList(List<String> stringList) {
        List<EMail> list = new ArrayList<>(stringList.size());

        for (String s: stringList) {
            EMail e = EMail.parseEmail(s);
            list.add(e);
        }
        return list;
    }

}
