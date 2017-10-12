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

public class MitterNotifier11 extends Notifier {
    public static void main(String[] args) throws Exception {
        try {
            init("127.0.0.1", 3004);
            System.out.println("Connected to SERVER 1");
            
            try {
                // Create object notification
                NotificationInfo notification = new NotificationInfo();
                notification = createNotification("Lecturer_1",
                                                  "Settlement Rd, Swan Marsh, VIC 3249",
                                                  "mutagen abrasive stealthy freedom afterworld clay delicious sun excitement carrot",
                                                  "caution",
                                                  0);
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(1000);

                notification = createNotification("Lecturer_1",
                                                  "Settlement Rd, Swan Marsh, VIC 3249",
                                                  "ambulatory disk herd compound laser heart home puppet spell demonstration",
                                                  "urgent",
                                                  1);

                dataWriter = new StringWriter();
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(1000);

                notification = createNotification("Lecturer_2",
                                                  "946 Golconda Rd, Lebrina, TAS 7254",
                                                  "Class in cancelled",
                                                  "urgent",
                                                  0);

                dataWriter = new StringWriter();
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(1000);

                notification = createNotification("Lecturer_2",
                                                  "946 Golconda Rd, Lebrina, TAS 7254",
                                                  "Class in progress",
                                                  "notice",
                                                  1);

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