package uni.mitter;

import generated.nonstandard.notification.*;

/**
 * This class is a wrapper class that holds the notification and it's sequence number for total ordering
 */
public class OrderedNotification {
    private Notification n;
    private long seqNum;

    public OrderedNotification() { }

    public void setNotification(Notification n) {
        this.n = n;
    }

    public void setSequenceNumber(long seq) {
        this.seqNum = seq;
    }

    public Notification getNotification() {
        return this.n;
    }

    public long getSequenceNumber() {
        return this.seqNum;
    }
}