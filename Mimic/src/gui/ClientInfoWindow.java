package gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ClientInfoWindow extends JFrame {
    private static int WIDTH, HEIGHT;
    private JList<String> list;
    private JTextField text;
    
    private void createButton(Container pane) {
        JButton b = new JButton("Connect");
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 2; c.gridy = 0;
        c.gridwidth = 1; c.gridheight = 2;
        c.weightx = 0; c.weighty = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        pane.add(b, c);
        b.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String ip = list.getSelectedValue();
                System.out.println(ip);
                String ip2 = text.getText();
                System.out.println(ip2);
            }
        });
    }
    
    private JList<String> createList(Container pane, String[] ips) {
        JList<String> list = new JList<>(ips);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(list);
        scroll.setMaximumSize(new Dimension(WIDTH / 4, HEIGHT)); //TODO: fix this; it doesn't work, but also not high priority
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0;
        c.gridwidth = 1; c.gridheight = GridBagConstraints.REMAINDER;
        c.weightx = 0.5; c.weighty = 0;
        c.fill = GridBagConstraints.BOTH;
        pane.add(scroll, c);
        return list;
    }
    
    private JTextField createManualEntry(Container pane) {
        JTextField a = new JTextField();
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1; c.gridy = 1;
        c.gridwidth = 1; c.gridheight = 1;
        c.weightx = 1.0; c.weighty = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        pane.add(a, c);
        JLabel label = new JLabel("Enter the IP manually: ");
        c.gridx = 1; c.gridy = 0;
        c.weightx = 1.0; c.weighty = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        pane.add(label, c);
        return a;
    }
    
    public ClientInfoWindow() {
        super("Mimic");
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        WIDTH = (int)(screensize.getWidth() * 3.0/14.0);
        HEIGHT = (int)(screensize.getHeight() * 2.0/6.0);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        Container pane = getContentPane();
        pane.setLayout(new GridBagLayout());
        
        list = createList(pane, new String[] {"192.168.1.0", "192.168.0.1"});
        text = createManualEntry(pane);
        createButton(pane);
        
        pack();
        setVisible(true);
    }
    
    public static void main(String[] args) {
        new ClientInfoWindow();
    }
}
