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

public class MitterNotifier6 extends Notifier {
    public static void main(String[] args) throws Exception {
        try {
            init("127.0.0.1", 3004);
            System.out.println("Connected to SERVER 1");
            
            try {
                // Create object notification
                NotificationInfo notification = new NotificationInfo();
                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Image_And_Copy_Centre",
                                                  "Hughes Building Level 1",
                                                  "Close on weekends",
                                                  "notice",
                                                  0);
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(1000);

                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Image_And_Copy_Centre",
                                                  "Hughes Building Level 1",
                                                  "analysis contagious bye bawling crisp profile grip filthy goldbricker granularity",
                                                  "caution",
                                                  1);

                dataWriter = new StringWriter();
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(1000);

                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Image_And_Copy_Centre",
                                                  "Hughes Building Level 1",
                                                  "brute snake bullwhip cranberry archive attitude aerodynamic cannon dial anything",
                                                  "caution",
                                                  2);

                dataWriter = new StringWriter();
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(1000);

                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Fire_Detector_1",
                                                  "103 Damascus drive",
                                                  "Fire and there a lot of smoke.",
                                                  "urgent",
                                                  3);

                dataWriter = new StringWriter();
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(1000);

                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Fire_Detector_2",
                                                  "Level 1, Building 1",
                                                  "False alarm",
                                                  "urgent",
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