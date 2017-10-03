/**
 * This class relays all notifications received by a non-leader server to the leader/proposer server.
 */

package uni.mitter;

import generated.nonstandard.notification.NotificationInfo;
import generated.nonstandard.notification.ObjectFactory;
import java.net.Socket;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBElement;

public class NotificationRelayer extends Thread {
    private Socket leader;
    BufferedWriter buffWriter;
    Marshaller jaxbMarshaller;
    ObjectFactory objectFactory;

    public NotificationRelayer() {

    }

    /**
     * This method connects to the leader/proposer notifier port
     */
    public void connect() throws IOException {
        int serverNotifierPort = findNotifierPort();
        leader = new Socket("127.0.0.1", serverNotifierPort);
    }

    /**
     * This method finds the port on the leader where the notifiers are suppose to connect to.
     */
    public int findNotifierPort() {
        int serverId = MitterServer.currentLeader.getId();
        for (List<Integer> list: MitterServer.serverPorts) {
            if (list.get(0) == serverId) {
                return list.get(2);
            }
        }

        return 0;
    }

    public void init() throws IOException, JAXBException {
        OutputStream out = leader.getOutputStream();
        buffWriter = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
        JAXBContext jaxbContext = JAXBContext.newInstance(NotificationInfo.class);
        jaxbMarshaller = jaxbContext.createMarshaller();
        objectFactory = new ObjectFactory();
    }

    public void sendNotification(NotificationInfo notification) throws IOException, JAXBException {
        /* marshalling of java objects in xml (send to sever) */
        StringWriter dataWriter = new StringWriter();
        JAXBElement<NotificationInfo> notificationInfo = objectFactory.createNotification(notification);
        jaxbMarshaller.marshal(notificationInfo, dataWriter);
        // buffWriter = new BufferedWriter(writer);
        buffWriter.write(dataWriter.toString());
        buffWriter.newLine();
        buffWriter.flush();
    }

    @Override
    public void run() {
        try {
            connect();
            init();
            while (true) {
                MitterServer.notificationListLock.lock();
                for (NotificationInfo notification: MitterServer.notificationList) {
                    synchronized (MitterServer.numOfNotificationsRelayed) {
                        // Send at most 10 notifications at a time to be replicated
                        if (MitterServer.numOfNotificationsRelayed == MitterServer.BATCH_SIZE) {
                            break;
                        }
                        // Take one notification from the list and relay it to the leader
                        try {
                            sendNotification(notification);
                            MitterServer.numOfNotificationsRelayed += 1;
                            TimeUnit.MILLISECONDS.sleep(200);
                        } catch (InterruptedException ex) {
                            System.err.printf("[ SERVER %d ] Thread NotificationRelayer interrupted", MitterServer.serverId);
                            ex.printStackTrace();    
                        }
                    }
                }
                MitterServer.notificationListLock.unlock();

                do {    // Wait until the batch of relayed notifications has been fully replicated
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);   
                    } catch (InterruptedException ex) {
                        System.err.printf("[ SERVER %d ] Thread NotificationRelayer interrupted", MitterServer.serverId);
                        ex.printStackTrace();
                    }
                } while (MitterServer.numOfNotificationsRelayed > 0);
            }
        } catch (IOException e) {   // The leader has crashed or got disonnected
            MitterServer.currentLeader = null;
        } catch (JAXBException e) {
            System.err.format("[ SERVER %d ] Error: NotificationRelayer, " + e.getMessage(), MitterServer.serverId);
            e.printStackTrace();
            System.exit(1);
        }
    }
}