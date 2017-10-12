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

public class MitterNotifier7 extends Notifier {
    public static void main(String[] args) throws Exception {
        try {
            init("127.0.0.1", 3007);
            System.out.println("Connected to SERVER 2");
            
            try {
                // Create object notification
                NotificationInfo notification = new NotificationInfo();
                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Bonython_Hall",
                                                  "Adelaide city centre",
                                                  "Close on weekends",
                                                  "notice",
                                                  0);
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(1000);

                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Bonython_Hall",
                                                  "Adelaide city centre",
                                                  "cagey history short need taste cautious gather certain crash ossified curve stimulating temper smiling activity wipe maid kindhearted soap cats",
                                                  "caution",
                                                  1);

                dataWriter = new StringWriter();
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(3000);

                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Bonython_Hall",
                                                  "Adelaide city centre",
                                                  "collar abject bushes search tenuous space draconian peck spill bleach division lunchroom nut cheat soggy boorish repair muddled lumpy obedient goofy knowing ruddy lonely excuse noiseless earn paint tick tremendous keen trick",
                                                  "caution",
                                                  2);

                dataWriter = new StringWriter();
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(3000);

                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Embedded_System_1",
                                                  "Ingkarni Wardli Elevator 1",
                                                  "Elevator Maintenance on progress.",
                                                  "notice",
                                                  3);

                dataWriter = new StringWriter();
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(3000);

                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Embedded_System_1",
                                                  "Ingkarni Wardli Elevator 1",
                                                  "Elevator Maintenance done.",
                                                  "notice",
                                                  4);

                dataWriter = new StringWriter();
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(3000);

                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Embedded_System_2",
                                                  "Level 2, Master Building",
                                                  "Water pipe leak. Be careful of slippery floor",
                                                  "caution",
                                                  5);

                dataWriter = new StringWriter();
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(3000);

                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Embedded_System_2",
                                                  "Level 2, Master Building",
                                                  "gushing mortal artist featherweight hacksaw blister hirsute hostage arrival wish",
                                                  "caution",
                                                  6);

                dataWriter = new StringWriter();
                sendNotification(notification, buffWriter);

            } catch (Exception e) {
                e.printStackTrace();
            }
                

            while (true) {
                // Run forever
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}