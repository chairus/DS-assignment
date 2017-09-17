/**
 * This class listens for incoming server connections and also tries to connect to other servers.
 * @author cyrusvillacampa
 */

package uni.mitter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import uni.mitter.ServerPeers.ServerIdentity;
import generated.nonstandard.heartbeat.Heartbeat;

public class ServerPeers extends Thread {
    ServerSocket serverSocket;
    List<Socket> sockets;
    int serverPort;

    // Constructor
    public ServerPeers (int serverPort) {
        this.serverPort = serverPort;
        this.sockets = new ArrayList<>();
    }

    /**
     * This method opens up a port for other servers to connect to and also starts a thread that attempts
     * to establish a connection to other servers.
     */
    @Override
    public void run() {
        try {
            JAXBContext jaxbContextHeartbeat = JAXBContext.newInstance(Heartbeat.class);
            Unmarshaller jaxbUnmarshallerHeartbeat = jaxbContextHeartbeat.createUnmarshaller();
            // Open a socket for other servers to connect to.
            serverSocket = new ServerSocket(serverPort);
            serverSocket.setSoTimeout(500); // Set the accept() method to wait/block for about 300ms

            // Start a thread that attempts to establish a connection between this server and the others
            Thread scThread = new ServerConnector();
            scThread.start();

            while (true) {
                if (MitterServer.serversList.size() < 3) {
                    try {
                        Socket s = serverSocket.accept();
                        InputStream in = s.getInputStream();
                        InputStreamReader reader = new InputStreamReader(in, "UTF-8");
                        BufferedReader buffReader = new BufferedReader(reader);
                        StringReader dataReader = new StringReader(buffReader.readLine());
                        Heartbeat hb = (Heartbeat) jaxbUnmarshallerHeartbeat.unmarshal(dataReader);
                        MitterServer.serversList.add(new ServerIdentity(s,hb));  
                    } catch (SocketException e) {
                        //ignore
                    }
                }
            }
            

        } catch (IOException e) {
            System.err.println("[ INFO ] Error: ServerListener, " + e.getMessage());
            e.printStackTrace();
        } catch (JAXBException e) {
            System.err.println("[ INFO ] Error: ServerListener, " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * This class attempts to establish a connection between this server and other servers.
     */
    private class ServerConnector extends Thread {
        public ServerConnector () {

        }

        @Override
        public void run () {

        }
    }

    /**
     * This class is a wrapper class that contains the socket of the active server and its server id. This
     * class represents the identity of an active server.
     */
    public static class ServerIdentity {
        Socket socket;
        Heartbeat hb;
        public ServerIdentity (Socket socket, Heartbeat hb) {
            this.socket = socket;
            this.hb = hb;
        }

        public Socket getSocket () {
            return this.socket;
        }

        public int getId () {
            return this.hb.getServerId();
        }
    }
}