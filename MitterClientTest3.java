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
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;
 
public class MitterClientTest3 extends Client {
    public static void main(String[] args) throws Exception {
        try {
            init("localhost", 3009);
            System.out.println("Connected to SERVER 3");
            Subscription subscription = createSubscription("all", "all");
            
            System.out.println("===================================================");
            System.out.println("Subscibing to all notifications...");
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