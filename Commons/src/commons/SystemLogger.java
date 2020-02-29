package commons;

public class SystemLogger {

    private Class theClass;
    private int logLevel = LEVEL_OFF;

    public static int LEVEL_OFF     = 0;
    public static int LEVEL_FATAL   = 1;
    public static int LEVEL_ERROR   = 2;
    public static int LEVEL_WARN    = 3;
    public static int LEVEL_INFO    = 4;
    public static int LEVEL_DEBUG   = 5;



    public SystemLogger(Class c) {
        this.theClass = c;
        String l = System.getProperty("logLevel");
        if(l != null) {
            try {
                logLevel = Integer.parseInt(l);
            }
            catch (Exception e) {

            }
        }
    }

    public void fatal(String message) {
        if(logLevel >= LEVEL_FATAL)
            log("FATAL", message);
    }
    public void error(String message) {
        if(logLevel >= LEVEL_ERROR)
            log("ERROR", message);
    }
    public void warn(String message) {
        if(logLevel >= LEVEL_WARN)
            log("WARN", message);
    }
    public void info(String message) {
        if(logLevel >= LEVEL_INFO)
            log("INFO", message);
    }
    public void debug(String message) {
        if(logLevel >= LEVEL_DEBUG)
            log("DEBUG", message);
    }

    private void log(String level, String message) {
        System.out.println(Utilities.dateStringMillis()+" "+level+" ["+theClass.getName()+"]("+Thread.currentThread().getName()+") "+message);
    }

}
