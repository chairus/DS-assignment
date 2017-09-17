package uni.mitter;

import generated.nonstandard.notification.*;
import java.net.Socket;
import java.io.IOException;
import java.io.Writer;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;
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
        // System.out.println("There are " + queue.size() + " notifications to be sent.");

        // boolean temp;
        // // Check if client wants to update their subscription
        // do {
        //     synchronized (clientThread.postponeNotificationTransmission) {
        //         temp = clientThread.postponeNotificationTransmission.booleanValue();
        //     }
        //     // wait until client has finished sending the subscription
        // } while (temp);

        try {
            while (clientThread.buffReader.ready()) {
                // wait until client has finished sending the subscription
            }
        } catch (Exception e) {
            System.err.println("Something went wrong in the Sender.");
            e.printStackTrace();
        }

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
                
                // System.out.print("Sending marshalled notification to the client...");
                /* marshalling of java objects in xml (send to client) */
                jaxbMarshaller.marshal(notification, dataWriter);
                buffWriter = new BufferedWriter(writer);
                buffWriter.write(dataWriter.toString());
                buffWriter.newLine();
                buffWriter.flush();
                // System.out.println("SENT");
                TimeUnit.MILLISECONDS.sleep(20);    // For throttling the output stream. This is to make sure that no overwritting of notifications will occur.
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
                    clientThread.clientStateConnectivity = false;
                    System.out.println("Stopping all threads related to this client...SUCCESS");
                }
                break;  // Stop sending the rest of the notifications
            }
        }
    }
}