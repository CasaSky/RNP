/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

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
        if (teilnehmer==null)
            throw new NullPointerException("Chat hat keine Teilnehmer!");
        return teilnehmer;
    }
    
    public ArrayList<String> getAllUsernames() {
        ArrayList<String> result = new ArrayList<>();
        Collection<String> values = teilnehmer.values();
        Iterator<String> it = values.iterator();
        while (it.hasNext())
            result.add(it.next());
        if (result==null)
            throw new NullPointerException("Username list ist im Chat nicht verfügbar!");
        return result;
    } 
}
