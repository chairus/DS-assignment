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

public class MitterNotifier4 extends Notifier {
    public static void main(String[] args) throws Exception {
        try {
            init("127.0.0.1", 3013);
            System.out.println("Connected to SERVER 4");
            
            try {
                // Create object notification
                NotificationInfo notification = new NotificationInfo();
                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("PowerRangerRed",
                                                  "Gold Gym",
                                                  "Gym Time",
                                                  "notice",
                                                  0);
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(1000);

                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("PowerRangerRed",
                                                  "Krispy Kreme",
                                                  "Currently fighting monsters.",
                                                  "urgent",
                                                  1);

                dataWriter = new StringWriter();
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(1000);

                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("PowerRangerRed",
                                                  "Secret base",
                                                  "It morphin time!!",
                                                  "caution",
                                                  2);

                dataWriter = new StringWriter();
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(1000);

                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Leonardo",
                                                  "Secret base",
                                                  "Eating pizza.",
                                                  "notice",
                                                  3);

                dataWriter = new StringWriter();
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(1000);

                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Raphael",
                                                  "Sewers",
                                                  "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc eleifend auctor augue, ac lacinia quam feugiat at. Aenean sit amet quam venenatis, finibus nulla id, finibus nisi. Curabitur sagittis metus ac lorem posuere finibus. Suspendisse potenti. Vestibulum eget enim mattis, sodales lacus at, malesuada enim. Phasellus nec justo lacinia nibh hendrerit accumsan sit amet ac justo. Sed venenatis libero sit amet ullamcorper porta. Sed vitae congue lacus.",
                                                  "caution",
                                                  4);

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