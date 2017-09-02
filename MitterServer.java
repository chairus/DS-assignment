package uni.mitter;

import generated.nonstandard.notification.*;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

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
    private long timer;
    private Writer writer;
    private Notification notification;
    private BufferedWriter buffWriter;

    /**
     * Constructor
     */
    public MitterServer(int clientPort, int notifierPort) {
        this.clientPort = clientPort;
        this.notifierPort = notifierPort;
        timer = 0;
    }

    /**
     * This method starts the server listens for connection from clients and notifiers.
     */
    public void start() {
        boolean sent = false;

        try {
            // Notification notification = new Notification();
            notification = new Notification();
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

            Socket client = server.accept();
            
            OutputStream out = client.getOutputStream();
            // Writer writer = new OutputStreamWriter(out, "UTF-8");
            writer = new OutputStreamWriter(out, "UTF-8");

            Timer t = new Timer();
            Ticker ticker = new Ticker();
            t.scheduleAtFixedRate(ticker,0,1000);

            System.out.println("Marshalling notification...");
            /* init jaxb marshaller */
            JAXBContext jaxbContext = JAXBContext.newInstance(Notification.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            StringWriter dataWriter = new StringWriter();
            /* set this flag to true to format the output */
            // jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            System.out.println("Sending marshalled notification to the client...");
            /* marshalling of java objects in xml (send to client) */
            jaxbMarshaller.marshal(notification, dataWriter);
            buffWriter = new BufferedWriter(writer);
            buffWriter.write(dataWriter.toString());
            // writer.flush();
            buffWriter.newLine();
            buffWriter.flush();

            // System.out.println("Entering while loop with timer value " + timer);
            
            while (true) {
                if (timer == 10) {
                    System.out.println("Timer is " + timer);
                }

                if (timer % 20 == 0) {
                    // System.out.println("20 seconds has passed!");
                    // if (!sent) {
                    //     System.out.println("Marshalling notification...");
                    //     /* init jaxb marshaller */
                    //     JAXBContext jaxbContext = JAXBContext.newInstance(Notification.class);
                    //     Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            
                    //     /* set this flag to true to format the output */
                    //     jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                    //     System.out.println("Storing marshalled notification into \'notification.xml\' file \n and sending to client...");
                    //     /* marshalling of java objects in xml (send to client) */
                    //     jaxbMarshaller.marshal(notification, new File("notification.xml"));
                    //     jaxbMarshaller.marshal(notification, writer);
                    //     writer.flush();

                    //     sent = true;
                    // }
                } else {
                    sent = false;
                    // System.out.println("Timer: " + timer);
                }
                
                // client.close();
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

    public class Ticker extends TimerTask {
        boolean sent;

        public Ticker() {
            sent = false;
        }

        public void run() {
            timer += 1;
            System.out.println("Timer: " + timer);

            if (timer % 10 == 0) {
                try {
                    if (!sent) {
                        System.out.println("Marshalling notification...");
                        /* init jaxb marshaller */
                        JAXBContext jaxbContext = JAXBContext.newInstance(Notification.class);
                        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
                        StringWriter dataWriter = new StringWriter();
                        /* set this flag to true to format the output */
                        // jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                        System.out.println("Sending marshalled notification to the client...");
                        /* marshalling of java objects in xml (send to client) */
                        jaxbMarshaller.marshal(notification, dataWriter);
                        // buffWriter = new BufferedWriter(writer);
                        buffWriter.write(dataWriter.toString());
                        // writer.flush();
                        buffWriter.newLine();
                        buffWriter.flush();

                        sent = true;
                    }
                } catch (Exception e) {
                
                }
            } else {
                sent = false;
            }
        }
    }

    public static void main(String[] args) {
        MitterServer mServer = new MitterServer(3000,3001);
        mServer.start();
    }    
}