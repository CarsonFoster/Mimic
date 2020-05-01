package gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;

public class ClientInfoWindow extends Application {
    private static int WIDTH, HEIGHT;
    private ListView<String> list;
    private TextField ip_manual, port1, port2;
    private TabPane tabs;
    private int networkScanPort;
    private static Stage stage;
    private final String SCAN_TAB_TITLE = "Network Scan";
    
    private BorderPane createManualEntryPane() {
        BorderPane pane = new BorderPane();
        
        ip_manual = new TextField();
        ip_manual.setPrefColumnCount(15);
        port1 = new TextField();
        port1.setPrefColumnCount(6);
        Button b = new Button("Connect");
        Label ip_label = new Label("IP:");
        Label port_label = new Label("Port: ");
        
        HBox ip_box = new HBox();
        ip_box.getChildren().add(ip_label);
        ip_box.getChildren().add(ip_manual);
        ip_box.setAlignment(Pos.CENTER);
        HBox.setMargin(ip_label, new Insets(0, 5, 0, 0));
        HBox port_box = new HBox();
        port_box.getChildren().add(port_label);
        port_box.getChildren().add(port1);
        port_box.setAlignment(Pos.CENTER);
        HBox.setMargin(port_label, new Insets(0, 5, 0, 0));
        VBox entry = new VBox();
        entry.getChildren().add(ip_box);
        entry.getChildren().add(port_box);
        entry.setAlignment(Pos.CENTER);
        VBox.setMargin(ip_box, new Insets(0, 0, 10, 0));
        
        pane.setBottom(b);
        BorderPane.setAlignment(b, Pos.BOTTOM_RIGHT);
        BorderPane.setMargin(b, new Insets(0, 25, 25, 0));
        
        pane.setCenter(entry);
        
        b.setOnAction(e -> {
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
            stage.close();
            ClientWindow.call(ip, port);
        });
        
        return pane;
    }
    
    private BorderPane createNetworkScanList(String[] ips) {
        BorderPane pane = new BorderPane();
        
        list = new ListView<>();
        ObservableList<String> ol = FXCollections.observableArrayList(ips);
        list.setItems(ol);
        list.setMaxWidth(WIDTH / 3);
        list.setMaxHeight(HEIGHT / 2);
        
        Label ip_label = new Label("IP: ");
        Button b = new Button("Connect");
        
        VBox ip_group = new VBox(ip_label, list);
        VBox.setMargin(ip_label, new Insets(0, 0, 10, 0));
        ip_group.setAlignment(Pos.CENTER);
        
        pane.setCenter(ip_group);
        pane.setBottom(b);
        BorderPane.setAlignment(b, Pos.BOTTOM_RIGHT);
        BorderPane.setMargin(b, new Insets(25, 25, 25, 0));
       
        b.setOnAction(e ->  {
            String ip = list.getSelectionModel().getSelectedItem();
            stage.close();
            ClientWindow.call(ip, networkScanPort);       
        });
        
        return pane;
    }
    
    private BorderPane createNetworkScanPane() {
        BorderPane pane = new BorderPane();
        
        port2 = new TextField();
        port2.setPrefColumnCount(6);
        port2.setMaxWidth(TextField.USE_PREF_SIZE);
        Label port_label = new Label("Port:");
        Button b = new Button("Scan");
        
        VBox port_group = new VBox(port_label, port2);
        VBox.setMargin(port_label, new Insets(0, 0, 10, 0));
        port_group.setAlignment(Pos.CENTER);
        
        pane.setCenter(port_group);
        pane.setBottom(b);
        BorderPane.setMargin(b, new Insets(0, 25, 25, 0));
        BorderPane.setAlignment(b, Pos.BOTTOM_RIGHT);
        
        b.setOnAction(e -> {
            String port_txt = port2.getText();
            int port = lowlevel.Client.isValidPort(port_txt);
            if (port == -1) {
                ClientWindow.error("The port number is invalid.", "Invalid Port");
                return;
            }
            networkScanPort = port;
            String[] ips = lowlevel.Client.scanParallel(port).toArray(new String[0]);
            if (ips.length == 0) ClientWindow.error("No suitable servers were found with the subnet mask of 255.255.255.0", "No servers found.");
            else tabs.getTabs().get(1).setContent(createNetworkScanList(ips));
        });
        
        return pane;
    }
    
    public static void call() {
        try {
            new ClientInfoWindow().start(new Stage());
        } catch (Exception e) {
            ClientWindow.error("Failed to start Client GUI.", "GUI Failed to Start");
        }
    }
    
    public void start(Stage primaryStage) throws Exception {
        ClientInfoWindow.stage = primaryStage;
        
        stage.setTitle("Mimic");
        stage.setResizable(false);
        stage.setWidth(WIDTH);
        stage.setHeight(HEIGHT);
        
        tabs = new TabPane();
        Tab manual = new Tab("Manual Entry", createManualEntryPane());
        Tab scan = new Tab(SCAN_TAB_TITLE, createNetworkScanPane());
        manual.setClosable(false);
        scan.setClosable(false);
        tabs.getTabs().add(manual);
        tabs.getTabs().add(scan);
        
        Scene scene = new Scene(tabs, WIDTH, HEIGHT);
        stage.setScene(scene);
        stage.show();
    }
    
    public ClientInfoWindow() {
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        WIDTH = (int)(screensize.getWidth() * 3.0/14.0);
        HEIGHT = (int)(screensize.getHeight() * 2.0/6.0);
    }
    
    public static void main(String[] args) {
        new Thread(() -> lowlevel.Server.start("default.properties")).start();
        launch(args);
    }
}
