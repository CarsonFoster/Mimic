package lowlevel;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.Collectors;

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
        public int port;
    }
    
    public static String getLocalIP() {
        try {
            return Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }
    
    public static int getNetmaskLength() { //NVM ignore: TODO: note: only returns first available non-loopback, non-virtual, up network interface
        try {
            return NetworkInterface.getByInetAddress(Inet4Address.getLocalHost()).getInterfaceAddresses().get(0).getNetworkPrefixLength() / 8;
        } catch (Exception e) {
            return 0;
        }
        /*try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface i = interfaces.nextElement();
                if (i.isUp() && !i.isLoopback() && !i.isVirtual())
                    return i;
            }
            return null;
        } catch (Exception e) {
            return null;
        }*/
    }
    
    public static Client initiate(String host, int port, gui.Client c, Consumer<String> r) {
        Client cl = new Client();
        Info i = new Info();
        i.host = host;
        i.port = port;
        if (cl.start(host, port) != Error.NONE) return null;
        Object[] ret = cl.setUsername(c);
        if (ret.length < 2) return null; // error, no set username
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
    
    public Error start(String ip, int port) {
        try {
            client = new Socket(ip, port);
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
    
    public static void main(String[] args) throws Exception {
        String ip = getLocalIP();
        int netmask = getNetmaskLength();
        System.out.println(ip + " " + netmask);
        new Thread(() -> Server.start("test.properties")).start();
        Network n = new Network(ip, 3);
        /*n.stream();
        for (int j = 0; j < n.list.size(); j++) { //do ur journal
           String x = n.list.get(j);
           try {
               InetAddress i = InetAddress.getByName(x);
               if (i.isReachable(1))
                   System.out.println("YES: " + i);
               else
                   System.out.println("NO: " + i);
           } catch (Exception e) {}  
        }*/
        n.stream().filter(x -> Network.open(x, 6464)).forEach(x -> System.out.println(x));
        System.out.println("done");
    }
    
    public List<String> scan() {
        List<String> list;
        String ip = getLocalIP();
        Network n;
        try {
            n = new Network(ip, 3); // TODO: Change later perhaps? Only accounts for 256 addresses.
        } catch (Exception e) {
            return null;
        }
        list = n.stream().filter(x -> Network.open(x, info.port)).collect(Collectors.toList());
        return list;
    }
}

class Network {
    public static final int TIMEOUT = 100;
    int netmask_length = 0;
    int[] ip = new int[4];
    List<String> list = null;
    
    public Network(String ip_s, int netmask) throws Exception {
        String[] ip_s_arr = ip_s.split("\\.");
        if (ip_s_arr.length != 4) throw new Exception("That's not a valid IP!");
        int i = 0;
        for (String section : ip_s_arr) {
            try {
                ip[i++] = Integer.parseInt(section);
            } catch (Exception e) {
                throw new Exception("That's not a valid IP!");
            }
        }
        
        if (netmask <= 0 || netmask > 3) throw new Exception("That's not a valid netmask!");
        netmask_length = netmask;
    }
    
    public Stream<String> stream() {
        if (list != null) return list.stream();
        list = new ArrayList<>();
        String prefix = "";
        for (int i = 0; i < netmask_length; i++) {
            prefix += "" + ip[i] + ".";
        }
        prefix = prefix.substring(0, prefix.length() - 1);
        
        int addresses = (int)Math.pow(256, 4 - netmask_length);
        for (int i = 0; i < addresses; i++)
            list.add(prefix);
       
        generate(0, list.size() - 1, list);
        
        return list.stream();
    }
    
    private void generate(int start, int end, List<String> l) {
        int section_length = (end - start + 1)/256; 
        if (section_length <= 0) return;
        for (int i = 0; i < 256; i++) {
            int start_mini = start + i * section_length;
            for (int j = start_mini; j < start_mini + section_length; j++) {
                try{
                    l.set(j, l.get(j) + "." + i);
                    //System.out.println(l.get(j));
                } catch (Exception e) {
                    System.out.println("error");
                }
            }
            generate(start_mini, start_mini + section_length - 1, l);
        }
    }
    
    public static boolean open(String host, int port) {
        boolean open = false;
        try {
            Socket s = new Socket();
            s.connect(new InetSocketAddress(host, 6464), TIMEOUT);
            open = true;
            s.close();
        } catch (Exception e) {}
        return open;
    }
    
}