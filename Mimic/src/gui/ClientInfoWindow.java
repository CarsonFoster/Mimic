package gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ClientInfoWindow extends JFrame {
    private static int WIDTH, HEIGHT;
    private JList<String> list;
    private JTextField ip_manual, port1, port2;
    
    /*private void createButton(Container pane) {
        JButton b = new JButton("Connect");
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 2; c.gridy = 0;
        c.gridwidth = 1; c.gridheight = 4;
        c.weightx = 0; c.weighty = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        pane.add(b, c);
        b.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String ip = list.getSelectedValue();
                System.out.println(ip);
                String ip2 = ip_manual.getText();
                System.out.println(ip2);
                String p = port.getText();
                System.out.println(p);
            }
        });
    }*/
    
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
    
    private JTextField[] createManualEntry(Container pane) {
        JTextField a = new JTextField();
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1; c.gridy = 1;
        c.gridwidth = 1; c.gridheight = 1;
        c.weightx = 1.0; c.weighty = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        pane.add(a, c);
        
        JTextField b = new JTextField();
        c.gridx = 1; c.gridy = 3;
        c.gridwidth = 1; c.gridheight = 1;
        c.weightx = 1.0; c.weighty = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        pane.add(b, c);
        
        JLabel label = new JLabel("Enter the IP manually: ");
        c.gridx = 1; c.gridy = 0;
        c.weightx = 1.0; c.weighty = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        pane.add(label, c);
        
        JLabel label2 = new JLabel("Enter the port: ");
        c.gridx = 1; c.gridy = 2;
        c.weightx = 1.0; c.weighty = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        pane.add(label2, c);
        
        return new JTextField[] {a, b};
    }
    
    private JComponent createManualEntryPanel() {
        final int PAD = 10;
        JPanel panel = new JPanel(false);
        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);
        
        ip_manual = new JTextField(null, 15);
        port1 = new JTextField(null, 6);
        JButton b = new JButton("Connect");
        JLabel ip_label = new JLabel("IP:");
        JLabel port_label = new JLabel("Port: ");
        
        
        JPanel ip_group = new JPanel();
        JPanel port_group = new JPanel();
        ip_group.add(ip_manual);
        ip_group.add(ip_label);
        port_group.add(port1);
        port_group.add(port_label);
        
        SpringLayout ip = new SpringLayout();
        SpringLayout port = new SpringLayout();
        ip_group.setLayout(ip);
        port_group.setLayout(port);
        ip.putConstraint(SpringLayout.WEST, ip_manual, 10, SpringLayout.EAST, ip_label);
        ip.putConstraint(SpringLayout.EAST, ip_group, 10, SpringLayout.EAST, ip_manual);
        ip.putConstraint(SpringLayout.SOUTH, ip_group, 10, SpringLayout.SOUTH, ip_manual);
        port.putConstraint(SpringLayout.WEST, port1, 10, SpringLayout.EAST, port_label);
        port.putConstraint(SpringLayout.EAST, port_group, 10, SpringLayout.EAST, port1);
        port.putConstraint(SpringLayout.SOUTH, port_group, 10, SpringLayout.SOUTH, port1);
        
        
        panel.add(ip_group);
        panel.add(port_group);
        panel.add(b);
        
        layout.putConstraint(SpringLayout.VERTICAL_CENTER, ip_group, -10, SpringLayout.VERTICAL_CENTER, panel);
        layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, ip_group, 0, SpringLayout.HORIZONTAL_CENTER, panel);
        
        layout.putConstraint(SpringLayout.WEST, port_group, 0, SpringLayout.WEST, ip_group);
        layout.putConstraint(SpringLayout.NORTH, port_group, 10, SpringLayout.SOUTH, ip_group);
        
        layout.putConstraint(SpringLayout.EAST, b, -10, SpringLayout.EAST, panel);
        layout.putConstraint(SpringLayout.SOUTH, b, -10, SpringLayout.SOUTH, panel);
        
        b.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String ip = ip_manual.getText();
                String port = port1.getText();
                System.out.println(ip);
                System.out.println(port);
                System.out.println(lowlevel.Client.isValidIP(ip));
                System.out.println(lowlevel.Client.isValidPort(port));
            }
        });
        
        return panel;
    }
    
    private JComponent createNetworkScanPanel() {
        JPanel panel = new JPanel(false);
        list = new JList<String>(new String[] {"192.168.1.0"});
        JScrollPane scroll = new JScrollPane(list);
        port2 = new JTextField();
        JButton b = new JButton("Connect");
        panel.add(scroll);
        panel.add(port2);
        panel.add(b);
        return panel;
    }
    
    public ClientInfoWindow() {
        super("Mimic");
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        WIDTH = (int)(screensize.getWidth() * 3.0/14.0);
        HEIGHT = (int)(screensize.getHeight() * 2.0/6.0);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Manual Entry", null, createManualEntryPanel(), "Enter the IP manually");
        tabs.addTab("Network Scan", null, createNetworkScanPanel(), "Find a server on your network");
       
        add(tabs);
        pack();
        setVisible(true);
    }
    
    public static void main(String[] args) {
        new ClientInfoWindow();
    }
}
