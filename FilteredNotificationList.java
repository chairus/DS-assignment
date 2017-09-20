package uni.mitter;

import generated.nonstandard.notification.NotificationInfo;
import generated.nonstandard.subscription.Subscription;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class holds a list of notifications to be sent. The added notifications should satisfy the
 * subscriptions of client, that is notifications will be filtered before adding to the list.
 * @author cyrusvillacampa
 */
public class FilteredNotificationList {
    private List<OrderedNotification> notificationsToBeSent;
    private Filter filter;

    public FilteredNotificationList() {
        this.notificationsToBeSent = new ArrayList<>();
    }
    
    /**
     * Sets the filter
     */
    public void setFilter(Filter f) {
        this.filter = f;
    }

    /**
     * This method returns the head of the list
     */
    public OrderedNotification popHead() {
        OrderedNotification notification;

        notification = notificationsToBeSent.get(0);    // Grab the first element in the list
        notificationsToBeSent.remove(0);
        return notification;
    }

    /**
     * This method returns true if the list is empty
     */
    public boolean isEmpty() {
        return notificationsToBeSent.isEmpty();
    }

    /**
     * This method adds a notification into the list if the filter returns true
     */
    public boolean add(OrderedNotification on) {
        NotificationInfo n = on.getNotification();

        if (filter.filterNotification(n)) {
            notificationsToBeSent.add(on);
            return true;
        }

        return false;
    }

    /**
     * This method checks if a particular notification is already in the list and replaces 
     * it with the new notification. If it's not in the list then add this method adds it.
     * This method is useful for notification update.
     */
    public void addNotificationUpdate(OrderedNotification on) {
        if (!filter.filterNotification(on.getNotification())) {
            return;
        }

        boolean hasReplaced = false;
        for (OrderedNotification notification: notificationsToBeSent) {
            if (isTheSame(on.getNotification(), notification.getNotification())) {
                notificationsToBeSent.set(notificationsToBeSent.indexOf(notification), on);
                hasReplaced = true;
            }
        }

        if (!hasReplaced) { // If the notification has not been added into the list
            notificationsToBeSent.add(on);
        }
    }

    /**
     * This method returns true if both notification has the same sender and messageId
     */
    public boolean isTheSame(NotificationInfo n1, NotificationInfo n2) {
        if (n1.getSender().compareToIgnoreCase(n2.getSender()) == 0 &&
                n1.getMessageId() == n2.getMessageId()) {
            return true;
        }

        return false;
    }

    /**
     * FOR DEBUGGING PURPOSES
     */
    public int size() {
        return notificationsToBeSent.size();
    }
}