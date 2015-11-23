package Server;
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
import com.sun.org.apache.bcel.internal.generic.SWITCH;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import javax.imageio.IIOException;


public class TCPServer {
   /* TCP-Server, der Verbindungsanfragen entgegennimmt */
    
   
   
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
            (new TCPWorkerThread(++nextThreadNumber, connectionSocket, this)).start();
          }
      } catch (Exception e) {
         System.err.println(e.toString());
      }
   }

   public static void main(String[] args) {
      /* Erzeuge Server und starte ihn */
      TCPServer myServer = new TCPServer(56789, 1);
      myServer.startServer();
   }
}

// ----------------------------------------------------------------------------

class TCPWorkerThread extends Thread {
   /*
    * Arbeitsthread, der eine existierende Socket-Verbindung zur Bearbeitung
    * erhaelt
    */
    
    /*ChatRaum enthält die Hashmap Teilnehmer<Socket, String> wo die username und die Socket gespeichert werden können */
    private ChatRaum chatraum;
   /* Befehl Konstanten*/
   private final String MESSAGE="message", USERNAME="username", STATUS="status", CLIENT="client"; 
   private int name;
   private Socket socket;
   private TCPServer server;
   private BufferedReader inFromClient;
   private DataOutputStream outToClient;
   boolean workerServiceRequested = true; // Arbeitsthread beenden?

   public TCPWorkerThread(int num, Socket sock, TCPServer server) {
      /* Konstruktor */
      chatraum = new ChatRaum();
      this.name = num;
      this.socket = sock;
      this.server = server;
   }

   public void run() {
      String capitalizedSentence;

      System.out.println("TCP Worker Thread " + name +
            " is running until QUIT is received!");

      try {
         /* Socket-Basisstreams durch spezielle Streams filtern */
         inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         outToClient = new DataOutputStream(socket.getOutputStream());

         while (workerServiceRequested) {
            /* String vom Client empfangen und in Grossbuchstaben umwandeln */
            checkDataFromClient();
            capitalizedSentence = readFromClient();

            /* Modifizierten String an Client senden */
            writeToClient(capitalizedSentence);

            /* Test, ob Arbeitsthread beendet werden soll */
            if (capitalizedSentence.startsWith("QUIT")) {
               workerServiceRequested = false;
            }
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

   // Prüft ob das gelieferte Data vom Client legal ist und ruft entsprechend die jeweiligen Funktionen für jeden Befehl
   private void checkDataFromClient() throws IOException {
       String data = readFromClient(); // liest data vom CLient
       String befehl;
       String inhalt;
       String[] splittedData = data.split(" ", 2);

        befehl = splittedData[0];
        inhalt = splittedData[1];
        switch (befehl) {
            case USERNAME:  if (chatraum.usernameCheck(inhalt)) chatraum.addTeilnehmer(socket, inhalt);
            break;
            case MESSAGE: // message function(inhalt);
            break;
            case CLIENT: // client function(inhalt);
            break;
            default: System.err.println("Befehl wurde nicht erkannt!");//throw new IOException("Befehl wurde nicht erkannt");
            break;
        }
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
}
