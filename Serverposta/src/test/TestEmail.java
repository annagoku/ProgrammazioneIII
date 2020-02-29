package test;

import commons.EMail;

public class TestEmail {
    public static void main(String[] args) {
        try {
            String s = ";2020-02-29 13:47:15;diana.rossi@mymail.com;laura.derossi@mymail.com, giulietta.bianchi@mymail.com;R: PROVA3;\\nadasdasd\\n------------------------------\\nFrom: laura.derossi@mymail.com\\nSent: 2020-02-29 11:46:35\\nTo: diana.rossi@mymail.com,giulietta.bianchi@mymail.com\\nSubject: PROVA3\\n\\nOK che bello!!\\n\\n------------------------------\\nFrom: diana.rossi@mymail.com\\nSent: 2020-02-23 12:21:51\\nTo: laura.derossi@mymail.com\\nSubject: R: Appuntamento\\n\\nSono libera\\n\\nFrom: laura.derossi@mymail.com\\nSent: 2020-02-13 07:00:00\\nTo: diana.rossi@mymail.com\\nSubject: AppuntamentoSei libera settimana prossima?";
            System.out.println(EMail.parseEmail(s).getTime());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
