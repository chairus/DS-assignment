package uni.mitter;

import generated.nonstandard.notification.Notification;

import generated.nonstandard.notification.Notification.Timestamp;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
/* JAVAX */
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.xml.datatype.XMLGregorianCalendar;
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
    public static final int MAX_NUM_READERS = 100;
    public static final int MAX_NOTIFICATIONS_LIST = 1000;
    // Locks for the MitterServer and the Notifier threads(PRODUCER-CONSUMER)
    public static final Lock notificationListLock = new ReentrantLock();
    public static final Condition notificationListNotFullCondition = notificationListLock.newCondition();
    public static final Condition notificationListNotEmptyCondition = notificationListLock.newCondition();
    // Lists that stores all received notifications
    public static List<OrderedNotification> urgentList, cautionList, noticeList;
    public static List<Thread> clientsList; // A list that stores active clients
    private ServerSocket serverSocket;
    private int clientPort;
    private int notifierPort;
    private Writer writer;
    private Notification notification;
    private BufferedWriter buffWriter;
    // A variable that holds if the MitterServer is ready to write or if the MitterServer is currently writing on one of the lists(urgent, caution, notice).
    public static Integer[] writerCount = {0,0,0};  // [urgent, caution, notice]
    // Semaphores that synchronizes the readers and writers. This semaphore allows 100 readers to read at the same time.
    public static List<Semaphore> readWriteSemaphores;    // [urgent, caution, notice]
    // public static Semaphore urgentListReadWriteSemaphore;
    // public static Semaphore cautionListReadWriteSemaphore;
    // public static Semaphore noticeListReadWriteSemaphore;
    public static List<Notification> notificationList;
    public Thread notifierListenerThread;
    public Thread clientListenerThread;
    public static long notificationListCount;
    private long[] totalOrderSequenceNumbers = {1,1,1}; // [urgent, caution, notice]

    /**
     * Constructor
     */
    public MitterServer(int clientPort, int notifierPort) {
        this.clientPort = clientPort;
        this.notifierPort = notifierPort;
        urgentList = new ArrayList<>();
        cautionList = new ArrayList<>();
        noticeList = new ArrayList<>();
        clientsList = new ArrayList<>();
        // writerCount = 0;
        notificationListCount = 0;
        // urgentListReadWriteSemaphore = new Semaphore(MAX_NUM_READERS, true);  // max 100 readers for urgent notifications
        // cautionListReadWriteSemaphore = new Semaphore(MAX_NUM_READERS, true);  // max 100 readers for caution notifications
        // noticeListReadWriteSemaphore = new Semaphore(MAX_NUM_READERS, true);  // max 100 readers for notice notifications
        readWriteSemaphores = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            readWriteSemaphores.add(new Semaphore(MAX_NUM_READERS, true));
        }
        notificationList = new ArrayList<>();
    }

    /**
     * This method starts the server listens for connection from clients and notifiers.
     */
    public void start() {
        boolean sent = false;

        try {
            // Open up port for clients to connect
            serverSocket = new ServerSocket(clientPort);
            // serverSocket.setSoTimeout(30000); // block for no more than 30 seconds
            
            // Create and start a notifier listener thread to open a port and listen for incoming notifier connections
            notifierListenerThread = new NotifierListener(notifierPort);
            notifierListenerThread.start();

            clientListenerThread = new ClientListener(serverSocket);
            clientListenerThread.start();

            System.out.println("MitterServer is running...");

            while (true) {
                notificationListLock.lock();    // Obtain the lock for the notification list
                if (!notificationList.isEmpty()) {
                    System.err.println("Notification list is not empty. Taking one out...");
                    Notification notification = takeFromNotificationList();
                    assignSequenceNumberAndStore(notification);
                    System.err.println("Notification list is not empty. Taking one out...SUCCESS");
                } else {
                    notificationListLock.unlock();  // Release lock for notification list
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Take notifications from the notification list and stores it into one of the lists.
     * This method does not wait if the notification list is empty.
     */
    public Notification takeFromNotificationList() throws InterruptedException {
        Notification notification = null;
        System.err.println("Taking notification from the list...");
        // notificationListLock.lock();    // Obtain the lock for the notification list

        if (notificationListCount != 0) { 
            notification = notificationList.get(0);
            notificationList.remove(0);
            notificationListCount -= 1;
        }

        notificationListNotFullCondition.signal();  // Signal waiting notifier thread
        notificationListLock.unlock();  // Release lock
        System.err.println("Taking notification from the list...SUCCESS");
        return notification;
    }

    /**
     * This method assign a sequence number to a notification and store it into the correct list.
     * @param notification - Notification received from one of the notifiers
     */
    public void assignSequenceNumberAndStore(Notification notification) throws InterruptedException {
        System.err.println("Assigning sequence number on a notification...");
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
        System.err.println("Assigning sequence number on a notification...SUCCESS");
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
        System.err.println("Putting ordered notification into the appropriate list...");
        synchronized (writerCount) {    // Tell the reader that the server is ready to write
            writerCount[listNumber] += 1;
        }

        readWriteSemaphores.get(listNumber).acquire(MAX_NUM_READERS);   // Obtain lock
        switch (listNumber) {
            case 0:
                urgentList.add(orderedNotification);
                break;
            case 1:
                cautionList.add(orderedNotification);
                break;
            case 2:
                noticeList.add(orderedNotification);
                break;
            default:
                break;
        }

        synchronized (writerCount) {    // Tell the readers that the server has finished writing
            writerCount[listNumber] -= 1;
        }

        readWriteSemaphores.get(listNumber).release(MAX_NUM_READERS);  // Release lock
        System.err.println("Putting ordered notification into the appropriate list...SUCCESS");
    }

    /**
     * Main
     */
    public static void main(String[] args) {
        MitterServer mServer = new MitterServer(3000,3001);
        mServer.start();
    }
}