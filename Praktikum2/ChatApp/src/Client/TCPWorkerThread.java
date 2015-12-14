/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import Client.TCPClient;

/**
 *
 * @author sasa
 */
public class TCPWorkerThread extends Thread{

    private TCPClient client;
    private String message;
    
    public TCPWorkerThread(TCPClient client, String message) {
        this.client = client;
        this.message = message;
    }

    @Override
    public void run() {
        client.sendMessage(message);
    }
    
}
