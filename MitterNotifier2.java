package uni.mitter;

import generated.nonstandard.notification.Notification;
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

public class MitterNotifier2 {
    public static void main(String[] args) throws Exception {
        Socket socket;

        try {
            socket = new Socket("localhost", 3001);
            OutputStream out = socket.getOutputStream();

            Writer writer = new OutputStreamWriter(out, "UTF-8");
            BufferedWriter buffWriter = new BufferedWriter(writer);
            
            JAXBContext jaxbContext = JAXBContext.newInstance(Notification.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            StringWriter dataWriter = new StringWriter();
            
            try {
                // Create object notification
                Notification notification = new Notification();
                System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Central_Hub",
                                                  "Central Hub, Room 402",
                                                  "Room currently unavailable. Cleaning in progress.",
                                                  "caution",
                                                  0);
                /* marshalling of java objects in xml (send to sever) */
                jaxbMarshaller.marshal(notification, dataWriter);
                buffWriter = new BufferedWriter(writer);
                buffWriter.write(dataWriter.toString());
                buffWriter.newLine();
                buffWriter.flush();

                TimeUnit.MILLISECONDS.sleep(1000);

                System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Physics_Bld",
                                                  "Physics Building, Room 112",
                                                  "Room currently unavailable. Experiment gone wild.",
                                                  "caution",
                                                  1);

                dataWriter = new StringWriter();
                /* marshalling of java objects in xml (send to sever) */
                jaxbMarshaller.marshal(notification, dataWriter);
                buffWriter = new BufferedWriter(writer);
                buffWriter.write(dataWriter.toString());
                buffWriter.newLine();
                buffWriter.flush();

                TimeUnit.MILLISECONDS.sleep(5000);

                System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Engineering_Bld",
                                                  "Engineering South Building, Room 321",
                                                  "Room currently unavailable. Robot gone wild.",
                                                  "urgent",
                                                  2);

                dataWriter = new StringWriter();
                /* marshalling of java objects in xml (send to sever) */
                jaxbMarshaller.marshal(notification, dataWriter);
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

    public static Notification createNotification(String sender,
                                                  String location,
                                                  String message,
                                                  String severity,
                                                  long messageId) throws DatatypeConfigurationException {
        Notification n = new Notification();
        
        n.setSender(sender);
        n.setLocation(location);
        n.setMessage(message);
        Notification.Timestamp timestamp = new Notification.Timestamp();
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