package uni.mitter;

import generated.nonstandard.notification.*;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;

/* JAVAX */
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;

public class MitterServer {
    private ServerSocket server;
    private int clientPort;
    private int notifierPort;

    /**
     * Constructor
     */
    public MitterServer(int clientPort, int notifierPort) {
        this.clientPort = clientPort;
        this.notifierPort = notifierPort; 
    }

    /**
     * This method starts the server listens for connection from clients and notifiers.
     */
    public void start() {
        try {
            Notification notification = new Notification();
            notification.setSender("IW_building");
            notification.setLocation("Ingkarni Wardli Building");
            notification.setMessage("Elevator maintenance");
            Notification.Timestamp timestamp = new Notification.Timestamp();
            GregorianCalendar gc = new GregorianCalendar();
            XMLGregorianCalendar xmlGC = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
            timestamp.setDate(xmlGC);
            timestamp.setTime(xmlGC);
            notification.setTimestamp(timestamp);
            notification.setSeverity("caution");
            notification.setUpdate(false);

            // Open up port for clients to connect
            server = new ServerSocket(clientPort);

            while (true) {
                try {
                    Socket client = server.accept();
                    
                    OutputStream out = client.getOutputStream();
                    Writer writer = new OutputStreamWriter(out, "UTF-8");
        
                    System.out.println("Marshalling notification...");
                    /* init jaxb marshaller */
                    JAXBContext jaxbContext = JAXBContext.newInstance(Notification.class);
                    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        
                    /* set this flag to true to format the output */
                    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                    System.out.println("Storing marshalled notification into \'notification.xml\' file \n and sending to client...");
                    /* marshalling of java objects in xml (send to client) */
                    jaxbMarshaller.marshal(notification, writer);
                    writer.flush();
                    client.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            

        }
    }

    /**
     * This method open's up two ports for clients and notifiers.
     */
    public void init() {

    }

    public static void main(String[] args) {
        MitterServer mServer = new MitterServer(3000,3001);
        mServer.start();
    }    
}