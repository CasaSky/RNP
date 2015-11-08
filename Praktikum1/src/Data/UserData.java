package Data;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Talal on 31.10.2015.
 */
public class UserData {

    private String mailAdress;
    private String username;
    private String password;
    private String hostname;
    private int port;
    private String subject, message;

    private UserData() {
    }

    public UserData(String configurationFile) throws IOException{
        Properties properties = new Properties();
        InputStream inputStream = new FileInputStream(configurationFile);
        properties.loadFromXML(inputStream);

        mailAdress = properties.getProperty("mailAddress");
        username = properties.getProperty("username");
        password = properties.getProperty("password");
        hostname = properties.getProperty("hostname");
        port = Integer.valueOf(properties.getProperty("port"));
        message = properties.getProperty("message");
        subject = properties.getProperty("subject");
        inputStream.close();
        System.out.println("hier");
    }

    public String getMailAdresse() {
        return mailAdress;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getHostname() {
        return hostname;
    }

    public String getSubject() {return subject;}

    public String getMessage() {return message;}

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "UserData{" +
                "mailAdress='" + mailAdress + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", hostname='" + hostname + '\'' +
                ", port=" + port +
                '}';
    }
}
