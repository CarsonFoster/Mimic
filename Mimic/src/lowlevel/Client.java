package lowlevel;

import java.io.*;
import java.net.*;

public class Client {
    private BufferedReader in;
    private DataOutputStream out;
    public Error start(String ip) {
        Socket client;
        
        try {
            client = new Socket(ip, Server.port);
        } catch (IOException e) {
            return Error.CONNECT;
        }
        System.out.println("Client connected.");
        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new DataOutputStream(client.getOutputStream());
        } catch (IOException e) {
            return Error.STREAM_CREATION;
        }
        //System.out.println("Streams created.");
        /*String line;
        try {
            line = in.readLine();
        } catch (IOException e) {
            System.out.println("Client exception.");
            try {
                in.close();
                out.close();
                client.close();
            } catch (IOException e2) {}
            return Error.MESSAGE_RECEIVE;
        }*/
        return Error.NONE;
    }
    
    private Error send(String msg) {
        if (!msg.endsWith("\n")) msg += "\n";
        try {
            out.writeBytes(msg);
        } catch (IOException e) {
            System.out.println("Client Exception: SEND");
            return Error.MESSAGE_SEND;
        }
        return Error.NONE;
    }
    
    private String receive() {
        try {
            return in.readLine().trim();
        } catch (IOException e) {
            System.out.println("Client Exception: RECEIVE");
            return null;
        }
    }
    
    private Error setUsername(gui.Client c) {
        String name, res = "";
        Error e;
        boolean ok = false;
        do {
            if (res == null) return Error.MESSAGE_RECEIVE; 
            name = c.promptForUsername();
            e = send(name);
            if (e != Error.NONE) return e;
            res = receive();
            if (!res.equals("200 OK"))
                c.errorUsername();
            else
                ok = true;
        } while (!ok);
        return Error.NONE;
    }
}
