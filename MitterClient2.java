package uni.mitter;

import generated.nonstandard.notification.*;

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
import java.util.concurrent.TimeUnit;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;

// import generated.nonstandard.notification.Notification;
 
public class MitterClient2 {
    public static void main(String[] args) throws Exception {
        Socket socket;
        List<String> receivedNotification = new ArrayList<>();
        int updateSubscription = 1;

        try {
            // Create object subscription
            Subscription subscription = new Subscription();
            subscription.setLocation("all");
            subscription.setSender("bss_library");


            socket = new Socket("localhost", 3006);
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            Writer writer = new OutputStreamWriter(out, "UTF-8");
            BufferedWriter buffWriter = new BufferedWriter(writer);
            
            JAXBContext jaxbContextSub = JAXBContext.newInstance(Subscription.class);
            Marshaller jaxbMarshaller = jaxbContextSub.createMarshaller();
            StringWriter dataWriter = new StringWriter();
            
            System.out.println("Sending marshalled subscription to the server...");
            /* marshalling of java objects in xml (send to sever) */
            jaxbMarshaller.marshal(subscription, dataWriter);
            buffWriter = new BufferedWriter(writer);
            buffWriter.write(dataWriter.toString());
            buffWriter.newLine();
            buffWriter.flush();
            
            InputStreamReader reader = new InputStreamReader(in, "UTF-8");
            BufferedReader buffReader = new BufferedReader(reader);

            JAXBContext jaxbContext = JAXBContext.newInstance(Notification.class);
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
                            Notification notification = (Notification) jaxbUnmarshaller.unmarshal(dataReader);
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
                    
                    // Update subscription
                    if (updateSubscription == 1) {
                        TimeUnit.MILLISECONDS.sleep(4000);
                        subscription.setSender("all");

                        dataWriter = new StringWriter();
                        
                        System.out.println("Sending marshalled subscription to the server...");
                        /* marshalling of java objects in xml (send to sever) */
                        jaxbMarshaller.marshal(subscription, dataWriter);
                        buffWriter = new BufferedWriter(writer);
                        buffWriter.write(dataWriter.toString());
                        buffWriter.newLine();
                        buffWriter.flush();

                        updateSubscription = 0;
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