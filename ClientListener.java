package uni.mitter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * This class listen and accept client connection, and create threads for each connected client.
 * @author cyrusvillacampa
 */
public class ClientListener extends Thread {
    private ServerSocket serverSocket;
    public Socket clientSocket;    

    public ClientListener(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    /**
     * This method accept client connection and create's a thread that manages each connected client.
     */
    public void run() {
        // System.out.println("Listening for client connection...");
        while (true) {
            try {
                clientSocket = serverSocket.accept();
                // Create and start the thread for the client
                Thread t = new ClientThread(clientSocket);
                t.start();
                // Add new connected client to the list of active clients
                synchronized (MitterServer.clientsList) {
                    MitterServer.clientsList.add(t);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}