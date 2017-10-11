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
 
public class Client {
    static Socket socket;
    static List<String> receivedNotification;
    static Writer writer;
    static BufferedWriter buffWriter;
    static JAXBContext jaxbContextSub;
    static Marshaller jaxbMarshaller;
    static StringWriter dataWriter;
    static InputStreamReader reader;
    static BufferedReader buffReader;
    static JAXBContext jaxbContext;
    static Unmarshaller jaxbUnmarshaller;
    static StringReader dataReader;
    static Constants constants = new Constants();

    public static void init(String ipAddress, int port) throws Exception {
        receivedNotification = new ArrayList<>();
        socket = new Socket(ipAddress, port);
        // For sending subscription
        writer = new OutputStreamWriter(socket.getOutputStream(), constants.encoding);
        buffWriter = new BufferedWriter(writer);
        jaxbContextSub = JAXBContext.newInstance(Subscription.class);
        jaxbMarshaller = jaxbContextSub.createMarshaller();
        dataWriter = new StringWriter();
        // For receiving notifications
        reader = new InputStreamReader(socket.getInputStream(), constants.encoding);
        buffReader = new BufferedReader(reader);
        jaxbContext = JAXBContext.newInstance("generated.nonstandard.notification");
        jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        dataReader = null;
    }

    public static Subscription createSubscription(String sender, String location) {
        // Create object subscription
        Subscription subscription = new Subscription();
        subscription.setLocation(location);
        subscription.setSender(sender);
        return subscription;
    }

    public static void sendSubscription(Subscription subscription) throws Exception {
        /* marshalling of java objects in xml (send to sever) */
        jaxbMarshaller.marshal(subscription, dataWriter);
        buffWriter = new BufferedWriter(writer);
        buffWriter.write(dataWriter.toString());
        buffWriter.newLine();
        buffWriter.flush();
    }

    public static void printNotification(NotificationInfo notification) {
        System.out.println("===================================================");
        System.out.println("Received notification");
        System.out.println("Sender: " + notification.getSender());
        System.out.println("Location: " + notification.getLocation());
        System.out.println("Message: " + notification.getMessage());
        System.out.println("Severity: " + notification.getSeverity());
        System.out.println("Update: " + notification.isUpdate());
        System.out.println("Timestamp: " + 
                            notification.getTimestamp().getDate() + 
                            " " + notification.getTimestamp().getTime());
    }

    public static NotificationInfo readNotification() throws Exception {
        dataReader = new StringReader(receivedNotification.get(0));
        receivedNotification.remove(0);
        System.out.println("Unmarshalling read XML data...");
        JAXBElement<NotificationInfo> notificationInfo = (JAXBElement<NotificationInfo>) jaxbUnmarshaller.unmarshal(dataReader);
        NotificationInfo notification = notificationInfo.getValue();
        return notification;
    }
}