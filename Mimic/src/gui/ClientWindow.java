package gui;

import java.awt.Dimension;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ClientWindow extends JFrame implements Client{

    private static int WIDTH, HEIGHT;
    private boolean SERVER_FLAGS = false;
    private String ip;
    private JList<String> list;
    private JTextArea messages;
    private JTextField text;
    private JButton send;
    private lowlevel.Client client;
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
    
    private static JList<String> constructChannelList(Container pane, String[] l) {
        JList<String> list = new JList<>(l);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
        return list;
    }
    
    private static JTextArea constructMessages(Container pane) {
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
        return a;
    }
    
    private static JComponent[] constructYourMessage(Container pane) { //TextField, button
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
        return new JComponent[] {a, b};
    }
    
    public ClientWindow(boolean server, String ip) {
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
            this.ip = lowlevel.Client.getLocalIP();
        } else {
            this.ip = ip;
        }
        constructTitle(pane);
        /*String[] abcs = new String[52];
        for (int i = 0; i < 26; i++) {
            abcs[i*2] = "" + (char)(97 + i);
            abcs[i*2 + 1] = "" + (char)(65 + i);
        }*/
        client = lowlevel.Client.initiate(ip, this, x -> {
            messages.append(x);
        });
        assert client != null : "Client is null";
        ArrayList<String> tmp = client.info.channels;
        assert tmp != null : "Channels are null"; //should literally never happen
        String[] channels = tmp.toArray(new String[1]);
        list = constructChannelList(pane, channels);
        list.setSelectedIndex(0);
        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                JList<String> l = (JList<String>)e.getSource();
                lowlevel.Error x = client.changeChannel(l.getSelectedValue());
                assert x == lowlevel.Error.NONE : "Fatal error: failed to change channels."; // help pls
                messages.setText("");
            }
        });
        messages = constructMessages(pane);
        JComponent[] arr = constructYourMessage(pane);
        text = (JTextField)arr[0];
        text.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
        AbstractAction sendMessage = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //System.out.println(text.getText());
                client.send("MSG " + text.getText());
                text.setText("");
            }
        };
        text.getActionMap().put("enter", sendMessage);
        send = (JButton)arr[1];
        send.addActionListener(sendMessage);
        //System.out.println(client);
        pack();
        setVisible(true);
    }
    
    public static void main(String[] args) {
        ClientWindow cw = new ClientWindow(false, null);
        //cw.promptForUsername();
        //cw.errorUsername();
    }
}
