/**
 * This class is an implementation of the Proposer role in the PAXOS algorithm. The Proposer class
 * sends a prepare request to all Acceptors with a proposal number attached to it. It then waits for the
 * response of the Acceptors, if this Proposer has received the majority of response from the Acceptors,
 * then it sends an accept request together with the value of the highest proposal number or if none then 
 * this Proposer picks the value to be accepted.
 * @author cyrusvillacampa
 */

package uni.mitter;

import generated.nonstandard.notification.NotificationInfo;
import generated.nonstandard.message.Message;
import java.util.List;
import java.util.ArrayList;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.StringWriter;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.InputStreamReader;
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
    public boolean write(NotificationInfo value) {
       if (MitterServer.prepared) { // Skip the prepare phase and go straight to accept phase

       } else { // Start with prepare phase then accept phase
            /* ========== PREPARE PHASE ========== */
            // Send a prepare request to all acceptors
            PreparePhaseResult result = prepareRequest(value);

            if (!result.hasMajority) {
                return false;
            }

            /* ========== ACCEPT PHASE ========== */
            if (result.acceptedValue == null) {     // No accepted value therefore pick a value
                acceptRequest(value);
            } else {                                // Use the accepted value
                acceptRequest(result.acceptedValue);
            }
       }

       return true;
    }

    /**
     * This method sends a prepare request to all Acceptors and in that request is a proposal number,
     * which is composed of the sender's server id and the round number.
     * @param value - The notification to be written to each log on each server
     */
    private PreparePhaseResult prepareRequest(NotificationInfo value) {
        Message prepareReq = setupPrepareRequest();
        PreparePhaseResult result = new PreparePhaseResult();
        // Send the prepare request to all acceptors
        int index = 0;
        synchronized(MitterServer.serversList) {
            while (index < MitterServer.serversList.size()) {
                ServerPeers.ServerIdentity acceptor = MitterServer.serversList.get(index);
                try {
                    sendPrepareRequest(prepareReq, acceptor.getSocket());    
                } catch (IOException e) {
                    if (updateActiveServers(acceptor)) {
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

        // Listen for the responses from all acceptors until a majority of reponses has
        // been received
        List<String> receivedResponses = receivePrepareResponse();
        
        // Check if the majority of the reponses has a status set to true, that is the proposal
        // has been accepted by the majority of acceptors, if it has received the majority of
        // the responses then set the hasMajority flag.
        List<Message> unmarshalledReceivedResponses = new ArrayList<>();

        result.hasMajority = checkMajority(unmarshalledReceivedResponses, receivedResponses);
        result.acceptedValue = checkIfAcceptedValueExist(unmarshalledReceivedResponses);

        return result;
    }

    /**
     * This method listens for the responses from the acceptors for the prepare request.
     */
    public List<String> receivePrepareResponse() {
        List<String> receivedResponses = new ArrayList<>();
        synchronized (MitterServer.serversList) {
            int numOfActiveServers = MitterServer.serversList.size();
            int index = 0;
            ServerPeers.ServerIdentity acceptor;
            while (index < numOfActiveServers) {
                acceptor = MitterServer.serversList.get(index);
                try {
                    String response = acceptPrepareResponse(acceptor.getSocket());
                    if (response != null) {
                        receivedResponses.add(response);
                    }
                } catch (IOException e) {
                    if (updateActiveServers(acceptor)) {
                        index -= 1;
                    }
                }

                // Check if a majority of responses has been received
                if (receivedResponses.size() >= ((numOfActiveServers/2) + 1)) {
                    break;
                }

                index += 1;
            }
        }

        return receivedResponses;
    }

    public NotificationInfo checkIfAcceptedValueExist(List<Message> responses) {
        NotificationInfo acceptedValue = null;
        float previouslySeenAcceptedProposal = 0.0f;

        for (Message response: responses) {
            int hasAcceptedProposal = Float.compare(Float.parseFloat(response.getPrepare().getResponse().getAcceptedProposal()), -1f);
            float acceptedProposal = Float.parseFloat(response.getPrepare().getResponse().getAcceptedProposal());
            int greaterThanSeenAcceptedProposal = Float.compare(acceptedProposal, previouslySeenAcceptedProposal);

            if (hasAcceptedProposal > 0 && greaterThanSeenAcceptedProposal > 0) {
                acceptedValue = response.getPrepare().getResponse().getAcceptedValue();
            }
        }

        return acceptedValue;
    }

    /**
     * This method checks if the majority of received prepare responses has their status set to true,
     * if it is then return true, else false.
     * @param responses - Stores the unmarshalled received prepare responses
     * @param receivedResponses - The received prepare responses
     * @return - True if the majority of responses has their status set to true 
     */
    public boolean checkMajority(List<Message> responses, List<String> receivedResponses) {
        for (String response: receivedResponses) {
            StringReader sReader = new StringReader(response);
            try {
                Message res = (Message) MitterServer.jaxbUnmarshallerMessage.unmarshal(sReader);
                if (!res.getPrepare().getResponse().isStatus()) {
                    return false;
                }
                responses.add(res);
            } catch (JAXBException e) {
                System.err.format("[ SERVER %d ] Error: Proposer, " + e.getMessage(), MitterServer.serverId);
                e.printStackTrace();
                System.exit(1);
            }
        }

        return true;
    }

    /**
     * This method sends an accept request to all Acceptors and in that request is a proposal number
     * and a value, which is either picked by the Proposer or a value that has already been accepted
     * by the majority of the Acceptors
     */
    private void acceptRequest(NotificationInfo value) {

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

    public String acceptPrepareResponse(Socket s) throws IOException {
        BufferedReader buffReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        String line = null;

        if (buffReader.ready()) {
            line = buffReader.readLine();
        }

        return line;
    }

    public boolean updateActiveServers(ServerPeers.ServerIdentity sId) {
        try {
            sId.getSocket().close();    
        } catch (IOException ex) {
            System.err.format("[ SERVER %d ] Error: Proposer, " + ex.getMessage(), MitterServer.serverId);
            ex.printStackTrace();
            System.exit(1);
        }
        
        return MitterServer.serversList.remove(sId);
    }

    private class PreparePhaseResult {
        public boolean hasMajority;
        public NotificationInfo acceptedValue;

        // Constructoru
        public PreparePhaseResult() { }
    }
}