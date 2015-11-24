/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import Client.TCPClient;
import Server.TCPServer;
import Gui.ChatroomUI;
import Gui.LoginUI;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author sasa
 */
public class ControllerImpl implements I_Controller{
    LoginUI login;
    ChatroomUI chatroom;
    TCPClient client;


    public ControllerImpl() {
    
        login = new LoginUI();
        chatroom = new ChatroomUI();
        recordEvents();        
    }
    
    public void recordEvents() {
       
        login.getConnButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String hostname = login.getServerTextField().getText();
                int port = Integer.parseInt(login.getPortTextField().getText());
                String username = login.getUsernameTextField().getText();
                //verbindungsaufbau
                client = new TCPClient(hostname, port, username);
                client.start();
                //sendUsername(username);
                chatroom.setVisible(true);
                login.setVisible(false);   
            }                 
        });
        
      
        
        chatroom.getSendMessage().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = chatroom.getMessageTextField().getText();
                client.sendMessage(message);
            }
        });   
        
        chatroom.getRefreshButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tmp = chatroom.getMessageArea().getText();
                chatroom.getMessageArea().setText(tmp+"\n"+client.getMessage());
            }
        });
        
    }

    @Override
    public void sendMessage(String message) {
    }

    @Override
    public String showMessage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] refresh() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
   
}
