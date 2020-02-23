package commons;

public class SystemLogger {

    private Class theClass;

    public SystemLogger(Class c) {
        this.theClass = c;
    }

    public void fatal(String message) {
        log("FATAL", message);
    }
    public void error(String message) {
        log("ERROR", message);
    }
    public void warn(String message) {
        log("WARN", message);
    }
    public void info(String message) {
        log("INFO", message);
    }
    public void debug(String message) {
        log("INFO", message);
    }

    public void log(String level, String message) {
        System.out.println(Utilities.dateStringMillis()+" ["+theClass.getName()+"]("+Thread.currentThread().getName()+") "+level+" "+message);
    }

}
