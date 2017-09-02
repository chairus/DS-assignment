package uni.mitter;

import generated.nonstandard.notification.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.GregorianCalendar;

/* JAVAX */
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;

// import generated.nonstandard.notification.Notification;
 
public class MitterClient {
    public static void main(String[] args) throws Exception {
        Socket socket;
        try {
            socket = new Socket("localhost", 3000);
            InputStream in = socket.getInputStream();
            InputStreamReader reader = new InputStreamReader(in, "UTF-8");

            System.out.println("Unmarshalling xml from connection stream");
            System.out.println("Printing it out...");
            JAXBContext jaxbContext = JAXBContext.newInstance(Notification.class);
            

            /* init jaxb unmarshaller */
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Notification notification = (Notification) jaxbUnmarshaller.unmarshal(reader);
            System.out.println("Sender: " + notification.getSender());
            System.out.println("Location: " + notification.getLocation());
            System.out.println("Message: " + notification.getMessage());
            System.out.println("Severity: " + notification.getSeverity());
            System.out.println("Update: " + notification.isUpdate());
            System.out.println("Timestamp: " + notification.getTimestamp().getDate() + " " + notification.getTimestamp().getTime());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // if (socket != null) {
            //     try {
            //         socket.close();
            //     } catch (IOException e) {
            //         // ignore
            //     }
            // }
        }
    }
}