package commons;



import javafx.collections.ObservableList;

import java.io.File;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

public abstract class Utilities {
    public static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static DateFormat DATE_FORMAT_MILLIS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");


    public static String dateString() {
        return dateString(Calendar.getInstance().getTime());
    }

    public static String dateString(Date date) {
        return DATE_FORMAT.format(date);
    }

    public static String dateStringMillis() {

        return DATE_FORMAT_MILLIS.format(Calendar.getInstance().getTime());
    }

    public static String dateStringMillis(Date date) {
        return DATE_FORMAT_MILLIS.format(date);
    }

    public static List<EMail> loadMailFromCSV(String filename)   throws  Exception{
        File f=new File(filename);

        List<EMail> list = new ArrayList<>();

        if (f.exists() && f.length()!=0) {
            Scanner mailIn = new Scanner(f);
            while (mailIn.hasNextLine()) {
                String tmp = mailIn.nextLine();
                Scanner l = new Scanner(tmp).useDelimiter("\\s*;\\s*");
                EMail mailA = new EMail(l.next(), l.next(), l.next(), l.next(), l.next(), parseText(l.next()));
                list.add(mailA);
                l.close();
            }
            mailIn.close();
        }
        return list;
    }

    public static List<EMail> loadMailFromCSVTimestamp(String filename, String timestamp)   throws  Exception{
        File f=new File(filename);

        List<EMail> list = new ArrayList<>();
        Date timestampDate = DATE_FORMAT.parse(timestamp);

        if (f.exists() && f.length()!=0) {
            Scanner mailIn = new Scanner(f);
            while (mailIn.hasNextLine()) {
                String tmp = mailIn.nextLine();
                Scanner l = new Scanner(tmp).useDelimiter("\\s*;\\s*");
                EMail mail = new EMail(l.next(), l.next(), l.next(), l.next(), l.next(), parseText(l.next()));
                l.close();

                Date emailDate = DATE_FORMAT.parse(mail.getTime());

                if(emailDate.after(timestampDate))
                    list.add(mail);

            }
            mailIn.close();
        }
        return list;
    }


    public static void saveEmailCsvToFile(List<EMail> emailList, String filename) throws  Exception {
        if(emailList != null) {
            File f = new File (filename);
            PrintWriter printWriter = new PrintWriter(f);
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

    public static String getReplyText(EMail sourceMail) {
        return  "\n\n------------------------------\nFrom: "+sourceMail.getSender()+ "\n" +
                "Sent: "+sourceMail.getTime() +"\n"+
                "To: "+sourceMail.getRecipients()+"\n"+
                "Subject: "+ sourceMail.getSubject()+"\n\n"+
                sourceMail.getText();
    }

    public static void removeFromListAndSaveFile (List<EMail> emailList, String filename, EMail selectedEmail) throws Exception {
        File f = new File(filename);
        synchronized (emailList) {
            emailList.removeIf(eMail -> eMail.getId().equals(selectedEmail.getId()));
            Utilities.saveEmailCsvToFile(emailList, filename);
        }
    }


    public static String escapeText(final String text) {
        if(text == null)
            return null;
        return text.replaceAll("\\n", "\\\\n");
    }


    public static String parseText(final String text) {
        if(text == null)
            return null;
        return text.replaceAll("\\\\n", "\n");
    }

    /*public static void main (String [] args) {
        String text = "Ciao \n Come stai?\nTutto bne?";
        System.out.println("-------------");
        System.out.println(text);

        System.out.println("-------------");


        System.out.println("prova escaping");
        String textEscaped = escapeText(text);
        System.out.println(textEscaped);
        System.out.println("prova parsing");
        System.out.println(parseText(textEscaped));


    }*/
}
