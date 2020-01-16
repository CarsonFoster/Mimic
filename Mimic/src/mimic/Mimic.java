package mimic;

import lowlevel.Client;
import lowlevel.Server;

public class Mimic {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new Thread(() -> Server.start()).start();
        new Client().start("127.0.0.1");
    }
    
}
