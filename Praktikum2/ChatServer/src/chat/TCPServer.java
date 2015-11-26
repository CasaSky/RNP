package chat;
/*
 * TCPServer.java
 *
 * Version 3.1
 * Autor: M. Huebner HAW Hamburg (nach Kurose/Ross)
 * Zweck: TCP-Server Beispielcode:
 *        Bei Dienstanfrage einen Arbeitsthread erzeugen, der eine Anfrage bearbeitet:
 *        einen String empfangen, in Grossbuchstaben konvertieren und zuruecksenden
 *        Maximale Anzahl Worker-Threads begrenzt durch Semaphore
 *  
 */
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TCPServer {
   /* TCP-Server, der Verbindungsanfragen entgegennimmt */
    
   
   /*ChatRaum enthält die Hashmap Teilnehmer<Socket, String> wo die username und die Socket gespeichert werden können */
   private static ChatRaum chatraum;
    /* Semaphore begrenzt die Anzahl parallel laufender Worker-Threads  */
   public Semaphore workerThreadsSem;

   /* Portnummer */
   public final int serverPort;
   
   /* Anzeige, ob der Server-Dienst weiterhin benoetigt wird */
   public boolean serviceRequested = true;
		 
   /* Konstruktor mit Parametern: Server-Port, Maximale Anzahl paralleler Worker-Threads*/
   public TCPServer(int serverPort, int maxThreads) {
      this.serverPort = serverPort;
      this.workerThreadsSem = new Semaphore(maxThreads);
      chatraum = new ChatRaum();
   }

   public void startServer() {
      ServerSocket welcomeSocket; // TCP-Server-Socketklasse
      Socket connectionSocket; // TCP-Standard-Socketklasse

      int nextThreadNumber = 0;

      try {
         /* Server-Socket erzeugen */
         welcomeSocket = new ServerSocket(serverPort);

         while (serviceRequested) { 
				workerThreadsSem.acquire();  // Blockieren, wenn max. Anzahl Worker-Threads erreicht
				
            System.out.println("TCP Server is waiting for connection - listening TCP port " + serverPort);
            /*
             * Blockiert auf Verbindungsanfrage warten --> nach Verbindungsaufbau
             * Standard-Socket erzeugen und an connectionSocket zuweisen
             */
            connectionSocket = welcomeSocket.accept();

            /* Neuen Arbeits-Thread erzeugen und die Nummer, den Socket sowie das Serverobjekt uebergeben */
            (new TCPWorkerThread(++nextThreadNumber, connectionSocket, this, chatraum)).start();
          }
      } catch (Exception e) {
         System.err.println(e.toString());
      }
   }

   public static void main(String[] args) {
      /* Erzeuge Server und starte ihn */
      TCPServer myServer = new TCPServer(56789, 2);
      myServer.startServer();
   }
}

// ----------------------------------------------------------------------------

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
   private DataOutputStream outToClients; // OutputStream für alle Clients
   boolean workerServiceRequested = true; // Arbeitsthread beenden?

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
         inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         outToClient = new DataOutputStream(socket.getOutputStream());
//         outToClients = outToClient;
        
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

        befehl = splittedData[0];
        inhalt = splittedData[1];
        switch (befehl) {
            case USERNAME:  
                if (chatraum.usernameCheck(inhalt)){
                    // Statt Soket Thread
                    chatraum.addTeilnehmer(socket, inhalt);
                    sendUsernameOk();
//           try {
//               sleep(3000);
//           } catch (InterruptedException ex) {
//               Logger.getLogger(TCPWorkerThread.class.getName()).log(Level.SEVERE, null, ex);
//           }
                    sendChatroomUsers();
                }
                else { sendUsernameNotOk(); workerServiceRequested= false;}
            break;
            case MESSAGE: sendMessagetoClients(inhalt);
            break;
            case CLIENTS: sendChatroomUsers();
            break;
            case LOGOUT: chatraum.deleteTeilnehmer(socket); sendLogoutOK(inhalt);
            break;
            default: System.err.println("Befehl wurde nicht erkannt!");//throw new IOException("Befehl wurde nicht erkannt");
            break;
        }
   }
   
   // leitet die erhaltene Nachricht vom Client an allen Clients weiter
   private void sendMessagetoClients(String inhalt) throws IOException {
       
       Collection<Socket> keys = chatraum.getTeilnehmer().keySet();
       Iterator<Socket> it = keys.iterator();
       while (it.hasNext()) {
            outToClients = new DataOutputStream(it.next().getOutputStream());
            writeToClients(MESSAGE+" "+inhalt);
       }
   }
   
   // der Client der nach Users abfragt bekommt das Ergebniss
   private void sendChatroomUsers() throws IOException {
       String messageToSend = CLIENTS;
       ArrayList<String> users = chatraum.getAllUsernames();
       Collection<Socket> keys = chatraum.getTeilnehmer().keySet();
       Iterator<Socket> it = keys.iterator();
       for (String username : users) {
           messageToSend += " "+username;
       }
        outToClients = new DataOutputStream(it.next().getOutputStream());
        writeToClients(messageToSend);  
   }
   
   private String readFromClient() throws IOException {
      /* Lies die naechste Anfrage-Zeile (request) vom Client */
      String request = inFromClient.readLine();
      System.out.println("TCP Worker Thread " + name + " detected job: " + request);
      return request;
   }

   private void writeToClient(String reply) throws IOException {
      /* Sende den String als Antwortzeile (mit CRLF) zum Client */
      outToClient.writeBytes(reply + '\r' + '\n');
      System.out.println("TCP Worker Thread " + name +
            " has written the message: " + reply);
   }
   
   // write über den OutputStream der für alle Clients gedacht ist
    private void writeToClients(String reply) throws IOException {
      /* Sende den String als Antwortzeile (mit CRLF) zum Client */
      outToClients.writeBytes(reply + '\r' + '\n');
      System.out.println("TCP Worker Thread " + name +
            " has written the message: " + reply);
   }

    private void sendLogoutOK(String inhalt) throws IOException {
        String logOutReply = LOGOUT+" "+inhalt;
        writeToClient(logOutReply);
        System.out.println("TCP Worker Thread " + name +
            " has written the message: " + logOutReply);
        workerServiceRequested = false;
    }

    private void sendUsernameNotOk() throws IOException {
        String notok = USERNAME+" notok";
        writeToClient(notok);
        System.out.println("TCP Worker Thread " + name +
            " has written the message: " + notok);
    }
    
    private void sendUsernameOk() throws IOException {
        String ok = USERNAME+" ok";
        writeToClient(ok);
        System.out.println("TCP Worker Thread " + name +
            " has written the message: " + ok);
    }
}
