package commons;



import javafx.collections.ObservableList;

import java.io.File;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

public abstract class Utilities {
    // FORMATI DATA
    public static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static DateFormat DATE_FORMAT_MILLIS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");




      //@return String representing "now"
    public static String dateString() {
        return dateString(Calendar.getInstance().getTime());
    }

    /* @param date
       @return String representing date*/

    public static String dateString(Date date) {
        return DATE_FORMAT.format(date);
    }

     // @return String (format with millis) representing "now"
    public static String dateStringMillis() {

        return DATE_FORMAT_MILLIS.format(Calendar.getInstance().getTime());
    }

     /* @param date
        @return String representing date (forma with millis)*/
    public static String dateStringMillis(Date date) {
        return DATE_FORMAT_MILLIS.format(date);
    }

    /**
     /* Loads an EMail list from csv file

      @param filename
     * @return
     * @throws Exception
     */
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

    /**
     * Loads a list of EMail from csv, it gets only emails newer than timestamp
     *
     * @param filename
     * @param timestamp
     * @return
     * @throws Exception
     */
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

    /**
     * Saves the list of EMail provided into the filename provided.
     * It doesn't append
     *
     * @param emailList
     * @param filename
     * @throws Exception
     */
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

    /**
     * Text for including original message into reply
     *
     * @param sourceMail
     * @return
     */
    public static String getReplyText(EMail sourceMail, String header, String indent) {
        String h = "------------------------------";
        if(header != null & !"".equals(header))
          h = "-----"+header+"-----";

        return  "\n\n"+indent+h+"\n"+indent+"From: "+sourceMail.getSender()+ "\n" +
                indent+"Sent: "+sourceMail.getTime() +"\n"+
                indent+"To: "+sourceMail.getRecipients()+"\n"+
                indent+"Subject: "+ sourceMail.getSubject()+"\n"+indent+"\n"+
                indent+sourceMail.getText();
    }


    public static String getReplyText(EMail sourceMail) {
        return  getReplyText(sourceMail, "", "");
    }

    public static void removeFromListAndSaveFile (List<EMail> emailList, String filename, EMail selectedEmail) throws Exception {
        File f = new File(filename);
        synchronized (emailList) {
            emailList.removeIf(eMail -> eMail.getId().equals(selectedEmail.getId()));
            Utilities.saveEmailCsvToFile(emailList, filename);
        }
    }

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

    public static List<EMail> readNewEmailsFromStringList(List<String> stringList) {
        List<EMail> list = new ArrayList<>(stringList.size());

        for (String s: stringList) {
            EMail e = EMail.parseEmail(s);
            list.add(e);
        }
        return list;
    }

}
