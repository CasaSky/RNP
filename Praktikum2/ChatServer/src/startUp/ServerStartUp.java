/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package startUp;

import chat.TCPServer;

/**
 *
 * @author sasa
 */
public class ServerStartUp {
    
    public static void main(String[] args) {
        /* Erzeuge Server und starte ihn */
        TCPServer myServer = new TCPServer(56789, 2);
        myServer.startServer();
   }
    
}
