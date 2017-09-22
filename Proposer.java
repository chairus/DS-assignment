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
        PreparePhaseResult result = new PreparePhaseResult();
        do {
            if (MitterServer.prepared) { // Skip the prepare phase and go straight to accept phase
            
            } else { // Start with prepare phase then accept phase
                // Find the firstUnchosenIndex in the log
                MitterServer.firstUnchosenIndex = MitterServer.findFirstUnchosenIndex();
                MitterServer.nextIndex = MitterServer.firstUnchosenIndex + 1;
    
                /* ========== PREPARE PHASE ========== */
                // Send a prepare request to all acceptors
                result = prepareRequest(value);
    
                if (!result.hasMajority) {
                    return false;
                }
    
                /* ========== ACCEPT PHASE ========== */
                boolean hasSuccessfullyProposed = true;
                if (result.acceptedValue == null) {     // No accepted value therefore pick a value
                    hasSuccessfullyProposed = acceptRequest(value);
                } else {                                // Use the accepted value
                    hasSuccessfullyProposed = acceptRequest(result.acceptedValue);
                }

                if (!hasSuccessfullyProposed) {
                    return false;
                }
            }
       } while (result.acceptedValue != value);

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
                    sendRequest(prepareReq, acceptor.getSocket());    
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
        List<String> receivedResponses = receiveResponses();
        
        // Check if the majority of the reponses has a status set to true, that is the proposal
        // has been accepted by the majority of acceptors.
        List<Message> unmarshalledReceivedResponses = new ArrayList<>();    // Container on where to store the unmarshalled received responses
        result.hasMajority = checkMajority(unmarshalledReceivedResponses, receivedResponses);
        result.acceptedValue = checkIfAcceptedValueExist(unmarshalledReceivedResponses);
        setOrUnsetPrepared(unmarshalledReceivedResponses);  // Sets the prepared value if majority of the Acceptors has set the noMoreAccepted field in their reponse

        return result;
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
            int hasAcceptedProposal = Float.compare(Float.parseFloat(response.getPrepare().getResponse().getAcceptedProposal()), -1.0f);

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
    private boolean acceptRequest(NotificationInfo value) {
        Message acceptReq = setupAcceptRequest(value);
        
        sendAcceptRequestToAll(acceptReq);

        // Listen for the responses from all acceptors until a majority of reponses has been received
        // and for each accept response received send a Success message if the firstUnchosenIndex in the
        // accept response is less than that of this server's lastLogIndex and the accepted proposal
        // in log[response.firstUnchosenIndex] == infinity(Float.MAX_VALUE).
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
                        String response = acceptResponse(acceptor.getSocket());
                        Message unmarshalledResponse;
                        if (response != null) {
                            receivedResponses.add(response);
                            unmarshalledResponse = (Message) MitterServer.jaxbUnmarshallerMessage.unmarshal(new StringReader(response));
                            Float acceptResponseProposalNumber = Float.parseFloat(unmarshalledResponse.getAccept().getResponse().getAcceptorMinProposalNumber());
                            // Abandon proposal
                            if (Float.compare(acceptResponseProposalNumber, MitterServer.nextIndex-1.0f) > 0) {
                                MitterServer.prepared = false;
                                return false;
                            }

                            // Send success message?
                            MitterServer.updateLastLogIndex();
                            int acceptResponseFirstUnchosenIndex = unmarshalledResponse.getAccept().getResponse().getAcceptorsFirstUnchosenIndex(); 
                            if (acceptResponseFirstUnchosenIndex <= MitterServer.lastLogIndex
                                && Float.compare(MitterServer.log.get(acceptResponseFirstUnchosenIndex).getAcceptedProposal(),Float.MAX_VALUE) == 0) {
                                sendSuccessRequest(acceptResponseFirstUnchosenIndex, acceptor);
                            }
                        }

                    } catch (IOException e) {
                        // A server has disconnected?
                        if (removeFromActiveServers(acceptor)) {
                            index -= 1;
                        }
                    } catch (JAXBException e) {
                        System.err.format("[ SERVER %d ] Error: Proposer, " + e.getMessage(), MitterServer.serverId);
                        e.printStackTrace();
                    }
    
                    index += 1;
                }
            }
        }

        return true;
    }

    public void sendSuccessRequest(int acceptResponseFirstUnchosenIndex, ServerPeers.ServerIdentity acceptor) {
        Message successReq = setupSuccessRequest(acceptResponseFirstUnchosenIndex);
        try {
            sendRequest(successReq, acceptor.getSocket());    
        } catch (IOException e) {
            // A server has been disconnected?
            removeFromActiveServers(acceptor);
        } catch (JAXBException e) {
            System.err.format("[ SERVER %d ] Error: Proposer, " + e.getMessage(), MitterServer.serverId);
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * This method sends Accept request to all acceptors.
     * @param value - The value to be broadcasted to all acceptors
     */
    public void sendAcceptRequestToAll(Message value) {
        // Send accept request to all acceptors
        int index = 0;
        synchronized(MitterServer.serversList) {
            while (index < MitterServer.serversList.size()) {
                ServerPeers.ServerIdentity acceptor = MitterServer.serversList.get(index);
                try {
                    sendRequest(value, acceptor.getSocket());    
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
    }

    /**
     * This method listens for the responses from the acceptors for the prepare request. It will stop
     * listening once it has received the majority of the responses
     * @return - The received XML responses from the Acceptors
     */
    public List<String> receiveResponses() {
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
                        String response = acceptResponse(acceptor.getSocket());
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

    private Message setupSuccessRequest(int acceptResponseFirstUnchosenIndex) {
        Message success = new Message();
        success.setAccept(null);
        success.setPrepare(null);
        success.setSuccess(new Message.Success());
        success.getSuccess().setRequest(new Message.Success.Request());
        success.getSuccess().getRequest().setIndex(acceptResponseFirstUnchosenIndex);
        success.getSuccess().getRequest().setValue(MitterServer.log.get(acceptResponseFirstUnchosenIndex).getAcceptedValue());

        return success;
    }

    /**
     * This method sets up the accept request to be sent to all acceptors.
     * @param value - A value, either the highest numbered one from a Prepare response, or if none, 
     *                then one from a client request
     * @return - The accept request
     */
    private Message setupAcceptRequest(NotificationInfo value) {
        Message acceptRequest = new Message();
        String mostRecentProposalNumber = String.valueOf(MitterServer.maxRound-1) + "." + MitterServer.serverId;
        acceptRequest.setPrepare(null);
        acceptRequest.setSuccess(null);
        acceptRequest.setAccept(new Message.Accept());
        acceptRequest.getAccept().setRequest(new Message.Accept.Request());
        acceptRequest.getAccept().getRequest().setIndex(MitterServer.nextIndex-1);
        acceptRequest.getAccept().getRequest().setProposalNumber(mostRecentProposalNumber);
        acceptRequest.getAccept().getRequest().setValue(value);
        acceptRequest.getAccept().getRequest().setFirstUnchosenIndex(MitterServer.firstUnchosenIndex);

        return acceptRequest;
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
     * @param request - The prepare request message
     * @param acceptor - The Acceptor socket
     */
    public void sendRequest(Message request, Socket acceptor) throws IOException, JAXBException {
        BufferedWriter buffWriter = new BufferedWriter(new OutputStreamWriter(acceptor.getOutputStream()));
        StringWriter sWriter = new StringWriter();
        MitterServer.jaxbMarshallerMessage.marshal(request, sWriter);
        buffWriter.write(sWriter.toString());
        buffWriter.newLine();
        buffWriter.flush();
        
    }

    public String acceptResponse(Socket s) throws IOException {
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
        public PreparePhaseResult() { 
            this.hasMajority = false;
            this.acceptedValue = null;
        }
    }
}