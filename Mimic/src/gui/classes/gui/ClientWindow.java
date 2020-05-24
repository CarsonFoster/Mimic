package gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Optional;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.control.Alert.*;
import javafx.event.*;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import java.time.ZonedDateTime;

public class ClientWindow extends Application implements lowlevel.ClientInterface {

    private static int WIDTH, HEIGHT;
    private String ip;
    private ListView<String> list;
    private TextFlow messages;
    private TextField text;
    private Button send;
    private lowlevel.Client client;
    private ScrollPane scroll;
    private static Stage stage;
    private final static String[] colors = new String[] {"RED", "BLUE", "GREEN", "YELLOW", "PURPLE", "PINK", "ORANGE"};
    private boolean server = false;
    
    public int getWidth() {
        return WIDTH;
    } 
    
    public int getHeight() {
        return HEIGHT;
    }
    
    @Override
    public String promptForUsername() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Mimic");
        dialog.setHeaderText("Username Entry");
        dialog.setContentText("Please enter your username:");

        Optional<String> result = dialog.showAndWait();
        while (!result.isPresent()){
            result = dialog.showAndWait();
        }
        return result.get();
    }

    protected static void info(String message, String title) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Mimic");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    protected static void error(String message, String title) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Mimic");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @Override
    public void errorUsername() {
        error("That username is already taken or invalid. Please try another.", "Invalid Username");
    }
    
    @Override
    public void errorChannel() {
        error("That channel has been forbidden to you.", "Channel Denied");
    }
    
    private BorderPane createPane() {
        BorderPane pane = new BorderPane();
        
        HBox bottom = new HBox(text, send);
        HBox.setHgrow(text, Priority.ALWAYS);
        HBox.setMargin(text, new Insets(5, 10, 5, 5));
        HBox.setMargin(send, new Insets(5, 5, 5, 0));
        
        pane.setLeft(list);
        pane.setBottom(bottom);
        pane.setCenter(scroll);
        
        BorderPane.setAlignment(bottom, Pos.BOTTOM_RIGHT);
        BorderPane.setMargin(list, new Insets(5, 5, 0, 5));
        BorderPane.setMargin(scroll, new Insets(5, 5, 0, 0));
        
        return pane;
    }
    
    private ListView<String> constructChannelList(String[] l) {
        ListView<String> list = new ListView<>();
        list.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        ObservableList<String> ol = FXCollections.observableArrayList(l);
        list.setItems(ol);
        list.setPrefWidth(WIDTH/10.0);
        
        //list.setMaxWidth(Region.USE_PREF_SIZE);//WIDTH / 3);
        list.setEditable(false);
        return list;
    }
    
    private void constructMessages() {
        messages = new TextFlow();
        
        scroll = new ScrollPane();
        scroll.setContent(messages);
        scroll.vvalueProperty().bind(messages.heightProperty());
        scroll.setStyle("-fx-background: white;");
        scroll.setFitToWidth(true);
    }
    
    private static Node[] constructYourMessage() { //TextField, button
        TextField a = new TextField();
        Button b = new Button("Send");
        return new Node[] {a, b};
    }
    
    private static Text[] format(String username, String message) {
        Text time = new Text();
        ZonedDateTime now = ZonedDateTime.now();
        time.setText(String.format(" [%02d:%02d] ", now.getHour(), now.getMinute()));
        Text u = new Text();
        u.setStyle("-fx-fill:" + colors[username.charAt(0) % 7] + ";-fx-font-weight:bold;");
        u.setText(String.format("%25s", username));
        Text m = new Text();
        m.setText(": " + message + "\n");
        return new Text[] {time, u, m};
    }
    
    public static void call(String ip, int port, boolean s) {
        try {
            new ClientWindow(ip, port, s).start(new Stage());
        } catch (Exception e) {
            ClientWindow.error("Failed to start Server GUI.", "GUI Failed to Start");
        }
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        ClientWindow.stage = primaryStage;
        
        stage.setTitle("Mimic on " + lowlevel.Client.getLocalIP() + ": " + client.info.username);
        stage.setResizable(false);
        stage.setWidth(WIDTH);
        stage.setHeight(HEIGHT);
        
        Scene scene = new Scene(createPane(), WIDTH, HEIGHT);
        stage.setScene(scene);
        stage.show();
        text.requestFocus();
        stage.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue == true)
                text.requestFocus();
        });
        
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                if (!server) {
                    Platform.exit();
                    System.exit(0);
                }
            }
        });
    }
    
    private void welcomeMessage() {
        if (!client.info.disable_default_msg) messages.getChildren().addAll(new Text((" " + client.info.default_msg).replaceAll("\n", "\n ")));
    }
    
    public ClientWindow() {
        this("localhost", 6464, true);
    }
    
    public ClientWindow(String ip, int port, boolean s) {
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        WIDTH = (int)(screensize.getWidth() * 3.0/7.0);
        HEIGHT = (int)(screensize.getHeight() * 2.0/3.0);

        server = s;
        
        client = lowlevel.Client.initiate(ip, port, this, x -> {
            String[] arr = x.split(" MSG ");
            String user = arr[0].substring(5), msg = "";
            try {
                msg = arr[1];
            } catch (Exception e) {
                System.out.println("ERROR1");
                System.out.println(x);
            }
            final String msg_lambda = msg;
            Platform.runLater(
                () -> {
                   messages.getChildren().addAll(format(user, msg_lambda));
                }
            );
        });
        if (client == null) {
            error("Could not connect to server.", "Connection Error");
            System.exit(1);
        }
        ArrayList<String> tmp = client.info.channels;
        assert tmp != null : "Channels are null"; //should literally never happen
        String[] channels = tmp.toArray(new String[1]);
        list = constructChannelList(channels);
        list.getSelectionModel().select(0);
        list.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            lowlevel.Error x1 = client.changeChannel(newValue);
            if (x1 != lowlevel.Error.NONE) {
                errorChannel();
                list.getSelectionModel().select(0);
                return;
            }
            assert x1 == lowlevel.Error.NONE : "Fatal error: failed to change channels."; // help pls
            messages.getChildren().removeIf(x -> true);
            if (newValue.equals("#welcome")) welcomeMessage();
        });
        constructMessages();
        Node[] arr = constructYourMessage();
        text = (TextField)arr[0];
        send = (Button)arr[1];
        Procedure sendMessage = () -> {
            String msg = text.getText();
            if (msg.length() == 0) 
                return;
            client.send("MSG " + msg);
            String ret;
            synchronized (client.lock) {
                ret = client.receive();
            }
            text.setText("");
            switch (ret.split(" ")[0]) {
                case "200":
                    break;
                case "401": //silent
                    error("This channel is silent.", "Silent Channel");
                    return;
                case "405": //muted
                    error("You have been muted.", "Muted");
                    return;
            }
            messages.getChildren().addAll(format(client.info.username, msg));
        };
        text.setOnKeyReleased((KeyEvent e) -> {
            if (e.getCode() == KeyCode.ENTER)
                sendMessage.run();
        });
        send.setOnAction((ActionEvent e) -> {
            sendMessage.run();
        });
        welcomeMessage();
    }
    
    public static void main(String[] args) {
        //new Thread(() -> lowlevel.Server.start("test.properties")).start();
        launch(args);
    }
}

interface Procedure {
    void run();
}