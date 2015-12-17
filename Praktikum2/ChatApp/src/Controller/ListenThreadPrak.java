package Controller;

/**
 * Created by talal on 17.12.15.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import Client.TCPClient;
import Gui.ChatroomUI;

import java.util.logging.Level;
import java.util.logging.Logger;

import Client.TCPClient;
import Gui.ChatroomUI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sasa
 */
public class ListenThreadPrak extends Thread {
    private TCPClient client;
    private ChatroomUI chatroomUI;

    ListenThreadPrak(TCPClient client, ChatroomUI chatroomUI) {
        this.client = client;
        this.chatroomUI = chatroomUI;
    }

    @Override
    public void run() {
        //Solange kein Logout hör auf das Schreiben zu, sonst Fenster schließen
        while (!client.logoutOk()) {
            //while (!client.isReady()) { // Falls etwas zum Schreib ist, wird ready gesetzt
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(ControllerImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (client.isNewUserJoined()) {
                chatroomUI.getUsersArea().setText(client.getChatroomUsers());
                client.setNewUserJoined(false);
            }
            // }

            if (client.isReady()) {
                String tmp = chatroomUI.getMessageArea().getText();
                chatroomUI.getMessageArea().setText(tmp + "\n" + client.getMessage());
                client.setReady(false);
            }
        }
        chatroomUI.dispose();
    }
}