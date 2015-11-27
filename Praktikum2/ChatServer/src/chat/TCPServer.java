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
import java.net.*;
import java.util.concurrent.*;

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

//   public static void main(String[] args) {
//      /* Erzeuge Server und starte ihn */
//      TCPServer myServer = new TCPServer(56789, 2);
//      myServer.startServer();
//   }
}

// ----------------------------------------------------------------------------
