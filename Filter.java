package uni.mitter;

import generated.nonstandard.notification.Notification;
import generated.nonstandard.subscription.Subscription;

public class Filter {
    private Subscription subs;

    public Filter() {
        
    }

    public void setSubscription(Subscription s) {
        this.subs = s;
    }

    /**
     * This method returns true if a given notification satisfies the subscription.
     */
    public boolean filterNotification(Notification n) {
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