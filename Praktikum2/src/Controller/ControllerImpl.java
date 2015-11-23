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
/**
 *
 * @author sasa
 */
public class ControllerImpl implements I_Controller{
    LoginUI login;
    ChatroomUI chatroom;
    TCPClient client;
    TCPServer server;
    Thread[] threads = new Thread[2];


    public ControllerImpl() {
     
        // Work with threads, damit Server und GUI quasi-parallel laufen
        //Server Thread
        threads[0] = new Thread(new Runnable() {
        public void run() {
            // some code to run in parallel
                server = new TCPServer(56789, 1);
                server.startServer();
        }
        });
        threads[0].start();
  
        // GUIS Thread
        threads[1] = new Thread(new Runnable() {
        public void run() {
            // some code to run in parallel
            login = new LoginUI();
            chatroom = new ChatroomUI();
            recordEvents();      
        }
        });
        threads[1].start();
        
//continue with work after dbThread is ready

        //server = new TCPServer(56789, 1);
        //server.startServer();
        //login = new LoginUI();
        //chatroom = new ChatroomUI();
        //recordEvents();        
    }
    
    public void recordEvents() {
       
        login.getConnButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String hostname = login.getServerTextField().getText();
                int port = Integer.parseInt(login.getPortTextField().getText());
                if (connection(hostname, port)) 
                    login.getJoinButton().setEnabled(true);
            }
        });
        
        login.getJoinButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = login.getUsernameTextField().getText();
                sendUsername(username);
                chatroom.setVisible(true);
            }
        });
        
        chatroom.getSendMessage().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = chatroom.getMessageTextField().getText();
                client.startJob(message);
            }
        });
        
        
        
    
    }

    
    @Override
    public boolean connection(String hostname, int port) {
        boolean connected=false;
                    
        // HIER SOll aber hostname und port aus dem Input genommen
        client = new TCPClient("localhost", 56789); 
 
        return connected;
    }
    
    public void startJob(String message) {
        client.startJob(message);
    }

    @Override
    public void sendMessage(String message) {
    }

    @Override
    public void sendUsername(String username) {
        client.setUsername(username);
    }
    

    @Override
    public String showMessage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] refresh() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
   
    public static void main(String[] args) {
        ControllerImpl controller = new ControllerImpl();
    }
}
