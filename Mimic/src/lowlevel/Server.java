package lowlevel;

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    public final static int port = 6464;
    public static boolean quit = false;
    protected static ConcurrentHashMap<Integer, Error> threadErrors = new ConcurrentHashMap<>();
    protected static ConcurrentHashMap<Integer, String> usernames = new ConcurrentHashMap<>();
    
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
        boolean ok = !usernames.containsValue(username);
        // other code TBD
        return ok;
    }
    
}

/*
S = Server, C = Client
Communication Example:
1. C connects to S at port 6464
2. C sends username
3. S sends "200 OK" if username available or "409 CONFLICT" if already taken or disallowed
4. Until "200 OK" in 3, repeat 2-3
5. S sends channel list
*/