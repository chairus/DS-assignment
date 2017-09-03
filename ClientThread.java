package uni.mitter;

import generated.nonstandard.notification.Notification;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.math.BigInteger;
import java.net.Socket;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.StringWriter;
import generated.nonstandard.subscription.Subscription;
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
    private Socket socket;
    public ConcurrentFilteredNotification notificationsToBeSent;
    public long[] sequenceNumbers = {0,0,0};    // Holds the current sequence number of all three notifications(urgent, caution and notice)

    public ClientThread(Socket socket) {
        this.socket = socket;
        notificationsToBeSent = new ConcurrentFilteredNotification();
    }

    /**
     * This method
     */
    public void run() {


            // // Get stream for reading subscriptions
            // InputStream in = socket.getInputStream();
            // Reader reader = new InputStreamReader(in, "UTF-8");
            // BufferedReader buffReader = new BufferedReader(reader);
            
            // // Get stream for writing notifications
            // OutputStream out = socket.getOutputStream();
            // Writer writer = new OutputStreamWriter(out, "UTF-8");
            // BufferedWriter buffWriter = new BufferedWriter(writer);

            // // Initialize both marshaller and unmarshaller(subscription)
            // JAXBContext jaxbContextSubs = JAXBContext.newInstance(Subscription.class);
            // Unmarshaller jaxbUnmarshallerSubs = jaxbContextSubs.createUnmarshaller();
            // Marshaller jaxbMarshallerSubs = jaxbContextSubs.createMarshaller();

            // // Read and unmarshall client subscription
            // System.out.println("Reading subscription from client...");
            // StringReader dataReader = new StringReader(buffReader.readLine());
            // Subscription subs = (Subscription) jaxbUnmarshallerSubs.unmarshal(dataReader);
        
    }
}