package uni.mitter;

import generated.nonstandard.notification.NotificationInfo;

import generated.nonstandard.subscription.Subscription;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.Writer;
import java.io.StringWriter;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.net.Socket;

/* JAVAX */
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;

 
public class MitterClientTest2 extends Client {
    public static void main(String[] args) throws Exception {
        try {
            init("127.0.0.1", 3000);
            System.out.println("Connected to SERVER 0");
            // Create object subscription
            Subscription subscription = createSubscription("The_Band", "all");
            
            System.out.println("===================================================");
            System.out.println("Subscribing to notifications with sender \"The_Band\"...");
            System.out.println("Sending marshalled subscription to the server...");
            System.out.println("===================================================");
            sendSubscription(subscription);

            while (true) {
                try {
                    if (buffReader.ready()) {
                        // System.out.print("Trying to read XML data...");
                        receivedNotification.add(buffReader.readLine());
                        // System.out.println("SUCCESS");
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