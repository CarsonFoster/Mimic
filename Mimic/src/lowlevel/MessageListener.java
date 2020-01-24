package lowlevel;

import java.io.*;
import java.util.function.*;

public class MessageListener implements Runnable {
    private ServerThread st;
    private Consumer<ServerThread> r;
    public volatile boolean running = true;
    private int length = 0;
    
    public MessageListener(ServerThread s) {
        st = s;
        length = Server.messages.get(Server.channels.get(st.id)).size();
    }
    
    public void addBehavior(Consumer<ServerThread> run) {
        r = run;
    }
    
    public void run() {
        while (running) {
            int l = Server.messages.get(Server.channels.get(st.id)).size();
            if (l != length) {
                r.accept(st);
                length = l;
            }
        }
    }
}
