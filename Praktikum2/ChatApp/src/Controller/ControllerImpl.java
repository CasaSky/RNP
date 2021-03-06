/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import Client.ClientWorkerThread;
import Client.TCPClient;
import Gui.ChatroomUI;
import Gui.LoginUI;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
//import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
/**
 *
 * @author sasa
 */
public class ControllerImpl implements I_Controller{
    private LoginUI login;
    private ChatroomUI chatroomUI;
    private TCPClient client;
    private ListenThread listenThread; // lesen
    private ClientWorkerThread workerThread; // schreiben
    private String username;

    public ControllerImpl() {
        login = new LoginUI();
        chatroomUI = new ChatroomUI();
        recordEvents();        
    }
    
    public void recordEvents() {
       
        login.getConnButton().addActionListener((ActionEvent e) -> {
            String hostname = login.getServerTextField().getText();
            int port = Integer.parseInt(login.getPortTextField().getText());
            username = login.getUsernameTextField().getText();
            //verbindungsaufbau
            client = new TCPClient(hostname, port, username);
            client.start();
            
            // Warten bis Client den Username geprueft hat
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(ControllerImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if (client.isUsernameValid()) {
                chatroomUI.setVisible(true);
                login.setVisible(false);
                listenThread = new ListenThread(client, chatroomUI);
                listenThread.start(); 
            }
            else 
                JOptionPane.showMessageDialog(null, "Username existiert bereits, bitte einen anderen auswählen!");
        });
        
        chatroomUI.getSendButton().addActionListener((ActionEvent e) -> {
            String message = chatroomUI.getMessageTextField().getText();
            chatroomUI.getMessageTextField().setText("");
            
            workerThread = new ClientWorkerThread(client,message);
            workerThread.start();
        });  
        
        
        chatroomUI.getUsersRefresh().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            client.sendClients();
            chatroomUI.getUsersArea().setText(client.getChatroomUser());
            }
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
        
        chatroomUI.getLogoutButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.sendLogout(username); 
            }
        });
        
        chatroomUI.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e)
            {
                System.out.println("Closed");
                client.sendLogout(username); 
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
