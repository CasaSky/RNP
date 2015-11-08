import Data.UserData;
import SMTP.SMTPClient;

import java.io.IOException;


/**
 * Created by Talal on 31.10.2015.
 */
public class MailFile {

    private String recmailadresse;
    private String filepath;
    private SMTPClient smtpClient;
    private final String configurationFile = "config2.xml";

    public MailFile(String recmailadresse, String filepath) throws IOException{

        this.recmailadresse = recmailadresse;
        this.filepath = filepath;
        startSending();

    }

    public void startSending() throws IOException{
        UserData userData = new UserData(configurationFile);
        System.out.println(userData.toString());
        smtpClient = new SMTPClient(userData, getRecmailadresse(), getFilepath());
        smtpClient.startJob();
    }

    public String getRecmailadresse() {
        return recmailadresse;
    }

    public String getFilepath() {
        return filepath;
    }

    public static void main(String[] args) throws IOException{

        MailFile mailFile = new MailFile("talal.tabia@haw-hamburg.de", "Z:\\5.semester\\Internet_Protokolle.docx");
        //MailFile mailFile = new MailFile(args[0], args[1]);
    }
}
