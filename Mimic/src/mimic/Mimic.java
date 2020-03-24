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
            return name;
        }
        
        public void errorChannel() {}
    }
    
    public static void main(String[] args) {
        new Thread(() -> Server.start("test.properties")).start();
        //Client c = Client.initiate("localhost", 6464, new ConsoleClient(), x -> {System.out.println(x);});
        
        Client c = new Client();
        c.info = new Client.Info();
        c.info.port = 6464;
        System.out.println(c.scanParallel());
        System.exit(0);
    }
    
}
