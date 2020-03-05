package lowlevel;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.function.Consumer;

public class Client {
    private BufferedReader in;
    private DataOutputStream out;
    private Socket client;
    private BufferedReaderListener brl;
    private Thread brlthread;
    public Info info;
    public final Object lock = new Object();
    
    public static class Info {
        public String username, channel, host, default_msg;
        public ArrayList<String> channels, additional;
        public boolean disable_default_msg;
    }
    
    public static String getLocalIP() {
        try {
            return Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }
    
    public static Client initiate(String host, gui.Client c, Consumer<String> r) {
        Client cl = new Client();
        Info i = new Info();
        i.host = host;
        if (cl.start(host) != Error.NONE) return null;
        Object[] ret = cl.setUsername(c);
        if (ret.length < 1) return null; // error, no set username
        i.username = (String)ret[1];
        ArrayList<String> channels = cl.getChannels();
        if (channels == null) return null;
        i.channels = channels;
        i.channel = channels.get(0);
        ArrayList<String> additional = new ArrayList<String>();
        for (int j = 0; j < 3; j++) {
            String line = cl.receive();
            if (line == null) return null;
            additional.add(line);
        }
        i.additional = additional;
        i.default_msg = additional.remove(0).replaceAll("\\\\n", "\n");
        i.disable_default_msg = Boolean.parseBoolean(additional.remove(0));
        cl.info = i;
        
        cl.setBehavior(r);
        cl.startBRL();
        
        return cl;
    }
    
    public void setBehavior(Consumer<String> r) {
        brl.addBehavior(r);
    }
    
    public void startBRL() {
        brlthread = new Thread(brl, "BufferedListener Client: " + info.username);
        brlthread.start();
    }
    
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
        brl = new BufferedReaderListener(in, this);
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
            String str = in.readLine();
            if (str != null) str = str.trim();
            return str;
        } catch (IOException e) {
            System.out.println("Client Exception: RECEIVE");
            return null;
        }
    }
    
    public ArrayList<String> getChannels() {
        String line = receive();
        if (line == null) return null;
        String[] channels = line.split(" ");
        boolean def = false;
        
        ArrayList<String> c = new ArrayList<>();
        
        for (String channel : channels) {
            if (channel.equals("DEFAULT")) {
                def = true;
                continue;
            }
            if (def) {
                c.add(0, channel);
                def = false;
            } else
                c.add(channel);
        }
        return c;
    }
    
    public Error changeChannel(String channel) {
        String line = null;
        //System.out.println("Starting change channel operation.");
        synchronized (lock) {
            //System.out.println("Change channel acquired lock.");
            send("\\channel " + channel);
            line = receive();
        }
        assert line != null : "Received a null";
        return (line.contains("200") ? Error.NONE : Error.DECLINED);
    }
    
    public void exit() {
        brl.running = false;
        brlthread.interrupt();
        send("BYE");
        receive();
        System.out.println("Exiting client.");
        try {
            in.close();
            out.close();
            client.close();
        } catch (IOException e2) {}
    }
    
    public Object[] setUsername(gui.Client c) { // 0 = error, 1, if there, = username
        String name, res = "";
        Error e;
        boolean ok = false;
        do {
            if (res == null) return new Object[] {Error.MESSAGE_RECEIVE}; 
            name = c.promptForUsername();
            if (name == null) return new Object[] {Error.USERNAME};
            e = send(name);
            if (e != Error.NONE) return new Object[] {e};
            res = receive();
            if (!res.equals("200 OK"))
                c.errorUsername();
            else
                ok = true;
        } while (!ok);
        return new Object[] {Error.NONE, name};
    }
    
}
