package SMTP;

import Data.UserData;
import base64Encode.EncodeWithBase64;
import LogFile.*;
import sun.security.ssl.SSLSocketImpl;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;

/**
 * Created by Talal on 01.11.2015.
 */
public class SMTPClient {

    LogFileUtils logFile;
    final String NEWLINE = "\r\n";
    //Commands
    private String helo, auth, loginDataInBase64, mailFrom, recpTo, data, from, to, subject, message, mimeVersion,
            contentType, contentType2, attachment, contentTransfer, contentTransfer2, filepathInBase64, boundary;
    /* Portnummer */
    private final int serverPort;

    /* Hostname */
    private final String hostname;

    private Socket clientSocket; // TCP-Standard-Socketklasse
    private SSLSocketFactory factory;

    private DataOutputStream outToServer; // Ausgabestream zum Server
    private BufferedReader inFromServer; // Eingabestream vom Server

    private boolean serviceRequested = true; // Client beenden?

    private final UserData userData;
    private final String recepientEmail;
    private final String filepath;

    public SMTPClient(UserData userData, String recepientEmail, String filepath) throws IOException{
        logFile = LogFileUtils.createLogFile();
        this.userData = userData;
        this.recepientEmail = recepientEmail;
        this.filepath = filepath;
        this.serverPort = userData.getPort();
        this.hostname = userData.getHostname();
    }

    public void prepareCommands() throws IOException{
        helo = ("HELO "+userData.getHostname());
        loginDataInBase64 = EncodeWithBase64.encodeString("\0"+userData.getUsername()+"\0"+userData.getPassword());
        auth = ("AUTH PLAIN "+loginDataInBase64);
        mailFrom = ("MAIL FROM: "+userData.getMailAdresse());
        recpTo = ("RCPT TO: "+recepientEmail);
        data=("DATA");
        from = ("From: "+userData.getMailAdresse());
        to = ("To: "+recepientEmail);
        subject = ("Subject:"+userData.getSubject());
        message = userData.getMessage();
        mimeVersion = ("MIME-Version: 1.0");
        boundary = Long.toString(System.nanoTime());
        contentType = ("Content-Type: multipart/mixed; boundary="+boundary);
        File file = new File(filepath); // so bekommt man den Filenamen
        attachment = ("Content-Disposition: attachment; filename="+file.getName()+";");
        contentType2 =("Content-Type: text/plain");
        contentTransfer = ("Content-Transfer-Encoding: base64");
        contentTransfer2 =("Content-Transfer-Encoding: quoted-printable");


        filepathInBase64 = (EncodeWithBase64.encodeFile(filepath));
    }

    public void startJob() {
        /* Client starten. Ende, wenn quit eingegeben wurde */

        try {
            /* Socket erzeugen --> Verbindungsaufbau mit dem Server */
            if (serverPort == 25) {
                clientSocket = new Socket(hostname, serverPort);
            }
            else {
                factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                clientSocket = (SSLSocket) factory.createSocket(hostname, serverPort);
            }

            /* Socket-Basisstreams durch spezielle Streams filtern */
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream()));

            System.out.println("Start Transfer Data: ");
            prepareCommands();
            System.out.println(readFromServer());
            writeToServer(helo);
            if (getStatusCode()!=250)
                throw new IOException("Connexion Failed!");
            writeToServer(auth);
            if (getStatusCode()!=235)
                throw new IOException("Authentification Failed!");
            writeToServer(mailFrom);
            if (getStatusCode()!=250)
                throw new IOException("Bad Mail!");
            writeToServer(recpTo);
            if (getStatusCode()!=250)
                throw new IOException("Bad Mail");
            writeToServer(data);
            if (getStatusCode()!=354)
                throw new IOException("Data Failed!");
            writeToServer(from);
            writeToServer(to);
            writeToServer(subject);
            writeToServer(mimeVersion);
            writeToServer(contentType);
            writeToServerNewLine();
            writeToServer("--" + boundary);
            writeToServer(contentTransfer2);
            writeToServer(contentType2);
            writeToServerNewLine();

           message = message.replaceAll("\n.","\n..");
            if (message.startsWith("."))
                message = "."+message;
            writeToServer(message);


            writeToServer("--" + boundary);
            writeToServer(contentTransfer);
            writeToServer(attachment);
            writeToServerNewLine();
            writeToServer(filepathInBase64);
            writeToServer("--" + boundary + "--");
            writeToServer(".");

            if (getStatusCode()!=250)
                throw new IOException("cannot find the end of the message!");
            writeToServer("QUIT");
            if (getStatusCode()!=221)
                throw new IOException("Cannot quit server");

            /* Socket-Streams schliessen --> Verbindungsabbau */
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Connection aborted by server!");
        }
        System.out.println("TCP Client stopped!");
    }

    private void writeToServer(String request) throws IOException {
        /* Sende eine Zeile (mit CRLF) zum Server */
        outToServer.writeBytes(request + '\r' + '\n');
        System.out.println("TCP Client has sent the message: " + request);
        logFile.protokolliere(request);
    }

    private void writeToServerNewLine() throws IOException {
        /* Sende eine Zeile (mit CRLF) zum Server */
        outToServer.writeBytes(NEWLINE);
        System.out.println("TCP Client has sent the message: ");
        logFile.protokolliere(NEWLINE);
    }

    private String readFromServer() throws IOException {
        /* Lies die Antwort (reply) vom Server */
        String reply = inFromServer.readLine();
        System.out.println("TCP Client got from Server: " + reply);
        logFile.protokolliere(reply);
        return reply;
    }

    private int getStatusCode() throws IOException{
        String reply = readFromServer();
        return Integer.parseInt(reply.substring(0, 3));
    }

}