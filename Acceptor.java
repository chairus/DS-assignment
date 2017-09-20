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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;

import java.io.StringWriter;
import javax.xml.bind.JAXBException;

import generated.nonstandard.message.Message;

 public class Acceptor {
     // Constructor
     public Acceptor() {

     }

     /**
     * This method will perform
     */
     public void readValue() {
        Message request = readARequestFromLeader();
        if (request.getPrepare() != null) {         // Prepare request
            responsePrepareRequest(request);
        } else if (request.getAccept() != null) {   // Accept request
            responseAcceptRequest(request);
        } else {                                    // Success request
            responseSuccessRequest(request);
        }
     }

     /**
      * This method will read a Prepare/Accept request sent by the Leader.
      * @return - The Prepare/Accept message request in String format
      */
     public Message readARequestFromLeader() {
        Message request = null;
        try {
            BufferedReader buffReader = new BufferedReader(new InputStreamReader(MitterServer.currentLeader.getSocket().getInputStream()));
            while (request == null) {
                if (buffReader.ready()) {
                    StringReader sReader = new StringReader(buffReader.readLine());
                    Message res = (Message) MitterServer.jaxbUnmarshallerMessage.unmarshal(sReader);
                    request = res;
                }
            }    
        } catch (IOException e) {
            // The leader has failed/disconnected?
            System.err.format("[ SERVER %d ] Error: Acceptor, " + e.getMessage(), MitterServer.serverId);
            e.printStackTrace();
            System.exit(1);
        } catch (JAXBException e) {
            System.err.format("[ SERVER %d ] Error: Acceptor, " + e.getMessage(), MitterServer.serverId);
            e.printStackTrace();
            System.exit(1);
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
    public void responsePrepareRequest(Message request) {
        float prepareRequestProposalNumber = Float.parseFloat(request.getPrepare().getRequest().getProposalNumber());
        boolean status = false;
        NotificationInfo acceptedValue = null;
        float acceptedProposal = -1.0f;
        boolean noMoreAccepted = false;

        // Check if the current proposal has larger number than the previous seen proposal. If it is
        // then assign it to minProposal and accept the prepare request by setting the status field to
        // true.
        if (Float.compare(prepareRequestProposalNumber, MitterServer.minProposal) > 0) {
            MitterServer.minProposal = prepareRequestProposalNumber;
            status = true;
        }

        int requestIndex = request.getPrepare().getRequest().getIndex();
        if (requestIndex <= MitterServer.log.size()-1) {
            acceptedValue = MitterServer.log.get(requestIndex).getAcceptedValue();
            acceptedProposal = MitterServer.log.get(requestIndex).getAcceptedProposal();

            // Check if there are more unchosen log entries aside from the firstUnchosenIndex. If there are
            // and is less than the index in the prepare request then set the noMoreAccepted field of the
            // response 
            int maxUnchosenIndex = findMaxUnchosenIndex();
            if (maxUnchosenIndex < requestIndex) {
                noMoreAccepted = true;
            }
        }

        Message response = setupPrepareResponse(status, acceptedValue, acceptedProposal, noMoreAccepted);
        sendPrepareRequestResponse(response);
    }

    /**
     * This method sends the response of the prepare request to the proposer/leader
     * @param response - The response to be sent to the proposer or leader
     */
    public void sendPrepareRequestResponse(Message response) {
        // There is no leader and therefore elect one.
        if (MitterServer.currentLeader == null) {
            return;
        }

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
                System.out.printf("[ SERVER %d ] Closed leader socket.", MitterServer.serverId);
            } catch (IOException ex) {
                System.err.format("[ SERVER %d ] Error: Proposer, " + ex.getMessage(), MitterServer.serverId);
                ex.printStackTrace();
            }
            MitterServer.currentLeader = null;
            System.err.format("[ SERVER %d ] Error: Proposer, " + e.getMessage(), MitterServer.serverId);
            e.printStackTrace();
        } catch (JAXBException e) {
            System.err.format("[ SERVER %d ] Error: Proposer, " + e.getMessage(), MitterServer.serverId);
            e.printStackTrace();
            System.exit(1);
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
        prepareResponse.setPrepare(new Message.Prepare());
        prepareResponse.getPrepare().setRequest(null);
        prepareResponse.getPrepare().setResponse(new Message.Prepare.Response());

        prepareResponse.getPrepare().getResponse().setStatus(status);
        prepareResponse.getPrepare().getResponse().setAcceptedProposal(String.valueOf(acceptedProposal));
        prepareResponse.getPrepare().getResponse().setAcceptedValue(acceptedValue);
        prepareResponse.getPrepare().getResponse().setNoMoreAccepted(noMoreAccepted);
        
        return prepareResponse;
    }

    /**
     * This method finds the maximum unchosen index, that is the entry in the log that has not yet been
     * chosen.
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
      * This method responds to the accept request of a proposer with the acceptedProposal(i.e. the
      * highest proposal number).
      * @param request - The accept request
      */
    public void responseAcceptRequest(Message request) {

    }

    /**
     * This method response to the success request of a proposer/leader.
     * @param request - The success request
     */
    public void responseSuccessRequest(Message request) {

    }
 }