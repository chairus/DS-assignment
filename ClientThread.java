package uni.mitter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.Socket;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.StringWriter;
import generated.nonstandard.subscription.*;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBContext;


/**
 * This class manages a single client and at the same time creates two TimerTask, that handles
 * sending of caution and notice notification. Furthermore this class handles sending of urgent
 * notifications.
 */
public class ClientThread extends Thread{
    private Filter filter;
    private Socket socket;

    public ClientThread(Socket socket) {
        this.socket = socket;
        this.filter = new Filter();
    }

    /**
     * This method
     */
    public void run() {
        try {
            // Get stream for reading subscriptions
            InputStream in = socket.getInputStream();
            Reader reader = new InputStreamReader(in, "UTF-8");
            BufferedReader buffReader = new BufferedReader(reader);
            
            // Get stream for writing notifications
            OutputStream out = socket.getOutputStream();
            Writer writer = new OutputStreamWriter(out, "UTF-8");
            BufferedWriter buffWriter = new BufferedWriter(writer);

            // Initialize both marshaller and unmarshaller
            JAXBContext jaxbContext = JAXBContext.newInstance(Subscription.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            // Receive and unmarshall client subscription
            System.out.println("Reading subscription from client...");
            StringReader dataReader = new StringReader(buffReader.readLine());
            Subscription subs = (Subscription) jaxbUnmarshaller.unmarshal(dataReader);
            
            // Set the filter subscription
            filter.setSubscription(subs);


        } catch (IOException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}