package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Properties;

public class Server extends JFrame {
    private static int WIDTH, HEIGHT;
    private Server thisOne;
    private JLabel config_label;
    private static final String CONFIG_TEXT = "Config File: ";
    private boolean selected = false;
    private String path;
   
    private JComponent create() {
        JPanel panel = new JPanel(false);
        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);
        final JFileChooser fc = new JFileChooser();
        
        config_label = new JLabel(CONFIG_TEXT);
        JButton a = new JButton("Select Config File");
        JButton b = new JButton("Start Server");
        
        panel.add(b);
        panel.add(a);
        panel.add(config_label);
        
        layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, a, 0, SpringLayout.HORIZONTAL_CENTER, panel);
        layout.putConstraint(SpringLayout.VERTICAL_CENTER, a, 0, SpringLayout.VERTICAL_CENTER, panel);
        layout.putConstraint(SpringLayout.SOUTH, config_label, -10, SpringLayout.NORTH, a);
        layout.putConstraint(SpringLayout.WEST, config_label, 0, SpringLayout.WEST, a);
        
        layout.putConstraint(SpringLayout.EAST, b, -10, SpringLayout.EAST, panel);
        layout.putConstraint(SpringLayout.SOUTH, b, -10, SpringLayout.SOUTH, panel);
        
        a.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int ret = fc.showOpenDialog(thisOne);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    config_label.setText(CONFIG_TEXT + file.getName());
                    path = file.getAbsolutePath();
                    selected = true;
                }
            }
        });
        
        b.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selected) {
                    int port;
                    try {
                        Properties tmp = lowlevel.Server.load(path);
                        port = Integer.parseInt(tmp.getProperty("port"));
                    } catch (Exception ex) {
                        ClientWindow.error("Invalid port given.", "Invalid port");
                        return;
                    }
                    new Thread(() -> lowlevel.Server.start(path)).start();
                    thisOne.dispose();
                    new ClientWindow("127.0.0.1", port);
                }
            }
            
        });
        
        return panel;
    }
    
    public Server() {
        super("Mimic");
        thisOne = this;
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        WIDTH = (int)(screensize.getWidth() * 3.0/14.0);
        HEIGHT = (int)(screensize.getHeight() * 2.0/6.0);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        add(create());
        pack();
        setVisible(true);
    }
    
    public static void main(String[] args) {
        new Server();
    }
}
