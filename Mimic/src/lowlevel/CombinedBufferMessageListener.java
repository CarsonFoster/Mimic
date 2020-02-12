package lowlevel;

import java.io.*;
import java.util.function.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CombinedBufferMessageListener implements Runnable {
    private BufferedReader in;
    private ServerThread st;
    private BiConsumer<String, ServerThread> r;
    private Consumer<ServerThread> s;
    private int length = 0;
    //public volatile boolean running = true;
    
    public CombinedBufferMessageListener(BufferedReader i, ServerThread s) {
        in = i;
        st = s;
        length = Server.messages.get(Server.channels.get(st.id)).size();
    }
    
    public void addBehavior(BiConsumer<String, ServerThread> run, Consumer<ServerThread> s) {
        r = run;
        this.s = s;
    }
    
    public void run() {
        while (st.running) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            try {
                if (in.ready()) {
                    r.accept(in.readLine().trim(), st);
                }
            } catch (IOException E) {}
            int ready = Server.ready.get(st.id);
            if (ready > 0) {
                s.accept(st);
                Server.ready.put(st.id, Server.ready.get(st.id) - 1); // check again in case it changed
            }
        }
    }
}


