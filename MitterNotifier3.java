package uni.mitter;

import generated.nonstandard.notification.NotificationInfo;
import generated.nonstandard.notification.ObjectFactory;
import java.net.Socket;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.bind.JAXBElement;

public class MitterNotifier3 extends Notifier {
    public static void main(String[] args) {
        try {
            init("localhost", 3001);
            System.out.println("Connected to SERVER 0");
            // Create object notification
            NotificationInfo notification = new NotificationInfo();
            System.out.println("Sending marshalled notification to the server...");
            notification = createNotification("The_Band",
                                              "Adelaide Entertainment Centre",
                                              "Room currently unavailable. Concert in progress.",
                                              "urgent",
                                              0);
            sendNotification(notification, buffWriter);

            TimeUnit.MILLISECONDS.sleep(1000);

            // System.out.println("Sending marshalled notification to the server...");
            notification = createNotification("The_Band",
                                              "Sydney Opera",
                                              "Room currently unavailable. Concert in progress",
                                              "urgent",
                                              1);

            dataWriter = new StringWriter();
            sendNotification(notification, buffWriter);

            TimeUnit.MILLISECONDS.sleep(500);

            // System.out.println("Sending marshalled notification to the server...");
            notification = createNotification("The_Band",
                                              "Intercontinental Adelaide",
                                              "Room currently occupied.",
                                              "notice",
                                              2);

            dataWriter = new StringWriter();
            sendNotification(notification, buffWriter);

            TimeUnit.MILLISECONDS.sleep(1000);

            // System.out.println("Sending marshalled notification to the server...");
            notification = createNotification("The_Band",
                                              "Adelaide Entertainment Centre",
                                              "Rock and Rolling.",
                                              "caution",
                                              3);

            dataWriter = new StringWriter();
            sendNotification(notification, buffWriter);
            while (true) {
                // Run forever
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}