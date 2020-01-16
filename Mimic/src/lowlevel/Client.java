package lowlevel;

import java.io.*;
import java.net.*;

public class Client {
    public Error start(String ip) {
        Socket client;
        BufferedReader in;
        DataOutputStream out;
        try {
            client = new Socket(ip, Server.port);
        } catch (IOException e) {
            return Error.CONNECT;
        }
        System.out.println("Client connected.");
        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new DataOutputStream(client.getOutputStream());
        } catch (IOException e) {
            return Error.STREAM_CREATION;
        }
        //System.out.println("Streams created.");
        /*String line;
        try {
            line = in.readLine();
        } catch (IOException e) {
            System.out.println("Client exception.");
            try {
                in.close();
                out.close();
                client.close();
            } catch (IOException e2) {}
            return Error.MESSAGE_RECEIVE;
        }*/
        return Error.NONE;
    }
}
