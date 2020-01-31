package lowlevel;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class Server {
    public final static int port = 6464;
    public static boolean quit = false;
    protected static ConcurrentHashMap<Integer, Error> threadErrors = new ConcurrentHashMap<>();
    protected static ConcurrentHashMap<Integer, String> usernames = new ConcurrentHashMap<>();
    protected static ConcurrentHashMap<Integer, String> channels = new ConcurrentHashMap<>();
    protected static ConcurrentHashMap<String, List<Integer>> idsByChannel = new ConcurrentHashMap<>();
    protected static ConcurrentHashMap<String, List<String>> messages = new ConcurrentHashMap<>();
    protected static ConcurrentHashMap<Integer, Integer> indices = new ConcurrentHashMap<>(); // next to read
    protected static ConcurrentHashMap<Integer, Integer> ready = new ConcurrentHashMap<>();
    protected static ArrayList<String> channelsList = new ArrayList<>();
    protected final static Object lock = new Object();
    protected final static Object idsLock = new Object();
    
    public static Error start() {
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
        channelsList.add("#general"); // change later
        channelsList.add("#welcome");
        
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
                                ids.stream()
                                        .forEach(x -> System.out.println(x + " : " + Server.indices.get(x)));
                                gtz = ids.stream()
                                        .mapToInt(
                                                x -> Server.indices.get(x))
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
        // other code TBD
        return ok;
    }
    
    protected static String defaultChannel() {
        return "#general"; // subject to change
    }
    
    protected static boolean checkChannel(int id, String channel) {
        return channelsList.contains(channel);
    } 
    
}

/*
S = Server, C = Client
Communication Example:
1. C connects to S at port 6464
2. C sends username
3. S sends "200 OK" if username available or "409 CONFLICT" if already taken or disallowed
4. Until "200 OK" in 3, repeat 2-3
5. S sends channel list (separated by spaces) and then " DEFAULT " and then the default channel (all channels start with #)
6. S sends <additional slot 1 (extra data TBD)> or "000 NONE"
7. S sends <additional slot 2 (extra data TBD)> or "000 NONE"
8. S sends <additional slot 3 (extra data TBD)> or "000 NONE"

Changing channels:
1. C sends "\channel " and then the channel
2. S sends "200 OK" or "409 CONFLICT" if not allowed in channel

Send message:
1. C sends "MSG " and then the message

Receive message:
1. S sends "USER " and the username of the sender and then " MSG " and the message

Exit connection:
1. C sends "BYE"
2. S ends "BYE"
3. sockets deconstruct
*/