package commons;

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

public class FileHandler {
    private File f;
    private String filename;
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final Lock readLock = readWriteLock.readLock();

    private final Lock writeLock = readWriteLock.writeLock();

    public FileHandler(String filename) {
        this.filename = filename;
        this.f = new File(filename);

        if(!f.exists() || !f.isFile()) {
            throw new IllegalArgumentException("file must exist");
        }
    }

    public void add(String id, String eMail) {
        PrintWriter pw = null;
        writeLock.lock();
        try {
            pw = new PrintWriter(new FileWriter(f, true), true);
            pw.println(eMail);

        } catch (IOException e) {
            System.out.println(getClass().getName()+" cannot add email "+id+" to file "+filename+" "+e.getMessage());
        } finally {
            if(pw != null) {
                pw.close();
            }
            writeLock.unlock();
        }
    }

    public void add(EMail eMail) {
        add(eMail.getId(), eMail.toString());
    }

    public void addAll(List<EMail> l) {
        PrintWriter pw = null;
        writeLock.lock();
        try {
            pw = new PrintWriter(new FileWriter(f, true), true);
            for(EMail e: l)
                pw.println(e.toString());

        } catch (IOException e) {
            System.out.println(getClass().getName()+" cannot add obj to file "+filename+" "+e.getMessage());
        } finally {
            if(pw != null) {
                pw.close();
            }
            writeLock.unlock();
        }
    }

    public void saveList(List<EMail> l) {
        PrintWriter pw = null;
        writeLock.lock();
        try {
            pw = new PrintWriter(new FileWriter(f), true);
            for(EMail obj: l)
                pw.println(obj.toString());

        } catch (IOException e) {
            System.out.println(getClass().getName()+" cannot save list to file "+filename+" "+e.getMessage());
        } finally {
            if(pw != null) {
                pw.close();
            }
            writeLock.unlock();
        }
    }

    public boolean remove(EMail eToDelete) {
        return remove(eToDelete.getId());
    }

    public boolean remove(String mailID) {
        Scanner mailIn =null;
        PrintWriter pw = null;
        List<EMail> list = new ArrayList<>();
        boolean deleted = false;

        writeLock.lock();
        try {
            if (f.length()!=0) {
                mailIn = new Scanner(f);
                while (mailIn.hasNextLine()) {
                    String tmp = mailIn.nextLine();
                    EMail mailA = EMail.parseEmail(tmp);

                    if(!mailID.equals(mailA.getId())){
                        list.add(mailA);
                    }
                    else {
                        deleted = true;
                    }

                }
                pw = new PrintWriter(new FileWriter(f), true);
                for(EMail obj: list)
                    pw.println(obj.toString());

            }
        } catch (IOException e) {
            System.out.println(getClass().getName()+" cannot remove email from file "+filename+" "+e.getMessage());
        }  finally {
            writeLock.unlock();
            return  deleted;
        }

    }

    public List<EMail> readList() {
        return readList(null);
    }

    public List<EMail> readList(String timeStamp) {
        Scanner mailIn =null;
        List<EMail> list = new ArrayList<>();



        readLock.lock();
        try {
            Date timestampDate = null;
            if(timeStamp != null)
                timestampDate= Utilities.DATE_FORMAT.parse(timeStamp);

            if (f.length()!=0) {
                mailIn = new Scanner(f);
                while (mailIn.hasNextLine()) {
                    String tmp = mailIn.nextLine();
                    EMail mail = EMail.parseEmail(tmp);


                    if(timeStamp != null) {
                        Date emailDate = Utilities.DATE_FORMAT.parse(mail.getTime());
                        if(emailDate.after(timestampDate))
                            list.add(mail);
                    }
                    else {
                        list.add(mail);
                    }


                }

            }

        } catch (FileNotFoundException | ParseException e) {
            System.out.println(getClass().getName()+" cannot get list from file "+filename+" "+e.getMessage());
        } finally {
            if(mailIn!= null) {
                mailIn.close();
            }
            readLock.unlock();
        }
        return list;
    }

    public List<String> readListString() {
        return readListString(null);
    }

    public List<String> readListString(String timeStamp) {
        Scanner mailIn =null;
        List<String> list = new ArrayList<>();



        readLock.lock();
        try {
            Date timestampDate = null;
            if(timeStamp != null)
                timestampDate= Utilities.DATE_FORMAT.parse(timeStamp);

            if (f.length()!=0) {
                mailIn = new Scanner(f);
                while (mailIn.hasNextLine()) {
                    String tmp = mailIn.nextLine();
                    EMail mail = EMail.parseEmail(tmp);

                    if(timeStamp != null) {
                        Date emailDate = Utilities.DATE_FORMAT.parse(mail.getTime());
                        if(emailDate.after(timestampDate))
                            list.add(tmp);
                    }
                    else {
                        list.add(tmp);
                    }


                }

            }

        } catch (FileNotFoundException | ParseException e) {
            System.out.println(getClass().getName()+" cannot get list from file "+filename+" "+e.getMessage());
        } finally {
            if(mailIn!= null) {
                mailIn.close();
            }
            readLock.unlock();
        }
        return list;
    }

}
