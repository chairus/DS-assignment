package uni.mitter;

import generated.nonstandard.notification.NotificationInfo;
import generated.nonstandard.notification.ObjectFactory;
import java.net.Socket;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.concurrent.TimeUnit;

public class MitterNotifier2 extends Notifier {
    public static void main(String[] args) {
        try {
            init("localhost", 3004);
            
            try {
                // Create object notification
                NotificationInfo notification = new NotificationInfo();
                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Central_Hub",
                                                  "Central Hub, Room 402",
                                                  "Room currently unavailable. Cleaning in progress.",
                                                  "caution",
                                                  0);
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(1000);

                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Physics_Bld",
                                                  "Physics Building, Room 112",
                                                  "Room currently unavailable. Experiment gone wild.",
                                                  "caution",
                                                  1);

                dataWriter = new StringWriter();
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(1000);

                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Maths_Learning_Centre",
                                                  "Engineering South Building, Room 221",
                                                  "Class in progress",
                                                  "notice",
                                                  2);

                dataWriter = new StringWriter();
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(1000);

                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Engineering_Bld",
                                                  "Engineering South Building, Room 321",
                                                  "Room currently unavailable. Robot gone wild.",
                                                  "urgent",
                                                  3);

                dataWriter = new StringWriter();
                sendNotification(notification, buffWriter);

                TimeUnit.MILLISECONDS.sleep(1000);

                // System.out.println("Sending marshalled notification to the server...");
                notification = createNotification("Fitness_Hub",
                                                  "Fitness Hub, Room 101",
                                                  "Room currently unavailable. Pilates on progress.",
                                                  "notice",
                                                  4);

                dataWriter = new StringWriter();
                sendNotification(notification, buffWriter);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (true) {
            // Run forever
        }
    }
}