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

public class Sender {
    private FilteredNotificationList queue;
    private Socket clientSocket;
    private ClientThread clientThread;
    private NotificationAssembler notificationAssembler;

    public Sender(FilteredNotificationList queue, 
                  Socket clientSocket, 
                  ClientThread clientThread,
                  NotificationAssembler notificationAssembler) {
        this.queue = queue;
        this.clientSocket = clientSocket;
        this.clientThread = clientThread;
        this.notificationAssembler = notificationAssembler;
    }

    /**
     * This method sends the notifications to the client.
     */
    public void send() {
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
                // Releasing all resources related to the disconnected client
                System.out.println("Connection lost.");
                synchronized (MitterServer.clientsList) {
                    System.out.println("Closing client socket...");
                    try {
                        clientSocket.close();
                    } catch (Exception ex) {
                        
                    }
                    System.out.println("Stopping all threads related to this client...");
                    MitterServer.clientsList.remove(clientThread);
                    notificationAssembler.cancel();
                    System.out.println("SUCCESS");
                }
                break;  // Stop sending the rest of the notifications
            }
        }
    }
}