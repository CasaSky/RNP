package Client;
/*
 * TCPClient.java
 * Autor: sasa
 */

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TCPClient extends Thread{
    
    private final String USERNAME="username";
    private final String MESSAGE="message";
    private final String CLIENTS="clients";
    private final String LOGOUT="logout";
    
    private boolean ready = false;
    
    private boolean logout = false;
    private boolean usernameValid = false;

    //private boolean messageReceived=false;
    private String username;
    private boolean newUserJoined=false;
    
    //
    private String chatroomUsers;

    /* Portnummer */
    private final int serverPort;

    /* Hostname */
    private final String hostname;
    private String message;

    private Socket clientSocket; // TCP-Standard-Socketklasse

    private DataOutputStream outToServer; // Ausgabestream zum Server
    private BufferedReader inFromServer; // Eingabestream vom Server

    private boolean serviceRequested = true; // Client beenden?

    public TCPClient(String hostname, int serverPort, String username) {
        this.serverPort = serverPort;
        this.hostname = hostname;
        this.username = username;   
    }

    @Override
    public void run() {
        /* Client starten. Ende, wenn quit eingegeben wurde */
        String modifiedSentence; // vom Server modifizierter String

        try {
            /* Socket erzeugen --> Verbindungsaufbau mit dem Server */
            clientSocket = new Socket(hostname, serverPort);

            /* Socket-Basisstreams durch spezielle Streams filtern */
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream(), "UTF-8"));
            
            //sendet den Benutzernamen
            sendUsername();
            
            while(serviceRequested) {
            try{
                checkDataFromServer();
            }catch (IOException e) {
                writeToServer("Fehler beim Empfangen von Data");
                System.err.println("Fehler beim Empfangen von Data");
                JOptionPane.showMessageDialog(null, "Fehler beim Empfangen von Data, Sie werden abgemeldet...");
                sendLogout(username);
            }
            }
            /* Socket-Streams schliessen --> Verbindungsabbau */
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Connection aborted by server!");
        }
        System.out.println("TCP Client stopped!");
    }

    private synchronized void checkDataFromServer() throws IOException {
       String data = readFromServer(); // liest data vom CLient
       String befehl;
       String inhalt;
       String[] splittedData = data.split(" ", 2);

        befehl = splittedData[0];
        inhalt = splittedData[1];
        switch (befehl) {
            case USERNAME: 
                if (inhalt.equals("ok")) 
                    usernameValid=true;
                else
                    serviceRequested = false;
            break;
            case MESSAGE: 
                this.message = inhalt;
                ready = true;
                this.notifyAll();
            break;
            case CLIENTS: 
                newUserJoined=true;
                chatroomUsers(inhalt);
                this.notifyAll();
            break;
            case LOGOUT:
                serviceRequested = false;
                ready=true;
                logout=true;
                this.notifyAll();
            break;
            default: System.err.println("Befehl wurde nicht erkannt!");//throw new IOException("Befehl wurde nicht erkannt");
            break;
        }
   }

    private void writeToServer(String request) throws IOException {
        /* Sende eine Zeile (mit CRLF) zum Server */
        //String newRequest;
        //newRequest = replaceUmlaut(request);
        outToServer.write((request + '\r' + '\n').getBytes(Charset.forName("UTF-8")));
        //System.out.println("TCP Client has sent the message: " + request);
    }

    private String readFromServer() throws IOException {
        /* Lies die Antwort (reply) vom Server */
        String reply = inFromServer.readLine();
        System.out.println("TCP Client got from Server: " + reply);
        return reply;
    }
    
    public void sendMessage(String message) {
        try {
            writeToServer(MESSAGE+ " " + username + " >> " + message);
            System.out.println("TCP Client has sent the Message: "+ message);
        } catch (IOException ex) {
            Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendUsername() { 
        try {
            writeToServer(USERNAME+ " " +this.username);
            System.out.println("TCP Client has sent the username: "+ this.username);
        } catch (IOException ex) {
            Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendLogout(String username) {
        try {
            writeToServer(LOGOUT+" "+username);
            System.out.println("TCP Client has sent a logout command for: "+ this.username);
        } catch (IOException ex) {
            Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public boolean logoutOk() {
        return logout;
    }
    
    public void sendClients(){
        try {
            writeToServer(CLIENTS+ " " +this.username);
            System.out.println("TCP Client has sent the chatroom Clients and "+ this.username);
                     
        } catch (IOException ex) {
            Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getMessage() {
        return this.message;
    }

    //public boolean isMessageReceived() { return messageReceived; }

    //public void setMessageReceived(boolean messageReceived) { this.messageReceived = messageReceived; }
    
    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public boolean isReady() {
        return ready;
    }

    public String getChatroomUser() {
        return chatroomUsers;
    }

    private void chatroomUsers(String inhalt) {
        chatroomUsers = inhalt.replaceAll(" ", "\n");        
    }
    
    public boolean isUsernameValid() {
        return usernameValid;
    }
    
    public String getChatroomUsers() {
        return chatroomUsers;
    }
    
    public boolean isNewUserJoined() {
        return newUserJoined;
    }
 
    public void setNewUserJoined(boolean f) {
        this.newUserJoined = false;
    }
}