package uni.mitter;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshalException;
import generated.nonstandard.notification.Notification;


/**
 * This class listen and accept notifier connection request. It uses a non-blocking version of
 * Java Socket to manage notifiers.
 * @author cyrusvillacampa
 */
public class NotifierListener extends Thread {
    private int NOTIFIER_PORT;
    private ServerSocketChannel serverChannel;
    private SocketChannel notifierChannel;
    private Selector selector;
    private SelectionKey notifierKey;

    public NotifierListener(int NOTIFIER_PORT) {
        this.NOTIFIER_PORT = NOTIFIER_PORT;
    }

    public void run() {
        try {
            init();

            while (true) {
                System.out.println("Checking if there are connections ready to be acted on...");
                selector.select(); // Check whether anything is ready to be acted upon.

                Set<SelectionKey> readyKeys = selector.selectedKeys();
                Iterator it = readyKeys.iterator();
                while (it.hasNext()) {
                    SelectionKey key = (SelectionKey)it.next();
                    it.remove();    // Remove key from the set so that it won't be processed twice.
                    processKey(key);
                }
            }
        } catch (IOException e) {
            //TODO: handle exception
        } catch (JAXBException e) {
            //TODO: handle exception
        } catch (InterruptedException e) {

        }
    }

    /**
     * This method open a port to listen for incoming notifier connections.
     */
    public void init() throws IOException {
        serverChannel = ServerSocketChannel.open();
        /* NOTE: UNCOMMENT FOR JAVA 6 and OLDER */
        // ServerSocket ss = serverChannel.socket();   // Grab socket of the server socket channel
        // ss.bind(new InetSocketAddress(NOTIFIER_PORT));  // Bind socket to a port
        
        /* NOTE: FOR JAVA 7 and LATER */
        serverChannel.bind(new InetSocketAddress(NOTIFIER_PORT));   // Bind socket to a port
        System.out.print("Listening for incoming notifier connections...");
        notifierChannel = serverChannel.accept(); // Accept a connection from a notifier
        System.out.println("ACCEPTED ONE");
        notifierChannel.configureBlocking(false);   // Make the notifier channel non-blocking
        serverChannel.configureBlocking(false); // Also make the server channel non-blocking so that calls to accept() method will immediately return if no incoming connections

        selector = Selector.open();    // Creates a Selector that iterates over all the connections that are ready to be processed.

        // Register channel on what operation should it monitor
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);   // Make the server channel listen for incoming connections
        notifierKey = notifierChannel.register(selector, SelectionKey.OP_READ);   // Make the notifier channel listen for incoming notifications
    }

    /**
     * This method checks whether there's a new incoming connection or a notifier is ready to send a
     * notification.
     */
    public void processKey(SelectionKey key) throws IOException, 
                                                    JAXBException,
                                                    InterruptedException {
        if (key.isAcceptable()) {   // New incoming connection from a notifier
            ServerSocketChannel server = (ServerSocketChannel) key.channel();
            SocketChannel connection = server.accept();
            connection.configureBlocking(false);
            connection.register(selector, SelectionKey.OP_READ);
        } else if (key.isReadable()) {  // New notifications from a notifier
            SocketChannel notifier = (SocketChannel) key.channel();
            storeNotification(notifier.socket());
        }
    }

    /**
     * This method stores the received notification into the notification list for the MitterServer to
     * associate sequence number to it.
     */
    public void storeNotification(Socket notifierSocket) throws IOException, 
                                                                JAXBException,
                                                                InterruptedException {
        // Get stream for reading notification
        InputStream in = notifierSocket.getInputStream();
        Reader reader = new InputStreamReader(in, "UTF-8");
        BufferedReader buffReader = new BufferedReader(reader);

        // Initialize unmarshaller(notification)
        JAXBContext jaxbContext = JAXBContext.newInstance(Notification.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

        // Read and unmarshall client notification
        System.out.println("Reading notification from notifier...");
        StringReader dataReader = new StringReader(buffReader.readLine());
        Notification notification = (Notification) jaxbUnmarshaller.unmarshal(dataReader);

        put(notification);
    }

    /**
     * This method puts the notification into the list. This method waits if the list is full.
     */
    public void put(Notification notification) throws InterruptedException {
        MitterServer.notificationListLock.lock();   // Obtain lock for the notification list

        while (MitterServer.MAX_NOTIFICATIONS_LIST == MitterServer.notificationList.size()) {
            MitterServer.notificationListNotFullCondition.await();
        }

        MitterServer.notificationList.add(notification);
        MitterServer.notificationListNotEmptyCondition.signal();    // Signal waiting threads
        MitterServer.notificationListLock.unlock(); // Release lock
    }
}