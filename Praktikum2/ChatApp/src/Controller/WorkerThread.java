/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import Client.TCPClient;

/**
 *
 * @author sasa
 */
public class WorkerThread extends Thread{

    TCPClient client;
    String message;
    
    
    public WorkerThread(TCPClient client, String message) {
        this.client = client;
        this.message = message;
    }
    
   
    @Override
    public void run() {
        client.sendMessage(message);
    }
    
}
