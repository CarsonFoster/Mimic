package gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ClientWindow extends JFrame implements Client{

    private int X, Y;
    
    private class ClientPanel extends JPanel {
        public void paintComponent(Graphics g) {
            g.fillRect(0, 0, 50, 50);
        }
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
    
    public ClientWindow() {
        super("SCI Graphing");
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        X = (int)(screensize.getWidth() * 3.0/7.0);
        Y = (int)(screensize.getHeight() * 2.0/3.0);
        setPreferredSize(new Dimension(X, Y));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        add(new ClientPanel());
        pack();
        setVisible(true);
    }
    
    public static void main(String[] args) {
        ClientWindow cw = new ClientWindow();
        cw.promptForUsername();
        cw.errorUsername();
    }
}
