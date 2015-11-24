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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
//import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author sasa
 */
public class ControllerImpl implements I_Controller{
    LoginUI login;
    ChatroomUI chatroomUI;
    TCPClient client;
    Thread listenThread; // lesen
    Thread workThread; // schreiben


    public ControllerImpl() {
        login = new LoginUI();
        chatroomUI = new ChatroomUI();
        recordEvents();        
    }
    
    public final void recordEvents() {
       
        login.getConnButton().addActionListener((ActionEvent e) -> {
            String hostname = login.getServerTextField().getText();
            int port = Integer.parseInt(login.getPortTextField().getText());
            String username = login.getUsernameTextField().getText();
            //verbindungsaufbau
            client = new TCPClient(hostname, port, username);
            client.start();
            chatroomUI.setVisible(true);
            login.setVisible(false);
            listenThread = new Thread()
            {
                @Override
                public void run() {
                    while (true) {
                        while (!client.isReady()) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(ControllerImpl.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        String tmp = chatroomUI.getMessageArea().getText();
                        chatroomUI.getMessageArea().setText(tmp+"\n"+client.getMessage());
                        client.setReady(false);
                    }
                }
            };
            listenThread.start();                 
        });
        
        chatroomUI.getSendButton().addActionListener((ActionEvent e) -> {
            String message = chatroomUI.getMessageTextField().getText();
            chatroomUI.getMessageTextField().setText("");
            workThread = new Thread()
            {
                @Override
                public void run() {
                    client.sendMessage(message);
                }
            };
            workThread.start();
        });  
        
        chatroomUI.getMessageTextField().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {}
            @Override
            public void keyReleased(KeyEvent e) {
                 if (e.getKeyCode()==KeyEvent.VK_ENTER)
                    chatroomUI.getSendButton().doClick();
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
