package mimic;

import java.util.Scanner;
import lowlevel.Client;
import lowlevel.Server;

public class Mimic {
    
    public static String trim(String in) {
        while (in.charAt(in.length() - 1) == '\n') {
            in = in.substring(0, in.length() - 1);
        }
        return in;
    }
    
    private static class ConsoleClient implements gui.Client {
        private Scanner in;
        protected ConsoleClient() {
            in = new Scanner(System.in);
        }   
        
        public void errorUsername() {
            System.out.println("That doesn't work.");
        }
        
        public String promptForUsername() {
            System.out.print("Enter a username: ");
            return in.nextLine().trim();
        }
        
        public void errorChannel(){}
    }
    
    private static class SingleClient implements gui.Client {
        private String name;
        
        protected SingleClient(String name) {
            this.name = name;
        }
        
        public void errorUsername() {
            return;
        }
        
        public String promptForUsername() {
            System.out.println("Username: " + name);
            return name;
        }
        
        public void errorChannel() {}
    }
    
    public static void main(String[] args) {
        /*new Thread(() -> Server.start("test.properties")).start();
        Client c = Client.initiate("localhost", 6464, new SingleClient("cwf"), x -> {System.out.println(x);});
        Client d = Client.initiate("localhost", 6464, new SingleClient("not_cwf"), x -> {System.out.println(x);});
        c.changeChannel("#general");
        d.changeChannel("#general");
        c.send("MSG Hello, not_cwf. This is a test of the basic client/server functionality.");
        System.out.println(c.receive());
        d.send("MSG Hello, this is no_cwf.");
        System.out.println(d.receive());*/
        gui.Main.main(args);
    }
    
}
