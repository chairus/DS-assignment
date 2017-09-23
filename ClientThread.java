package uni.mitter;

import generated.nonstandard.notification.NotificationInfo;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.Socket;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.StringWriter;
import generated.nonstandard.subscription.Subscription;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBContext;


/**
 * This class manages a single client and at the same time creates a TimerTask, that handles
 * preparing the notifications and sending them to the client. Furthermore this class monitors
 * the state of the connection of the client.
 * @author cyrusvillacampa
 */
public class ClientThread extends Thread {
    private Socket clientSocket;
    public FilteredNotificationList notificationsToBeSent;
    public List<OrderedNotification> deletedNotifications;
    public Filter filter;
    public BufferedReader buffReader;
    public StringReader dataReader;
    private JAXBContext jaxbContextSubs;
    private Unmarshaller jaxbUnmarshallerSubs;
    private Subscription subs;
    private NotificationAssembler na;
    private long currentTime;
    public boolean clientStateConnectivity;
    public Boolean postponeNotificationTransmission;

    public ClientThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.notificationsToBeSent = new FilteredNotificationList();
        this.deletedNotifications = new ArrayList<>();
        this.filter = new Filter();
        this.currentTime = System.currentTimeMillis();
        this.clientStateConnectivity = true;
        this.postponeNotificationTransmission = false;
    }

    /**
     * This method reads XML subscription sent by the client and sets the filter with it. It also creates
     * a task that will be executed in a 10ms interval. Furthermore this method also monitors the state
     * of the connection of the client, that is it checks if an update of subscription is initiated by the
     * client.
     */
    public void run() {
        try {
            // Get stream for reading subscriptions
            InputStream in = clientSocket.getInputStream();
            InputStreamReader reader = new InputStreamReader(in, "UTF-8");
            // BufferedReader buffReader = new BufferedReader(reader);
            buffReader = new BufferedReader(reader);

            // Initialize unmarshaller(subscription)
            jaxbContextSubs = JAXBContext.newInstance(Subscription.class);
            jaxbUnmarshallerSubs = jaxbContextSubs.createUnmarshaller();

            Timer t = new Timer();
            na = new NotificationAssembler(notificationsToBeSent, 
                                           clientSocket,
                                           deletedNotifications,
                                           this,
                                           true);

            updateSubscription();

            // System.out.println("Starting notification assembler...");
            // Execute the "run" method in the NotificationAssembler every 10 milliseconds
            t.scheduleAtFixedRate(na, 0, 10);
            // System.out.println("Starting notification assembler...SUCCESS");

            while (true) {
                try {
                    if (buffReader.ready()) {   // Client wants to update it's subscription
                        synchronized (postponeNotificationTransmission) {
                            postponeNotificationTransmission = true;
                        }

                        updateSubscription();

                        synchronized (postponeNotificationTransmission) {
                            postponeNotificationTransmission = false;
                        }
                    }

                    if (!clientStateConnectivity) {
                        System.err.println("Stopping Thread due to connection lost...");
                        break;
                    }
                } catch (IOException e) {
                    System.err.println("Something happend to the input reader in ClientThread");
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method updates the subscription of the client by setting the filter associated to the
     * client's thread.
     */
    private void updateSubscription() throws JAXBException, IOException {
        // Read and unmarshall client subscription
        // System.out.println("Reading subscription from client...");
        dataReader = new StringReader(buffReader.readLine());
        subs = (Subscription) jaxbUnmarshallerSubs.unmarshal(dataReader);

        /* ======== FOR DEBUGGING PURPOSES ======== */
        // System.out.println("Received client subscription...printing");
        // System.out.println("Sender subscription: " + subs.getSender());
        // System.out.println("Location subscription: " + subs.getLocation());

        // System.out.println("Setting filter...");
        // Set the filter
        filter.setSubscription(subs);
        na.setFilter(filter);
        // System.out.println("Setting filter...SUCCESS");
    }
}