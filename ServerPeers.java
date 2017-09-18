/**
 * This class listens for incoming server connections and also tries to connect to other servers.
 * @author cyrusvillacampa
 */

package uni.mitter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import generated.nonstandard.heartbeat.Heartbeat;

public class ServerPeers extends Thread {
    public ServerSocket serverSocket;
    public int serverPort;


    // Constructor
    public ServerPeers (int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * This method opens up a port for other servers to connect to and also starts a thread that attempts
     * to establish a connection to other servers.
     */
    public void init () {
        try {
            // Open a socket for other servers to connect to.
            serverSocket = new ServerSocket(serverPort);
            // Set the accept() method to wait/block for a random amount of time in ms.
            double socketTimeout = 500 + Math.random() * 500;
            serverSocket.setSoTimeout((int) socketTimeout);
        } catch (Exception e) {
            System.err.format("[ SERVER %d ] Error: ServerPeers, " + e.getMessage() + "\n", MitterServer.serverId);
            e.printStackTrace();
        }

        // Start a thread that attempts to establish a connection between this server and the others
        // Thread scThread = new ServerConnector();
        // scThread.start();
    }

    /**
     * This method listens for incoming server connections.
     */
    @Override
    public void run() {
        init();

        while (true) {
            try {
                Socket s = serverSocket.accept();
                int remotePort = s.getPort();
                int serverId = 0;
                // Find the id of the newly connected server using the remote port
                for (List<Integer> list: MitterServer.serverPorts) {
                    if (list.get(1) == remotePort) {
                        serverId = list.get(0);
                    }
                }
                System.out.format("Obtained port number of server %d\n", serverId);
                MitterServer.serversList.add(new ServerIdentity(s,serverId));
                System.out.format("[ SERVER %d ] Established connection with server %d\n",MitterServer.serverId,serverId);
            } catch (IOException e) {
                Socket s = new Socket();
                int serverPort = 0, serverId = 0;

                if (MitterServer.serversList.size() < MitterServer.serverPorts.size()) {
                    // Find a server that is still unconnected using the server id.
                    for (List<Integer> list: MitterServer.serverPorts) {
                        boolean haveSeen = false;
                        for (ServerIdentity sId: MitterServer.serversList) {
                            if (sId.getId() == list.get(0)) {
                                haveSeen = true;
                                break;
                            }
                        }

                        try {
                            if (!haveSeen) {
                                serverPort = list.get(1);
                                serverId = list.get(0);

                                System.out.format("Trying to establish connection to %d with port %d\n", serverId,serverPort);
                                InetSocketAddress endpoint = new InetSocketAddress("localhost", serverPort);
                                s.connect(endpoint);
                                MitterServer.serversList.add(new ServerIdentity(s,serverId));
                                System.out.format("[ SERVER %d ] Established connection with server %d\n",MitterServer.serverId,serverId);
                            }
                        } catch (IOException ex) {
                            // System.err.format("[ SERVER %d ] Error: ServerPeers, " + e.getMessage(), MitterServer.serverId);
                            // e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * This class attempts to establish a connection between this server and other servers.
     */
    // private class ServerConnector extends Thread {
    //     public ServerConnector () { }

    //     @Override
    //     public void run () {
    //         System.out.println("ServerConnector has started");
    //         Socket s = new Socket();
    //         int serverPort = 0, serverId = 0;

    //         while (true) {
    //             // synchronized (MitterServer.serverPorts) {
    //                 if (MitterServer.serversList.size() < MitterServer.serverPorts.size()) {
    //                     try {
    //                         // Find a server that is still unconnected using the server id.
    //                         for (List<Integer> list: MitterServer.serverPorts) {
    //                             boolean haveSeen = false;
    //                             for (ServerIdentity sId: MitterServer.serversList) {
    //                                 if (sId.getId() == list.get(0)) {
    //                                     haveSeen = true;
    //                                     break;
    //                                 }
    //                             }

    //                             if (!haveSeen) {
    //                                 serverPort = list.get(1);
    //                                 serverId = list.get(0);
    //                             }
    //                         }

    //                         System.out.format("Trying to establish connection to %d with port %d\n", serverId,serverPort);
    //                         InetSocketAddress endpoint = new InetSocketAddress("localhost", serverPort);
    //                         s.connect(endpoint);
    //                         MitterServer.serversList.add(new ServerIdentity(s,serverId));
    //                         System.out.format("[ SERVER %d ] Established connection with server %d\n",MitterServer.serverId,serverId);
    //                     } catch (IOException e) {
    //                         // System.err.format("[ SERVER %d ] Error: ServerPeers, " + e.getMessage(), MitterServer.serverId);
    //                         // e.printStackTrace();
    //                     }
    //                 }
    //             // }
    //         }
    //     }
    // }

    /**
     * This class is a wrapper class that contains the socket of the active server and its server id. This
     * class represents the identity of an active server.
     */
    public static class ServerIdentity {
        Socket socket;
        int id;
        public ServerIdentity (Socket socket, int id) {
            this.socket = socket;
            this.id = id;
        }

        public Socket getSocket () {
            return this.socket;
        }

        public int getId () {
            return this.id;
        }
    }
}