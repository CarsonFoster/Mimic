package lowlevel;

import java.net.*;
import java.io.*;

public class ServerThread extends Thread {
    private static int idCounter = 0;
    private Socket client;
    private int id;
    
    public ServerThread(Socket c) {
        super();
        client = c;
        id = idCounter++;
        Server.threadErrors.put(id, Error.NONE);
    }
    
    @Override
    public void run() {
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);
        } catch (IOException e) {
            Server.threadErrors.put(id, Error.STREAM_CREATION);
            return;
        }
        String line;
        while (true) {
            try {
                line = in.readLine();
                System.out.println(line);
                out.write(line);
            } catch (IOException e) {
                try {
                    in.close();
                    out.close();
                    client.close();
                } catch (IOException e2) {}
                Server.threadErrors.put(id, Error.MESSAGE_RECEIVE);
                return;
            }
        }
    }
}
