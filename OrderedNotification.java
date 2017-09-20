package uni.mitter;

import generated.nonstandard.notification.NotificationInfo;

/**
 * This class is a wrapper class that holds the notification and it's sequence number for total ordering
 */
public class OrderedNotification {
    private NotificationInfo n;
    private long seqNum;

    public OrderedNotification() { }

    public void setNotification(NotificationInfo n) {
        this.n = n;
    }

    public void setSequenceNumber(long seq) {
        this.seqNum = seq;
    }

    public NotificationInfo getNotification() {
        return this.n;
    }

    public long getSequenceNumber() {
        return this.seqNum;
    }
}