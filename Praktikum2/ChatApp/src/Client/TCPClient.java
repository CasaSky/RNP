package Client;
/*
 * TCPClient.java
 * Autor: sasa
 */

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TCPClient extends Thread{
    
    private final String USERNAME="username";
    private final String MESSAGE="message";
    private final String STATUS="status";
    private final String CLIENTS="clients";
    private final String LOGOUT="logout";
    
    private boolean ready = false;
    
    private boolean logout = false;
    private boolean usernameValid = false;

    private boolean messageReceived=false;
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
                    clientSocket.getInputStream()));
            
            //sendet den Benutzernamen
            sendUsername();
            
            //sende eine client teilnehmer anfrage
//            sendClients();
            
            while(serviceRequested) {
            try{
                checkDataFromServer();
            }catch (IOException e) {
                //try {
                    //sleep(3000);
                    //} catch (InterruptedException e1) {
                    //}
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
//            {
                try {
                    sleep(3000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
                }
//            }
            break;
            case CLIENTS: 
                newUserJoined=true;
                System.err.println("Oh new User");
                chatroomUsers(inhalt);
                this.notifyAll();
//            {
                try {
                    sleep(3000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
                }
                //saveChatroomUsers(inhalt);
            break;
            case LOGOUT:
                serviceRequested = false;
                ready=true;
                logout=true;
                System.err.println("JO ich führe das aus");
                this.notifyAll();
                
                try {
                    sleep(3000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            break;
            default: System.err.println("Befehl wurde nicht erkannt!");//throw new IOException("Befehl wurde nicht erkannt");
            break;
        }
   }

    private void writeToServer(String request) throws IOException {
        /* Sende eine Zeile (mit CRLF) zum Server */
        String newRequest;
        newRequest = replaceUmlaut(request);
        outToServer.writeBytes(newRequest + '\r' + '\n');
        //System.out.println("TCP Client has sent the message: " + request);
    }

    private String readFromServer() throws IOException {
        /* Lies die Antwort (reply) vom Server */
        String reply = inFromServer.readLine();
        String newRequest;
        newRequest = replaceBackUmlaut(reply);

        System.out.println("TCP Client got from Server: " + reply);
        return newRequest;
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

    public boolean isMessageReceived() {
        return messageReceived;
    }

    public void setMessageReceived(boolean messageReceived) {
        this.messageReceived = messageReceived;
    }
    
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

//    private void saveChatroomUsers(String inhalt) {
//        String[] splittedData = inhalt.split(" ");
//    }

    
     private String replaceUmlaut(String input) {
 
     //replace all lower Umlauts
     String o_strResult =
             input
             .replaceAll("ü", "ue")
             .replaceAll("ö", "oe")
             .replaceAll("ä", "ae")
             .replaceAll("ß", "ss");
 
     //first replace all capital umlaute in a non-capitalized context (e.g. Übung)
     o_strResult =
             o_strResult
             .replaceAll("Ü(?=[a-zäöüß ])", "Ue")
             .replaceAll("Ö(?=[a-zäöüß ])", "Oe")
             .replaceAll("Ä(?=[a-zäöüß ])", "Ae");
 
     //now replace all the other capital umlaute
     o_strResult =
             o_strResult
             .replaceAll("Ü", "UE")
             .replaceAll("Ö", "OE")
             .replaceAll("Ä", "AE");
 
     return o_strResult;
 }
     
     
     private String replaceBackUmlaut(String input) {
 
     //replace all lower Umlauts
     String o_strResult =
             input
             .replaceAll("ue", "ü")
             .replaceAll("oe", "ö")
             .replaceAll("ae", "ä");
 
     //first replace all capital umlaute in a non-capitalized context (e.g. Übung)
     o_strResult =
             o_strResult
             .replaceAll("Ue", "Ü(?=[a-zäöüß ])")
             .replaceAll("Oe", "Ö(?=[a-zäöüß ])")
             .replaceAll("Ae", "Ä(?=[a-zäöüß ])");
 
     //now replace all the other capital umlaute
     o_strResult =
             o_strResult
             .replaceAll("UE", "Ü")
             .replaceAll("OE", "Ö")
             .replaceAll("AE", "Ä");
 
     return o_strResult;
 }

}