package lowlevel;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.*;

public class MessageListener implements Runnable {
    private final ServerThread st;
    private Consumer<ServerThread> r;
    //public volatile boolean running = true;
    private int length = 0;
    
    public MessageListener(ServerThread s) {
        st = s;
        length = Server.messages.get(Server.channels.get(st.id)).size();
    }
    
    public void addBehavior(Consumer<ServerThread> run) {
        r = run;
    }
    
    public void run() {
        while (st.running) {
            /*int l = Server.messages.get(Server.channels.get(st.id)).size();
            if (l > length) {
                r.accept(st);
                length++;
            } else if (l < length) {
                length = l;
            }*/
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            int ready = Server.ready.get(st.id);
            if (ready > 0) {
                r.accept(st);
                Server.ready.put(st.id, Server.ready.get(st.id) - 1); // check again in case it changed
            }
        }
    }
}
