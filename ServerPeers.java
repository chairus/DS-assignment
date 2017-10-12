/**
 * This class listens for incoming server connections and also tries to connect to other servers.
 * @author cyrusvillacampa
 */

package uni.mitter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import generated.nonstandard.message.Message;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class ServerPeers extends Thread {
    public  int             serverPort;

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
            MitterServer.serverSocket = new ServerSocket(serverPort);
            // Set the accept method to wait/block for a random amount of time between 300ms and 1000ms.
            double socketTimeout = 300 + Math.random() * 500;
            MitterServer.serverSocket.setSoTimeout((int) socketTimeout);
        } catch (IOException e) {
            System.err.format("[ SERVER %d ] Error: ServerPeers, " + e.getMessage() + "\n", MitterServer.serverId);
            e.printStackTrace();
        }
    }

    /**
     * This method listens for incoming server connections.
     */
    @Override
    public void run() {
        init();

        int numberOfServersConnected = 0;
        while (true) {
            synchronized (MitterServer.serversList) { 
                numberOfServersConnected = MitterServer.serversList.size();
            }
            if (numberOfServersConnected < MitterServer.serverInfo.size()) {
                try {
                    Socket remoteServerSocket = MitterServer.serverSocket.accept();

                    // Find the id of the newly connected server by exchanging heartbeat messages
                    Message hb = MitterServer.readMessage(remoteServerSocket);
                    while (hb == null) {
                        hb = MitterServer.readMessage(remoteServerSocket);
                    }

                    // Check if the accepted server connection is already connected
                    boolean isConnected = false;
                    int serverId = hb.getHeartbeat().getServerId();

                    synchronized (MitterServer.serversList) {
                        for (ServerIdentity sId: MitterServer.serversList) {
                            if (sId.getId() == serverId) {
                                isConnected = true;
                                break;
                            }
                        }
                        
                        if (!isConnected) {
                            MitterServer.serversList.add(new ServerIdentity(remoteServerSocket,serverId));
                            // MitterServer.sendHeartbeatMessage(s);
                            // System.out.printf("[ SERVER %d ] Established connection with server %d\n",MitterServer.serverId,serverId);
                            // System.out.printf("[ SERVER %d ] Number of active servers: %d\n", MitterServer.serverId, MitterServer.serversList.size());
                        }
                    }
                } catch (IOException e) {
                    int remotePort      = 0; 
                    int remoteServerId  = 0;
                    String remoteIpAddress = "";

                    // Find a server that is still unconnected using the server id.
                    for (ServerInfo sInfo: MitterServer.serverInfo) {
                        Socket remoteServerSocket = new Socket();
                        boolean hasSeen = false;
                        synchronized (MitterServer.serversList) {
                            for (ServerIdentity sId: MitterServer.serversList) {
                                if (sId.getId() == sInfo.id) {
                                    hasSeen = true;
                                    break;
                                }
                            }
                            try {
                                if (!hasSeen) {
                                    remotePort = sInfo.serverPort;
                                    remoteServerId = sInfo.id;
                                    remoteIpAddress = sInfo.ipAddress;

                                    InetSocketAddress endpoint = new InetSocketAddress(remoteIpAddress, remotePort);
                                    remoteServerSocket.connect(endpoint);

                                    // Send a heartbeat message to identify itself
                                    MitterServer.sendHeartbeatMessage(remoteServerSocket);
                                    Message hb = MitterServer.readMessage(remoteServerSocket);
                                    // Then add the server to the active servers list and check if a leader has already been elected
                                    MitterServer.serversList.add(new ServerIdentity(remoteServerSocket,remoteServerId));
                                    // System.out.format("[ SERVER %d ] Established connection with server %d\n",MitterServer.serverId,remoteServerId);
                                    // System.out.printf("[ SERVER %d ] Number of active servers: %d\n", MitterServer.serverId, MitterServer.serversList.size());
                                }
                            } catch (IOException ex) {
                                // IGNORE
                            } catch (JAXBException ex) {
                                System.err.printf("[ SERVER %d ] Error: ServerPeers, %s", ex.getMessage(), MitterServer.serverId);
                            }
                        }
                    }
                } catch (JAXBException e) {
                    System.err.printf("[ SERVER %d ] Error: ServerPeers, %s", e.getMessage(), MitterServer.serverId);
                }
            }

            MitterServer.sleepFor(50);
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

        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (o == this) return true;
            if (!(o instanceof ServerIdentity)) return false;
            return ((ServerIdentity)o).getId() == this.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
}