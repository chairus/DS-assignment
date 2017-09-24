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

public class MitterNotifier {
    public static void main(String[] args) throws Exception {
        Socket socket;

        try {
            socket = new Socket("localhost", 3013);
            OutputStream out = socket.getOutputStream();

            Writer writer = new OutputStreamWriter(out, "UTF-8");
            BufferedWriter buffWriter = new BufferedWriter(writer);
            
            JAXBContext jaxbContext = JAXBContext.newInstance(NotificationInfo.class);
            ObjectFactory objectFactory = new ObjectFactory();
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            StringWriter dataWriter = new StringWriter();
            
            try {
                // Create object notification
                NotificationInfo notification = new NotificationInfo();
                notification = createNotification("IW_building",
                                                  "Ingkarni Wardli Building",
                                                  "Elevator Maintenance",
                                                  "caution",
                                                  0);
                System.out.println("Sending marshalled notification to the server...");
                
                /* marshalling of java objects in xml (send to sever) */
                JAXBElement<NotificationInfo> notificationInfo = objectFactory.createNotification(notification);
                jaxbMarshaller.marshal(notificationInfo, dataWriter);
                buffWriter = new BufferedWriter(writer);
                buffWriter.write(dataWriter.toString());
                buffWriter.newLine();
                buffWriter.flush();

                TimeUnit.MILLISECONDS.sleep(2000);

                System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("BSS_library",
                                                  "Barr Smith South Library, Room 301",
                                                  "Room currently unavailable. Asbestos contamination.",
                                                  "urgent",
                                                  0);

                dataWriter = new StringWriter();
                /* marshalling of java objects in xml (send to sever) */
                notificationInfo = objectFactory.createNotification(notification);
                jaxbMarshaller.marshal(notificationInfo, dataWriter);
                buffWriter = new BufferedWriter(writer);
                buffWriter.write(dataWriter.toString());
                buffWriter.newLine();
                buffWriter.flush();

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