package lowlevel;

import java.io.*;
import java.net.*;

public class Client {
    public Error start(String ip) {
        Socket client;
        BufferedReader in;
        PrintWriter out;
        try {
            client = new Socket(ip, Server.port);
        } catch (IOException e) {
            return Error.CONNECT;
        }
        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);
        } catch (IOException e) {
            return Error.STREAM_CREATION;
        }
        
        return Error.NONE;
    }
}
