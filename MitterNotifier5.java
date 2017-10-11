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

public class MitterNotifier5 extends Notifier {
    public static void main(String[] args) throws Exception {
        try {
            init("127.0.0.1", 3010);
            System.out.println("Connected to SERVER 3");
            
            try {
                // Create object notification
                NotificationInfo notification = new NotificationInfo();
                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Ask_Adelaide",
                                                  "Hub Central Level 3",
                                                  "Close on weekends",
                                                  "notice",
                                                  0);
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(1000);

                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Ask_Adelaide",
                                                  "Hub Central Level 3",
                                                  "Bacon ipsum dolor amet venison kevin porchetta beef ribs sirloin ball tip meatloaf corned beef landjaeger turkey beef drumstick short ribs hamburger tongue.",
                                                  "notice",
                                                  1);

                dataWriter = new StringWriter();
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(1000);

                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Ask_Adelaide",
                                                  "Hub Central Level 3",
                                                  "Pancetta kielbasa sausage ham hock boudin chuck bresaola salami shank ham beef strip steak. ",
                                                  "caution",
                                                  2);

                dataWriter = new StringWriter();
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(1000);

                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Adelaide_University_Union",
                                                  "Level 4, Union House",
                                                  "Free pizza",
                                                  "notice",
                                                  3);

                dataWriter = new StringWriter();
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(1000);

                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Adelaide_University_Union",
                                                  "Level 4, Union House",
                                                  "Tenderloin prosciutto short ribs biltong beef swine. Corned beef andouille strip steak, pork loin sausage hamburger ball tip pig tongue spare ribs drumstick.",
                                                  "notice",
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