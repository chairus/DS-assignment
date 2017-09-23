package uni.mitter;

import generated.nonstandard.notification.NotificationInfo;

import generated.nonstandard.subscription.Subscription;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.Writer;
import java.io.StringWriter;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.net.Socket;

/* JAVAX */
import java.util.ArrayList;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;
 
public class MitterClientTest3 {
    public static void main(String[] args) throws Exception {
        Socket socket;
        List<String> receivedNotification = new ArrayList<>();

        try {
            // Create object subscription
            Subscription subscription = new Subscription();
            subscription.setLocation("all");
            subscription.setSender("all");


            socket = new Socket("localhost", 3000);
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            Writer writer = new OutputStreamWriter(out, "UTF-8");
            BufferedWriter buffWriter = new BufferedWriter(writer);
            
            JAXBContext jaxbContextSub = JAXBContext.newInstance(Subscription.class);
            Marshaller jaxbMarshaller = jaxbContextSub.createMarshaller();
            StringWriter dataWriter = new StringWriter();
            
            System.out.println("===================================================");
            System.out.println("Subscibing to all notifications...");
            System.out.println("Sending marshalled subscription to the server...");
            System.out.println("===================================================");
            /* marshalling of java objects in xml (send to sever) */
            jaxbMarshaller.marshal(subscription, dataWriter);
            buffWriter = new BufferedWriter(writer);
            buffWriter.write(dataWriter.toString());
            buffWriter.newLine();
            buffWriter.flush();
            
            InputStreamReader reader = new InputStreamReader(in, "UTF-8");
            BufferedReader buffReader = new BufferedReader(reader);

            // JAXBContext jaxbContext = JAXBContext.newInstance(NotificationInfo.class);
            JAXBContext jaxbContext = JAXBContext.newInstance("generated.nonstandard.notification");
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            StringReader dataReader = null;

            while (true) {
                try {
                    if (buffReader.ready()) {
                        System.out.print("Trying to read XML data...");
                        receivedNotification.add(buffReader.readLine());
                        System.out.println("SUCCESS");
                    } else {
                        if (!receivedNotification.isEmpty()) {
                            dataReader = new StringReader(receivedNotification.get(0));
                            receivedNotification.remove(0);
                            System.out.println("Unmarshalling read XML data...");
                            JAXBElement<NotificationInfo> notificationInfo = (JAXBElement<NotificationInfo>) jaxbUnmarshaller.unmarshal(dataReader);
                            NotificationInfo notification = notificationInfo.getValue();
                            System.out.println("===================================================");
                            System.out.println("Received notification!!!");
                            System.out.println("Sender: " + notification.getSender());
                            System.out.println("Location: " + notification.getLocation());
                            System.out.println("Message: " + notification.getMessage());
                            System.out.println("Severity: " + notification.getSeverity());
                            System.out.println("Update: " + notification.isUpdate());
                            System.out.println("Timestamp: " + 
                                                notification.getTimestamp().getDate() + 
                                                " " + notification.getTimestamp().getTime());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}