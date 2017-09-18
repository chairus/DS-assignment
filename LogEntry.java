package uni.mitter;

import generated.nonstandard.notification.Notification;

/**
 * This class represents a single entry in the replicated log.
 * @author cyrusvillacampa
 */

 public class LogEntry {
    private float acceptedProposal; // The accepted proposal number for a particular entry in the log
    private Notification acceptedValue; // The accepted value that is associated with a proposal number
    
    // Constructor
    public LogEntry() {

    }

    public void setAcceptedProposal(float proposalNumber) {
        this.acceptedProposal = proposalNumber;
    }

    public void setAcceptedValue(Notification notification) {
        this.acceptedValue = notification;
    }

    public float getAcceptedProposal() {
        return this.acceptedProposal;
    }

    public Notification getAcceptedValue() {
        return this.acceptedValue;
    }

 }