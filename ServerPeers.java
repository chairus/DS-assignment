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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import generated.nonstandard.heartbeat.Heartbeat;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class ServerPeers extends Thread {
    public ServerSocket serverSocket;
    public int serverPort;
    public int serverId;
    private JAXBContext jaxbContextHeartbeat;
    private Unmarshaller jaxbUnmarshallerHeartbeat;
    private Marshaller jaxbMarshallerHeartbeat;


    // Constructor
    public ServerPeers (int serverPort, int serverId) {
        this.serverPort = serverPort;
        this.serverId = serverId;
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
            // Create unmarshaller for the heartbeat message
            jaxbContextHeartbeat = JAXBContext.newInstance(Heartbeat.class);
            jaxbUnmarshallerHeartbeat = jaxbContextHeartbeat.createUnmarshaller();
            jaxbMarshallerHeartbeat = jaxbContextHeartbeat.createMarshaller();
        } catch (IOException e) {
            System.err.format("[ SERVER %d ] Error: ServerPeers, " + e.getMessage() + "\n", MitterServer.serverId);
            e.printStackTrace();
        } catch (JAXBException e) {
            System.err.format("[ SERVER %d ] Error: ServerPeers, " + e.getMessage(), MitterServer.serverId);
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * This method listens for incoming server connections.
     */
    @Override
    public void run() {
        init();

        while (true) {
            try {
                if (MitterServer.serversList.size() < MitterServer.serverPorts.size()) {
                    Socket s = serverSocket.accept();

                    // Find the id of the newly connected server by exchanging heartbeat message
                    BufferedReader buffReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    StringReader sReader = new StringReader(buffReader.readLine());
                    Heartbeat hb = (Heartbeat) jaxbUnmarshallerHeartbeat.unmarshal(sReader);
                    
                    // Check if the accepted server connection is already connected
                    boolean isConnected = false;
                    int serverId = hb.getServerId();
                    for (ServerIdentity sId: MitterServer.serversList) {
                        if (sId.getId() == serverId) {
                            isConnected = true;
                        }
                    }
                    
                    if (!isConnected) {
                        // System.out.println("[ ACCEPT ] Server remote port: " + s.getPort());
                        // System.out.println("[ ACCEPT ] Server remote InetAddress: " + s.getInetAddress());
                        MitterServer.serversList.add(new ServerIdentity(s,serverId));
                        System.out.format("[ SERVER %d ] Established connection with server %d\n",MitterServer.serverId,serverId);
                    }
                }
            } catch (IOException e) {
                Socket s = new Socket();
                int remotePort = 0, remoteServerId = 0;

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
                                remotePort = list.get(1);
                                remoteServerId = list.get(0);

                                InetSocketAddress endpoint = new InetSocketAddress("localhost", remotePort);
                                s.connect(endpoint);
                                // Send a heartbeat message to identify itself
                                StringWriter sWriter = new StringWriter();
                                BufferedWriter buffWriter = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                                Heartbeat hb = new Heartbeat();
                                hb.setServerId(MitterServer.serverId);
                                jaxbMarshallerHeartbeat.marshal(hb, sWriter);
                                buffWriter.write(sWriter.toString());
                                buffWriter.newLine();
                                buffWriter.flush();

                                MitterServer.serversList.add(new ServerIdentity(s,remoteServerId));
                                // System.out.println("[ CONNECT ] Server remote port: " + s.getPort());
                                // System.out.println("[ CONNECT ] Server remote InetAddress: " + s.getInetAddress());
                                System.out.format("[ SERVER %d ] Established connection with server %d\n",MitterServer.serverId,remoteServerId);
                            }
                        } catch (IOException ex) {
                            // System.err.format("[ SERVER %d ] Error: ServerPeers, " + e.getMessage(), MitterServer.serverId);
                            // e.printStackTrace();
                        } catch (JAXBException ex) {
                            System.err.format("[ SERVER %d ] Error: ServerPeers, " + e.getMessage(), MitterServer.serverId);
                            e.printStackTrace();
                            System.exit(1);
                        }
                    }
                }
            } catch (JAXBException e) {
                System.err.format("[ SERVER %d ] Error: ServerPeers, " + e.getMessage(), MitterServer.serverId);
                e.printStackTrace();
                System.exit(1);
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