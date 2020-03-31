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
    private JTabbedPane tabs;
    private int networkScanPort;
    private ClientInfoWindow thisOne;
    
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
    
    private JComponent createManualEntryPanel() {
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
        
        JPanel group = new JPanel();
        SpringLayout group_layout = new SpringLayout();
        group.setLayout(group_layout);
        group.add(ip_group);
        group.add(port_group);
        group_layout.putConstraint(SpringLayout.WEST, port_group, 0, SpringLayout.WEST, ip_group);
        group_layout.putConstraint(SpringLayout.NORTH, port_group, 10, SpringLayout.SOUTH, ip_group);
        group_layout.putConstraint(SpringLayout.EAST, group, 0, SpringLayout.EAST, ip_group);
        group_layout.putConstraint(SpringLayout.SOUTH, group, 0, SpringLayout.SOUTH, port_group);
        
        panel.add(group);
        panel.add(b);
        
        layout.putConstraint(SpringLayout.VERTICAL_CENTER, group, 0, SpringLayout.VERTICAL_CENTER, panel);
        layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, group, 0, SpringLayout.HORIZONTAL_CENTER, panel);
        
        layout.putConstraint(SpringLayout.EAST, b, -10, SpringLayout.EAST, panel);
        layout.putConstraint(SpringLayout.SOUTH, b, -10, SpringLayout.SOUTH, panel);
        
        b.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String ip = ip_manual.getText();
                if (!lowlevel.Client.isValidIP(ip)) {
                    ClientWindow.error("The IP is invalid.", "Invalid IP");
                    return;
                }
                String port_txt = port1.getText();
                int port = lowlevel.Client.isValidPort(port_txt);
                if (port == -1) {
                    ClientWindow.error("The port number is invalid.", "Invalid Port");
                    return;
                }
                if (!lowlevel.Network.open(ip, port)) {
                    ClientWindow.error("Could not connect to server.", "Connection Error");
                    return;
                }
                thisOne.dispose();
                new ClientWindow(ip, port);
            }
        });
        
        return panel;
    }
    
    private JComponent createNetworkScanList(String[] ips) {
        JPanel panel = new JPanel(false);
        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);
        
        list = new JList<String>(ips);
        JScrollPane scroll = new JScrollPane(list);
        JLabel ip_label = new JLabel("IP: ");
        JButton b = new JButton("Connect");
        
        panel.add(scroll);
        panel.add(ip_label);
        panel.add(b);
        
        layout.putConstraint(SpringLayout.SOUTH, ip_label, -10, SpringLayout.NORTH, scroll);
        layout.putConstraint(SpringLayout.WEST, ip_label, 0, SpringLayout.WEST, scroll);
        layout.putConstraint(SpringLayout.VERTICAL_CENTER, scroll, 0, SpringLayout.VERTICAL_CENTER, panel);
        layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, scroll, 0, SpringLayout.HORIZONTAL_CENTER, panel);
        
        layout.putConstraint(SpringLayout.EAST, b, -10, SpringLayout.EAST, panel);
        layout.putConstraint(SpringLayout.SOUTH, b, -10, SpringLayout.SOUTH, panel);
        
        b.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String ip = list.getSelectedValue();
                thisOne.dispose();
                new ClientWindow(ip, networkScanPort);
            }
            
        });
        
        return panel;
    }
    private JComponent createNetworkScanPanel() {
        JPanel panel = new JPanel(false);
        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);
        
        port2 = new JTextField(6);
        JLabel port_label = new JLabel("Port:");
        JButton b = new JButton("Scan");
        
        JPanel port_group = new JPanel();
        SpringLayout port_layout = new SpringLayout();
        port_group.setLayout(port_layout);
        port_group.add(port2);
        port_group.add(port_label);
        
        port_layout.putConstraint(SpringLayout.WEST, port2, 10, SpringLayout.EAST, port_label);
        port_layout.putConstraint(SpringLayout.VERTICAL_CENTER, port2, 0, SpringLayout.VERTICAL_CENTER, port_label);
        port_layout.putConstraint(SpringLayout.EAST, port_group, 0, SpringLayout.EAST, port2);
        port_layout.putConstraint(SpringLayout.SOUTH, port_group, 0, SpringLayout.SOUTH, port2);
        
        panel.add(port_group);
        panel.add(b);
        
        layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, port_group, 0, SpringLayout.HORIZONTAL_CENTER, panel);
        layout.putConstraint(SpringLayout.VERTICAL_CENTER, port_group, 0, SpringLayout.VERTICAL_CENTER, panel);
        
        layout.putConstraint(SpringLayout.EAST, b, -10, SpringLayout.EAST, panel);
        layout.putConstraint(SpringLayout.SOUTH, b, -10, SpringLayout.SOUTH, panel);
        
        b.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String port_txt = port2.getText();
                int port = lowlevel.Client.isValidPort(port_txt);
                if (port == -1) {
                    ClientWindow.error("The port number is invalid.", "Invalid Port");
                    return;
                }
                networkScanPort = port;
                String[] ips = lowlevel.Client.scanParallel(port).toArray(new String[0]);
                if (ips.length == 0) ClientWindow.error("No suitable servers were found with the subnet mask of 255.255.255.0", "No servers found.");
                else tabs.setComponentAt(1, createNetworkScanList(ips));
            }
        });
        
        return panel;
    }
    
    public ClientInfoWindow() {
        super("Mimic");
        thisOne = this;
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        WIDTH = (int)(screensize.getWidth() * 3.0/14.0);
        HEIGHT = (int)(screensize.getHeight() * 2.0/6.0);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        tabs = new JTabbedPane();
        tabs.addTab("Manual Entry", null, createManualEntryPanel(), "Enter the IP manually");
        tabs.addTab("Network Scan", null, createNetworkScanPanel(), "Find a server on your network");
       
        add(tabs);
        pack();
        setVisible(true);
    }
    
    public static void main(String[] args) {
        new Thread(() -> lowlevel.Server.start("test.properties")).start();
        new ClientInfoWindow();
    }
}
