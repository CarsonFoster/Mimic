package lowlevel;

import java.io.*;
import java.util.function.*;

public class BufferedReaderListener implements Runnable {
    private BufferedReader in;
    private Consumer<String> r;
    private Client client;
    public volatile boolean running = true;
    
    public BufferedReaderListener(BufferedReader i, Client c) {
        in = i;
        client = c;
    }
    
    public void addBehavior(Consumer<String> run) {
        r = run;
    }
    
    public void run() {
        while (running) {
            try {
                if (in.ready()) {
                    synchronized (client.lock) {
                        //System.out.println("BufferedReaderListener acquired lock.");
                        if (!in.ready()) continue;
                        r.accept(mimic.Mimic.trim(in.readLine()));
                        //System.out.println("BufferedReaderListener releasing lock.");
                    }
                    //System.out.println("BufferedReaderListener released lock.");
                }
            } catch (IOException E) {}
        }
    }
}
