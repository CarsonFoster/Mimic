package mimic;

import java.util.Scanner;
import lowlevel.Client;
import lowlevel.Server;

public class Mimic {
    
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
    }
    
    public static void main(String[] args) {
        new Thread(() -> Server.start()).start();
        Client c = Client.initiate("localhost", new ConsoleClient(), str -> {System.out.println(str);});
        c.send("MSG this is a test");
        c.changeChannel("#welcome");
        c.send("MSG test");
        c.exit();
    }
    
}
