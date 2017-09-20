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

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;

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
        float requestProposalNumber = Float.parseFloat(request.getPrepare().getRequest().getProposalNumber());

        // Check if the current proposal has larger number than the previous seen proposal. If it is
        // then assign it to minProposal.
        if (Float.compare(requestProposalNumber, MitterServer.minProposal) > 0) {
            MitterServer.minProposal = requestProposalNumber;
        }

        int requestIndex = request.getPrepare().getRequest().getIndex();
        if (MitterServer.log.size() > requestIndex) {
            
        }

        
    }

    public Message setupPrepareResponse(boolean status, NotificationInfo acceptedValue, float requestProposalNumber) {
        Message prepareResponse = new Message();
        prepareResponse.setAccept(null);
        prepareResponse.setSuccess(null);
        prepareResponse.setPrepare(new Message.Prepare());
        prepareResponse.getPrepare().setRequest(null);
        prepareResponse.getPrepare().setResponse(new Message.Prepare.Response());

        prepareResponse.getPrepare().getResponse().setStatus(status);

        return prepareResponse;
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