/**
 * This class is an implementation of the Acceptor role in the PAXOS algorithm. The Acceptor class receives
 * prepare request with a proposal number n from a Proposer and sends back a promise that states that it 
 * would not accept any proposal greater than n if n > m(the most recent proposal number the Acceptor has
 * accepted) together with the promise response is the proposal number and the associated value(Proposal[m,v]).
 * The Acceptor class also receives an accept request sends an acknowledgement to 
 * @author cyrusvillacampa
 */

 package uni.mitter;

 public class Acceptor {
     // Constructor
     public Acceptor() {

     }

     /**
      * This method responds to the prepare request it has received if the maximum proposal number it has
      * received from a previous prepare request is less than the currently received prepare request.
      */
     public void responsePrepareRequest() {

     }

     /**
      * This method responds to the accept request of a proposer with the acceptedProposal(i.e. the
      * highest proposal number).
      */
    public void responseAcceptRequest() {

    }
 }