package gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import javafx.application.Application;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;


public class Main extends Application {

    private static int WIDTH, HEIGHT;
    private static Stage stage;
    
    public Main() {
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        WIDTH = (int)(screensize.getWidth() * 3.0/14.0);
        HEIGHT = (int)(screensize.getHeight() * 2.0/6.0);
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        Main.stage = primaryStage;
        
        stage.setTitle("Mimic");
        stage.setResizable(false);
        stage.setWidth(WIDTH);
        stage.setHeight(HEIGHT);
        
        Scene scene = new Scene(createPane(), WIDTH, HEIGHT);
        stage.setScene(scene);
        stage.show();
    }
    
    private GridPane createPane() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        
        Button a = new Button("Client");
        Button b = new Button("Server");
        a.setPrefSize(HEIGHT / 3, WIDTH / 3);
        b.setPrefSize(HEIGHT / 3, WIDTH / 3);
        
        a.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Client");
                stage.close();
                ClientInfoWindow.call();
            }
        });
        
        b.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Server");
                stage.close();
                Server.call();
            }
        });
        
        grid.add(a, 0, 0);
        grid.add(b, 1, 0);
        
        return grid;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
}
