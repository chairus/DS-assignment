package uni.mitter;

import generated.nonstandard.notification.NotificationInfo;

/**
 * This class represents a single entry in the replicated log.
 * @author cyrusvillacampa
 */

 public class LogEntry {
    private float acceptedProposal; // The accepted proposal number for a particular entry in the log
    private NotificationInfo acceptedValue; // The accepted value that is associated with a proposal number
    
    // Constructor
    public LogEntry() {
        this.acceptedProposal = -1.0f;
        this.acceptedValue = null;
    }

    public LogEntry(float acceptedProposal, NotificationInfo acceptedValue) {
        this.acceptedProposal = acceptedProposal;
        this.acceptedValue = acceptedValue;
    }

    public void setAcceptedProposal(float proposalNumber) {
        this.acceptedProposal = proposalNumber;
    }

    public void setAcceptedValue(NotificationInfo notification) {
        this.acceptedValue = notification;
    }

    public float getAcceptedProposal() {
        return this.acceptedProposal;
    }

    public NotificationInfo getAcceptedValue() {
        return this.acceptedValue;
    }

 }