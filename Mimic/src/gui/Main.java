package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class Main extends JFrame {
    private static int WIDTH, HEIGHT;
    private Main thisOne;
   
    private JComponent create() {
        JPanel panel = new JPanel(false);
        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);
        
        JButton a = new JButton("Client");
        JButton b = new JButton("Server");
        a.setPreferredSize(new Dimension(WIDTH / 3, HEIGHT / 3));
        b.setPreferredSize(new Dimension(WIDTH / 3, HEIGHT / 3));
        
        panel.add(b);
        panel.add(a);
        
        layout.putConstraint(SpringLayout.VERTICAL_CENTER, a, 0, SpringLayout.VERTICAL_CENTER, panel);
        layout.putConstraint(SpringLayout.VERTICAL_CENTER, b, 0, SpringLayout.VERTICAL_CENTER, panel);
        layout.putConstraint(SpringLayout.WEST, b, 10, SpringLayout.EAST, a);
        layout.putConstraint(SpringLayout.EAST, a, -10, SpringLayout.HORIZONTAL_CENTER, panel);
        
        a.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                thisOne.dispose();
                new ClientInfoWindow();
            }
        });
        
        b.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                thisOne.dispose();
                new Server();
            }
            
        });
        
        return panel;
    }
    
    public Main() {
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
        new Main();
    }
}
