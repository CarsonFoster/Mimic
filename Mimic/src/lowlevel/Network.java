package lowlevel;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Network {
    public static final int TIMEOUT = 100;
    int netmask_length = 0;
    int[] ip = new int[4];
    List<String> list = null;
    
    public Network(String ip_s, int netmask) throws Exception {
        String[] ip_s_arr = ip_s.split("\\.");
        if (ip_s_arr.length != 4) throw new Exception("That's not a valid IP!");
        int i = 0;
        for (String section : ip_s_arr) {
            try {
                ip[i++] = Integer.parseInt(section);
            } catch (Exception e) {
                throw new Exception("That's not a valid IP!");
            }
        }
        
        if (netmask <= 0 || netmask > 3) throw new Exception("That's not a valid netmask!");
        netmask_length = netmask;
    }
    
    public Stream<String> stream() {
        if (list != null) return list.stream();
       
        generate();
        
        return list.stream();
    }
    
    public void generate() {
        list = new ArrayList<>();
        String prefix = "";
        for (int i = 0; i < netmask_length; i++) {
            prefix += "" + ip[i] + ".";
        }
        prefix = prefix.substring(0, prefix.length() - 1);
        
        int addresses = (int)Math.pow(256, 4 - netmask_length);
        for (int i = 0; i < addresses; i++)
            list.add(prefix);
        generate(0, list.size() - 1, list);
        list = list.stream().filter(x -> !x.matches(".*((\\.0)|(\\.254)|(\\.255))")).collect(Collectors.toList());
    }
    
    private void generate(int start, int end, List<String> l) {
        int section_length = (end - start + 1)/256; 
        if (section_length <= 0) return;
        for (int i = 0; i < 256; i++) {
            int start_mini = start + i * section_length;
            for (int j = start_mini; j < start_mini + section_length; j++) {
                try{
                    l.set(j, l.get(j) + "." + i);
                    //System.out.println(l.get(j));
                } catch (Exception e) {
                    System.out.println("error");
                }
            }
            generate(start_mini, start_mini + section_length - 1, l);
        }
    }
    
    public static boolean open(String host, int port) {
        return open(host, port, TIMEOUT);
    }
    
    public static boolean open(String host, int port, int timeout) {
        boolean open = false;
        try {
            Socket s = new Socket();
            s.connect(new InetSocketAddress(host, port), timeout);
            open = true;
            s.close();
        } catch (Exception e) {
            if (open) return open;
        }
        return open;
    }
    
}