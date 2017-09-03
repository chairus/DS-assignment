package uni.mitter;

import generated.nonstandard.subscription.Subscription;
import java.net.Socket;
import java.util.TimerTask;

/**
 * This class reads notifications maintained in the server and adds notifications to the list
 * of notifications to be sent to the client.
 */
public class NotificationAssembler extends TimerTask {
    private final int URGENT = 0;
    private final int CAUTION = 1;
    private final int NOTICE = 2;
    private long timer;
    private Socket clientSocket;
    private ClientThread clientThread;
    // Holds the current sequence number of all three notifications(urgent, caution and notice)
    public long[] notificationSequenceNumbers = {0,0,0}; /* [urgent, caution, notice] */
    public ConcurrentFilteredNotification notificationsToBeSent;
    public Subscription currentSub;

    public NotificationAssembler(ConcurrentFilteredNotification notificationsToBeSent, 
                                 Socket clientSocket, 
                                 ClientThread clientThread) {
        this.notificationsToBeSent = notificationsToBeSent;
        this.clientSocket = clientSocket;
        this.clientThread = clientThread;
        this.timer = 0;
    }

    public void setFilter(Filter f) {
        notificationsToBeSent.setFilter(f);
    }

    public void run() {
        /* !!!!!!!!!! NOTE: PROVIDE SYNCHRONIZATION LATER !!!!!!!!! */

        // System.out.println("Checking the urgent list...");
        // Check urgent notification first
        for (OrderedNotification on: MitterServer.urgentList) {
            long seqNum = on.getSequenceNumber();
            if (seqNum > notificationSequenceNumbers[URGENT]) {
                boolean hasAdded = notificationsToBeSent.add(on);
                if (hasAdded) {
                    notificationSequenceNumbers[URGENT] = seqNum;
                    // System.out.println("Added urgent notification");
                }
            }
        }
        
        timer += 1;
        // System.out.println("Checking the caution list...");
        // Check caution notification second
        if (timer % 1000 == 0) {    // 10 seconds has passed
            for (OrderedNotification on: MitterServer.cautionList) {
                long seqNum = on.getSequenceNumber();
                if (seqNum > notificationSequenceNumbers[CAUTION]) {
                    boolean hasAdded = notificationsToBeSent.add(on);
                    if (hasAdded) {
                        notificationSequenceNumbers[CAUTION] = seqNum;
                        // System.out.println("Added caution notification");
                    }
                }
            }
        }

        // System.out.println("Checking the notice list...");
        // Check notice notification third
        if (timer % 2000 == 0) {
            for (OrderedNotification on: MitterServer.noticeList) {
                long seqNum = on.getSequenceNumber();
                if (seqNum > notificationSequenceNumbers[NOTICE]) {
                    boolean hasAdded = notificationsToBeSent.add(on);
                    if (hasAdded) {
                        notificationSequenceNumbers[NOTICE] = seqNum;
                        // System.out.println("Added notice notification");
                    }
                }
            } 
        }


        // System.out.println("Size of notificationsToBeSent list is : " + notificationsToBeSent.size());
        // Start a thread that sends notifications to clients
        if (!notificationsToBeSent.isEmpty()) {
            System.out.print("Starting sender thread...");
            Thread sender = new Sender(notificationsToBeSent, clientSocket, clientThread, this);
            sender.start();
            System.out.println("SUCCESS");
        }
    }
}