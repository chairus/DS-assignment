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
import java.util.List;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/* JAVAX */
import java.util.concurrent.TimeUnit;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;

public class MitterServer {
    public static List<OrderedNotification> urgentList, cautionList, noticeList;
    private ServerSocket serverSocket;
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
        urgentList = new ArrayList<>();
        cautionList = new ArrayList<>();
        noticeList = new ArrayList<>();
        timer = 0;
        init();
    }

    public void init() {
        OrderedNotification on;
        try {
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
            notification.setMessageId(0);
    
            on = new OrderedNotification();
            on.setSequenceNumber(1);
            on.setNotification(notification); 
    
            System.out.print("Adding notification to urgent list...");
            urgentList.add(on);
            System.out.println("SUCCESS");
            System.out.println("Size of urgent list is " + urgentList.size());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    /**
     * This method starts the server listens for connection from clients and notifiers.
     */
    public void start() {
        boolean sent = false;

        try {
            // Notification notification = new Notification();
            // notification = new Notification();
            // notification.setSender("IW_building");
            // notification.setLocation("Ingkarni Wardli Building");
            // notification.setMessage("Elevator maintenance");
            // Notification.Timestamp timestamp = new Notification.Timestamp();
            // GregorianCalendar gc = new GregorianCalendar();
            // XMLGregorianCalendar xmlGC = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
            // timestamp.setDate(xmlGC);
            // timestamp.setTime(xmlGC);
            // notification.setTimestamp(timestamp);
            // notification.setSeverity("caution");
            // notification.setUpdate(false);

            // Open up port for clients to connect
            serverSocket = new ServerSocket(clientPort);
            // serverSocket.setSoTimeout(30000); // block for no more than 30 seconds
            // Socket client = server.accept();
            
            // OutputStream out = client.getOutputStream();
            // // Writer writer = new OutputStreamWriter(out, "UTF-8");
            // writer = new OutputStreamWriter(out, "UTF-8");

            // Timer t = new Timer();
            // Ticker ticker = new Ticker(client);
            // t.scheduleAtFixedRate(ticker,0,1000);

            // System.out.println("Marshalling notification...");
            // /* init jaxb marshaller */
            // JAXBContext jaxbContext = JAXBContext.newInstance(Notification.class);
            // Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            // StringWriter dataWriter = new StringWriter();
            // /* set this flag to true to format the output */
            // // jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            // System.out.println("Sending marshalled notification to the client...");
            // /* marshalling of java objects in xml (send to client) */
            // jaxbMarshaller.marshal(notification, dataWriter);
            // buffWriter = new BufferedWriter(writer);
            // buffWriter.write(dataWriter.toString());
            // // writer.flush();
            // buffWriter.newLine();
            // buffWriter.flush();

            Thread tClient = new ClientListener(serverSocket);
            tClient.start();

            System.out.println("MitterServer is running...");
            while (true) {

            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class Ticker extends TimerTask {
        boolean sent;
        Socket clientSocket;

        public Ticker(Socket sock) {
            sent = false;
            this.clientSocket = sock;
        }

        public void run() {
            timer += 1;
            System.out.println("Timer: " + timer);

            if (timer % 10 == 0) {
                // sendNow = true;

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
                        buffWriter.write(dataWriter.toString());
                        // writer.flush();
                        buffWriter.newLine();
                        buffWriter.flush();

                        sent = true;
                    }
                } catch (JAXBException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("Connection lost.");
                    this.cancel();
                    System.out.println("Closing socket...");
                    try {
                        clientSocket.close();    
                    } catch (Exception ex) {
                        //TODO: handle exception
                    }
                    System.out.println("Exiting...");
                    System.exit(0);
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