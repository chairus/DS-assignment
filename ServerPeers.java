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

import uni.mitter.ServerPeers.ServerIdentity;
import generated.nonstandard.heartbeat.Heartbeat;

public class ServerPeers extends Thread {
    public ServerSocket serverSocket;
    public int serverPort;


    // Constructor
    public ServerPeers (int serverPort) {
        this.serverPort = serverPort;
        init();
    }

    public void init () {
        try {
            // Open a socket for other servers to connect to.
            serverSocket = new ServerSocket(serverPort);
            serverSocket.setSoTimeout(500); // Set the accept() method to wait/block for about 300ms
        } catch (Exception e) {
            System.err.format("[ SERVER %d ] Error: ServerPeers, " + e.getMessage() + "\n", MitterServer.serverId);
            e.printStackTrace();
        }

        // Start a thread that attempts to establish a connection between this server and the others
        Thread scThread = new ServerConnector();
        scThread.start();
    }

    /**
     * This method opens up a port for other servers to connect to and also starts a thread that attempts
     * to establish a connection to other servers.
     */
    @Override
    public void run() {
        while (true) {
            try {
                synchronized (MitterServer.serversList) {
                    if (MitterServer.serversList.size() < 3) {
                        Socket s = serverSocket.accept();
                        int remotePort = s.getPort();
                        int serverId = 0;
                        // Find the id of the newly connected server using the remote port
                        for (int port: MitterServer.serverPorts) {
                            if (port == remotePort) {
                                serverId = MitterServer.serverPorts.indexOf(port);
                            }
                        }
                        System.out.format("Obtained port number of server %d\n", serverId);
                        MitterServer.serversList.add(new ServerIdentity(s,serverId));
                        System.out.format("[ SERVER %d ] Established connection with server %d\n",MitterServer.serverId,serverId);
                    }
                }
            } catch (IOException e) {
                // IGNORE
                // System.err.format("[ SERVER %d ] Error: ServerPeers, " + e.getMessage(), MitterServer.serverId);
                // e.printStackTrace();
            }
        }
    }

    /**
     * This class attempts to establish a connection between this server and other servers.
     */
    private class ServerConnector extends Thread {
        public ServerConnector () { }

        @Override
        public void run () {
            System.out.println("ServerConnector has started");
            Socket s = new Socket();
            int serverPort = 0;

            while (true) {
                synchronized (MitterServer.serversList) {
                    if (MitterServer.serversList.size() < 3) {
                        try {
                            int serverId = 0;
                            // Find a server that is still unconnected using the server id.
                            for (ServerIdentity sId: MitterServer.serversList) {
                                if (sId.getId() != MitterServer.serverId && sId.getId() != serverId) {
                                    serverPort = MitterServer.serverPorts.get(serverId);
                                    break;
                                }
                                serverId += 1;
                            }
                            System.out.format("Trying to establish connection to %d with port %d\n", serverId,serverPort);
                            InetSocketAddress endpoint = new InetSocketAddress("localhost", serverPort);
                            s.connect(endpoint);
                            MitterServer.serversList.add(new ServerIdentity(s,serverId));
                            System.out.format("[ SERVER %d ] Established connection with server %d\n",MitterServer.serverId,serverId);
                        } catch (IOException e) {
                            // System.err.format("[ SERVER %d ] Error: ServerPeers, " + e.getMessage(), MitterServer.serverId);
                            // e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

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