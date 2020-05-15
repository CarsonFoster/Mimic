package lowlevel;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Client {
    private BufferedReader in;
    private DataOutputStream out;
    private Socket client;
    private BufferedReaderListener brl;
    private Thread brlthread;
    public Info info;
    public final Object lock = new Object();
    private static final int SECTIONS = 4;
    
    public static class Info {
        public String username, channel, host, default_msg;
        public ArrayList<String> channels, additional;
        public boolean disable_default_msg;
        public int port;
    }
    
    public static String getLocalIP() {
        NetworkInterface nic = null;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface i = interfaces.nextElement();
                if (i.isUp() && !i.isLoopback() && !i.isVirtual() && !i.getDisplayName().contains("Virtual")) {
                    nic = i;
                    break;
                }
            }
        } catch (Exception e) {}
        if (nic == null) return null;
        return nic.getInetAddresses().nextElement().getHostAddress();
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
    
    public static List<String> scan(int port) {
        List<String> list;
        String ip = getLocalIP();
        Network n;
        try {
            n = new Network(ip, 3); // TODO: Change later perhaps? Only accounts for 256 addresses.
        } catch (Exception e) {
            return null;
        }
        list = n.stream().filter(x -> Network.open(x, port)).collect(Collectors.toList());
        return list;
    }
    
    public static List<String> scanParallel(int port) {
        List<String> list;
        String ip = getLocalIP();
        Network n;
        try {
            n = new Network(ip, 3);
        } catch (Exception e) { return null; }
        list = n.stream().parallel().filter(x -> Network.open(x, port)).collect(Collectors.toList()); // reduces time to scan 256 w/ 100 ms timeout from 26s -> 3s
        return list;
    }
    
    public static List<String> scanSplitParallel(int port) {
        class Int {
            int i;
            public Int() {
                i = 0;
            }
            public void increment() {
                i++;
            }
            public int get() {
                return i;
            }
        }
        List<String> list = new ArrayList<>();
        String ip = getLocalIP();
        Network n;
        try {
            n = new Network(ip, 3); //11 min 7 sec
        } catch (Exception e) { return null; }
        n.generate();
        int size = n.list.size() / SECTIONS;
        HashMap<Integer, List<String>> results = new HashMap<>();
        final Int done = new Int();
        for (int i = 0; i < SECTIONS; i++) {
            final int j = i;
            new Thread(() -> {
                List<String> subList;
                if (j == SECTIONS - 1)
                    subList = n.list.subList(j * size, n.list.size());
                else
                    subList = n.list.subList(j * size, (j + 1) * size);
                results.put(j, subList.stream().parallel().filter(x -> Network.open(x, port)).collect(Collectors.toList()));
                done.increment();
                //System.out.println(j + " done; " + done.get());
            }).start();
        }
        //System.out.println("test");
        while (done.get() != SECTIONS) {
            try {
                Thread.sleep(1);
            } catch (Exception e) {}
        }
        //System.out.println("test2");
        for (int i = 0; i < SECTIONS; i++) {
            list.addAll(results.get(i));
        }
        return list;
    }
    
    public static boolean isValidIP(String ip) {
        String[] sections = ip.split("\\.");
        if (sections.length != 4) return false;
        boolean ok = true;
        for (String s : sections) {
            try {
                int tmp = Integer.parseInt(s);
                ok &= (tmp >= 0 && tmp <= 255);
            } catch (Exception e) {
                return false;
            }
        }
        return ok;
    }
    
    public static int isValidPort(String port) {
        try {
            int tmp = Integer.parseInt(port);
            if (tmp > 0 && tmp <= 65535) return tmp;
            return -1;
        } catch (Exception e) {
            return -1;
        }
    }
}