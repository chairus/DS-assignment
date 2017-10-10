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
    private int leaderId;
    BufferedWriter buffWriter;
    Marshaller jaxbMarshaller;
    ObjectFactory objectFactory;

    public NotificationRelayer() {
        leader = null;
        leaderId = -1;
    }

    /**
     * This method connects to the leader/proposer notifier port
     */
    public void connect() throws IOException, NullPointerException {
        // int serverNotifierPort = findLeaderNotifierPort();
        // if (serverNotifierPort > 0) {
        //     leader = new Socket("127.0.0.1", serverNotifierPort);
        // }
        ServerInfo sInfo = findLeader();
        if (sInfo != null) {
            leader = new Socket(sInfo.ipAddress, sInfo.notifierPort);
        }
    }

    /**
     * This method finds the port on the leader where the notifiers are suppose to connect to.
     * @return - The port number(0 if the leader port is not found(i.e. there is no leader))
     */
    public ServerInfo findLeader() {
        int serverId = MitterServer.currentLeader.getId();
        for (ServerInfo sInfo: MitterServer.serverInfo) {
            if (sInfo.id == serverId) {
                leaderId = serverId;
                return sInfo;
            }
        }

        return null;
        // int serverId = MitterServer.currentLeader.getId();
        // for (List<Integer> ports: MitterServer.serverPorts) {
        //     if (ports.get(0) == serverId) {
        //         leaderId = serverId;
        //         return ports.get(2);
        //     }
        // }

        // return -1;
    }

    /**
     * Initializes the socket writer, marshaller and unmarshaller
     * @throws IOException   [description]
     * @throws JAXBException [description]
     */
    public void init() throws IOException, JAXBException {
        OutputStream out = leader.getOutputStream();
        buffWriter = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
        JAXBContext jaxbContext = JAXBContext.newInstance(NotificationInfo.class);
        jaxbMarshaller = jaxbContext.createMarshaller();
        objectFactory = new ObjectFactory();
    }

    /**
     * Sends a notification
     * @param  notification  [description]
     * @throws IOException   [description]
     * @throws JAXBException [description]
     */
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

    public void setUp() throws IOException, JAXBException {
        connect();
        init();
    }

    /**
     * Waits until a leader has been elected.
     */
    public void waitForLeaderElection() {
        do {
            try {
                leader = null;
                TimeUnit.MILLISECONDS.sleep(200);  // Wait for the re-election of the leader to finish
            } catch (Exception ex) {
                // IGNORE
            }
        } while (MitterServer.currentLeader == null);
    }

    @Override
    public void run() {
        while (!MitterServer.isLeader && !MitterServer.changeInLeader) {    // While this server is not the leader let this notification relayer to conitnue to exist
            try {
                setUp();
                while (MitterServer.currentLeader.getId() == leaderId) {    // While there was no change of leadership, stay connected to the same leader
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
            } catch (IOException e) {            // The leader has crashed or got diconnected
                waitForLeaderElection();
            } catch (NullPointerException e) {   // The leader has crashed or got disonnected
                waitForLeaderElection();
            } catch (JAXBException e) {
                System.err.format("[ SERVER %d ] Error: NotificationRelayer, " + e.getMessage(), MitterServer.serverId);
                e.printStackTrace();
                // System.exit(1);
            }
        }
    }
}