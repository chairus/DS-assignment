package uni.mitter;

import generated.nonstandard.notification.Notification;
import generated.nonstandard.heartbeat.Heartbeat;
import generated.nonstandard.notification.Notification.Timestamp;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;

/**
 * This class is the implementation of a single Mitter server.
 * @author cyrusvillacampa
 */

public class MitterServer {
    // The number of the smallest proposal this server will accept for any log entry, or 0 if it
    // has never received a Prepare request.
    public static int minProposal;
    // The largest entry for which this server has accepted a proposal
    public static int lastLogIndex;
    // The smallest log index where a proposal has not been accepted, that is 
    // acceptedProposal[firstUnchosenIndex] < inf.
    public static int firstUnchosenIndex;
    // The largest round number that the proposer has seen or used.
    public static int maxRound;
    // This variable indicates that there is no need to issue Prepare requests because a majority 
    // of acceptors has responded to Prepare requests with noMoreAccepted set to true; initially false
    public static boolean prepared;
    // A list that stores the replicated log entries
    List<LogEntry> log;
    // A list of the ports and the server id of each individual server in the network.
    public static List<List<Integer>> serverPorts;
    // A variable that stores the server id of the current leader
    public static ServerPeers.ServerIdentity currentLeader;
    // Server ID of this server
    public static int serverId;
    // A list that stores active servers, each entry in the list stores the socket of the server.
    public static List<ServerPeers.ServerIdentity> serversList;
    // Maximum number of notifications maintained by the server at all times for each severity.
    public static final int[] MAX_NUM_OF_NOTIFICATIONS = {1000, 500, 100}; // [urgent, caution, notice]
    public static final int MAX_NUM_READERS = 100;
    public static final int MAX_NOTIFICATIONS_LIST = 1000;
    // Locks for the MitterServer and the Notifier threads(PRODUCER-CONSUMER)
    public static final Lock notificationListLock = new ReentrantLock();
    public static final Condition notificationListNotFullCondition = notificationListLock.newCondition();
    public static final Condition notificationListNotEmptyCondition = notificationListLock.newCondition();
    // A list that maintains the lists that stores all received notifications
    public static List<List<OrderedNotification>> setOfNotificationList;    // [0 - urgent, 1 - caution, 2 - notice]
    public static List<Thread> clientsList; // A list that stores active clients
    public static ServerSocket serverSocket;
    private int clientPort;
    private int notifierPort;
    private int serverPort;
    private Writer writer;
    private Notification notification;
    private BufferedWriter buffWriter;
    // A variable that holds if the MitterServer is ready to write or if the MitterServer is currently writing on one of the lists(urgent, caution, notice).
    public static Integer[] writerCount = {0,0,0};  // [urgent, caution, notice]
    // Semaphores that synchronizes the readers and writers. This semaphore allows 100 readers to read at the same time.
    public static List<Semaphore> readWriteSemaphores;    // [urgent, caution, notice]
    public static List<Notification> notificationList;
    public Thread serverListenerThread;
    public Thread notifierListenerThread;
    public Thread clientListenerThread;
    public static long notificationListCount;
    private long[] totalOrderSequenceNumbers = {1,1,1}; // [urgent, caution, notice]
    // Marshaller and unmarshaller of the heartbeat message
    public static JAXBContext jaxbContextHeartbeat;
    public static Unmarshaller jaxbUnmarshallerHeartbeat;
    public static Marshaller jaxbMarshallerHeartbeat;

    /**
     * Constructor
     */
    public MitterServer() {
        serverPorts = new ArrayList<>();
        serversList = new ArrayList<>();
        clientsList = new ArrayList<>();
        notificationListCount = 0;
        readWriteSemaphores = new ArrayList<>();
        setOfNotificationList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {   // Initialize the lists and their associated Semaphores
            readWriteSemaphores.add(new Semaphore(MAX_NUM_READERS, true));
            setOfNotificationList.add(new ArrayList<OrderedNotification>());
        }
        notificationList = new ArrayList<>();
        minProposal = 0;
        lastLogIndex = 0;
        maxRound = 0;
        currentLeader = null;
    }

    /**
     * This method setup the ports(client, notifier, server) and the ID of the server.
     * @param clientPort
     * @param notifierPort
     * @param serverPort
     * @param serverId
     */
    public void setUp(int clientPort, int notifierPort, int serverPort, int serverId) {
        this.clientPort = clientPort;
        this.notifierPort = notifierPort;
        this.serverPort = serverPort;
        MitterServer.serverId = serverId;
        try {
            // Create marshaller and unmarshaller for the heartbeat message
            MitterServer.jaxbContextHeartbeat = JAXBContext.newInstance(Heartbeat.class);
            MitterServer.jaxbUnmarshallerHeartbeat = jaxbContextHeartbeat.createUnmarshaller();
            MitterServer.jaxbMarshallerHeartbeat = jaxbContextHeartbeat.createMarshaller();
        } catch (JAXBException e) {
            System.err.format("[ SERVER %d ] Error: MitterServer, " + e.getMessage() + "\n", MitterServer.serverId);
            e.printStackTrace();
        }
    }

    /**
     * This method starts the server and listens for connection from clients, notifiers and servers.
     */
    public void start() {
        // boolean sent = false;

        try {
            // Create and start a notifier listener thread to open a port and listen for incoming notifier connections
            notifierListenerThread = new NotifierListener(notifierPort);
            notifierListenerThread.start();
            // Create and start a client listener thread to open a port and listen for incoming client connections
            clientListenerThread = new ClientListener(clientPort);
            clientListenerThread.start();
            // Create and start a server listener thread to open a port and listen for incoming server connections
            serverListenerThread = new ServerPeers(serverPort);
            serverListenerThread.start();

            System.out.println("MitterServer is running...");
            System.out.format("[ SERVER %d ] Listening to incoming clients on port %d\n",serverId,clientPort);
            System.out.format("[ SERVER %d ] Listening to incoming notifiers on port %d\n",serverId,notifierPort);
            System.out.format("[ SERVER %d ] Listening to incoming servers on port %d\n",serverId,serverPort);

            int serverSize = 0;
            while (serverSize < 1) {
                synchronized (serversList) {
                    serverSize = serversList.size();
                }
            }
            System.out.format("[ SERVER %d ] All servers connected.\n", serverId);

            // Elect a leader
            System.out.println("Electing a leader...");
            while (!electLeader()) { }
            System.out.format("[ SERVER %d ] A leader has been elected.\n", serverId);

            if (currentLeader.getId() == serverId) {
                System.out.println("I AM THE LEADER!");
            } else {
                System.out.println("I AM A SERVANT!");
            }

            while (true) {
                notificationListLock.lock();    // Obtain the lock for the notification list
                if (!notificationList.isEmpty()) {
                    // System.err.println("Notification list is not empty. Taking one out...");
                    Notification notification = takeOneFromNotificationList();
                    assignSequenceNumberAndStore(notification);
                    // System.err.println("Notification list is not empty. Taking one out...SUCCESS");
                } else {
                    notificationListLock.unlock();  // Release lock for notification list
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method performs a single leader election. Returns true if a leader has been elected.
     * @return True if a leader has been elected, false otherwise.
     */
    public boolean electLeader() {
        ServerPeers.ServerIdentity highestId = findHighestServerId(); 

        try {
            if (highestId.getId() == serverId) {
                synchronized (serversList) {
                    // Reply to all heartbeat messages
                    for (ServerPeers.ServerIdentity sId: serversList) {
                        sendHeartbeatMessage(sId.getSocket());
                    }
                    // Read all received heartbeat messages
                    for (ServerPeers.ServerIdentity sId: serversList) {
                        readHeartbeatMessage(sId.getSocket());
                    }
                }
                
                currentLeader = highestId;
                return true;
            }

            // Send heartbeat message to server with highest id
            sendHeartbeatMessage(highestId.getSocket());

            // Read the received heartbeat with a 300ms time limit
            long startTime = System.currentTimeMillis();
            long currentTime;
            do {
                BufferedReader buffReader = new BufferedReader(new InputStreamReader(highestId.getSocket().getInputStream()));
                StringReader sReader;
                Heartbeat hb;
                if (buffReader.ready()) {
                    sReader = new StringReader(buffReader.readLine());
                    hb = (Heartbeat) jaxbUnmarshallerHeartbeat.unmarshal(sReader);
                    if (hb.getServerId() == highestId.getId()) {
                        currentLeader = highestId;
                        return true;
                    }
                } 
                currentTime = System.currentTimeMillis();
            } while ((currentTime-startTime) < 300);
        } catch (JAXBException e) {
            System.err.format("[ SERVER %d ] Error: MitterServer, " + e.getMessage() + "\n", MitterServer.serverId);
            e.printStackTrace();
        } catch (IOException e) {
            return false;
        }

        return false;
    }

    /**
     * This method sends a heartbeat message to a server using the given socket.
     * @param s - A socket on where to send a heartbeat message to
     */
    public static void sendHeartbeatMessage(Socket s) throws JAXBException, IOException {
        // Send a heartbeat to the server that has the highest id.
        BufferedWriter buffWriter = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
        StringWriter sWriter = new StringWriter();
        Heartbeat hb = new Heartbeat();
        hb.setServerId(serverId);
        jaxbMarshallerHeartbeat.marshal(hb, sWriter);
        buffWriter.write(sWriter.toString());
        buffWriter.newLine();
        buffWriter.flush();
    }

    /**
     * This method reads a heartbeat message from a given server using the given socket.
     * @param s - A socket on where to listen for a heartbeat message
     * @return - The heartbeat message, null if none is read
     */
    public static Heartbeat readHeartbeatMessage(Socket s) throws JAXBException, IOException {
        BufferedReader buffReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        StringReader sReader;
        Heartbeat hb = null;

        sReader = new StringReader(buffReader.readLine());
        hb = (Heartbeat) jaxbUnmarshallerHeartbeat.unmarshal(sReader);

        return hb;
    }

    /**
     * This method searches for an active server including this server that has the 
     * highest server id.
     * @return - The server's id together with the socket associated to it.
     */
    public ServerPeers.ServerIdentity findHighestServerId() {
        // Server assumes that it is the current leader
        ServerPeers.ServerIdentity highestId = new ServerPeers.ServerIdentity(null, serverId);

        synchronized (serversList) {
            for (ServerPeers.ServerIdentity sId: serversList) {
                if (highestId.getId() < sId.getId()) {
                    highestId = sId;
                }
            }
        }

        return highestId;
    }

    /**
     * Take notifications from the notification list and stores it into one of the lists.
     * This method does not wait if the notification list is empty.
     */
    public Notification takeOneFromNotificationList() throws InterruptedException {
        Notification notification = null;
        // System.err.println("Taking notification from the list...");
        // notificationListLock.lock();    // Obtain the lock for the notification list

        // if (notificationListCount != 0) { 
            notification = notificationList.get(0);
            notificationList.remove(0);
            notificationListCount -= 1;
        // }

        notificationListNotFullCondition.signal();  // Signal waiting notifier thread
        notificationListLock.unlock();  // Release lock for notification list
        // System.err.println("Taking notification from the list...SUCCESS");
        return notification;
    }

    /**
     * This method assign a sequence number to a notification and store it into the correct list.
     * @param notification - Notification received from one of the notifiers or server
     */
    public void assignSequenceNumberAndStore(Notification notification) throws InterruptedException {
        // System.err.println("Assigning sequence number on a notification...");
        OrderedNotification orderedNotification = new OrderedNotification();

        switch (notification.getSeverity().toLowerCase()) {
            case "urgent":
                orderedNotification = assignSequenceNumber(notification, 0);
                break;
            case "caution":
                orderedNotification = assignSequenceNumber(notification, 1);
                break;
            case "notice":
                orderedNotification = assignSequenceNumber(notification, 2);
                break;
            default:
                break;
        }
        // System.err.println("Assigning sequence number on a notification...SUCCESS");
        putOrderedNotificationToList(orderedNotification);
    }

    /**
     * This assigns the sequence number to the notification.
     * This method will eventually run PAXOS algorithm to obtain a sequence number.
     * @param notification - Notification taken from the notification list
     * @param severity - The encoded severity number[0 - urgent, 1 - caution, 2 - notice]
     */
    public OrderedNotification assignSequenceNumber(Notification notification, int severity) {
        OrderedNotification on = new OrderedNotification();
        long currSeqNum = totalOrderSequenceNumbers[severity];; // The current sequence number

        on.setNotification(notification);
        on.setSequenceNumber(currSeqNum);

        totalOrderSequenceNumbers[severity] += 1;   // Update the current sequence number

        return on;
    }

    /**
     * This method stores the ordered notification into the correct list.
     * @param orderedNotification - A notification with a sequence number
     */
    public void putOrderedNotificationToList(OrderedNotification orderedNotification) throws InterruptedException {
        String severity = orderedNotification.getNotification().getSeverity();
        switch (severity.toLowerCase()) {
            case "urgent":
                put(orderedNotification,0);
                break;
            case "caution":
                put(orderedNotification,1);
                break;
            case "notice":
                put(orderedNotification,2);
                break;
            default:
                break;
        }
    }

    /**
     * This method puts the ordered notification into the list specified by the list number argument
     * @param orderedNotification - A notification with a sequence number
     * @param listNumber - 0 for urgent, 1 for caution and 2 for notice
     */
    public void put(OrderedNotification orderedNotification, int listNumber) throws InterruptedException {
        // System.err.println("Putting ordered notification into the appropriate list...");
        synchronized (writerCount) {    // Tell the reader that the server is ready to write
            writerCount[listNumber] += 1;
        }

        readWriteSemaphores.get(listNumber).acquire(MAX_NUM_READERS);   // Obtain lock
        checkNotificationLimit(listNumber);
        setOfNotificationList.get(listNumber).add(orderedNotification);

        synchronized (writerCount) {    // Tell the readers that the server has finished writing
            writerCount[listNumber] -= 1;
        }

        readWriteSemaphores.get(listNumber).release(MAX_NUM_READERS);  // Release lock
        // System.err.println("Putting ordered notification into the appropriate list...SUCCESS");
    }

    /**
     * This method checks if the maximum limit for stored notifications has been reached,if it
     * is then this calls a method that stores the notification that is about to be deleted to
     * all client thread's maintained list.
     * @param listNumber - The associated number to one of the three lists(0 - urgent, 1 - caution, 2 - notice).
     */
    public void checkNotificationLimit(int listNumber) {
        if (setOfNotificationList.get(listNumber).size() == MAX_NUM_OF_NOTIFICATIONS[listNumber]) {
            OrderedNotification on = setOfNotificationList.get(listNumber).get(0);  // Get the oldest notification
            storeDeletedNotificationToAllClientThreadCache(on);
            setOfNotificationList.get(listNumber).remove(0);    // Remove the oldest notification from the list
        }
    }

    /**
     * This method stores the notification that is about to be deleted into the list that each 
     * client thread maintains.
     * @param on - An ordered notification.
     */
    public void storeDeletedNotificationToAllClientThreadCache(OrderedNotification on) {
        // System.err.println("Storing deleted notification...");
        Iterator it = clientsList.iterator();

        while (it.hasNext()) {  // Loop through all active clients
            ClientThread t = (ClientThread) it.next();
            synchronized (t.deletedNotifications) { // Obtain lock for the deleted notifications list
                t.deletedNotifications.add(on);
                // System.err.println("Size of deletedNotifications " + t.deletedNotifications.size());
            }
        }

        // System.err.println("Storing deleted notification...SUCCESS");
    }

    /**
     *  ========
     * |  MAIN  |
     *  ========
     */
    public static void main(String[] args) {
        MitterServer mServer = new MitterServer();
        int cPort = 0, 
            nPort = 0, 
            sPort = 0,
            serverID = 0;

        if (args.length != 1) {
            System.err.println("[ INFO ] Usage: MitterServer [server_id]");
            System.exit(1);
        }

        try {
            FileReader fReader = new FileReader("config.txt");
            BufferedReader bReader = new BufferedReader(fReader);
            String line;
            String regex = "\\s+";
            while ((line = bReader.readLine()) != null) {
                String[] lineArr = line.trim().split(regex);
                if (lineArr.length != 4) {
                    System.err.println("[ INFO ] Error: Missing port number or server id.");
                    System.exit(1);
                }

                serverID = Integer.parseInt(args[0]);
                int configServerId = Integer.parseInt(lineArr[0]);

                if (serverID == configServerId) {
                    cPort = Integer.parseInt(lineArr[1]);
                    nPort = Integer.parseInt(lineArr[2]);
                    sPort = Integer.parseInt(lineArr[3]);
                } else {
                    serverPorts.add(new ArrayList<>());
                    serverPorts.get(serverPorts.size()-1).add(Integer.parseInt(lineArr[0]));   // Server ID
                    serverPorts.get(serverPorts.size()-1).add(Integer.parseInt(lineArr[3]));   // Server port                    
                }
            }

            if (cPort == 0 || nPort == 0 || sPort == 0) {
                System.err.println("[ INFO ] Error: The given server id argument must be one in the config file.");
                System.exit(1);
            }
        } catch (NumberFormatException e) {
            System.err.println("[ INFO ] Error: Ports and server id must be an integer.");
            System.exit(1);
        } catch (FileNotFoundException e) {
            System.err.println("[ INFO ] Error: File 'config.txt' could not be found.");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("[ INFO ] Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        mServer.setUp(cPort,nPort,sPort,serverID);
        mServer.start();
    }
}