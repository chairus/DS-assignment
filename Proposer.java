/**
 * This class is an implementation of the Proposer role in the PAXOS algorithm. The Proposer class
 * sends a prepare request to all Acceptors with a proposal number attached to it. It then waits for the
 * response of the Acceptors, if this Proposer has received the majority of response from the Acceptors,
 * then it sends an accept request together with the value of the highest proposal number or if none then 
 * this Proposer picks the value to be accepted.
 * @author cyrusvillacampa
 */

 public class Proposer {
     // Constructor
     public Proposer() {

     }

     /**
      * This method sends a prepare request to all Acceptors and in that request is a proposal number,
      * which is composed of the sender's server id and the round number.
      */
     public void prepareRequest() {
        
     }

     /**
      * This method sends an accept request to all Acceptors and in that request is a proposal number
      * and a value, which is either picked by the Proposer or a value that has already been accepted
      * by the majority of the Acceptors
      */
     public void accepRequest() {

     }
 }