package commons;



import java.io.File;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

public abstract class Utilities {
    public static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public static String dateString() {
        return dateString(Calendar.getInstance().getTime());
    }

    public static String dateString(Date date) {
        return DATE_FORMAT.format(date);
    }

    public static List<EMail> loadMailFromCSV(String filename)   throws  Exception{
        File f=new File(filename);

        List<EMail> list = new ArrayList<>();

        if (f.exists() && f.length()!=0) {
            Scanner mailIn = new Scanner(f);
            while (mailIn.hasNextLine()) {
                String tmp = mailIn.nextLine();
                Scanner l = new Scanner(tmp).useDelimiter("\\s*;\\s*");
                EMail mailA = new EMail(l.next(), l.next(), l.next(), l.next(), l.next(), l.next());
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
                EMail mail = new EMail(l.next(), l.next(), l.next(), l.next(), l.next(), l.next());
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
}
