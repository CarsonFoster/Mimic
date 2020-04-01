package lowlevel;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;

public class Server {
    private static final int DEFAULT_PORT = 6464;
    public static int port;
    public static boolean quit = false;
    protected static ConcurrentHashMap<Integer, Error> threadErrors = new ConcurrentHashMap<>();
    protected static ConcurrentHashMap<Integer, String> usernames = new ConcurrentHashMap<>();
    protected static ConcurrentHashMap<Integer, String> channels = new ConcurrentHashMap<>();
    protected static ConcurrentHashMap<String, List<Integer>> idsByChannel = new ConcurrentHashMap<>();
    protected static ConcurrentHashMap<String, List<String>> messages = new ConcurrentHashMap<>();
    protected static ConcurrentHashMap<Integer, Integer> indices = new ConcurrentHashMap<>(); // next to read
    protected static ConcurrentHashMap<Integer, Integer> ready = new ConcurrentHashMap<>();
    protected static ArrayList<String> channelsList;// = new ArrayList<>();
    protected static List<String> forbiddenUsers;
    protected static List<String> mutedUsers;
    protected static HashMap<String, ArrayList<String>> forbiddenChannelsByUser;
    protected static List<String> silentChannels;
    protected final static Object lock = new Object();
    protected final static Object idsLock = new Object();
    private static Properties props;
    
    public static Error start(String config) {
        props = load(config);
        if (props == null) {
            System.out.printf("Uh oh! Couldn't read config file %s.%n", config);
            return Error.CONFIG;
        }
        try {
            port = Integer.parseInt(props.getProperty("port"));
        } catch (Exception e) {
            port = DEFAULT_PORT;
            System.out.println("Error in parsing port, using default port: " + DEFAULT_PORT + ".");
        }
        
        ServerSocket server = null;
        Socket client = null;
        
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Uh oh! Couldn't create server.");
            return Error.SERVER_CREATION;
        }
        System.out.println("Server created.");
        // initilization and config file here
        channelsList = getChannels();
        if (channelsList == null) {
            System.out.println("Uh oh! Couldn't read channels list.");
            return Error.CONFIG;
        }
        
        forbiddenUsers = Arrays.asList(props.getProperty("forbidden_users","").split(","));
        forbiddenChannelsByUser = getForbiddenUserChannelPairs();
        silentChannels = getSilentChannels();
        mutedUsers = getMutedUsers();
        
        for (String channel : channelsList) {
            messages.put(channel, Collections.synchronizedList(new ArrayList<String>()));
            idsByChannel.put(channel, Collections.synchronizedList(new ArrayList<>()));
        }
        new Thread(() -> {
            while (!Server.quit) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {}
                synchronized (Server.lock) {
                    synchronized (Server.idsLock) {
                        for (String channel : Server.idsByChannel.keySet()) {
                            List<Integer> ids = Server.idsByChannel.get(channel);
                            boolean size = ids.size() > 0;
                            boolean gtz = false;
                            if (size) {
                                /*ids.stream()
                                        .forEach(x -> System.out.println(x + " : " + Server.indices.get(x)));*/
                                gtz = ids.stream()
                                        .mapToInt(x -> Server.indices.get(x))
                                        .min()
                                        .getAsInt() > 0;
                            }

                            if (size && gtz) {
                                Server.messages.get(channel).remove(0);
                                for (int id : ids) {
                                    int index = Server.indices.get(id);
                                    Server.indices.put(id, index - 1); // all should be > 0
                                }
                            }
                        }
                    }
                }
            }
        }, "Remover Guy").start();
        while (!quit) {
            try {
                client = server.accept();
            } catch (IOException e) {
                System.out.println("Uh oh! Couldn't accept client.");
                return Error.CLIENT_ACCEPT;
            }
            //System.out.println("Starting ServerThread.");
            new ServerThread(client).start();
        }
        return Error.NONE;
    }
    
    protected static boolean checkUser(String username) { // true if useable, false otherwise
        boolean ok = !usernames.containsValue(username) && username.matches("[A-Za-z0-9_]{3,25}");
        for (String exp : forbiddenUsers) {
            ok = ok && !username.matches(exp);
        }
        // other code TBD
        return ok;
    }
    
    protected static String defaultChannel() {
        return channelsList.get(0);
    }
    
    protected static boolean checkChannel(int id, String channel) {
        boolean ok = channelsList.contains(channel);
        if (forbiddenChannelsByUser != null) {
            ArrayList<String> forbidden = forbiddenChannelsByUser.get(usernames.get(id));
            if (forbidden != null)
                ok = ok && !forbidden.contains(channel);
        }
        return ok;
    } 
    
    protected static Error checkMessage(int id) {
        String channel = channels.get(id);
        return (silentChannels.contains(channel) ? Error.SILENT : (mutedUsers.contains(usernames.get(id)) ? Error.MUTED : Error.NONE));
    }
    
    /*
    Config file options:
    port (port=)
    channel list without default channel included (channels=)
    default channel (default=)
    default message; optional (default_message=line1\nline2\nline3)
    disable default message; optional (disable_default_message = false/true)
    forbidden usernames; optional (forbidden_users=user,user)
    username and channel forbidden pairs; optional (forbidden_channels_by_user=user:channel,channel,channel;user2:....;)
    no talk channels; optional (silent=channel,channel)
    muted users; optional (muted=user,user)
    */
    
    public static Properties load(String path) {
        Properties defaultProps = new Properties();
        try {
            defaultProps.load(new StringReader("port=" + DEFAULT_PORT + "\n" + "channels=#general\n" + "default=#welcome\n" + "silent=#welcome\n" + "default_message=Welcome!\n" + "disable_default_message=false"));
            Properties props = new Properties(defaultProps);
            FileInputStream in = new FileInputStream(path);
            props.load(in);
            in.close();
            return props;
        } catch (IOException e) {
            return null;
        }
    }
    
    protected static List<String> getMutedUsers() {
        assert props != null;
        return Arrays.asList(props.getProperty("muted").split(","));
    }
    
    protected static List<String> getSilentChannels() {
        assert props != null;
        List<String> x = Arrays.asList(props.getProperty("silent", "").split(","));
        for (String channel : x) {
            if (!checkChannelSyntax(channel)) return new ArrayList<>();
        }
        return x;
    }
    
    protected static String getDefaultMessageDisabled() {
        assert props != null;
        return props.getProperty("disable_default_message");
    }
    
    protected static String getDefaultMessage() {
        assert props != null;
        String msg = props.getProperty("default_message");
        if (msg.contains("\n")) {
            msg = msg.replaceAll("\n", "\\\\n");
            props.setProperty("default_message", msg);
        }
        return msg;
    }
    
    private static boolean checkChannelSyntax(String name) {
        return name.matches("#[a-zA-Z]{1,15}");
    }
    
    private static ArrayList<String> getChannels() {
        assert props != null;
        ArrayList<String> channels = new ArrayList<String>();
        String[] c = props.getProperty("channels").trim().split(",");
        String def = props.getProperty("default");
        channels.add(def);
        channels.addAll(Arrays.asList(c));
        int i = channels.lastIndexOf(def);
        while (i != 0) {
            channels.remove(i);
            i = channels.lastIndexOf(def);
        }
            
        for (String channel : channels)
            if (!checkChannelSyntax(channel)) return null;
        return channels;
    }
    
    private static HashMap<String, ArrayList<String>> getForbiddenUserChannelPairs() {
        assert props != null;
        HashMap<String, ArrayList<String>> pairs = new HashMap<>();
        String line = props.getProperty("forbidden_channels_by_user","");
        //System.out.println(line);
        if (!line.endsWith(";")) line += ";";
        //System.out.println(line);
        if (line.contains(defaultChannel()) || !line.matches("([A-Za-z0-9_]{3,25}:#[a-zA-Z]{1,15}(,#[a-zA-Z]{1,15})*;)+"))
            return pairs;
        String[] rawPairs = line.split(";");
        for (String rawPair: rawPairs) {
            String[] split = rawPair.split(":");
            ArrayList<String> channels = new ArrayList<>();
            channels.addAll(Arrays.asList(split[1].split(",")));
            pairs.put(split[0], channels);
        }
        return pairs;
    }
    
    public static void main(String[] args) {
        props = load("test.properties");
        System.out.println(getDefaultMessage());
        //System.out.println("poopoohead:#welcome,#general;peepeehead:#welcome;".matches("([A-Za-z0-9_]{3,25}:#[a-zA-Z]{1,15}(,#[a-zA-Z]{1,15})*;)+"));
    }
    
}

/*
S = Server, C = Client
Communication Example:
1. C connects to S at port 6464
2. C sends username
3. S sends "200 OK" if username available or "409 CONFLICT" if already taken or disallowed
4. Until "200 OK" in 3, repeat 2-3
5. S sends channel list (separated by spaces and without the default one) and then " DEFAULT " and then the default channel (all channels start with #)
6. S sends the default welcome message
7. S sends <additional slot 2 (extra data TBD)> or "000 NONE"
8. S sends <additional slot 3 (extra data TBD)> or "000 NONE"

Changing channels:
1. C sends "\channel " and then the channel
2. S sends "200 OK" or "409 CONFLICT" if not allowed in channel

Send message:
1. C sends "MSG " and then the message
2. S sends "200 OK" or "401 SILENT" or "405 MUTED"

Receive message:
1. S sends "USER " and the username of the sender and then " MSG " and the message

Exit connection:
1. C sends "BYE"
2. S ends "BYE"
3. sockets deconstruct
*/