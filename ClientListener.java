package uni.mitter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * This class listen and accept client connection, and create threads for each connected client.
 */
public class ClientListener extends Thread {
    private int clientPort;
    private ServerSocket serverSocket;
    public Socket clientSocket;
    public List<Thread> clientsList;    // A list that stores active clients

    public ClientListener(ServerSocket serverSocket, int clientPort, List<Thread> clientsList) {
        this.serverSocket = serverSocket;
        this.clientPort = clientPort;
        this.clientsList = clientsList;
    }

    /**
     * This method accept client connection and create's a thread that manages each connected client.
     */
    public void run() {
        while (true) {
            try {
                clientSocket = serverSocket.accept();
                // Create and start the thread for the client
                Thread t = new ClientThread(clientSocket);
                t.start();
                // Add new connected client to the list of active clients
                synchronized (clientsList) {
                    clientsList.add(t);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}