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
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

public class MitterNotifier10 extends Notifier {
    public static void main(String[] args) throws Exception {
        try {
            init("127.0.0.1", 3007);
            System.out.println("Connected to SERVER 2");
            
            try {
                // Create object notification
                NotificationInfo notification = new NotificationInfo();
                notification = createNotification("Embedded_System_22",
                                                  "Level 1 Hartley Building",
                                                  "Concert in progress",
                                                  "caution",
                                                  0);
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(1000);

                notification = createNotification("Embedded_System_22",
                                                  "Ground Floor Mawson Building",
                                                  "Room currently unavailable. Asbestos contamination.",
                                                  "urgent",
                                                  1);

                dataWriter = new StringWriter();
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(1000);

                notification = createNotification("Embedded_System_22",
                                                  "Elder Hall",
                                                  "Class in progress",
                                                  "notice",
                                                  2);

                dataWriter = new StringWriter();
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(1000);

                notification = createNotification("Embedded_System_22",
                                                  "Elder Hall",
                                                  "Class in progress",
                                                  "notice",
                                                  3);

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