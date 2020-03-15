package commons;

public class SystemLogger {

    private Class theClass;



    //Costruttore di classe
    public SystemLogger(Class c) {
        this.theClass = c;
    }


    public void log(String message) {
        System.out.println(Utilities.dateStringMillis()+" ["+theClass.getName()+"]("+Thread.currentThread().getName()+") "+message);
    }



}
