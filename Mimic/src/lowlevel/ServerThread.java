package lowlevel;

import java.net.*;
import java.io.*;

public class ServerThread extends Thread {
    protected static int numThreads = 0;
    private Socket client;
    public int id;
    private BufferedReader in;
    //private BufferedReaderListenerServer brl;
    //private MessageListener ml;
    private CombinedBufferMessageListener cbml;
    private DataOutputStream out;
    private Thread cbmlt;//bt, mt;
    public volatile boolean running = true;
    
    public ServerThread(Socket c) {
        super();
        client = c;
        id = numThreads++;
        Server.threadErrors.put(id, Error.NONE);
        Server.indices.put(id, 0);
        Server.ready.put(id, 0);
        System.out.println("ServerThread " + id + " started.");
        setName("ServerThread " + id);
    }
    
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new DataOutputStream(client.getOutputStream());
        } catch (IOException e) {
            Server.threadErrors.put(id, Error.STREAM_CREATION);
            return;
        }
        //System.out.println("sStreams created.");
        /*String line;
        while (true) {
            try {
                line = in.readLine();
                //out.writeBytes(line + "\n");
            } catch (IOException e) {
                System.out.println("ServerThread " + id + " Exception");
                try {
                    in.close();
                    out.close();
                    client.close();
                } catch (IOException e2) {}
                Server.threadErrors.put(id, Error.MESSAGE_RECEIVE);
                return;
            }
        }*/
        String username = receive();
        if (username == null) {
            //shutdown();
            System.out.println("Exiting thread " + id + " because of null username. Probably a test connection.");
            Server.threads.remove(this);
            return;
        }
        while (!Server.checkUser(username)) {
            send("409 CONFLICT");
            username = receive();
        }
        send("200 OK");
        Server.usernames.put(id, username);
        send(Server.channelsList.subList(1, Server.channelsList.size()).stream().reduce("", (x, y) -> x + y + " ") + "DEFAULT " + Server.defaultChannel());
        send(Server.getDefaultMessage());
        send(Server.getDefaultMessageDisabled());
        send("000 NONE");
        Server.channels.put(id, Server.defaultChannel());
        Server.idsByChannel.get(Server.defaultChannel()).add(id);
        cbml = new CombinedBufferMessageListener(in, this);
        cbml.addBehavior((msg, st) -> {
            String[] arr = msg.split(" ");
            String first = arr[0];
            switch (first) {
                case "MSG": 
                    try {
                        System.out.println("Message from " + Server.usernames.get(id) + " in channel " + Server.channels.get(id) + ": " + msg.substring(4));
                    } catch (Exception e) {
                        assert false : ("Error: x" + msg + "x");
                    }
                    String channel = Server.channels.get(id);
                    switch (Server.checkMessage(id)) {
                        case NONE:
                            send("200 OK");
                            break;
                        case SILENT:
                            send("401 SILENT");
                            return;
                        case MUTED:
                            send("405 MUTED");
                            return;
                    }
                    synchronized (Server.lock) {
                        Server.messages.get(channel).add("USER " + Server.usernames.get(id) + " " + msg);
                        synchronized (Server.idsLock) {
                            //System.out.println("IDS: " + Server.idsByChannel.get("#general"));
                            Server.idsByChannel.get(channel).stream().filter(x -> x != st.id).forEach(x -> Server.ready.put(x, Server.ready.get(x) + 1));
                        }
                    }
                    System.out.println(Server.messages.get("#general"));
                    System.out.println("Ready: " + Server.ready.get(0) + " " + Server.ready.get(1) + " " + Server.ready.get(2));
                    System.out.println("Indices: " + Server.indices.get(0) + " " + Server.indices.get(1) + " " + Server.ready.get(2));
                    break;
                case "\\channel": 
                    channel = arr[1];
                    if (arr.length > 2 || !Server.checkChannel(id, channel))
                        send("409 CONFLICT");
                    else {
                        synchronized (Server.idsLock) {
                            Server.idsByChannel.get(Server.channels.get(id)).remove(Integer.valueOf(id));
                            Server.idsByChannel.get(channel).add(id);
                        }
                        Server.channels.put(id, channel);
                        Server.indices.put(id, Server.messages.get(channel).size());
                        Server.ready.put(id, 0);
                        send("200 OK");
                    }
                    break;
                case "BYE": 
                    send("BYE");
                    st.shutdown();
                    break;
            }
        }, (st) -> {
            int index = Server.indices.get(id);
            String channel = Server.channels.get(id);
            String message = Server.messages.get(channel).get(index);
            boolean send_ = !message.startsWith("USER " + Server.usernames.get(st.id) + " ");
            if (send_) {
                send(message);
            }
            Server.indices.put(id, index + 1);
            return send_;
        });
        cbmlt = new Thread(cbml);
        cbmlt.setName("Combined Buffered Reader & Message Listener " + id);
        cbmlt.start();
        /*brl = new BufferedReaderListenerServer(in, this);
        brl.addBehavior((msg, st) -> {
            String[] arr = msg.split(" ");
            String first = arr[0];
            switch (first) {
                case "MSG": 
                    System.out.println("Message from " + Server.usernames.get(id) + " in channel " + Server.channels.get(id) + ": " + msg.substring(4));
                    String channel = Server.channels.get(id);
                    synchronized (Server.lock) {
                        Server.messages.get(channel).add("USER " + Server.usernames.get(id) + " " + msg);
                        synchronized (Server.idsLock) {
                            Server.idsByChannel.get(channel).stream().filter(x -> x != st.id).forEach(x -> Server.ready.put(x, Server.ready.get(x) + 1));
                        }
                    }
                    break;
                case "\\channel": 
                    channel = arr[1];
                    if (arr.length > 2 || !Server.checkChannel(id, channel))
                        send("409 CONFLICT");
                    else {
                        synchronized (Server.idsLock) {
                            Server.idsByChannel.get(Server.channels.get(id)).remove(id);
                            Server.idsByChannel.get(channel).add(id);
                        }
                        Server.channels.put(id, channel);
                        Server.indices.put(id, 0);
                        send("200 OK");
                    }
                    break;
                case "BYE": 
                    send("BYE");
                    st.shutdown();
                    break;
            }
        });
        ml = new MessageListener(this);
        ml.addBehavior((st) -> {
            int index = Server.indices.get(id);
            String channel = Server.channels.get(id);
            String message = Server.messages.get(channel).get(index);
            if (!message.startsWith("USER " + Server.usernames.get(st.id)))
                send(message);
            Server.indices.put(id, index + 1);
        });
        bt = new Thread(brl);
        mt = new Thread(ml);
        bt.setName("BufferListener " + id);
        mt.setName("MessageListener " + id);
        bt.start();
        mt.start();*/
    }
    
    private Error send(String msg) {
        if (!msg.endsWith("\n")) msg += "\n";
        try {
            out.writeBytes(msg);
        } catch (IOException e) {
            System.out.println("ServerThread " + id + " Exception");
            Server.threadErrors.put(id, Error.MESSAGE_SEND);
            return Error.MESSAGE_SEND;
        }
        return Error.NONE;
    }
    
    private String receive() {
        try {
            return in.readLine();
        } catch (IOException e) {
            System.out.println("ServerThread " + id + " Exception");
            Server.threadErrors.put(id, Error.MESSAGE_RECEIVE);
            return null;
        }
    }
    
    public synchronized void shutdown() {
        running = false;
        /*bt.interrupt();
        mt.interrupt();*/
        cbmlt.interrupt();
        try {
            in.close();
            out.close();
            client.close();
        } catch (IOException e) {}
        Server.threadErrors.remove(id);
        Server.idsByChannel.get(Server.channels.get(id)).remove((Integer)id);
        Server.channels.remove(id);
        Server.indices.remove(id);
        Server.usernames.remove(id);
        Server.ready.remove(id);
        System.out.println("Exited thread " + id);
    }
}
