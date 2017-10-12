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

public class MitterNotifier8 extends Notifier {
    public static void main(String[] args) throws Exception {
        try {
            init("127.0.0.1", 3010);
            System.out.println("Connected to SERVER 3");
            
            try {
                // Create object notification
                NotificationInfo notification = new NotificationInfo();
                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Benham_Lecture_Theatre",
                                                  "Benham Building G",
                                                  "Class in progress. Please be quiet.",
                                                  "notice",
                                                  0);
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(1000);

                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Benham_Lecture_Theatre",
                                                  "Benham Building G",
                                                  "Exam in progress. Please be quiet.",
                                                  "notice",
                                                  1);

                dataWriter = new StringWriter();
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(3000);

                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Benham_Lecture_Theatre",
                                                  "Benham Building G",
                                                  "arms day greatest ambush filament lock ethical sparrow assassination complete",
                                                  "notice",
                                                  2);

                dataWriter = new StringWriter();
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(3000);

                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Embedded_System_3",
                                                  "Nexus10 Tower",
                                                  "echo rare nuclear islamism gradient flaming obsession adventure glitter ego",
                                                  "caution",
                                                  3);

                dataWriter = new StringWriter();
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(3000);

                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Embedded_System_3",
                                                  "Nexus10 Tower",
                                                  "licker error dismemberment weirdo passion someone electron nomadic ten sparkler",
                                                  "urgent",
                                                  4);

                dataWriter = new StringWriter();
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(3000);

                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Embedded_System_3",
                                                  "Nexus10 Tower",
                                                  "selfish curfew drown evolution divorce vixen empowerment fresh plant fall",
                                                  "notice",
                                                  5);

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