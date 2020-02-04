package gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import javax.swing.*;

public class ClientWindow extends JFrame implements Client{

    private static int WIDTH, HEIGHT;
    /*private Painter p;
    
    private interface Painter {
        public void paint(Graphics g);
    }
    
    private class ClientPanel extends JPanel {
        public void paintComponent(Graphics g) {
            p.paint(g);
        }
    }*/
    
    public int getWidth() {
        return WIDTH;
    } 
    
    public int getHeight() {
        return HEIGHT;
    }
    
    @Override
    public String promptForUsername() {
        return JOptionPane.showInputDialog(this, "Enter a username: ", "Username", JOptionPane.QUESTION_MESSAGE);
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void errorUsername() {
        JOptionPane.showMessageDialog(this, "That username is already taken or invalid. Please try another.", "Invalid Username", JOptionPane.ERROR_MESSAGE);
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private static JScrollPane constructChannelsList() {
        String[] abcs = new String[52];
        for (int i = 0; i < 26; i++) {
            abcs[i*2] = "" + (char)(97 + i);
            abcs[i*2 + 1] = "" + (char)(65 + i);
        }
        JList<String> list = new JList<>(abcs);
        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(40, HEIGHT));
        return scroll;
    }
    
    private static JTextField constructMessages() {
        JTextField a = new JTextField();
        
        return a;
    }
    
    public ClientWindow() {
        super("Mimic");
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        WIDTH = (int)(screensize.getWidth() * 3.0/7.0);
        HEIGHT = (int)(screensize.getHeight() * 2.0/3.0);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        //add(new ClientPanel());
        
        add(ClientWindow.constructChannelsList());
        pack();
        setVisible(true);
    }
    
    public static void main(String[] args) {
        ClientWindow cw = new ClientWindow();
        //cw.promptForUsername();
        //cw.errorUsername();
    }
}
