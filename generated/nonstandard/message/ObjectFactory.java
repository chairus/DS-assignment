//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.09.22 at 04:02:49 PM ACST 
//


package generated.nonstandard.message;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the generated.nonstandard.message package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: generated.nonstandard.message
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Message }
     * 
     */
    public Message createMessage() {
        return new Message();
    }

    /**
     * Create an instance of {@link Message.Success }
     * 
     */
    public Message.Success createMessageSuccess() {
        return new Message.Success();
    }

    /**
     * Create an instance of {@link Message.Accept }
     * 
     */
    public Message.Accept createMessageAccept() {
        return new Message.Accept();
    }

    /**
     * Create an instance of {@link Message.Prepare }
     * 
     */
    public Message.Prepare createMessagePrepare() {
        return new Message.Prepare();
    }

    /**
     * Create an instance of {@link Message.Success.Request }
     * 
     */
    public Message.Success.Request createMessageSuccessRequest() {
        return new Message.Success.Request();
    }

    /**
     * Create an instance of {@link Message.Success.Response }
     * 
     */
    public Message.Success.Response createMessageSuccessResponse() {
        return new Message.Success.Response();
    }

    /**
     * Create an instance of {@link Message.Accept.Request }
     * 
     */
    public Message.Accept.Request createMessageAcceptRequest() {
        return new Message.Accept.Request();
    }

    /**
     * Create an instance of {@link Message.Accept.Response }
     * 
     */
    public Message.Accept.Response createMessageAcceptResponse() {
        return new Message.Accept.Response();
    }

    /**
     * Create an instance of {@link Message.Prepare.Request }
     * 
     */
    public Message.Prepare.Request createMessagePrepareRequest() {
        return new Message.Prepare.Request();
    }

    /**
     * Create an instance of {@link Message.Prepare.Response }
     * 
     */
    public Message.Prepare.Response createMessagePrepareResponse() {
        return new Message.Prepare.Response();
    }

}
