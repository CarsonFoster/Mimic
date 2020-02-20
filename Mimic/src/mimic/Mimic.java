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
    }
    
    public static void main(String[] args) {
        new Thread(() -> Server.start("default.properties")).start();
        //Client c = Client.initiate("localhost", new SingleClient("cwf"), str -> {System.out.println("c" + str);});
        gui.ClientWindow cw = new gui.ClientWindow(false, "localhost");
        gui.ClientWindow cw2 = new gui.ClientWindow(false, "localhost");
        gui.ClientWindow cw3 = new gui.ClientWindow(false, "localhost");
        //try { Thread.sleep(8000); } catch (Exception e) {}
        //c.send("MSG this is a test");
        //Client b = Client.initiate("localhost", new ConsoleClient(), str -> {System.out.println("b" + str);});
        //b.changeChannel("#welcome");
        //c.changeChannel("#welcome");
        //c.send("MSG test");
        //c.exit();
    }
    
}
