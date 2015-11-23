/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.net.Socket;
import java.util.HashMap;

/**
 *
 * @author talal
 */
public class ChatRaum {
    
    // Teilnehmer besteht aus Sitzung(Socket) und Username(String)
    private HashMap<Socket, String> teilnehmer;

    public ChatRaum() {
        teilnehmer = new HashMap<>();
    }
    
    //fügt socket, username als teilnehmer 
    public void addTeilnehmer(Socket socket, String username) {
        if (socket != null && username != null)
            teilnehmer.put(socket, username);
        else throw new NullPointerException("Socket oder username darf nicht null sein!");
    }
    
    // Prüft ob username nicht vorhanden ist
    public boolean usernameCheck(String username) {
        
        boolean result;
        if (username != null)
            result = !teilnehmer.containsValue(username);
        else 
            throw new NullPointerException("Username darf nicht null sein!");
        return result;
    }
    
    public HashMap<Socket, String> getTeilnehmer() {
        return teilnehmer;
    }
    
}
