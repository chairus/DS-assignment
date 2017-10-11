package uni.mitter;

import generated.nonstandard.notification.NotificationInfo;
import generated.nonstandard.subscription.Subscription;
 
public class MitterClientTest1 extends Client {
    public static void main(String[] args) throws Exception {
        try {
            init("localhost", 3006);
            Subscription subscription = createSubscription("all", "all");
            
            System.out.println("===================================================");
            System.out.println("Subscibing to all notifications...");
            System.out.println("Sending marshalled subscription to the server...");
            System.out.println("===================================================");
            sendSubscription(subscription);

            while (true) {
                try {
                    if (buffReader.ready()) {
                        System.out.print("Trying to read XML data...");
                        receivedNotification.add(buffReader.readLine());
                        System.out.println("SUCCESS");
                    } else {
                        if (!receivedNotification.isEmpty()) {
                            NotificationInfo notification = readNotification();
                            printNotification(notification);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}