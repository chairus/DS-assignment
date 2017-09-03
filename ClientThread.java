package uni.mitter;

import generated.nonstandard.notification.Notification;
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
import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBContext;


/**
 * This class manages a single client and at the same time creates two TimerTask, that handles
 * sending of caution and notice notification. Furthermore this class handles sending of urgent
 * notifications.
 * @author cyrusvillacampa
 */
public class ClientThread extends Thread {
    private Socket clientSocket;
    public ConcurrentFilteredNotification notificationsToBeSent;
    public Filter filter;

    public ClientThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
        notificationsToBeSent = new ConcurrentFilteredNotification();
        this.filter = new Filter();
    }

    /**
     * This method
     */
    public void run() {
        try {
            // Get stream for reading subscriptions
            InputStream in = clientSocket.getInputStream();
            Reader reader = new InputStreamReader(in, "UTF-8");
            BufferedReader buffReader = new BufferedReader(reader);

            // Initialize unmarshaller(subscription)
            JAXBContext jaxbContextSubs = JAXBContext.newInstance(Subscription.class);
            Unmarshaller jaxbUnmarshallerSubs = jaxbContextSubs.createUnmarshaller();

            // Read and unmarshall client subscription
            System.out.println("Reading subscription from client...");
            StringReader dataReader = new StringReader(buffReader.readLine());
            Subscription subs = (Subscription) jaxbUnmarshallerSubs.unmarshal(dataReader);

            /* ======== FOR DEBUGGING PURPOSES ======== */
            System.out.println("Received client subscription...printing");
            System.out.println("Sender subscription: " + subs.getSender());
            System.out.println("Location subscription: " + subs.getLocation());

            Timer t = new Timer();
            NotificationAssembler na = new NotificationAssembler(notificationsToBeSent, clientSocket, this);

            System.out.print("Setting filter...");
            // Set the filter
            filter.setSubscription(subs);
            na.setFilter(filter);
            System.out.println("SUCCESS");

            System.out.print("Starting notification assembler...");
            // Execute the task in the NotificationAssembler every 10ms
            t.scheduleAtFixedRate(na, 0, 10);
            System.out.println("SUCCESS");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}