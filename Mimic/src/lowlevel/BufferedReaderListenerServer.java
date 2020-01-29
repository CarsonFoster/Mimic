package lowlevel;

import java.io.*;
import java.util.function.*;

public class BufferedReaderListenerServer implements Runnable {
    private BufferedReader in;
    private ServerThread st;
    private BiConsumer<String, ServerThread> r;
    //public volatile boolean running = true;
    
    public BufferedReaderListenerServer(BufferedReader i, ServerThread s) {
        in = i;
        st = s;
    }
    
    public void addBehavior(BiConsumer<String, ServerThread> run) {
        r = run;
    }
    
    public void run() {
        while (st.running) {
            try {
                if (in.ready()) {
                    r.accept(in.readLine().trim(), st);
                }
            } catch (IOException E) {}
        }
    }
}
