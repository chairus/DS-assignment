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
            if (numberOfServersConnected < MitterServer.serverPorts.size()) {
                try {
                    Socket s = MitterServer.serverSocket.accept();

                    // Find the id of the newly connected server by exchanging heartbeat message
                    Message hb = MitterServer.readMessage(s);
                    while (hb == null) {
                        hb = MitterServer.readMessage(s);
                    }

                    // Check if the accepted server connection is already connected
                    boolean isConnected = false;
                    int serverId = hb.getHeartbeat().getServerId();

                    synchronized (MitterServer.serversList) {
                        for (ServerIdentity sId: MitterServer.serversList) {
                            if (sId.getId() == serverId) {
                                isConnected = true;
                            }
                        }
                        
                        if (!isConnected) {
                            MitterServer.serversList.add(new ServerIdentity(s,serverId));
                            System.out.format("[ SERVER %d ] Established connection with server %d\n",MitterServer.serverId,serverId);
                        }
                    }
                } catch (IOException e) {
                    int remotePort      = 0; 
                    int remoteServerId  = 0;

                    // Find a server that is still unconnected using the server id.
                    for (List<Integer> list: MitterServer.serverPorts) {
                        Socket s = new Socket();
                        boolean haveSeen = false;
                        synchronized (MitterServer.serversList) {
                            for (ServerIdentity sId: MitterServer.serversList) {
                                if (sId.getId() == list.get(0)) {
                                    haveSeen = true;
                                    break;
                                }
                            }
                        }

                        try {
                            if (!haveSeen) {
                                remotePort = list.get(1);
                                remoteServerId = list.get(0);

                                InetSocketAddress endpoint = new InetSocketAddress("127.0.0.1", remotePort);
                                s.connect(endpoint);

                                // Send a heartbeat message to identify itself
                                MitterServer.sendMessage(s);

                                synchronized (MitterServer.serversList) {
                                    MitterServer.serversList.add(new ServerIdentity(s,remoteServerId));
                                }
                                System.out.format("[ SERVER %d ] Established connection with server %d\n",MitterServer.serverId,remoteServerId);
                            }
                        } catch (IOException ex) {
                            // IGNORE
                        } catch (JAXBException ex) {
                            System.err.format("[ SERVER %d ] Error: ServerPeers, " + ex.getMessage(), MitterServer.serverId);
                            ex.printStackTrace();
                            System.exit(1);
                        }
                    }
                } catch (JAXBException e) {
                    System.err.format("[ SERVER %d ] Error: ServerPeers, " + e.getMessage(), MitterServer.serverId);
                    e.printStackTrace();
                    System.exit(1);
                }
            }

            try {
                TimeUnit.MILLISECONDS.sleep(50);   
            } catch (Exception e) {
                //IGNORE
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