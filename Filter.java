package uni.mitter;

import generated.nonstandard.notification.NotificationInfo;
import generated.nonstandard.subscription.Subscription;

/**
 * This class filters out the notifications using the given subscription.
 * @author cyrusvillacampa
 */
public class Filter {
    private Subscription subs;

    public Filter() {
        
    }

    public void setSubscription(Subscription s) {
        this.subs = s;
    }

    /**
     * This method checks if a given notification is what the client has subscribed to.
     * @param n - The notification to be checked
     * @return boolean - True if a given notification satisfies the subscription
     */
    public boolean filterNotification(NotificationInfo n) {
        boolean isSubscribedTo = false;

        // Has subscribed to all notifications
        if (subs.getSender().compareToIgnoreCase("all") == 0 &&
                subs.getLocation().compareToIgnoreCase("all") == 0) {
            return !isSubscribedTo;
        }

        // Has subscribed to all the sender in a particular location
        if (subs.getSender().compareToIgnoreCase("all") == 0 &&
                subs.getLocation().compareToIgnoreCase(n.getLocation()) == 0) {
            return !isSubscribedTo;
        }

        // Has subscribed to a particular sender in all location
        if (subs.getSender().compareToIgnoreCase(n.getSender()) == 0 &&
                subs.getLocation().compareToIgnoreCase("all") == 0) {
            return !isSubscribedTo;
        }

        // Has subscribed to a particular sender and location
        if (subs.getSender().compareToIgnoreCase(n.getSender()) == 0 && 
                subs.getLocation().compareToIgnoreCase(n.getLocation()) == 0) {
            return !isSubscribedTo;
        }

        return isSubscribedTo;
    }
}