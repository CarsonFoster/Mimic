package gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import javafx.application.Application;
import javafx.event.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.*;
import java.io.File;
import java.util.Properties;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;

public class Server {
    private static int WIDTH, HEIGHT;
    private static Stage stage;
    private Label config_label;
    private static final String CONFIG_TEXT = "Config File: ";
    private boolean selected = false;
    private String path;
   
    public static void call() {
        try {
            new Server().start(new Stage());
        } catch (Exception e) {
            ClientWindow.error("Failed to start Server GUI.", "GUI Failed to Start");
        }
    }
    
    public void start(Stage primaryStage) throws Exception {
        Server.stage = primaryStage;
        
        stage.setTitle("Mimic");
        stage.setResizable(false);
        stage.setWidth(WIDTH);
        stage.setHeight(HEIGHT);
        
        //Pane root = new Pane();
        Scene scene = new Scene(createPane(), WIDTH, HEIGHT);
        stage.setScene(scene);
        /*stage.setOnShown(e -> {
            drawPane(root);
            System.out.println(scene.getX());
        });*/
        stage.show();
    }
    
    public BorderPane createPane() {
        BorderPane pane = new BorderPane();
        FileChooser fc = new FileChooser();
        fc.setTitle("Select the config file to use");
        
        config_label = new Label(CONFIG_TEXT);
        Button a = new Button("Select Config File");
        Button b = new Button("Start Server");
        VBox box = new VBox();
        box.getChildren().add(config_label);
        box.getChildren().add(a);
        box.setAlignment(Pos.CENTER);
        
        pane.setCenter(box);
        pane.setBottom(b);
        BorderPane.setAlignment(b, Pos.BOTTOM_RIGHT);
        BorderPane.setMargin(b, new Insets(0, 25, 25, 0));
        
        a.setOnAction((ActionEvent e) -> {
            File file = fc.showOpenDialog(stage);
            if (file != null) {
                config_label.setText(CONFIG_TEXT + file.getName());
                path = file.getAbsolutePath();
                selected = true;
            }
        });
        
        b.setOnAction((ActionEvent e) -> {
            if (selected) {
                int port;
                try {
                    Properties tmp = lowlevel.Server.load(path);
                    port = Integer.parseInt(tmp.getProperty("port"));
                } catch (Exception ex) {
                    ClientWindow.error("Invalid port given.", "Invalid port");
                    return;
                }
                new Thread(() -> lowlevel.Server.start(path), "ServerThreadByGUI").start();
                stage.close();
                ClientWindow.call("127.0.0.1", port, true); // TODO: change when ClientWindow is made into JavaFX
                Stop.call();
            }          
        });
        return pane;
    }
    
    public Server() {
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        WIDTH = (int)(screensize.getWidth() * 3.0/14.0);
        HEIGHT = (int)(screensize.getHeight() * 2.0/6.0);
    }
    
    public static void main(String[] args) {
        Server.call();
    }

    
}
