package gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.*;
import javafx.scene.layout.*;

public class Stop extends Application {
    private static int WIDTH, HEIGHT;
    private ListView<String> list;
    private TextField ip_manual, port1, port2;
    private TabPane tabs;
    private int networkScanPort;
    private static Stage stage;
    private final String SCAN_TAB_TITLE = "Network Scan";
    
    
    private Pane createPane() {
        BorderPane pane = new BorderPane();
        Button b = new Button("Stop Server");
        b.setPrefSize(WIDTH - 20, HEIGHT - 40);
        pane.setCenter(b);
        
        b.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                lowlevel.Server.hardStop();
            }
        });
        
        return pane;
    }
    
    public static void call() {
        try {
            new Stop().start(new Stage());
        } catch (Exception e) {
            ClientWindow.error("Failed to start Stop GUI.", "GUI Failed to Start");
        }
    }
    
    public void start(Stage primaryStage) throws Exception {
        Stop.stage = primaryStage;
        
        stage.setTitle("Mimic");
        stage.setResizable(false);
        stage.setWidth(WIDTH);
        stage.setHeight(HEIGHT);
        
        
        Scene scene = new Scene(createPane(), WIDTH, HEIGHT);
        stage.setScene(scene);
        stage.show();
    }
    
    public Stop() {
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        WIDTH = (int)(screensize.getWidth() * 2.0/14.0);
        HEIGHT = (int)(screensize.getHeight() * 1.0/6.0);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
