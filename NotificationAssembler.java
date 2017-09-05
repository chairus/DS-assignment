package uni.mitter;

import generated.nonstandard.notification.Notification;
import generated.nonstandard.subscription.Subscription;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.TimerTask;

/**
 * This class reads notifications maintained in the server and adds notifications to the list
 * of notifications to be sent to the client.
 * @author cyrusvillacampa
 */
public class NotificationAssembler extends TimerTask {
    private final int URGENT = 0;
    private final int CAUTION = 1;
    private final int NOTICE = 2;
    private long timer;
    private Socket clientSocket;
    private ClientThread clientThread;
    /* Holds the current sequence number of all three notifications(urgent, caution and notice) */
    public List<Long> notificationSequenceNumbers;  /* [urgent, caution, notice] */
    /* Holds the notifications that will be sent to the client */
    public FilteredNotificationList notificationsToBeSent;
    /* Holds the notifications that has been deleted by the server. The notifications stored in 
       here could possibly be sent to the client and if they are they will be sent in the order
       they where added to the list. */
    public List<OrderedNotification> deletedNotifications;
    public Subscription currentSub;
    public boolean newConnection;

    public NotificationAssembler(FilteredNotificationList notificationsToBeSent, 
                                 Socket clientSocket,
                                 List<OrderedNotification> deletedNotifications,
                                 ClientThread clientThread,
                                 boolean newConnection) {
        this.notificationsToBeSent = notificationsToBeSent;
        this.deletedNotifications = deletedNotifications;
        this.clientSocket = clientSocket;
        this.clientThread = clientThread;
        this.notificationSequenceNumbers = Arrays.asList(Long.parseLong("0"),Long.parseLong("0"),Long.parseLong("0"));
        this.timer = 0;
        this.newConnection = newConnection;
    }

    public void setFilter(Filter f) {
        notificationsToBeSent.setFilter(f);
    }

    /**
     * This method goes through the notification list maintained by the server and adds notifications
     * into the list that will be sent to the client. It first checks the urgent list every 10ms and 
     * if 1 min. has passed it will check the urgent and then the caution list in that order, and when
     * 30 mins. have passed it will check the urgent, caution and then notice list in that order. This
     * guarantees the order of the notifications sent to client to be 'urgent then caution then notice'.
     */
    public void run() {

        // Check urgent notification first
        addDeletedNotifications("urgent", notificationSequenceNumbers.get(URGENT));
        int count;
        do {    // Check if there are writers that are ready or already writing
            synchronized (MitterServer.writerCount) {
                count = MitterServer.writerCount[URGENT].intValue();
            }
        } while (count > 0);
        
        try {
            MitterServer.readWriteSemaphores.get(URGENT).acquire();    // Acquire a Semaphore for urgent list
        } catch (InterruptedException e) {
            //TODO: handle exception
        }

        for (OrderedNotification on: MitterServer.urgentList) { // Perform read operation on the list
            long seqNum = on.getSequenceNumber();

            if (seqNum > notificationSequenceNumbers.get(URGENT)) {
                boolean hasAdded = notificationsToBeSent.add(on);
                if (hasAdded) { // Update the sequence number for "urgent" notifications
                    notificationSequenceNumbers.set(URGENT, seqNum);
                }
            }
        }
        
        MitterServer.readWriteSemaphores.get(URGENT).release();    // Release a Semaphore for urgent list

        timer += 1;
        // Check caution notification second
        addDeletedNotifications("caution", notificationSequenceNumbers.get(CAUTION));
        if (timer % 1000 == 0 || newConnection) {    // 10 seconds has passed(CHANGE THIS TO 1 min. or 6000)
            do {    // Check if there are writers that are ready or already writing
                synchronized (MitterServer.writerCount) {
                    count = MitterServer.writerCount[CAUTION].intValue();
                }
            } while (count > 0);

            try {
                MitterServer.readWriteSemaphores.get(CAUTION).acquire();   // Acquire a Semaphore for caution list
            } catch (InterruptedException e) {
                System.err.println("Interrupted Thread.");
            }

            for (OrderedNotification on: MitterServer.cautionList) {    // Perform read operation on the list
                long seqNum = on.getSequenceNumber();
                if (seqNum > notificationSequenceNumbers.get(CAUTION)) {
                    boolean hasAdded = notificationsToBeSent.add(on);
                    if (hasAdded) { // Update the sequence number for "caution" notifications
                        notificationSequenceNumbers.set(CAUTION,seqNum);
                    }
                }
            }

            MitterServer.readWriteSemaphores.get(CAUTION).release();   // Release a Semaphore for caution list
        }

        // Check notice notification third
        addDeletedNotifications("notice", notificationSequenceNumbers.get(NOTICE));
        if (timer % 2000 == 0) {    // 20 seconds has passed(CHANGE THIS TO 30 mins. or 180000)
            do {    // Check if there are writers that are ready or already writing
                synchronized (MitterServer.writerCount) {
                    count = MitterServer.writerCount[NOTICE].intValue();
                }
            } while (count > 0);
            
            try {
                MitterServer.readWriteSemaphores.get(NOTICE).acquire();    // Acquire a Semaphore for notice list
            } catch (InterruptedException e) {
                System.err.println("Interrupted Thread.");
            }

            for (OrderedNotification on: MitterServer.noticeList) { // Perform read operation on the list
                long seqNum = on.getSequenceNumber();
                if (seqNum > notificationSequenceNumbers.get(NOTICE)) {
                    boolean hasAdded = notificationsToBeSent.add(on);
                    if (hasAdded) { // Update the sequence number for "notice" notifications
                        notificationSequenceNumbers.set(NOTICE, seqNum);
                    }
                }
            }

            MitterServer.readWriteSemaphores.get(NOTICE).release();    // Release a Semaphore for notice list
        }

        if (newConnection) {    // Update new client connection to old client connection
            newConnection = !newConnection;
        }

        // Send notifications to clients
        if (!notificationsToBeSent.isEmpty()) {
            Sender sender = new Sender(notificationsToBeSent, clientSocket, clientThread, this);
            sender.send();
        }
    }

    /**
     * This method returns true if a given ordered notification has a larger sequence number than the
     * last previously sent notification to the client. 
     */
    private boolean isLargerSeqNum(OrderedNotification on, Long currentSeqNum) {
        long seqNum = on.getSequenceNumber();   // Get the sequence number of the notification
        if (seqNum > currentSeqNum.longValue()) {
            return true;
        }

        return false;
    }

    /**
     * This method checks the severity type of the given ordered notification. This method returns true
     * if the ordered notification has the same severity type as the given severity parameter.
     */
    private boolean isOfSeverityType(OrderedNotification on, String severityType) {
        String severity = on.getNotification().getSeverity();   // Get the severity of the notification
        if (severity.compareToIgnoreCase(severityType) == 0) {
            return true;
        }
        return false;
    }

    /**
     * This method adds the deleted notifications to the list of notifications to be sent.
     */
    private void addDeletedNotifications(String severityType, Long currentSeqNum) {
        // Check for deleted urgent notifications and add it onto the list of notifications to be sent if necessary
        synchronized (deletedNotifications) {
            if (!deletedNotifications.isEmpty()) {
                int i = 0;
                OrderedNotification on;
                while (i < deletedNotifications.size()) {
                    on = deletedNotifications.get(i);
                    if (isOfSeverityType(on, severityType)) {
                        if (isLargerSeqNum(on, on.getSequenceNumber())) {
                            boolean hasAdded = notificationsToBeSent.add(on);
                            if (hasAdded) { // If it is the notification that the client has subscribed to.
                                switch (severityType.toLowerCase()) {   // Update current sequence number
                                    case "urgent":
                                        notificationSequenceNumbers.set(URGENT, on.getSequenceNumber());                                 
                                        break;
                                    case "caution":
                                        notificationSequenceNumbers.set(CAUTION, on.getSequenceNumber());                                 
                                        break;
                                    case "notice":
                                        notificationSequenceNumbers.set(NOTICE, on.getSequenceNumber());                                 
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                        deletedNotifications.remove(i);
                        i -= 1;
                    }
                    i += 1;
                }
            }
        }
    }
}