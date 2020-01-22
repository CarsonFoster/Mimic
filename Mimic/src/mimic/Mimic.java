package mimic;

import lowlevel.Client;
import lowlevel.Server;

public class Mimic {
    
    public static void main(String[] args) {
        new Thread(() -> Server.start()).start();
        Client c = new Client();
        c.start("127.0.0.1");
        c.send("raveneus");
        System.out.println(c.receive()); // 200 ok
        System.out.println(c.receive()); // channel list
        System.out.println(c.receive()); // 000
        System.out.println(c.receive()); // 000
        System.out.println(c.receive()); // 000
        c.send("MSG this is a test");
        c.send("MSG test");
        c.send("BYE");
    }
    
}
