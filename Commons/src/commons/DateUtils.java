package commons;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public abstract class DateUtils {
    public static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public static String dateString() {
        return dateString(Calendar.getInstance().getTime());
    }

    public static String dateString(Date date) {

        return DATE_FORMAT.format(date);
    }

}
