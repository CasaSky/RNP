/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import Client.TCPClient;
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
    

    public ControllerImpl() {
        login = new LoginUI();
        recordEvents();
        //chatroom = new ChatroomUI();
        
    }
    
    public void recordEvents() {
       
        login.getConnButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String hostname = login.getServerTextField().getText();
                int port = Integer.parseInt(login.getPortTextField().getText());
                connection(hostname, port);
            }
        });
        
        login.getJoinButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = login.getUsernameTextField().getText();
                sendUsername(username);
            }
        });
        
    
    }

    
    @Override
    public boolean connection(String hostname, int port) {
            boolean connected;
            client = new TCPClient(hostname, port);
            return true;
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
