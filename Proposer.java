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
    // Keeps track of the porposed index
    private int proposedIndex;

    // Constructor
    public Proposer() {
        proposedIndex = 0;
    }

    /**
     * This method will perform a single round of Basic Paxos with the argument as the proposed value.
     * This will return a boolean value to indicate if the value was successfully chosen.
     * @param value - The notification to be written to each log on each server
     * @return - True if the value has been successfully chosen
     */
    public boolean writeValue(NotificationInfo value) {
        Proposal result = new Proposal();
        do {
            boolean hasSuccessfullyAccepted = true;
            if (MitterServer.prepared) { // Skip the prepare phase and go straight to accept phase
                proposedIndex = MitterServer.nextIndex;
                MitterServer.nextIndex += 1;
                hasSuccessfullyAccepted = acceptRequest(value);
                
                if (!hasSuccessfullyAccepted) {
                    return false;
                }
            } else { // Start with prepare phase then accept phase
                // MitterServer.firstUnchosenIndex = MitterServer.findFirstUnchosenIndex();
                proposedIndex = MitterServer.firstUnchosenIndex;
                MitterServer.nextIndex = proposedIndex + 1;
                
                /* ========== PREPARE PHASE ========== */
                result = prepareRequest();
    
                if (!result.hasMajority) {
                    return false;
                }
    
                /* ========== ACCEPT PHASE ========== */
                if (result.acceptedValue == null) {     // No accepted value therefore pick a value
                    hasSuccessfullyAccepted = acceptRequest(value);
                } else {                                // Use the accepted value
                    hasSuccessfullyAccepted = acceptRequest(result.acceptedValue);
                }

                if (!hasSuccessfullyAccepted) { // Need to elect a leader
                    return false;
                }
            }
       } while (result.acceptedValue != null);

       return true;
    }

    /**
     * This method sends a prepare request to all Acceptors and in that request is a proposal number,
     * which is composed of the sender's server id and the round number.
     * @param value - The notification to be written to each log on each server
     */
    private Proposal prepareRequest() {
        Message prepareReq = setupPrepareRequest(); // Initialize the prepare message
        Proposal result = new Proposal();

        broadcastRequest(prepareReq);
        result = collectPrepareResponses();
        // // Listen for the responses from all acceptors until a majority of reponses has
        // // been received
        // List<String> receivedResponses = receiveResponses();
        // // Check if the majority of the reponses has a status set to true, that is the proposal
        // // has been accepted by the majority of acceptors.
        // List<Message> unmarshalledReceivedResponses = new ArrayList<>();    // Container on where to store the unmarshalled received responses
        // result.hasMajority = checkMajority(unmarshalledReceivedResponses, receivedResponses);
        // result.acceptedValue = checkIfAcceptedValueExist(unmarshalledReceivedResponses);
        // setOrUnsetPrepared(unmarshalledReceivedResponses);  // Sets the prepared value if majority of the Acceptors has set the noMoreAccepted field in their reponse

        return result;
    }

    /**
     * This method collects the prepare responses sent by the acceptors. In addition it also checks if
     * it has received the majority of the votes for a certain proposal number by checking the "status"
     * field of the received prepare response. Furthermore it also updates the "prepared" variable which
     * indicates if the majority of acceptors has set the "noMoreAccepted" field on their response to
     * true.
     * @return - The result of the vote and the accepted value.
     */
    public Proposal collectPrepareResponses() {
        Proposal result = new Proposal();
        int numOfVotes = 0;
        int numOfServersResponded = 0;
        float largestProposalNumberSeen = 0.0f;
        int numOfNoMoreAcceptedResponse = 0;

        synchronized (MitterServer.serversList) {
            int numOfActiveServers = MitterServer.serversList.size();
            int majoritySize = numOfActiveServers/2;
            ServerPeers.ServerIdentity acceptor;
            // Keep looping until a majority of responses/votes has been received
            while (numOfVotes < majoritySize && numOfServersResponded < numOfActiveServers) {
                int index = 0;
                while (index < numOfActiveServers) {
                    acceptor = MitterServer.serversList.get(index);
                    try {
                        Message response = MitterServer.readMessage(acceptor.getSocket());
                        if (response != null) {
                           if (response.getPrepare() != null) { // Received response to prepare request
                                if (response.getPrepare().getResponse().isStatus()) {
                                    float receivedAcceptedProposal = Float.parseFloat(response.getPrepare().getResponse().getAcceptedProposal());
                                    NotificationInfo receivedAcceptedValue = response.getPrepare().getResponse().getAcceptedValue();
                                    if (Float.compare(receivedAcceptedProposal, largestProposalNumberSeen) > 0) {
                                        largestProposalNumberSeen = receivedAcceptedProposal;
                                        result.acceptedValue = receivedAcceptedValue;
                                    }
                                    if (response.getPrepare().getResponse().isNoMoreAccepted()) {
                                        numOfNoMoreAcceptedResponse += 1;
                                    }
                                    numOfVotes += 1;
                                    numOfServersResponded += 1;
                                }
                            } else if (response.getAccept() != null) { // Received response to accept request
                                float acceptResponseProposalNumber = Float.parseFloat(response.getAccept().getResponse().getAcceptorMinProposalNumber());
                                int acceptorsFirstUnchosenIndex = response.getAccept().getResponse().getAcceptorsFirstUnchosenIndex();
                                if (acceptorsFirstUnchosenIndex <= MitterServer.lastLogIndex
                                    && Float.compare(MitterServer.log.get(acceptorsFirstUnchosenIndex).getAcceptedProposal(),Float.MAX_VALUE) >= 0) {
                                    sendSuccessRequest(acceptorsFirstUnchosenIndex, acceptor);
                                }
                            } else if (response.getSuccess() != null) { // Received response to success request
                                int acceptorsFirstUnchosenIndex = response.getSuccess().getResponse().getAcceptorsFirstUnchosenIndex();
                                if (acceptorsFirstUnchosenIndex < MitterServer.firstUnchosenIndex) {
                                    sendSuccessRequest(acceptorsFirstUnchosenIndex, acceptor);
                                }
                            } else {                                    // Received heartbeat message
                                MitterServer.sendHeartbeatMessage(acceptor.getSocket());
                            }
                        }
                    } catch (IOException e) { // A server has crashed or got disconnected
                        if (removeFromActiveServers(acceptor)) {
                            index -= 1;
                            numOfActiveServers = MitterServer.serversList.size();
                            majoritySize = numOfActiveServers/2;
                        }
                    } catch (JAXBException e) {
                        System.err.format("[ SERVER %d ] Error: Proposer, " + e.getMessage(), MitterServer.serverId);
                        e.printStackTrace();
                    }
                    index += 1;
                }
            }
            if (numOfNoMoreAcceptedResponse >= majoritySize) {
                MitterServer.prepared = true;
            }
            if (numOfVotes >= majoritySize) {
                result.hasMajority = true;
            }
        }
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
        
        broadcastRequest(acceptReq);

        // List<String> receivedResponses = new ArrayList<>();
        // synchronized (MitterServer.serversList) {
        //     int numOfActiveServers = MitterServer.serversList.size();
        //     // int majoritySize = (numOfActiveServers/2) + 1;
        //     int majoritySize = numOfActiveServers;
        //     ServerPeers.ServerIdentity acceptor;
        //     // Keep looping until a majority of responses has been received
        //     while (receivedResponses.size() < majoritySize && numOfServersResponded < numOfActiveServers) {
        //         int index = 0;
        //         while (index < numOfActiveServers) {
        //             acceptor = MitterServer.serversList.get(index);
        //             try {
        //                 String response = receiveResponse(acceptor.getSocket());
        //                 Message unmarshalledResponse;
        //                 if (response != null) {
        //                     receivedResponses.add(response);
        //                     unmarshalledResponse = (Message) MitterServer.jaxbUnmarshallerMessage.unmarshal(new StringReader(response));
        //                     Float acceptResponseProposalNumber = Float.parseFloat(unmarshalledResponse.getAccept().getResponse().getAcceptorMinProposalNumber());
        //                     // Abandon proposal
        //                     float acceptRequestProposalNumber = Float.parseFloat(acceptReq.getAccept().getRequest().getProposalNumber());
        //                     if (Float.compare(acceptResponseProposalNumber, acceptRequestProposalNumber) > 0) {
        //                         MitterServer.maxRound = Math.round(acceptResponseProposalNumber);
        //                         MitterServer.prepared = false;
        //                         return false;
        //                     }

        //                     // Send success message
        //                     MitterServer.updateLastLogIndex();
        //                     int acceptResponseFirstUnchosenIndex = unmarshalledResponse.getAccept().getResponse().getAcceptorsFirstUnchosenIndex(); 
        //                     if (acceptResponseFirstUnchosenIndex <= MitterServer.lastLogIndex
        //                         && Float.compare(MitterServer.log.get(acceptResponseFirstUnchosenIndex).getAcceptedProposal(),Float.MAX_VALUE) >= 0) {
        //                         sendSuccessRequest(acceptResponseFirstUnchosenIndex, acceptor);
        //                     }
        //                 }

        //             } catch (IOException e) {
        //                 // A server has disconnected?
        //                 if (removeFromActiveServers(acceptor)) {
        //                     index -= 1;
        //                 }
        //             } catch (JAXBException e) {
        //                 System.err.format("[ SERVER %d ] Error: Proposer, " + e.getMessage(), MitterServer.serverId);
        //                 e.printStackTrace();
        //             }
    
        //             index += 1;
        //         }
        //     }

        //     // boolean hasMajority = checkMajority(responses, receivedResponses);

        //     // Obtained majority of replies to proposal "proposedIndex" then set the corresponding log entry
        //     if (proposedIndex >= MitterServer.log.size()) {
        //         MitterServer.increaseLogCapacity(proposedIndex+1);
        //     }
        //     LogEntry updatedEntry = MitterServer.log.get(proposedIndex);
        //     updatedEntry.setAcceptedProposal(Float.MAX_VALUE);
        //     updatedEntry.setAcceptedValue(acceptReq.getAccept().getRequest().getValue());
        //     MitterServer.log.set(proposedIndex, updatedEntry);
        //     MitterServer.firstUnchosenIndex += 1;
        // }

        // return true;

        return collectAcceptResponses(acceptReq);
    }

    /**
     * Listen for the responses from all acceptors until a majority of reponses has been received
     * and for each accept response received send a Success message if the firstUnchosenIndex in the
     * accept response is less than that of this server's lastLogIndex and the accepted proposal
     * in log[response.firstUnchosenIndex] == infinity(Float.MAX_VALUE).
     * @return - 
     */
    public boolean collectAcceptResponses(Message acceptReq) {
        List<String> receivedResponses = new ArrayList<>();
        synchronized (MitterServer.serversList) {
            float acceptRequestProposalNumber = Float.parseFloat(acceptReq.getAccept().getRequest().getProposalNumber());
            int numOfActiveServers = MitterServer.serversList.size();
            int numOfServersResponded = 0;
            int numOfVotes = 0;
            int majoritySize = numOfActiveServers/2;
            ServerPeers.ServerIdentity acceptor;
            // Keep looping until a majority of responses has been received
            while (/*numOfVotes < majoritySize &&*/ numOfServersResponded < numOfActiveServers) {
                int index = 0;
                while (index < numOfActiveServers) {
                    acceptor = MitterServer.serversList.get(index);
                    try {
                        Message response = MitterServer.readMessage(acceptor.getSocket());
                        if (response != null) {
                            if (response.getAccept() != null) { // Received response to accept request
                                float acceptResponseProposalNumber = Float.parseFloat(response.getAccept().getResponse().getAcceptorMinProposalNumber());
                                // Abandon proposal
                                if (Float.compare(acceptResponseProposalNumber, acceptRequestProposalNumber) > 0) {
                                    MitterServer.maxRound = Math.round(acceptResponseProposalNumber);
                                    MitterServer.prepared = false;
                                    return false;
                                }
                                numOfVotes += 1;
                                numOfServersResponded += 1;
                                // Send success message
                                int acceptorsFirstUnchosenIndex = response.getAccept().getResponse().getAcceptorsFirstUnchosenIndex();
                                if (acceptorsFirstUnchosenIndex <= MitterServer.lastLogIndex
                                    && Float.compare(MitterServer.log.get(acceptorsFirstUnchosenIndex).getAcceptedProposal(),Float.MAX_VALUE) >= 0) {
                                    sendSuccessRequest(acceptorsFirstUnchosenIndex, acceptor);
                                }
                            } else if (response.getSuccess() != null) { // Received response to success request
                                int acceptorsFirstUnchosenIndex = response.getSuccess().getResponse().getAcceptorsFirstUnchosenIndex();
                                if (acceptorsFirstUnchosenIndex < MitterServer.firstUnchosenIndex) {
                                    sendSuccessRequest(acceptorsFirstUnchosenIndex, acceptor);
                                }
                            }
                        }
                    } catch (IOException e) {   // A server has crashed or got disconnected
                        if (removeFromActiveServers(acceptor)) {
                            index -= 1;
                            numOfActiveServers = MitterServer.serversList.size();
                            majoritySize = numOfActiveServers/2;
                        }
                    } catch (JAXBException e) {
                        System.err.format("[ SERVER %d ] Error: Proposer, " + e.getMessage(), MitterServer.serverId);
                        e.printStackTrace();
                    }
                    index += 1;
                }
            }

            // Check if it has obtained majority of votes to the proposal then set the corresponding log 
            // entry
            if (numOfVotes >= majoritySize) {
                if (proposedIndex >= MitterServer.log.size()) {
                    MitterServer.increaseLogCapacity(proposedIndex+20);
                }
                LogEntry updatedEntry = MitterServer.log.get(proposedIndex);
                updatedEntry.setAcceptedProposal(Float.MAX_VALUE);
                updatedEntry.setAcceptedValue(acceptReq.getAccept().getRequest().getValue());
                MitterServer.log.set(proposedIndex, updatedEntry);
                MitterServer.updateLastLogIndex();
                System.out.println("LAST LOG INDEX: " + MitterServer.lastLogIndex);
                MitterServer.firstUnchosenIndex += 1;
                System.out.println("FIRST UNCHOSEN INDEX: " + MitterServer.firstUnchosenIndex);
            }
        }

        return true;
    }

    /**
     * This method sends a success request to an acceptor.
     * @param acceptorsFirstUnchosenIndex - The acceptor's first unchosen index
     * @param acceptor - The acceptor's identity(i.e. its server id and socket)
     */
    public void sendSuccessRequest(int acceptorsFirstUnchosenIndex, ServerPeers.ServerIdentity acceptor) {
        Message successReq = setupSuccessRequest(acceptorsFirstUnchosenIndex);
        try {
            sendRequest(successReq, acceptor.getSocket());    
        } catch (IOException e) { // A server has crashed or got disconnected
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
    public void broadcastRequest(Message value) {
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
            // int majoritySize = (numOfActiveServers/2) + 1;
            int majoritySize = numOfActiveServers;
            ServerPeers.ServerIdentity acceptor;
            // Keep looping until a majority of responses has been received
            while (receivedResponses.size() < majoritySize) {
                int index = 0;
                while (index < numOfActiveServers) {
                    acceptor = MitterServer.serversList.get(index);
                    try {
                        String response = receiveResponse(acceptor.getSocket());
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
        Message message = new Message();
        message.setAccept(null);
        message.setPrepare(null);
        message.setSuccess(new Message.Success());
        message.getSuccess().setRequest(new Message.Success.Request());
        message.getSuccess().getRequest().setIndex(acceptResponseFirstUnchosenIndex);
        message.getSuccess().getRequest().setValue(MitterServer.log.get(acceptResponseFirstUnchosenIndex).getAcceptedValue());

        return message;
    }

    /**
     * This method sets up the accept request to be sent to all acceptors.
     * @param value - A value, either the highest numbered one from a Prepare response, or if none, 
     *                then one from a client request
     * @return - The accept request
     */
    private Message setupAcceptRequest(NotificationInfo value) {
        Message acceptRequest = new Message();
        String mostRecentProposalNumber = String.valueOf(MitterServer.maxRound) + "." + MitterServer.serverId;
        acceptRequest.setPrepare(null);
        acceptRequest.setSuccess(null);
        acceptRequest.setAccept(new Message.Accept());
        acceptRequest.getAccept().setRequest(new Message.Accept.Request());
        acceptRequest.getAccept().getRequest().setIndex(proposedIndex);
        acceptRequest.getAccept().getRequest().setProposalNumber(mostRecentProposalNumber);
        acceptRequest.getAccept().getRequest().setValue(value);
        acceptRequest.getAccept().getRequest().setFirstUnchosenIndex(MitterServer.firstUnchosenIndex);
        MitterServer.maxRound += 1;

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
        prepareReq.setPrepare(new Message.Prepare());
        prepareReq.getPrepare().setResponse(null);
        prepareReq.getPrepare().setRequest(new Message.Prepare.Request());
        prepareReq.getPrepare().getRequest().setIndex(proposedIndex);
        // Proposal number would be of the format "[maxRound].[serverId]"
        prepareReq.getPrepare().getRequest().setProposalNumber(new String(String.valueOf(MitterServer.maxRound) + "." + MitterServer.serverId));

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

    public String receiveResponse(Socket s) throws IOException {
        BufferedReader buffReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        String line = null;

        if (buffReader.ready()) {
            line = buffReader.readLine();
        }

        return line;
    }

    /**
     * This removes the server from the list of active servers.
     * @param sId - The server to be removed
     * @return - True if the server has been successfully removed from the active list of servers
     */
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

    /**
     * This class constitutes if the proposal was successful and the accepted value.
     */
    private class Proposal {
        public boolean hasMajority;
        public NotificationInfo acceptedValue;

        // Constructor
        public Proposal() { 
            this.hasMajority = false;
            this.acceptedValue = null;
        }
    }
}