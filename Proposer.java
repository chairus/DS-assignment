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
import java.util.concurrent.TimeUnit;
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
        if (value == null) {    // Listen for responses from accept and success messages
            successRequest();
            return true;
        }

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
        System.out.println("SENT PREPARE REQUESTS.");
        result = collectPrepareResponses();
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
            int majoritySize = (int)Math.ceil(((double)numOfActiveServers)/2);
            // System.out.println("MAJORITY SIZE: " + majoritySize);
            ServerPeers.ServerIdentity acceptor;
            // Keep looping until a majority of responses/votes has been received
            while (numOfVotes < majoritySize && numOfServersResponded < numOfActiveServers) {
                // System.out.println("CURRENT NUMBER OF VOTES: " + numOfVotes);
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
        System.out.println("result.hasMajority: " + result.hasMajority);
        System.out.println("result.acceptedValue: " + result.acceptedValue);
        return result;
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
            while (numOfVotes < majoritySize && numOfServersResponded < numOfActiveServers) {
                int index = 0;
                while (index < numOfActiveServers) {
                    acceptor = MitterServer.serversList.get(index);
                    try {
                        Message response = MitterServer.readMessage(acceptor.getSocket());
                        if (response != null) {
                            if (response.getAccept() != null) { // Received response to accept request
                                float acceptResponseProposalNumber = Float.parseFloat(response.getAccept().getResponse().getAcceptorMinProposalNumber());
                                int acceptorsFirstUnchosenIndex = response.getAccept().getResponse().getAcceptorsFirstUnchosenIndex();
                                // Abandon proposal
                                if (Float.compare(acceptResponseProposalNumber, acceptRequestProposalNumber) > 0) {
                                    MitterServer.maxRound = Math.round(acceptResponseProposalNumber);
                                    MitterServer.prepared = false;
                                    return false;
                                }
                                
                                numOfVotes += 1;
                                numOfServersResponded += 1;

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
     * This method listens for responses from other servers and reply back to them
     */
    public void successRequest() {
        System.out.println("Listening for responses from accept and success requests...");
        int numOfReplicatedServers = 0;
        int notificationListSize = 0;
        while (notificationListSize == 0) {
            synchronized (MitterServer.serversList) {
                int numOfActiveServers = MitterServer.serversList.size();
                ServerPeers.ServerIdentity acceptor;
                int index = 0;
                while (index < numOfActiveServers) {
                    acceptor = MitterServer.serversList.get(index);
                    try {
                        Message response = MitterServer.readMessage(acceptor.getSocket());
                        if (response != null) {
                            if (response.getSuccess() != null) { // Received response from success request
                                int acceptorsFirstUnchosenIndex = response.getSuccess().getResponse().getAcceptorsFirstUnchosenIndex();
                                if (acceptorsFirstUnchosenIndex < MitterServer.firstUnchosenIndex) {
                                    sendSuccessRequest(acceptorsFirstUnchosenIndex, acceptor);
                                } else {
                                    sendSuccessRequest(-1, acceptor);
                                    numOfReplicatedServers += 1;
                                }
                            } else if (response.getAccept() != null) { // Received response from accept request
                                int acceptorsFirstUnchosenIndex = response.getAccept().getResponse().getAcceptorsFirstUnchosenIndex();
                                if (acceptorsFirstUnchosenIndex <= MitterServer.lastLogIndex
                                    && Float.compare(MitterServer.log.get(acceptorsFirstUnchosenIndex).getAcceptedProposal(),Float.MAX_VALUE) >= 0) {
                                    sendSuccessRequest(acceptorsFirstUnchosenIndex, acceptor);
                                }
                            } else if (response.getHeartbeat() != null) { // Received heartbeat message
                                MitterServer.sendHeartbeatMessage(acceptor.getSocket());
                            } else {
                                System.err.println(response);
                            }
                        } else { // Send success request to all acceptors for full replication
                            if (numOfReplicatedServers < numOfActiveServers
                                && MitterServer.lastLogIndex >= 0) {
                                sendSuccessRequest(MitterServer.firstUnchosenIndex-1, acceptor);
                            } else { // Send heartbeat message to each acceptors to notify them that this server/leader is still alive    
                                MitterServer.sendHeartbeatMessage(acceptor.getSocket());
                            }
                        }
                    } catch (IOException e) { // A server has crashed or got disconnected
                        if (removeFromActiveServers(acceptor)) {
                            index -= 1;
                            numOfActiveServers = MitterServer.serversList.size();
                        }
                    } catch (JAXBException e) {
                        System.err.format("[ SERVER %d ] Error: Proposer, " + e.getMessage(), MitterServer.serverId);
                        e.printStackTrace();
                    }
                    index += 1;
                }
            }
            try {
                TimeUnit.MILLISECONDS.sleep(300);        // To control the access times of this thread on the list of notitifcations(i.e. notificationList)
            } catch (Exception e) {
                // Ignore
            }
            MitterServer.notificationListLock.lock();    // Obtain the lock for the notification list
            notificationListSize = MitterServer.notificationList.size();
            MitterServer.notificationListLock.unlock();  // Release lock for notification list
            // System.out.println("Exited loop in success request.");
        }
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
     * This method sends request(Prepare/Accept/Success) to all acceptors.
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

    private Message setupSuccessRequest(int acceptResponseFirstUnchosenIndex) {
        Message message = new Message();
        message.setAccept(null);
        message.setPrepare(null);
        message.setSuccess(new Message.Success());
        message.getSuccess().setRequest(new Message.Success.Request());
        message.getSuccess().getRequest().setIndex(acceptResponseFirstUnchosenIndex);
        if (acceptResponseFirstUnchosenIndex > -1) {
            message.getSuccess().getRequest().setValue(MitterServer.log.get(acceptResponseFirstUnchosenIndex).getAcceptedValue());
        }
        
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
        
        synchronized (MitterServer.serversList) {
            return MitterServer.serversList.remove(sId);
        }
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