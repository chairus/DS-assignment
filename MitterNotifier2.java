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

public class MitterNotifier2 {
    static Socket socket;
    static OutputStream out;
    static Writer writer;
    static BufferedWriter buffWriter;
    static JAXBContext jaxbContext;
    static Marshaller jaxbMarshaller;
    static ObjectFactory objectFactory;
    static StringWriter dataWriter;

    public static void main(String[] args) throws Exception {
        try {
            init();
            
            try {
                // Create object notification
                NotificationInfo notification = new NotificationInfo();
                System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Central_Hub",
                                                  "Central Hub, Room 402",
                                                  "Room currently unavailable. Cleaning in progress.",
                                                  "caution",
                                                  0);
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(1000);

                System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Physics_Bld",
                                                  "Physics Building, Room 112",
                                                  "Room currently unavailable. Experiment gone wild.",
                                                  "caution",
                                                  1);

                dataWriter = new StringWriter();
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(4000);

                System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Engineering_Bld",
                                                  "Engineering South Building, Room 321",
                                                  "Room currently unavailable. Robot gone wild.",
                                                  "urgent",
                                                  2);

                dataWriter = new StringWriter();
                sendNotification(notification, buffWriter);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (true) {
            // Run forever
        }
    }

    public static void init() throws Exception {
        socket = new Socket("localhost", 3004);
        out = socket.getOutputStream();

        writer = new OutputStreamWriter(out, "UTF-8");
        buffWriter = new BufferedWriter(writer);
        
        jaxbContext = JAXBContext.newInstance(NotificationInfo.class);
        jaxbMarshaller = jaxbContext.createMarshaller();
        objectFactory = new ObjectFactory();
        dataWriter = new StringWriter();
    }

    public static void sendNotification(NotificationInfo notification, Writer writer) throws Exception {
        BufferedWriter buffWriter = (BufferedWriter)writer;
        /* marshalling of java objects in xml (send to sever) */
        JAXBElement<NotificationInfo> notificationInfo = objectFactory.createNotification(notification);
        jaxbMarshaller.marshal(notificationInfo, dataWriter);
        buffWriter = new BufferedWriter(writer);
        buffWriter.write(dataWriter.toString());
        buffWriter.newLine();
        buffWriter.flush();
    }

    public static NotificationInfo createNotification(String sender,
                                                  String location,
                                                  String message,
                                                  String severity,
                                                  long messageId) throws DatatypeConfigurationException {
        NotificationInfo n = new NotificationInfo();
        
        n.setSender(sender);
        n.setLocation(location);
        n.setMessage(message);
        NotificationInfo.Timestamp timestamp = new NotificationInfo.Timestamp();
        GregorianCalendar gc = new GregorianCalendar();
        XMLGregorianCalendar xmlGC = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
        timestamp.setDate(xmlGC);
        timestamp.setTime(xmlGC);
        n.setTimestamp(timestamp);
        n.setSeverity(severity);
        n.setMessageId(messageId);

        return n;
    }
}