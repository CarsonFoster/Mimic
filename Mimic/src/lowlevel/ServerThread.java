package lowlevel;

import java.net.*;
import java.io.*;

public class ServerThread extends Thread {
    private static int idCounter = 0;
    private Socket client;
    private int id;
    private BufferedReader in;
    private DataOutputStream out;
    
    public ServerThread(Socket c) {
        super();
        client = c;
        id = idCounter++;
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
}
