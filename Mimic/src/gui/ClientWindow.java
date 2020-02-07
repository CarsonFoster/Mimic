package gui;

import java.awt.Dimension;
import java.awt.*;
import javax.swing.*;

public class ClientWindow extends JFrame implements Client{

    private static int WIDTH, HEIGHT;
    private boolean SERVER_FLAGS = false;
    private String ip = "";
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
    
    private static void constructTitle(Container pane) {
        
    }
    
    private static void constructChannelList(Container pane, String[] l) {
        JList<String> list = new JList<>(l);
        JScrollPane scroll = new JScrollPane(list);
        scroll.setMaximumSize(new Dimension(WIDTH / 4, HEIGHT)); //TODO: fix this; it doesn't work, but also not high priority
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 1;
        c.gridwidth = 1; c.gridheight = GridBagConstraints.REMAINDER;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.VERTICAL;
        //JPanel wrapper = new JPanel(null);
        //wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.LINE_AXIS));
        //wrapper.add(scroll);
        pane.add(scroll, c);
    }
    
    private static void constructMessages(Container pane) {
        JTextArea a = new JTextArea();
        a.setEditable(false);
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Dimension s = getSize();
                g.setColor(Color.white);
                g.fillRect(0, 0, (int)s.getWidth(), (int)s.getHeight());
                g.setColor(Color.black);
                g.drawRect(0, 0, (int)s.getWidth(), (int)s.getHeight());
            }
        };
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1; c.gridy = 1;
        c.gridwidth = GridBagConstraints.REMAINDER; c.gridheight = GridBagConstraints.RELATIVE;
        c.weightx = 1; c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        pane.add(p, c);
        pane.add(a, c);
    }
    
    private static void constructYourMessage(Container pane) {
        JTextField a = new JTextField();
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1; c.gridy = 2;
        c.gridwidth = GridBagConstraints.RELATIVE; c.gridheight = GridBagConstraints.RELATIVE;
        c.weightx = 1; c.weighty = 0;
        c.fill = GridBagConstraints.BOTH;
        pane.add(a, c);
        JButton b = new JButton("Send");
        c.gridx = 2; c.gridy = 2;
        c.gridwidth = GridBagConstraints.REMAINDER; c.gridheight = GridBagConstraints.RELATIVE;
        c.weightx = 0; c.weighty = 0;
        c.fill = GridBagConstraints.BOTH;
        pane.add(b, c);
    }
    
    public ClientWindow(boolean server) {
        super("Mimic");
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        WIDTH = (int)(screensize.getWidth() * 3.0/7.0);
        HEIGHT = (int)(screensize.getHeight() * 2.0/3.0);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //setLocationRelativeTo(null);
        Container pane = getContentPane();
        pane.setLayout(new GridBagLayout());
        if (server) {
            SERVER_FLAGS = true;
            ip = lowlevel.Client.getLocalIP();
        }
        constructTitle(pane);
        String[] abcs = new String[52];
        for (int i = 0; i < 26; i++) {
            abcs[i*2] = "" + (char)(97 + i);
            abcs[i*2 + 1] = "" + (char)(65 + i);
        }
        constructChannelList(pane, abcs);
        constructMessages(pane);
        constructYourMessage(pane);
        pack();
        setVisible(true);
    }
    
    public static void main(String[] args) {
        ClientWindow cw = new ClientWindow(false);
        //cw.promptForUsername();
        //cw.errorUsername();
    }
}
