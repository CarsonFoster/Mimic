package lowlevel;

import java.io.*;
import java.util.function.*;

public class BufferedReaderListener implements Runnable {
    private BufferedReader in;
    private ServerThread st;
    private BiConsumer<String, ServerThread> r;
    public volatile boolean running = true;
    
    public BufferedReaderListener(BufferedReader i, ServerThread s) {
        in = i;
        st = s;
    }
    
    public void addBehavior(BiConsumer<String, ServerThread> run) {
        r = run;
    }
    
    public void run() {
        while (running) {
            try {
                if (in.ready()) {
                    r.accept(in.readLine().trim(), st);
                }
            } catch (IOException E) {}
        }
    }
}
