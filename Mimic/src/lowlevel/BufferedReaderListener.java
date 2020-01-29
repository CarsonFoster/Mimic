package lowlevel;

import java.io.*;
import java.util.function.*;

public class BufferedReaderListener implements Runnable {
    private BufferedReader in;
    private Consumer<String> r;
    public volatile boolean running = true;
    
    public BufferedReaderListener(BufferedReader i) {
        in = i;
    }
    
    public void addBehavior(Consumer<String> run) {
        r = run;
    }
    
    public void run() {
        while (running) {
            try {
                if (in.ready()) {
                    r.accept(in.readLine().trim());
                }
            } catch (IOException E) {}
        }
    }
}
