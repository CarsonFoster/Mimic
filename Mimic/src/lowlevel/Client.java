package lowlevel;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Client {
    private BufferedReader in;
    private DataOutputStream out;
    private Socket client;
    public Error start(String ip) {
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
    
    public Error send(String msg) {
        if (!msg.endsWith("\n")) msg += "\n";
        try {
            out.writeBytes(msg);
        } catch (IOException e) {
            System.out.println("Client Exception: SEND");
            return Error.MESSAGE_SEND;
        }
        return Error.NONE;
    }
    
    public String receive() {
        try {
            return in.readLine().trim();
        } catch (IOException e) {
            System.out.println("Client Exception: RECEIVE");
            return null;
        }
    }
    
    public ArrayList<String> getChannels() {
        String line = receive();
        String[] channels = line.split(" ");
        boolean def = false;
        
        ArrayList<String> c = new ArrayList<>();
        
        for (String channel : channels) {
            if (channel.equals("DEFAULT")) {
                def = true;
                continue;
            }
            if (def)
                c.add(0, channel);
            else
                c.add(channel);
        }
        return c;
    }
    
    public Error changeChannel(String channel) {
        send("\\channel " + channel);
        String line = receive();
        return (line.contains("200") ? Error.NONE : Error.DECLINED);
    }
    
    public void exit() {
        send("BYE");
        receive();
        System.out.println("Exiting client.");
        try {
            in.close();
            out.close();
            client.close();
        } catch (IOException e2) {}
    }
    
    public Error setUsername(gui.Client c) {
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
