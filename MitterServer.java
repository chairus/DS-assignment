package uni.mitter;

import generated.nonstandard.notification.NotificationInfo;
import generated.nonstandard.message.Message;
import generated.nonstandard.notification.ObjectFactory;
import generated.nonstandard.notification.NotificationInfo.Timestamp;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
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
    public static float minProposal;
    // The index to be used to write a value to each server on their log.
    public static int nextIndex;
    // The largest entry for which this server has accepted a proposal
    public static int lastLogIndex;
    // The smallest log index where a proposal has not been accepted, that is 
    // acceptedProposal[firstUnchosenIndex] < inf.
    public static int firstUnchosenIndex;
    // The largest round number that the proposer has seen or used.
    public static long maxRound;
    // This variable indicates that there is no need to issue Prepare requests because a majority 
    // of acceptors has responded to Prepare requests with noMoreAccepted set to true; initially false
    public static boolean prepared;
    // A list that stores the replicated log entries
    public static List<LogEntry> log;
    // A list of information(i.e. server id, server/notifier port, ip address) of each individual server in the network.
    public static List<ServerInfo> serverInfo;
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
    public static final Condition notificationListHasNotReplicatedCondition = notificationListLock.newCondition();
    // A list that maintains the lists that stores all received notifications
    public static List<List<OrderedNotification>> setOfNotificationList;    // [0 - urgent, 1 - caution, 2 - notice]
    public static List<Thread> clientsList; // A list that stores active clients
    public static ServerSocket serverSocket;
    private int clientPort;
    private int notifierPort;
    private int serverPort;
    private Writer writer;
    private NotificationInfo notification;
    private BufferedWriter buffWriter;
    // A variable that holds if the MitterServer is ready to write or if the MitterServer is currently writing on one of the lists(urgent, caution, notice).
    public static Integer[] writerCount = {0,0,0};  // [urgent, caution, notice]
    // Semaphores that synchronizes the readers and writers. This semaphore allows 100 readers to read at the same time.
    public static List<Semaphore> readWriteSemaphores;    // [urgent, caution, notice]
    public static List<NotificationInfo> notificationList;
    public Thread serverListenerThread;
    public Thread notifierListenerThread;
    public Thread clientListenerThread;
    public static long notificationListCount;
    private long[] totalOrderSequenceNumbers = {1,1,1}; // [urgent, caution, notice]
    // Marshaller and unmarshaller of the Prepare, Accept and Success message
    public static JAXBContext jaxbContextMessage;
    public static Unmarshaller jaxbUnmarshallerMessage;
    public static Marshaller jaxbMarshallerMessage;
    // Proposer and Acceptor objects
    private Proposer proposer;
    private Acceptor acceptor;
    // An assertion if this server is the current leader/proposer
    public static boolean isLeader;
    // This index keeps track of which entry in the log is now ready to copy into the setOfNotifications list 
    private int nextLogEntryToStore;
    // Number of notifications to relay to leader
    public static final int BATCH_SIZE = 10;
    // Number of notifications that was sent/relayed to the leader
    public static Integer numOfNotificationsRelayed;
    // The thread that relays the notifications to the leader
    private Thread notificationRelayer;
    // A flag that indicates if there is a change in leader
    public static boolean changeInLeader;
    // Debug mode(i.e. for printing the contents of the log)
    private boolean debug;

    /**
     * Constructor
     */
    public MitterServer() {
        serverInfo = new ArrayList<>();
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
        log = new ArrayList<>();
        minProposal = 0;
        lastLogIndex = -1;
        maxRound = 1;
        currentLeader = null;
        notificationRelayer = null;
        proposer = new Proposer();
        acceptor = new Acceptor();
        prepared = false;
        firstUnchosenIndex = 0;
        isLeader = false;
        nextLogEntryToStore = 0;
        numOfNotificationsRelayed = 0;
        changeInLeader = false;
        debug = false;
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
            // Create marshaller and unmarshaller for the Prepare, Accept and Success messages
            MitterServer.jaxbContextMessage = JAXBContext.newInstance(Message.class);
            MitterServer.jaxbUnmarshallerMessage = jaxbContextMessage.createUnmarshaller();
            MitterServer.jaxbMarshallerMessage = jaxbContextMessage.createMarshaller();
        } catch (JAXBException e) {
            System.err.format("[ SERVER %d ] Error: MitterServer, " + e.getMessage() + "\n", MitterServer.serverId);
        }
    }

    /**
     * This method starts the server and starts threads that listens for connection from clients, 
     * notifiers and servers.
     */
    public void start() {
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

            System.out.printf("[ SERVER %d ] MitterServer is running.\n", serverId);
            System.out.printf("[ SERVER %d ] Listening to incoming clients on port %d\n",serverId,clientPort);
            System.out.printf("[ SERVER %d ] Listening to incoming notifiers on port %d\n",serverId,notifierPort);
            System.out.printf("[ SERVER %d ] Listening to incoming servers on port %d\n",serverId,serverPort);

            int numOfActiveServers = 0;
            
            // The first time this server runs make sure that it is connected to two servers at minimum.
            while (numOfActiveServers < 2) {
                synchronized (serversList) {
                    numOfActiveServers = serversList.size();
                }
            }

            // Wait for other servers to connect
            sleepFor(1000);

            int leaderId = discoverLeader();
            if (leaderId > -1) {    // A leader already exists
                while(!setLeader(leaderId)) { sleepFor(100); }   // Wait for the leader to establish connection
            } else {
                System.out.printf("[ SERVER %d ] Electing a leader...\n",serverId);
                System.out.printf("[ SERVER %d ] A leader has been elected.\n", serverId);
                while (!electLeader()) { }
            }

            inspectLeader();
            int prevFirstUnchosenIndex = firstUnchosenIndex;

            while (true) {
                if (isLeader) {
                    notificationListLock.lock();    // Obtain the lock for the notification list
                    if (!notificationList.isEmpty()) {
                        // Take one from notification list
                        NotificationInfo notification = notificationList.get(0);
                        // notificationListNotFullCondition.signal();  // Signal waiting notifier thread
                        notificationListLock.unlock();  // Release lock for notification list

                        notificationListLock.lock();    // Obtain the lock for the notification list
                        if (proposer.writeValue(notification)) {  // Propose a value
                            notificationList.remove(0);
                            notificationListCount -= 1;
                        }
                        notificationListNotFullCondition.signal();  // Signal waiting notifier thread
                        notificationListLock.unlock();  // Release lock for notification list
                    } else {
                        notificationListLock.unlock();  // Release lock for notification list
                        proposer.writeValue(null);  // Listen for success request
                    }
                } else {
                    acceptor.readValue();
                    // Send heartbeat message to all replicas except the leader
                    respondToHearbeat();
                }
                
                storeLogEntriesToListContainer();

                if (!log.isEmpty() && prevFirstUnchosenIndex < firstUnchosenIndex) {
                    if (debug) {
                        printLog();
                    }
                    prevFirstUnchosenIndex = firstUnchosenIndex;
                }

                if (currentLeader == null || changeInLeader) {
                    changeInLeader = false;
                    System.out.printf("[ SERVER %d ] Electing a leader...\n",serverId);
                    System.out.printf("[ SERVER %d ] A leader has been elected.\n", serverId);
                    while (!electLeader()) { }
                    inspectLeader();
                    synchronized (numOfNotificationsRelayed) {
                        numOfNotificationsRelayed = 0;      // Reset this variable to initiate re-send of the notifications that has not been replicated
                    }
                }
            }  
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method performs a single round of electing a leader. Returns true if a leader has been elected.
     * @return True if a leader has been elected, false otherwise.
     */
    public boolean electLeader() {
        ServerPeers.ServerIdentity highestId = findHighestServerId(); 
        
        // If this server has the highest serverId and there is no leader elected yet, then send heartbeat 
        // messages to all servers to notify other servers that this server should be the leader
        if (highestId.getId() == serverId) {
            synchronized (serversList) {
                // Send heartbeat message to all servers
                int index = 0;
                while (index < serversList.size()) {
                    ServerPeers.ServerIdentity sId = serversList.get(index);
                    try {
                        sendHeartbeatMessage(sId.getSocket());    
                    } catch (IOException e) {
                        // serversList.remove(sId);
                        // index -= 1;
                    } catch (JAXBException e) {
                        // IGNORE
                    }
                    index += 1;
                }
            }
            
            currentLeader = highestId;
            sleepFor(1000);
            return true;
        } else {
            try {
                // Read the received heartbeat with a 3000ms time limit
                Message hb = readMessage(highestId.getSocket(), 3000);
                // System.out.printf("Listening for heartbeat message from leader(SERVER %d)\n", highestId.getId());
                if (hb != null && hb.getHeartbeat() != null) {
                    if (hb.getHeartbeat().getServerId() == highestId.getId()) {
                        currentLeader = highestId;
                        return true;
                    }
                } else {
                    try {
                        highestId.getSocket().close();    
                    } catch (IOException ex) {
                        // IGNORE
                    }
                    synchronized (MitterServer.serversList) {   // Remove the server from the active servers list
                        try {
                            MitterServer.serversList.remove(highestId);
                            highestId.getSocket().close();
                        } catch (Exception ex) {

                        }
                    }
                }
            } catch (Exception e) {
                // IGNORE
            }
        }
        return false;
    }

    /**
     * Listen and sends heartbeat messages to discover the current leader, if a leader exists.
     * @return - A non-negative integer if a leader exist, -1 if the leader doesn't exist
     */
    public int discoverLeader() {
        int receivedLeaderId = -1;
        int index;
        boolean leaderDiscovered = false;
        ServerPeers.ServerIdentity sId;
        
        while (!leaderDiscovered) {
            synchronized (serversList) {
                index = 0;
                while (index < serversList.size()) {
                    sId = serversList.get(index);
                    try {
                        sendHeartbeatMessage(sId.getSocket());
                    } catch (Exception e) {
                        // IGNORE
                        System.err.println("AN ERROR HAS OCCURED");
                    }
                    index += 1;
                }
                // System.out.println("SENT HEARTBEAT TO REPLICAS TO DISCOVER LEADER");

                index = 0;
                while (index < serversList.size()) {
                    sId = serversList.get(index);
                    try {
                        Message hb = null;
                        // System.out.println("LISTENING TO SERVER " + sId.getId());
                        // while ((hb = readMessage(sId.getSocket())) == null) { }
                        hb = readMessage(sId.getSocket(), 5000);
                        if (hb != null) {
                            if (hb.getHeartbeat() != null && hb.getHeartbeat().getLeaderId() > -1) {
                                // System.out.println("THE LEADER IS SERVER " + hb.getHeartbeat().getLeaderId());
                                receivedLeaderId = hb.getHeartbeat().getLeaderId();
                                // leaderDiscovered = true;
                                // break;
                            }
                            leaderDiscovered = true;
                        } else {
                            disconnect();
                            leaderDiscovered = false;
                            break;
                        }
                    } catch (Exception e) {
                        // IGNORE
                    }
                    index += 1;
                }

                sleepFor(200);  // Wait for the leader to broadcast the active servers and for the replicas to update their active servers
            }
            sleepFor(1000);     // Wait for the servers to establish connection
        }
        return receivedLeaderId;
    }

    public static void respondToHearbeat() {
        synchronized (serversList) {
            int index = 0;
            ServerPeers.ServerIdentity sId;
            while (index < serversList.size()) {
                sId = serversList.get(index);
                if (currentLeader != null) {
                    if (sId.getId() != currentLeader.getId()) {
                        Message hb = null;
                        try {
                            hb = readMessage(sId.getSocket());
                            if (hb != null && hb.getHeartbeat() != null) {
                                sendHeartbeatMessage(sId.getSocket());
                                // System.out.println("SENT HEARTBEAT MESSAGE TO SERVER " + sId.getId());
                            }
                        } catch (IOException e) {
                            System.err.printf(" [ SERVER %d ] Error: %s\n", serverId, e.getMessage());
                        } catch (JAXBException e) {
                            // IGNORE
                            System.err.println("AN ERROR HAS OCCURED");
                        }
                    }
                }
                index += 1;
            }
        }
    }

    /**
     * Disconnect connection to all servers
     */
    public void disconnect() {
        ServerPeers.ServerIdentity sId;
        synchronized (serversList) {
            while (!serversList.isEmpty()) {
                sId = serversList.get(0);
                try {
                    sId.getSocket().close();    
                } catch (IOException ex) {
                    System.err.format("[ SERVER %d ] Error: Proposer, " + ex.getMessage(), MitterServer.serverId);
                    ex.printStackTrace();
                    System.exit(1);
                }
                
                if (!MitterServer.serversList.remove(sId)) {
                    // DO SOMETHING
                }
            }
        }
    }

    /**
     * Sets the currentLeader variable with the found leader id
     * @param  leaderId - The server id of the leader
     * @return          - True if it has successfully found and set the leader id in the list of active servers, false otherwise
     */
    public boolean setLeader(int leaderId) {
        synchronized (serversList) {
            for (ServerPeers.ServerIdentity sId: serversList) {
                if (sId.getId() == leaderId) {
                    currentLeader = sId;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if this server is the leader or not and updates the "isLeader" variable
     */
    public void inspectLeader() {
        if (currentLeader.getId() == serverId) {
            System.out.printf("[ SERVER %d ] Proposer\n", serverId);
            isLeader = true;
            notificationRelayer = null;
        } else {
            System.out.printf("[ SERVER %d ] Acceptor\n", serverId);
            isLeader = false;
            if (notificationRelayer == null) {
                notificationRelayer = new NotificationRelayer();
                notificationRelayer.start();
            }
        }
    }

    /**
     * Stores the log entries/notifications into their corresponding list container(i.e. 
     * caution, notice and urgent lists)
     */
    public void storeLogEntriesToListContainer() {
        LogEntry entry = null;
        try {
            while (nextLogEntryToStore < firstUnchosenIndex) {
                entry = log.get(nextLogEntryToStore);
                assignSequenceNumberAndStore(entry.getAcceptedValue());
                synchronized (numOfNotificationsRelayed) {
                    if (!isLeader && numOfNotificationsRelayed > 0) {
                        notificationListLock.lock();    // Obtain the lock for the notification list
                        if (isEqual(entry.getAcceptedValue(), notificationList.get(0))) {
                            notificationList.remove(0);
                            notificationListCount -= 1;
                            numOfNotificationsRelayed -= 1;
                            notificationListNotFullCondition.signal();  // Signal waiting notifier thread
                        }
                        notificationListLock.unlock();  // Release lock for notification list
                    }
                }
                nextLogEntryToStore += 1;
            }
        } catch (Exception e) {
            System.err.printf("[ Server %d ] Error: %s", serverId, e.getMessage());
        }
    }

    /**
     * This method sends a heartbeat message to a server using the given socket.
     * @param s - A socket on where to send a heartbeat message to
     * @throws JAXBException [description]
     * @throws IOException   [description]
     */
    public static void sendHeartbeatMessage(Socket s) throws JAXBException, IOException {
        BufferedWriter buffWriter = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
        StringWriter sWriter = new StringWriter();
        Message hb = setupHeartbeatMessage();
        jaxbMarshallerMessage.marshal(hb, sWriter);
        buffWriter.write(sWriter.toString());
        buffWriter.newLine();
        buffWriter.flush();
    }

    /**
     * This method reads a message from a given server using the given socket.
     * @param s - A socket on where to listen for a heartbeat message
     * @return - The heartbeat message, null if none is read
     * @throws JAXBException [description]
     * @throws IOException   [description]
     */
    public static Message readMessage(Socket s) throws JAXBException, IOException {
        BufferedReader buffReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        StringReader sReader;
        Message message = null;

        if (buffReader.ready()) {
            String line = buffReader.readLine();
            if (line != null) {
                sReader = new StringReader(line.trim());
                // sReader = new StringReader(line.trim().replaceFirst("^([\\W]+)<","<"));
                message = (Message) jaxbUnmarshallerMessage.unmarshal(sReader);
            }
        }

        return message;
    }

    /**
     * This method reads a message, from a given socket, if it is ready to be read or waits until the given 
     * time period for the message to arrive.
     * @param  s             The socket on where the message to read from
     * @param  waitTime      The time period that this method would wait for the message to arrive
     * @return               The received message or null
     * @throws JAXBException [description]
     * @throws IOException   [description]
     */
    public static Message readMessage(Socket s, long waitTime) throws JAXBException, IOException {
        Message receivedMessage = null;
        long startTime = System.currentTimeMillis();
        long currentTime;
        do {
            Message message = readMessage(s);
            if (message != null) {
                return message;
            }
            currentTime = System.currentTimeMillis();
        } while ((currentTime-startTime) <= waitTime);

        return receivedMessage;
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
     * This method assign a sequence number to a notification and store it into the correct list.
     * @param notification - Notification received from one of the notifiers or server
     */
    public void assignSequenceNumberAndStore(NotificationInfo notification) throws InterruptedException {
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
    public OrderedNotification assignSequenceNumber(NotificationInfo notification, int severity) {
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
        synchronized (writerCount) {    // Tell the reader that a writer is ready to write
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
        Iterator clientIterator = clientsList.iterator();

        while (clientIterator.hasNext()) {  // Loop through all active clients
            ClientThread cThread = (ClientThread) clientIterator.next();
            synchronized (cThread.deletedNotifications) { // Obtain lock for the deleted notifications list
                cThread.deletedNotifications.add(on);
            }
        }

        // System.err.println("Storing deleted notification...SUCCESS");
    }

    /**
     * This method finds the first unchosen index, that is the index in the log where a value has
     * not yet been chosen.
     * @return - The unchosen index
     */
    public static int findFirstUnchosenIndex() {
        // int index = 0;
        int index = firstUnchosenIndex;
        while (index < MitterServer.log.size()) {
            LogEntry entry = MitterServer.log.get(index);
            if (Float.compare(entry.getAcceptedProposal(), Float.MAX_VALUE) < 0) {  // No value has been chosen for this log index
                return index;
            }
            index += 1;
        }
        return index;
    }

    /**
     * This method updates the lastLogIndex variable
     */
    public static void updateLastLogIndex() {
        int index = 0;
        while (index < MitterServer.log.size()) {
            LogEntry entry = MitterServer.log.get(index);
            if (Float.compare(entry.getAcceptedProposal(), Float.MAX_VALUE) >= 0) {  // A value has been chosen for this log index
                lastLogIndex = index;
            }
            index += 1;
        }
    }

    /**
     * This method increases the capacity of the replicated log of this server, without modifying/deleting
     * the existing contents/entries.
     * @param size - The size to which the log will be resized to 
     */
    public static void increaseLogCapacity(int size) {
        if (size <= 0) {
            System.err.println("Negative or zero size not applicable.");
            return;
        }

        while (MitterServer.log.size() < size) {
            MitterServer.log.add(new LogEntry());
        }
    }

    /**
     * Creates a heartbeat message
     * @return - The heartbeat message
     */
    public static Message setupHeartbeatMessage() {
        Message message = new Message();
        new Message();
        message.setServerId(serverId);
        message.setHeartbeat(new Message.Heartbeat());
        message.setAccept(null);
        message.setPrepare(null);
        message.setSuccess(null);
        message.getHeartbeat().setServerId(serverId);
        if (currentLeader != null) {                        // If there is a leader set the leaderId field of the heartbeat message to the leader's id
            message.getHeartbeat().setLeaderId(currentLeader.getId());
        } else {
            message.getHeartbeat().setLeaderId(-1);         // else if there is no leader set the leaderId field to -1
        }
        if (isLeader) {
            String activeServers = String.valueOf(serverId);
            synchronized (serversList) {
                for (ServerPeers.ServerIdentity sId: serversList) {
                    activeServers += new String(" " + sId.getId());
                }
            }
            message.getHeartbeat().setActiveServers(activeServers);
        }
        return message;
    }

    /**
     * Lets the thread wait for about "waitTime" milliseconds
     * @param waitTime - The time to wait in milliseconds
     */
    public static void sleepFor(long waitTime) {
        try {
            TimeUnit.MILLISECONDS.sleep(waitTime);
        } catch (Exception e) {
            // IGNORE
        }
    }

    /**
     * Compares two notification if they are equal or not based on their sender and message id
     * @param  n1 - The first notification
     * @param  n2 - The second notification
     * @return    - True if equal, otherwise false
     */
    public boolean isEqual(NotificationInfo n1, NotificationInfo n2) {
        boolean res = false;

        if (n1.getSender().compareToIgnoreCase(n2.getSender()) == 0 &&
            n1.getMessageId() == n2.getMessageId()) {
            res = true;
        }

        return res;
    }

    /////////////////////////////
    // FOR DEBUGGING PURPOSES  //
    /////////////////////////////
    public void printLog() {
        System.out.println("======================================================");
        System.out.println("Log entries: ");
        int index = 0;
        LogEntry entry = null;
        while (index < firstUnchosenIndex) {
            entry = log.get(index);
            System.out.println("Accepted value: ");
            System.out.println("\tSender: " + entry.getAcceptedValue().getSender());
            System.out.println("\tMessage: " + entry.getAcceptedValue().getMessage());
            index += 1;
        }
    }

    //////////
    // MAIN //
    //////////
    public static void main(String[] args) {
        MitterServer mServer = new MitterServer();
        int cPort = 0, 
            nPort = 0, 
            sPort = 0,
            serverID = 0;

        if (args.length > 2 || args.length < 1) {
            System.err.println("[ INFO ] Usage: MitterServer [server_id]");
            System.exit(1);
        }

        if (args.length == 2) {
            mServer.debug = true;
        }

        try {
            FileReader fReader = new FileReader("config.txt");
            BufferedReader bReader = new BufferedReader(fReader);
            String line;
            String regex = "\\s+";
            while ((line = bReader.readLine()) != null) {
                String[] lineArr = line.trim().split(regex);
                
                if (lineArr[0].charAt(0) == '#') continue; // Ignore comment lines
                
                if (lineArr.length != 5) {
                    System.err.println("[ INFO ] Error: Missing port number or server id or ip address.");
                    System.exit(1);
                }

                serverID = Integer.parseInt(args[0]);
                int configServerId = Integer.parseInt(lineArr[0]);

                if (serverID == configServerId) {
                    cPort = Integer.parseInt(lineArr[1]);
                    nPort = Integer.parseInt(lineArr[2]);
                    sPort = Integer.parseInt(lineArr[3]);
                } else {
                    ServerInfo sInfo = new ServerInfo();
                    sInfo.id = Integer.parseInt(lineArr[0]);                // Server ID
                    sInfo.serverPort = Integer.parseInt(lineArr[3]);        // Server Port
                    sInfo.notifierPort = Integer.parseInt(lineArr[2]);      // Notifier Port
                    sInfo.ipAddress = lineArr[4];                           // IP Address
                    serverInfo.add(sInfo);
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