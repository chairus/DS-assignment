package uni.mitter;

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
    public FilteredNotificationList deletedNotifications;
    public Subscription currentSub;

    public NotificationAssembler(FilteredNotificationList notificationsToBeSent, 
                                 Socket clientSocket,
                                 FilteredNotificationList deletedNotifications,
                                 ClientThread clientThread) {
        this.notificationsToBeSent = notificationsToBeSent;
        this.deletedNotifications = deletedNotifications;
        this.clientSocket = clientSocket;
        this.clientThread = clientThread;
        this.notificationSequenceNumbers = Arrays.asList(Long.parseLong("0"),Long.parseLong("0"),Long.parseLong("0"));
        this.timer = 0;
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
        /* !!!!!!!!!! NOTE: PROVIDE SYNCHRONIZATION LATER !!!!!!!!! */

        // Check urgent notification first
        for (OrderedNotification on: MitterServer.urgentList) {
            long seqNum = on.getSequenceNumber();
            if (seqNum > notificationSequenceNumbers.get(URGENT)) {
                boolean hasAdded = notificationsToBeSent.add(on);
                if (hasAdded) { // Update the sequence number for "urgent" notifications
                    notificationSequenceNumbers.set(URGENT, seqNum);
                    // System.out.println("Added urgent notification");
                }
            }
        }
        
        timer += 1;
        // Check caution notification second
        if (timer % 1000 == 0) {    // 10 seconds has passed(CHANGE THIS TO 1 min. or 6000)
            for (OrderedNotification on: MitterServer.cautionList) {
                long seqNum = on.getSequenceNumber();
                if (seqNum > notificationSequenceNumbers.get(CAUTION)) {
                    boolean hasAdded = notificationsToBeSent.add(on);
                    if (hasAdded) { // Update the sequence number for "caution" notifications
                        notificationSequenceNumbers.set(CAUTION,seqNum);
                        // System.out.println("Added caution notification");
                    }
                }
            }
        }

        // Check notice notification third
        if (timer % 2000 == 0) {    // 20 seconds has passed(CHANGE THIS TO 30 mins. or 180000)
            for (OrderedNotification on: MitterServer.noticeList) {
                long seqNum = on.getSequenceNumber();
                if (seqNum > notificationSequenceNumbers.get(NOTICE)) {
                    boolean hasAdded = notificationsToBeSent.add(on);
                    if (hasAdded) { // Update the sequence number for "notice" notifications
                        notificationSequenceNumbers.set(NOTICE, seqNum);
                        // System.out.println("Added notice notification");
                    }
                }
            } 
        }

        // Start a thread that sends notifications to clients
        if (!notificationsToBeSent.isEmpty()) {
            // System.out.print("Starting sender thread...");
            // Thread sender = new Sender(notificationsToBeSent, clientSocket, clientThread, this);
            // sender.start();
            // System.out.println("SUCCESS");
            Sender sender = new Sender(notificationsToBeSent, clientSocket, clientThread, this);
            sender.send();
        }
    }
}