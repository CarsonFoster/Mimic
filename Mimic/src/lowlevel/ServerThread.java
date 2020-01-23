package lowlevel;

import java.net.*;
import java.io.*;

public class ServerThread extends Thread {
    protected static int numThreads = 0;
    private Socket client;
    private int id;
    private BufferedReader in;
    private BufferedReaderListener brl;
    private DataOutputStream out;
    private volatile boolean running = true;
    
    public ServerThread(Socket c) {
        super();
        client = c;
        id = numThreads++;
        Server.threadErrors.put(id, Error.NONE);
        System.out.println("ServerThread " + id + " started.");
    }
    
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new DataOutputStream(client.getOutputStream());
        } catch (IOException e) {
            Server.threadErrors.put(id, Error.STREAM_CREATION);
            return;
        }
        //System.out.println("sStreams created.");
        /*String line;
        while (true) {
            try {
                line = in.readLine();
                //out.writeBytes(line + "\n");
            } catch (IOException e) {
                System.out.println("ServerThread " + id + " Exception");
                try {
                    in.close();
                    out.close();
                    client.close();
                } catch (IOException e2) {}
                Server.threadErrors.put(id, Error.MESSAGE_RECEIVE);
                return;
            }
        }*/
        String username = receive();
        while (!Server.checkUser(username)) {
            send("409 CONFLICT");
            username = receive();
        }
        send("200 OK");
        Server.usernames.put(id, username);
        send(Server.channelsList.stream().reduce("", (x, y) -> x + y + " ") + "DEFAULT " + Server.defaultChannel());
        send("000 NONE");
        send("000 NONE");
        send("000 NONE");
        Server.channels.put(id, Server.defaultChannel());
        brl = new BufferedReaderListener(in, this);
        brl.addBehavior((msg, st) -> {
            String[] arr = msg.split(" ");
            String first = arr[0];
            switch (first) {
                case "MSG": 
                    System.out.println("Message from " + Server.usernames.get(id) + " in channel " + Server.channels.get(id) + ": " + msg.substring(4));
                    Server.messages.get(Server.channels.get(id)).add("USER " + Server.usernames.get(id) + " " + msg);
                    break;
                case "\\channel": 
                    String channel = arr[1];
                    if (arr.length > 2 || !Server.checkChannel(id, channel))
                        send("409 CONFLICT");
                    else {
                        Server.channels.put(id, channel);
                        send("200 OK");
                    }
                    break;
                case "BYE": 
                    send("BYE");
                    st.shutdown();
                    break;
            }
        });
        new Thread(brl).start();
        while (running) {}
        System.out.println("exiting thread " + id);
    }
    
    private Error send(String msg) {
        if (!msg.endsWith("\n")) msg += "\n";
        try {
            out.writeBytes(msg);
        } catch (IOException e) {
            System.out.println("ServerThread " + id + " Exception");
            Server.threadErrors.put(id, Error.MESSAGE_SEND);
            return Error.MESSAGE_SEND;
        }
        return Error.NONE;
    }
    
    private String receive() {
        try {
            return in.readLine();
        } catch (IOException e) {
            System.out.println("ServerThread " + id + " Exception");
            Server.threadErrors.put(id, Error.MESSAGE_RECEIVE);
            return null;
        }
    }
    
    public synchronized void shutdown() {
        brl.running = false;
        try {
            in.close();
            out.close();
            client.close();
        } catch (IOException e) {}
        Server.threadErrors.remove(id);
        running = false;
    }
}
