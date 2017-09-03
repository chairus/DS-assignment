package uni.mitter;

import generated.nonstandard.notification.*;
import java.net.Socket;
import java.io.Writer;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

public class Sender extends Thread {
    private ConcurrentFilteredNotification queue;
    private Socket clientSocket;
    private ClientThread clientThread;
    private NotificationAssembler notificationAssembler;

    public Sender(ConcurrentFilteredNotification queue, 
                  Socket clientSocket, 
                  ClientThread clientThread,
                  NotificationAssembler notificationAssembler) {
        this.queue = queue;
        this.clientSocket = clientSocket;
        this.clientThread = clientThread;
        this.notificationAssembler = notificationAssembler;
    }

    public void run() {
        while (!queue.isEmpty()) {
            Notification notification = queue.popHead().getNotification();

            try {
                // Get stream for writing notifications
                OutputStream out = clientSocket.getOutputStream();
                Writer writer = new OutputStreamWriter(out, "UTF-8");
                BufferedWriter buffWriter = new BufferedWriter(writer);
                
                JAXBContext jaxbContext = JAXBContext.newInstance(Notification.class);
                Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
                StringWriter dataWriter = new StringWriter();
                
                System.out.println("Sending marshalled notification to the client...");
                /* marshalling of java objects in xml (send to client) */
                jaxbMarshaller.marshal(notification, dataWriter);
                buffWriter = new BufferedWriter(writer);
                buffWriter.write(dataWriter.toString());
                buffWriter.newLine();
                buffWriter.flush();
            } catch (Exception e) {
                System.out.println("Connection lost.");
                synchronized (ClientListener.clientsList) {
                    System.out.println("Closing client socket...");
                    try {
                        clientSocket.close();
                    } catch (Exception ex) {
                        
                    }
                    System.out.println("Stopping all threads related to this client...");
                    ClientListener.clientsList.remove(clientThread);
                    notificationAssembler.cancel();
                }
            }
        }
    }
}