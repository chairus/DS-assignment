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
    public boolean writeValue(NotificationInfo value) {
       if (MitterServer.prepared) { // Skip the prepare phase and go straight to accept phase

       } else { // Start with prepare phase then accept phase
            // Find the firstUnchosenIndex in the log
            if (MitterServer.log.size() > 0) {
                int index = 0;
                while (index < MitterServer.log.size()) {
                    LogEntry entry = MitterServer.log.get(index);
                    if (Float.compare(entry.getAcceptedProposal(), Float.MAX_VALUE) < 0) {
                        MitterServer.firstUnchosenIndex = index;
                    }
                }
            } else {
                MitterServer.firstUnchosenIndex = 0;
            }
            MitterServer.nextIndex = MitterServer.firstUnchosenIndex + 1;

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
        Message prepareReq = setupPrepareRequest(); // Initialize the prepare message
        PreparePhaseResult result = new PreparePhaseResult();
        // Send the prepare request to all acceptors
        int index = 0;
        synchronized(MitterServer.serversList) {
            while (index < MitterServer.serversList.size()) {
                ServerPeers.ServerIdentity acceptor = MitterServer.serversList.get(index);
                try {
                    sendPrepareRequest(prepareReq, acceptor.getSocket());    
                } catch (IOException e) {
                    // A server has been disconnected?
                    if (removeFromActiveServers(acceptor)) {
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
        // has been accepted by the majority of acceptors.
        List<Message> unmarshalledReceivedResponses = new ArrayList<>();    // Container on where to store the unmarshalled received responses
        result.hasMajority = checkMajority(unmarshalledReceivedResponses, receivedResponses);
        result.acceptedValue = checkIfAcceptedValueExist(unmarshalledReceivedResponses);
        setOrUnsetPrepared(unmarshalledReceivedResponses);  // Sets the prepared value if majority of the Acceptors has set the noMoreAccepted field in their reponse

        return result;
    }

    /**
     * This method listens for the responses from the acceptors for the prepare request. It will stop
     * listening once it has received the majority of the responses
     * @return - The received XML responses from the Acceptors
     */
    public List<String> receivePrepareResponse() {
        List<String> receivedResponses = new ArrayList<>();
        synchronized (MitterServer.serversList) {
            int numOfActiveServers = MitterServer.serversList.size();
            int majoritySize = ((numOfActiveServers/2) + 1);
            ServerPeers.ServerIdentity acceptor;
            // Keep looping until a majority of responses has been received
            while (receivedResponses.size() < majoritySize) {
                int index = 0;
                while (index < numOfActiveServers) {
                    acceptor = MitterServer.serversList.get(index);
                    try {
                        String response = acceptPrepareResponse(acceptor.getSocket());
                        if (response != null) {
                            receivedResponses.add(response);
                        }
                    } catch (IOException e) {
                        // A server has disconnected?
                        if (removeFromActiveServers(acceptor)) {
                            index -= 1;
                        }
                    }
    
                    index += 1;
                }
            }
            
        }

        return receivedResponses;
    }

    /**
     * This method checks if the majority of received prepare responses has their status set to true,
     * if it is then return true, else false. This means that the majority of acceptors has promised
     * to reject proposals with number less than this Proposers proposal number.
     * @param responses - Stores the unmarshalled received prepare responses
     * @param receivedResponses - The received prepare responses
     * @return - True if the majority of responses has their status field set to true 
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
     * This method checks if an accepted value already exists. If there is then return it.
     * @param reponses - The list of responses from Acceptors
     * @return - The accepted value if there is, else null
     */
    public NotificationInfo checkIfAcceptedValueExist(List<Message> responses) {
        NotificationInfo acceptedValue = null;
        float previouslySeenAcceptedProposal = 0.0f;

        for (Message response: responses) {
            // Check if the acceptedProposal field has a value of '-1', because a '-1' value means that
            // it has not accepted any proposal.
            int hasAcceptedProposal = Float.compare(Float.parseFloat(response.getPrepare().getResponse().getAcceptedProposal()), -1f);

            if (hasAcceptedProposal > 0) {
                float acceptedProposal = Float.parseFloat(response.getPrepare().getResponse().getAcceptedProposal());
                int greaterThanSeenAcceptedProposal = Float.compare(acceptedProposal, previouslySeenAcceptedProposal);

                if (greaterThanSeenAcceptedProposal > 0) {
                    acceptedValue = response.getPrepare().getResponse().getAcceptedValue();
                    previouslySeenAcceptedProposal = acceptedProposal;
                }
            }
        }

        return acceptedValue;
    }

    /**
     * This method sets or unsets the prepared variable which indicates that there is no need to issue 
     * Prepare requests because a majority of acceptors has responded to Prepare requests with 
     * noMoreAccepted set to true
     * @param responses - The received responses from the majority of acceptors
     */
    public void setOrUnsetPrepared(List<Message> responses) {
        boolean prepared = true;
        for (Message response: responses) {
            if (!response.getPrepare().getResponse().isNoMoreAccepted()) {
                prepared = false;
            }
        }
        MitterServer.prepared = prepared;
    }

    /**
     * This method sends an accept request to all Acceptors and in that request is a proposal number
     * and a value, which is either picked by the Proposer or a value that has already been accepted
     * by the majority of the Acceptors
     * @param value - The chosen value to be written on the logs of each server
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
        request.setIndex(MitterServer.firstUnchosenIndex);
        // Proposal number would be of the format "[maxRound].[serverId]"
        request.setProposalNumber(new String(String.valueOf(MitterServer.maxRound) + "." + MitterServer.serverId));
        prep.setRequest(request);
        prepareReq.setPrepare(prep);
        MitterServer.maxRound += 1;

        return prepareReq;
    }

    /**
     * This method will send the prepare request to an acceptor.
     * @param prepReq - The prepare request message
     * @param acceptor - The Acceptor socket
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

    public boolean removeFromActiveServers(ServerPeers.ServerIdentity sId) {
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

        // Constructor
        public PreparePhaseResult() { }
    }
}