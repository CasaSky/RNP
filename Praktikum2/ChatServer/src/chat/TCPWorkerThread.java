/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author sasa
 */

class TCPWorkerThread extends Thread {
   /*
    * Arbeitsthread, der eine existierende Socket-Verbindung zur Bearbeitung
    * erhaelt
    */
   private ChatRaum chatraum;

   /* Befehl Konstanten*/
   private final String MESSAGE="message", USERNAME="username", STATUS="status", CLIENTS="clients", LOGOUT="logout"; 
   private final int name;
   private final Socket socket;
   private final TCPServer server;
   private BufferedReader inFromClient;
   private DataOutputStream outToClient;
   private boolean workerServiceRequested = true; // Arbeitsthread beenden?

   public TCPWorkerThread(int num, Socket sock, TCPServer server, ChatRaum chatraum) {
      /* Konstruktor */
      this.chatraum = chatraum;
      this.name = num;
      this.socket = sock;
      this.server = server;
   }

   public void run() {

      System.out.println("TCP Worker Thread " + name +
            " is running until QUIT is received!");

      try {
         /* Socket-Basisstreams durch spezielle Streams filtern */
         inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
         outToClient = new DataOutputStream(socket.getOutputStream());

        // Socket bleibt offen -> checkDataFromClient wird solange aufgerufen werden, bis workerServiceRequested = false -> das heißt der Benutzer möchte sich abmelden -> Danach wird Socket geschloßen 
         while (workerServiceRequested) {
            /* prüft eingehende Data vom Client und erledigt die Anforderung */
            checkDataFromClient();
         }
          /* Socket-Streams schliessen --> Verbindungsabbau */
         socket.close();
      } catch (IOException e) {
         System.err.println("Connection aborted by client!");
      } finally {
         System.out.println("TCP Worker Thread " + name + " stopped!");
         /* Platz fuer neuen Thread freigeben */
			server.workerThreadsSem.release();         
      }
   }

   // Prüft ob das gelieferte Data vom Client legal ist und ruft entsprechend die jeweiligen Funktionen für jeden Befehl auf
   private void checkDataFromClient() throws IOException {
       String data = readFromClient(); // liest data vom CLient
       String befehl;
       String inhalt;
       String[] splittedData = data.split(" ", 2);

        if(splittedData.length != 2) {
            System.err.println("Data Format incompatibel. Hinweis siehe Protokoll!");
            return;
        }
        befehl = splittedData[0];
        inhalt = splittedData[1];
        switch (befehl) {
            case USERNAME:
                if (chatraum.getTeilnehmer().containsKey(this)) { // Falls der WorkerThread bereits im Chat ist
                    writeToClient("You already logged in");
                    System.err.println("You already logged in");
                    sendUsernameNotOk();
                    workerServiceRequested = false;
                }
                else if (!inhalt.contains(" ") && chatraum.usernameCheck(inhalt)) { // Falls Benutzername keine Leerezeichen enthaelt und nicht bereits im Chat vorhanden
                        chatraum.addTeilnehmer(this, inhalt);
                        System.err.println("New user joined!");
                        sendUsernameOk();
                        sendChatroomUsers();
                }
                else { // Falls der Name bereits existiert
                    sendUsernameNotOk();
                    workerServiceRequested= false;
                }
            break;
            case MESSAGE: sendMessagetoClients(inhalt);
            break;
            case CLIENTS: sendChatroomUsers();
            break;
            case LOGOUT: chatraum.deleteTeilnehmer(this); sendChatroomUsers(); sendLogoutOK(inhalt);
            break;
            default: System.err.println("Befehl wurde nicht erkannt!");//throw new IOException("Befehl wurde nicht erkannt");
            break;
        }
   }
   
   // leitet die erhaltene Nachricht vom Client an allen Clients weiter
   private void sendMessagetoClients(String inhalt) throws IOException {
       Collection<TCPWorkerThread> keys = chatraum.getTeilnehmer().keySet();
       Iterator<TCPWorkerThread> it = keys.iterator();
       while (it.hasNext()) {
            it.next().writeToClient(MESSAGE+" "+inhalt);
       }
   }
   
   // der Client der nach Users abfragt bekommt das Ergebniss
   private void sendChatroomUsers() throws IOException {
       String messageToSend = CLIENTS;
       ArrayList<String> users = chatraum.getAllUsernames();
       Collection<TCPWorkerThread> keys = chatraum.getTeilnehmer().keySet();
       for (String username : users) {
           messageToSend += " "+username;
       }
       
       Iterator<TCPWorkerThread> it = keys.iterator();
       while (it.hasNext())
           it.next().writeToClient(messageToSend);
   }
   
   private String readFromClient() throws IOException {
      /* Lies die naechste Anfrage-Zeile (request) vom Client */
      String request = inFromClient.readLine();
      System.out.println("TCP Worker Thread " + name + " detected job: " + request);
      return request;
   }

   private void writeToClient(String reply) throws IOException {
      /* Sende den String als Antwortzeile (mit CRLF) zum Client */
      //outToClient.writeBytes(reply + '\r' + '\n');
       outToClient.write((reply + '\r' + '\n').getBytes(Charset.forName("UTF-8")));
      System.out.println("TCP Worker Thread " + name +
            " has written the message: " + reply);
   }

    private void sendLogoutOK(String inhalt) throws IOException {
        String logOutReply = LOGOUT+" "+inhalt;
        writeToClient(logOutReply);
        workerServiceRequested = false;
    }

    private void sendUsernameNotOk() throws IOException {
        String notok = USERNAME+" notok";
        writeToClient(notok);
    }
    
    private void sendUsernameOk() throws IOException {
        String ok = USERNAME+" ok";
        writeToClient(ok);
    }
}

