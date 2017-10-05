/**
 * This class is an implementation of the Acceptor role in the PAXOS algorithm. The Acceptor class receives
 * prepare request with a proposal number n from a Proposer and sends back a promise that states that it 
 * would not accept any proposal greater than n if n > m(the most recent proposal number the Acceptor has
 * accepted) together with the promise response is the proposal number and the associated value(Proposal[m,v]).
 * The Acceptor class also receives an accept request sends an acknowledgement to 
 * @author cyrusvillacampa
 */

 package uni.mitter;

import generated.nonstandard.notification.NotificationInfo;
import uni.mitter.MitterServer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;

import java.io.StringWriter;
import javax.xml.bind.JAXBException;

import generated.nonstandard.message.Message;

 public class Acceptor {
     // Constructor
     public Acceptor() {

     }

     /**
      * This method will invoke methods to read a request from the leader and respond to it with the 
      * appropriate message.
      */
     public void readValue() {
        Message request = readARequestFromLeader(null);
        respondToLeader(request);
     }

     public void respondToLeader(Message request) {
        if (request != null) {
            if (request.getPrepare() != null) {         // Prepare request
                System.out.println("RECEIVED PREPARE REQUEST");
                respondPrepareRequest(request);
            } else if (request.getAccept() != null) {   // Accept request
                System.out.println("RECEIVED ACCEPT REQUEST");
                respondAcceptRequest(request);
            } else if (request.getSuccess() != null) {  // Success request
                System.out.println("RECEIVED SUCCESS REQUEST");
                respondSuccessRequest(request);
            } else {                                    // Heartbeat message
                // System.out.println("RECEIVED HEARTBEAT MESSAGE");
                // if (request.getHeartbeat().getServerId() == MitterServer.currentLeader.getId()) {
                    // System.out.println("RECEIVED UPDATE FOR ACTIVE SERVERS");
                    updateActiveServersList(request.getHeartbeat().getActiveServers());
                // }
            }
        } else {    // Set the currentLeader variable to null to initiate re-election
            System.err.println("==========================OH NO!!=====================");
            System.err.printf("[ SERVER %d ] The leader(SERVER %d) has crashed or got disconnected.\n", MitterServer.serverId, MitterServer.currentLeader.getId());
            try {
                if (MitterServer.currentLeader != null) {
                    MitterServer.currentLeader.getSocket().close();
                    removeFromActiveServers(MitterServer.currentLeader);
                    System.out.printf("[ SERVER %d ] Closed leader socket.\n", MitterServer.serverId);
                }
            } catch (IOException ex) {
                // IGNORE
            }
            MitterServer.currentLeader = null;
            MitterServer.changeInLeader = true;
        }
     }

     /**
      * Thie method will read a request(Prepare/Accept/Success) sent by the Leader.
      * @param  response - The response that was sent for the Leader's request
      * @return          - The unmarshalled response from the leader
      */
     public Message readARequestFromLeader(Message response) {
        Message request = null;

        try {
            if (MitterServer.currentLeader != null) {
                // System.out.println("========THE LEADER IS NOT NULL!!!========");
                request = MitterServer.readMessage(MitterServer.currentLeader.getSocket(), 2000);
                // System.out.println("REQUEST FROM readARequestFromLeader: " + request);
            }
        } catch (IOException e) {           // The leader has crashed or got disconnected
            System.err.printf("[ SERVER %d ] The leader(SERVE %d) has crashed or got disconnected.\n", MitterServer.serverId, MitterServer.currentLeader.getId());
        } catch (JAXBException e) {         // There was a problem in the received XML message, therefore resend the response to the leader
            System.err.printf("[ SERVER %d ] Error: Acceptor, " + e.getMessage() + "\n", MitterServer.serverId);
            e.printStackTrace();
            if (response != null) {
                sendRequestResponse(response);
            }
        }

        return request;
     }

    /**
      * This method responds to the prepare request with a status of "true" if the proposal number
      * associated with the prepare request is greater than what this acceptor has seen so far, else
      * it will respond with a status of "false" indicating that this acceptor has seen larger proposal
      * number. This method will also attach in the response the accepted proposal and value if there
      * are any.
      * @param request - The prepare request
      */
    public void respondPrepareRequest(Message request) {
        float prepareRequestProposalNumber = Float.parseFloat(request.getPrepare().getRequest().getProposalNumber());
        int requestIndex = request.getPrepare().getRequest().getIndex();
        boolean status = false;
        NotificationInfo acceptedValue = null;
        float acceptedProposal = -1.0f;
        boolean noMoreAccepted = false;

        // Check if the current proposal has larger number than the previous seen proposal. If it is
        // then assign it to minProposal and accept the prepare request by setting the status field to
        // true.
        if (Float.compare(prepareRequestProposalNumber, MitterServer.minProposal) >= 0) {
            MitterServer.minProposal = prepareRequestProposalNumber;
            MitterServer.maxRound = (new Float(MitterServer.minProposal)).longValue();
            status = true;
        }
        // System.out.println("prepareRequestProposalNumber: " + prepareRequestProposalNumber);
        // System.out.println("MitterServer.minProposal: " + MitterServer.minProposal);
        
        if (requestIndex > MitterServer.log.size()-1) {
            MitterServer.increaseLogCapacity(requestIndex+20);
        }

        acceptedValue = MitterServer.log.get(requestIndex).getAcceptedValue();
        acceptedProposal = MitterServer.log.get(requestIndex).getAcceptedProposal();

        // Check if there are more unchosen log entries aside from the firstUnchosenIndex. If there are
        // and is less than the index in the prepare request then set the noMoreAccepted field of the
        // response 
        MitterServer.updateLastLogIndex();
        int maxChosenIndex = MitterServer.lastLogIndex;
        if (maxChosenIndex < requestIndex) {
            noMoreAccepted = true;
        }

        Message response = setupPrepareResponse(status, acceptedValue, acceptedProposal, noMoreAccepted);
        if (!sendRequestResponse(response)) {
            return;
        }
        
        // Listen for request from leader
        Message receivedReq = readARequestFromLeader(response);
        respondToLeader(receivedReq);
    }

    /**
     * This method sends a response(prepare/accept/success) to the proposer/leader
     * @param response - The response to be sent to the proposer or leader
     * @return         - True if it has successfully sent the response to the leader, False otherwise
     */
    public boolean sendRequestResponse(Message response) {
        // There is no leader and therefore elect one.
        if (MitterServer.currentLeader == null) {
            return false;
        }

        boolean retry;
        do {
            retry = false;
            try {
                BufferedWriter buffWriter = new BufferedWriter(new OutputStreamWriter(MitterServer.currentLeader.getSocket().getOutputStream()));
                StringWriter sWriter = new StringWriter();
                MitterServer.jaxbMarshallerMessage.marshal(response, sWriter);
                buffWriter.write(sWriter.toString());
                buffWriter.newLine();
                buffWriter.flush();
            } catch (IOException e) {
                // Leader is disconnected or has crashed and so elect a new leader
                try {
                    MitterServer.currentLeader.getSocket().close();
                    removeFromActiveServers(MitterServer.currentLeader);
                    System.out.printf("[ SERVER %d ] Closed leader socket.", MitterServer.serverId);
                } catch (IOException ex) {
                    System.err.printf("[ SERVER %d ] Error: Acceptor, " + ex.getMessage() + "\n", MitterServer.serverId);
                    ex.printStackTrace();
                }
                MitterServer.currentLeader = null;
                return false;
            } catch (JAXBException e) {     // If there was something wrong with the XML object(i.e. it got corrupted) resend the response
                System.err.printf("[ SERVER %d ] Error: Acceptor, " + e.getMessage() + "\n", MitterServer.serverId);
                e.printStackTrace();
                retry = true;
            }
        } while (retry);

        return true;
    }

    /**
     * This method finds the maximum unchosen index, that is the largest entry in the log that has not yet
     * been chosen.
     * @return - The maximum unchosen index
     */
    public int findMaxUnchosenIndex() {
        int maxUnchosenIndex = MitterServer.firstUnchosenIndex;
        int index = maxUnchosenIndex + 1;
        while (index < MitterServer.log.size()) {
            LogEntry entry = MitterServer.log.get(index);
            if (Float.compare(entry.getAcceptedProposal(), Float.MAX_VALUE) < 0) {
                maxUnchosenIndex = index;
            }
            index += 1;
        }
        return maxUnchosenIndex;
    }


     /**
      * This method updates the state of the acceptor with the received accept request and responds back to 
      * the proposer/leader.
      * @param request - The accept request
      */
    public void respondAcceptRequest(Message request) {
        float requestProposalNumber = Float.parseFloat(request.getAccept().getRequest().getProposalNumber());
        int requestIndex = request.getAccept().getRequest().getIndex();
        NotificationInfo requestValue = request.getAccept().getRequest().getValue();
        int requestFirstUnchosenIndex = request.getAccept().getRequest().getFirstUnchosenIndex();
        if (Float.compare(requestProposalNumber, MitterServer.minProposal) >= 0) {
            MitterServer.maxRound = (new Float(requestProposalNumber)).longValue() + 1;
            if (requestIndex > MitterServer.log.size()-1) {
                MitterServer.increaseLogCapacity(requestIndex+10);
            }

            MitterServer.log.set(requestIndex, new LogEntry(requestProposalNumber,requestValue));
            MitterServer.minProposal = requestProposalNumber;

            // Mark entries that has an index less than the firstUnchosenIndex of the leader/proposer
            // as chosen.
            int index = 0;
            while (index < requestFirstUnchosenIndex) {
                LogEntry entry = MitterServer.log.get(index);
                if (Float.compare(requestProposalNumber, entry.getAcceptedProposal()) == 0) {
                    entry.setAcceptedProposal(Float.MAX_VALUE);
                    MitterServer.log.set(index,entry);
                }
                index += 1;
            }
        }
        Message acceptResponse = setupAcceptResponse();
        if (!sendRequestResponse(acceptResponse)) {
            return;
        }
        System.out.println("SENT RESPONSE TO ACCEPT REQUEST(firstUnchosenIndex): " + acceptResponse.getAccept().getResponse().getAcceptorsFirstUnchosenIndex());
        // Read another request from the leader/proposer
        Message req = readARequestFromLeader(acceptResponse);
        respondToLeader(req);
    }

    /**
     * This method responds to the success request of a proposer/leader.
     * @param request - The success request
     */
    public void respondSuccessRequest(Message request) {
        int proposersFirstUnchosenIndex = request.getSuccess().getRequest().getIndex();
        System.out.println("PROPOSERS FIRST UNCHOSEN INDEX: " + proposersFirstUnchosenIndex);
        System.out.println("ACCEPTORS FIRST UNCHOSEN INDEX: " + MitterServer.firstUnchosenIndex);
        if (proposersFirstUnchosenIndex > -1) {
            updateLog(request);
            Message successReq = setupSuccessRequest();
            if (!sendRequestResponse(successReq)) {
                return;
            }
            System.out.println("SENT RESPONSE TO SUCCESS REQUEST(firstUnchosenIndex): " + MitterServer.firstUnchosenIndex);
            Message receivedRequest = readARequestFromLeader(successReq);
            respondToLeader(receivedRequest);
        }
    }

    /**
     * This method creates a response to the prepare request.
     * @param status - Indicates if the acceptor accepts/rejects the prepare request
     * @param acceptedValue
     * @param requestProposalNumber
     * @param noMoreAccepted
     * @return The response to the prepare request
     */
    public Message setupPrepareResponse(boolean status, 
                                        NotificationInfo acceptedValue, 
                                        float acceptedProposal,
                                        boolean noMoreAccepted) {
        Message prepareResponse = new Message();
        prepareResponse.setAccept(null);
        prepareResponse.setSuccess(null);
        prepareResponse.setHeartbeat(null);
        prepareResponse.setPrepare(new Message.Prepare());
        prepareResponse.getPrepare().setRequest(null);
        prepareResponse.getPrepare().setResponse(new Message.Prepare.Response());

        prepareResponse.getPrepare().getResponse().setStatus(status);
        prepareResponse.getPrepare().getResponse().setAcceptedProposal(String.valueOf(acceptedProposal));
        prepareResponse.getPrepare().getResponse().setAcceptedValue(acceptedValue);
        prepareResponse.getPrepare().getResponse().setNoMoreAccepted(noMoreAccepted);

        return prepareResponse;
    }

    public Message setupAcceptResponse() {
        Message acceptRes = new Message();
        acceptRes.setPrepare(null);
        acceptRes.setSuccess(null);
        acceptRes.setHeartbeat(null);
        acceptRes.setAccept(new Message.Accept());
        acceptRes.getAccept().setRequest(null);
        acceptRes.getAccept().setResponse(new Message.Accept.Response());
        acceptRes.getAccept().getResponse().setAcceptorMinProposalNumber(String.valueOf(MitterServer.minProposal));
        acceptRes.getAccept().getResponse().setAcceptorsFirstUnchosenIndex(MitterServer.firstUnchosenIndex);

        return acceptRes;
    }

    public Message setupSuccessRequest() {
        Message successReq = new Message();
        successReq.setAccept(null);
        successReq.setPrepare(null);
        successReq.setHeartbeat(null);
        successReq.setSuccess(new Message.Success());
        successReq.getSuccess().setResponse(new Message.Success.Response());
        successReq.getSuccess().getResponse().setAcceptorsFirstUnchosenIndex(MitterServer.firstUnchosenIndex);
        return successReq;
    }

    /**
     * Marks a particular entry in the log as chosen and stores the chosen value in that log entry.
     * @param successRequest - The message containing the proposed index, chosen value and proposal number
     */
    public void updateLog(Message successRequest) {
        int successRequestIndex = successRequest.getSuccess().getRequest().getIndex();
        NotificationInfo successRequestValue = successRequest.getSuccess().getRequest().getValue();

        if (successRequestIndex > MitterServer.log.size()) {
            MitterServer.increaseLogCapacity(successRequestIndex+10);
        }

        LogEntry updatedEntry = MitterServer.log.get(successRequestIndex);
        // Check if this entry has already been chosen, if it is then don't update that entry, else
        // update it with the chosen value.
        if (Float.compare(updatedEntry.getAcceptedProposal(), Float.MAX_VALUE) < 0) {
            updatedEntry.setAcceptedProposal(Float.MAX_VALUE);
            updatedEntry.setAcceptedValue(successRequestValue);
            MitterServer.log.set(successRequestIndex, updatedEntry);
            // MitterServer.firstUnchosenIndex += 1;
        }
        MitterServer.firstUnchosenIndex = MitterServer.findFirstUnchosenIndex();

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
            // System.exit(1);
        }
        
        synchronized (MitterServer.serversList) {
            return MitterServer.serversList.remove(sId);
        }
    }

    /**
     * Updates the active servers list with the given string of active server ids by the leader
     * @param activeServer - A string of active server ids
     */
    public void updateActiveServersList(String activeServers) {
        if (activeServers == null) {
            return;
        }
        System.out.println("ACTIVE SERVERS: " + activeServers);
        String[] activeServerIds = activeServers.trim().split("\\s++");
        synchronized (MitterServer.serversList) {
            int index = 0;
            while (index < MitterServer.serversList.size()) {
                boolean found = false;
                ServerPeers.ServerIdentity sId = MitterServer.serversList.get(index);
                for (String serverId: activeServerIds) {
                    if (Integer.parseInt(serverId) == sId.getId()) {
                        found = true;
                    }
                }
                if (!found) {
                    MitterServer.serversList.remove(sId);
                    index -= 1;
                }
                index += 1;
            }
        }
    }
 }