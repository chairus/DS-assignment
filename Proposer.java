/**
 * This class is an implementation of the Proposer role in the PAXOS algorithm. The Proposer class
 * sends a prepare request to all Acceptors with a proposal number attached to it. It then waits for the
 * response of the Acceptors, if this Proposer has received the majority of response from the Acceptors,
 * then it sends an accept request together with the value of the highest proposal number or if none then 
 * this Proposer picks the value to be accepted.
 * @author cyrusvillacampa
 */

package uni.mitter;

import generated.nonstandard.notification.Notification;
import generated.nonstandard.message.Message;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.StringWriter;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Proposer {
    // Constructor
    public Proposer() {

    }

    /**
     * This method will perform a single round of Basic Paxos with the argument as the proposed value.
     * This will return a boolean value to indicate if the value was successfully chosen.
     * @param value - The notification to be written to each log on each server
     */
    public boolean write(Notification value) {
       if (MitterServer.prepared) { // Skip the prepare phase and go straight to accept phase

       } else { // Start with prepare phase then accept phase
            /* ========== PREPARE PHASE ========== */
            prepareRequest(value);
       }

       return false;
    }

    /**
     * This method sends a prepare request to all Acceptors and in that request is a proposal number,
     * which is composed of the sender's server id and the round number.
     * @param value - The notification to be written to each log on each server
     */
    private void prepareRequest(Notification value) {
        Message prepareReq = setupPrepareRequest();
        // Send the prepare request to all acceptors
        int index = 0;
        synchronized(MitterServer.serversList) {
            while (index < MitterServer.serversList.size()) {
                ServerPeers.ServerIdentity acceptor = MitterServer.serversList.get(index);
                try {
                    sendPrepareRequest(prepareReq, acceptor.getSocket());    
                } catch (IOException e) {
                    if (MitterServer.serversList.remove(acceptor)) {
                        index -= 1;
                    }
                } catch (JAXBException e) {
                    System.err.format("[ SERVER %d ] Error: Proposer, " + e.getMessage(), MitterServer.serverId);
                    e.printStackTrace();
                    System.exit(1);
                }
                index += 1;
            }
        }
    }

    /**
     * This method sends an accept request to all Acceptors and in that request is a proposal number
     * and a value, which is either picked by the Proposer or a value that has already been accepted
     * by the majority of the Acceptors
     */
    private void accepRequest() {

    }

    /**
     * This method sets up the prepare request to be sent to all acceptors and also increments the
     * maxRound so that each round will have a unique proposal number.
     * @return - The prepare request
     */
    private Message setupPrepareRequest() {
        // Setup the prepare request
        Message prepareReq = new Message();
        prepareReq.setAccept(null);
        prepareReq.setSuccess(null);
        Message.Prepare prep = new Message.Prepare();
        prep.setResponse(null);
        Message.Prepare.Request request = new Message.Prepare.Request();
        request.setIndex(MitterServer.nextIndex);
        // Proposal number would be of the format "[maxRound].[serverId]"
        request.setProposalNumber(new String(String.valueOf(MitterServer.maxRound) + "." + MitterServer.serverId));
        prep.setRequest(request);
        prepareReq.setPrepare(prep);
        MitterServer.maxRound += 1;

        return prepareReq;
    }

    /**
     * This method will send the prepare request to an acceptor.
     */
    public void sendPrepareRequest(Message prepReq, Socket acceptor) throws IOException, JAXBException {
        BufferedWriter buffWriter = new BufferedWriter(new OutputStreamWriter(acceptor.getOutputStream()));
        StringWriter sWriter = new StringWriter();
        MitterServer.jaxbMarshallerMessage.marshal(prepReq, sWriter);
        buffWriter.write(sWriter.toString());
        buffWriter.newLine();
        buffWriter.flush();
        
    }
}