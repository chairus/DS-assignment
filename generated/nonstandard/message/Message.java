//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.10.09 at 10:28:21 PM ACDT 
//


package generated.nonstandard.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import generated.nonstandard.notification.NotificationInfo;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="serverId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="prepare">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="request">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="proposalNumber" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="index" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="response">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="acceptedProposal" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="acceptedValue" type="{urn:generated:nonstandard:notification}notificationInfo"/>
 *                             &lt;element name="noMoreAccepted" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *                             &lt;element name="status" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="accept">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="request">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="proposalNumber" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="index" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                             &lt;element name="value" type="{urn:generated:nonstandard:notification}notificationInfo"/>
 *                             &lt;element name="firstUnchosenIndex" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="response">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="acceptorMinProposalNumber" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="acceptorsFirstUnchosenIndex" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="success">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="request">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="index" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                             &lt;element name="value" type="{urn:generated:nonstandard:notification}notificationInfo"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="response">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="acceptorsFirstUnchosenIndex" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="heartbeat">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="serverId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                   &lt;element name="leaderId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                   &lt;element name="activeServers" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "serverId",
    "prepare",
    "accept",
    "success",
    "heartbeat"
})
@XmlRootElement(name = "message")
public class Message {

    protected int serverId;
    @XmlElement(required = true)
    protected Message.Prepare prepare;
    @XmlElement(required = true)
    protected Message.Accept accept;
    @XmlElement(required = true)
    protected Message.Success success;
    @XmlElement(required = true)
    protected Message.Heartbeat heartbeat;

    /**
     * Gets the value of the serverId property.
     * 
     */
    public int getServerId() {
        return serverId;
    }

    /**
     * Sets the value of the serverId property.
     * 
     */
    public void setServerId(int value) {
        this.serverId = value;
    }

    /**
     * Gets the value of the prepare property.
     * 
     * @return
     *     possible object is
     *     {@link Message.Prepare }
     *     
     */
    public Message.Prepare getPrepare() {
        return prepare;
    }

    /**
     * Sets the value of the prepare property.
     * 
     * @param value
     *     allowed object is
     *     {@link Message.Prepare }
     *     
     */
    public void setPrepare(Message.Prepare value) {
        this.prepare = value;
    }

    /**
     * Gets the value of the accept property.
     * 
     * @return
     *     possible object is
     *     {@link Message.Accept }
     *     
     */
    public Message.Accept getAccept() {
        return accept;
    }

    /**
     * Sets the value of the accept property.
     * 
     * @param value
     *     allowed object is
     *     {@link Message.Accept }
     *     
     */
    public void setAccept(Message.Accept value) {
        this.accept = value;
    }

    /**
     * Gets the value of the success property.
     * 
     * @return
     *     possible object is
     *     {@link Message.Success }
     *     
     */
    public Message.Success getSuccess() {
        return success;
    }

    /**
     * Sets the value of the success property.
     * 
     * @param value
     *     allowed object is
     *     {@link Message.Success }
     *     
     */
    public void setSuccess(Message.Success value) {
        this.success = value;
    }

    /**
     * Gets the value of the heartbeat property.
     * 
     * @return
     *     possible object is
     *     {@link Message.Heartbeat }
     *     
     */
    public Message.Heartbeat getHeartbeat() {
        return heartbeat;
    }

    /**
     * Sets the value of the heartbeat property.
     * 
     * @param value
     *     allowed object is
     *     {@link Message.Heartbeat }
     *     
     */
    public void setHeartbeat(Message.Heartbeat value) {
        this.heartbeat = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="request">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="proposalNumber" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="index" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *                   &lt;element name="value" type="{urn:generated:nonstandard:notification}notificationInfo"/>
     *                   &lt;element name="firstUnchosenIndex" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="response">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="acceptorMinProposalNumber" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="acceptorsFirstUnchosenIndex" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "request",
        "response"
    })
    public static class Accept {

        @XmlElement(required = true)
        protected Message.Accept.Request request;
        @XmlElement(required = true)
        protected Message.Accept.Response response;

        /**
         * Gets the value of the request property.
         * 
         * @return
         *     possible object is
         *     {@link Message.Accept.Request }
         *     
         */
        public Message.Accept.Request getRequest() {
            return request;
        }

        /**
         * Sets the value of the request property.
         * 
         * @param value
         *     allowed object is
         *     {@link Message.Accept.Request }
         *     
         */
        public void setRequest(Message.Accept.Request value) {
            this.request = value;
        }

        /**
         * Gets the value of the response property.
         * 
         * @return
         *     possible object is
         *     {@link Message.Accept.Response }
         *     
         */
        public Message.Accept.Response getResponse() {
            return response;
        }

        /**
         * Sets the value of the response property.
         * 
         * @param value
         *     allowed object is
         *     {@link Message.Accept.Response }
         *     
         */
        public void setResponse(Message.Accept.Response value) {
            this.response = value;
        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence>
         *         &lt;element name="proposalNumber" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="index" type="{http://www.w3.org/2001/XMLSchema}int"/>
         *         &lt;element name="value" type="{urn:generated:nonstandard:notification}notificationInfo"/>
         *         &lt;element name="firstUnchosenIndex" type="{http://www.w3.org/2001/XMLSchema}int"/>
         *       &lt;/sequence>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "proposalNumber",
            "index",
            "value",
            "firstUnchosenIndex"
        })
        public static class Request {

            @XmlElement(required = true)
            protected String proposalNumber;
            protected int index;
            @XmlElement(required = true)
            protected NotificationInfo value;
            protected int firstUnchosenIndex;

            /**
             * Gets the value of the proposalNumber property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getProposalNumber() {
                return proposalNumber;
            }

            /**
             * Sets the value of the proposalNumber property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setProposalNumber(String value) {
                this.proposalNumber = value;
            }

            /**
             * Gets the value of the index property.
             * 
             */
            public int getIndex() {
                return index;
            }

            /**
             * Sets the value of the index property.
             * 
             */
            public void setIndex(int value) {
                this.index = value;
            }

            /**
             * Gets the value of the value property.
             * 
             * @return
             *     possible object is
             *     {@link NotificationInfo }
             *     
             */
            public NotificationInfo getValue() {
                return value;
            }

            /**
             * Sets the value of the value property.
             * 
             * @param value
             *     allowed object is
             *     {@link NotificationInfo }
             *     
             */
            public void setValue(NotificationInfo value) {
                this.value = value;
            }

            /**
             * Gets the value of the firstUnchosenIndex property.
             * 
             */
            public int getFirstUnchosenIndex() {
                return firstUnchosenIndex;
            }

            /**
             * Sets the value of the firstUnchosenIndex property.
             * 
             */
            public void setFirstUnchosenIndex(int value) {
                this.firstUnchosenIndex = value;
            }

        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence>
         *         &lt;element name="acceptorMinProposalNumber" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="acceptorsFirstUnchosenIndex" type="{http://www.w3.org/2001/XMLSchema}int"/>
         *       &lt;/sequence>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "acceptorMinProposalNumber",
            "acceptorsFirstUnchosenIndex"
        })
        public static class Response {

            @XmlElement(required = true)
            protected String acceptorMinProposalNumber;
            protected int acceptorsFirstUnchosenIndex;

            /**
             * Gets the value of the acceptorMinProposalNumber property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getAcceptorMinProposalNumber() {
                return acceptorMinProposalNumber;
            }

            /**
             * Sets the value of the acceptorMinProposalNumber property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setAcceptorMinProposalNumber(String value) {
                this.acceptorMinProposalNumber = value;
            }

            /**
             * Gets the value of the acceptorsFirstUnchosenIndex property.
             * 
             */
            public int getAcceptorsFirstUnchosenIndex() {
                return acceptorsFirstUnchosenIndex;
            }

            /**
             * Sets the value of the acceptorsFirstUnchosenIndex property.
             * 
             */
            public void setAcceptorsFirstUnchosenIndex(int value) {
                this.acceptorsFirstUnchosenIndex = value;
            }

        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="serverId" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *         &lt;element name="leaderId" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *         &lt;element name="activeServers" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "serverId",
        "leaderId",
        "activeServers"
    })
    public static class Heartbeat {

        protected int serverId;
        protected int leaderId;
        @XmlElement(required = true)
        protected String activeServers;

        /**
         * Gets the value of the serverId property.
         * 
         */
        public int getServerId() {
            return serverId;
        }

        /**
         * Sets the value of the serverId property.
         * 
         */
        public void setServerId(int value) {
            this.serverId = value;
        }

        /**
         * Gets the value of the leaderId property.
         * 
         */
        public int getLeaderId() {
            return leaderId;
        }

        /**
         * Sets the value of the leaderId property.
         * 
         */
        public void setLeaderId(int value) {
            this.leaderId = value;
        }

        /**
         * Gets the value of the activeServers property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getActiveServers() {
            return activeServers;
        }

        /**
         * Sets the value of the activeServers property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setActiveServers(String value) {
            this.activeServers = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="request">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="proposalNumber" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="index" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="response">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="acceptedProposal" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="acceptedValue" type="{urn:generated:nonstandard:notification}notificationInfo"/>
     *                   &lt;element name="noMoreAccepted" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
     *                   &lt;element name="status" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "request",
        "response"
    })
    public static class Prepare {

        @XmlElement(required = true)
        protected Message.Prepare.Request request;
        @XmlElement(required = true)
        protected Message.Prepare.Response response;

        /**
         * Gets the value of the request property.
         * 
         * @return
         *     possible object is
         *     {@link Message.Prepare.Request }
         *     
         */
        public Message.Prepare.Request getRequest() {
            return request;
        }

        /**
         * Sets the value of the request property.
         * 
         * @param value
         *     allowed object is
         *     {@link Message.Prepare.Request }
         *     
         */
        public void setRequest(Message.Prepare.Request value) {
            this.request = value;
        }

        /**
         * Gets the value of the response property.
         * 
         * @return
         *     possible object is
         *     {@link Message.Prepare.Response }
         *     
         */
        public Message.Prepare.Response getResponse() {
            return response;
        }

        /**
         * Sets the value of the response property.
         * 
         * @param value
         *     allowed object is
         *     {@link Message.Prepare.Response }
         *     
         */
        public void setResponse(Message.Prepare.Response value) {
            this.response = value;
        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence>
         *         &lt;element name="proposalNumber" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="index" type="{http://www.w3.org/2001/XMLSchema}int"/>
         *       &lt;/sequence>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "proposalNumber",
            "index"
        })
        public static class Request {

            @XmlElement(required = true)
            protected String proposalNumber;
            protected int index;

            /**
             * Gets the value of the proposalNumber property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getProposalNumber() {
                return proposalNumber;
            }

            /**
             * Sets the value of the proposalNumber property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setProposalNumber(String value) {
                this.proposalNumber = value;
            }

            /**
             * Gets the value of the index property.
             * 
             */
            public int getIndex() {
                return index;
            }

            /**
             * Sets the value of the index property.
             * 
             */
            public void setIndex(int value) {
                this.index = value;
            }

        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence>
         *         &lt;element name="acceptedProposal" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="acceptedValue" type="{urn:generated:nonstandard:notification}notificationInfo"/>
         *         &lt;element name="noMoreAccepted" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
         *         &lt;element name="status" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
         *       &lt;/sequence>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "acceptedProposal",
            "acceptedValue",
            "noMoreAccepted",
            "status"
        })
        public static class Response {

            @XmlElement(required = true)
            protected String acceptedProposal;
            @XmlElement(required = true)
            protected NotificationInfo acceptedValue;
            @XmlElement(defaultValue = "false")
            protected boolean noMoreAccepted;
            @XmlElement(defaultValue = "false")
            protected boolean status;

            /**
             * Gets the value of the acceptedProposal property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getAcceptedProposal() {
                return acceptedProposal;
            }

            /**
             * Sets the value of the acceptedProposal property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setAcceptedProposal(String value) {
                this.acceptedProposal = value;
            }

            /**
             * Gets the value of the acceptedValue property.
             * 
             * @return
             *     possible object is
             *     {@link NotificationInfo }
             *     
             */
            public NotificationInfo getAcceptedValue() {
                return acceptedValue;
            }

            /**
             * Sets the value of the acceptedValue property.
             * 
             * @param value
             *     allowed object is
             *     {@link NotificationInfo }
             *     
             */
            public void setAcceptedValue(NotificationInfo value) {
                this.acceptedValue = value;
            }

            /**
             * Gets the value of the noMoreAccepted property.
             * 
             */
            public boolean isNoMoreAccepted() {
                return noMoreAccepted;
            }

            /**
             * Sets the value of the noMoreAccepted property.
             * 
             */
            public void setNoMoreAccepted(boolean value) {
                this.noMoreAccepted = value;
            }

            /**
             * Gets the value of the status property.
             * 
             */
            public boolean isStatus() {
                return status;
            }

            /**
             * Sets the value of the status property.
             * 
             */
            public void setStatus(boolean value) {
                this.status = value;
            }

        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="request">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="index" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *                   &lt;element name="value" type="{urn:generated:nonstandard:notification}notificationInfo"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="response">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="acceptorsFirstUnchosenIndex" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "request",
        "response"
    })
    public static class Success {

        @XmlElement(required = true)
        protected Message.Success.Request request;
        @XmlElement(required = true)
        protected Message.Success.Response response;

        /**
         * Gets the value of the request property.
         * 
         * @return
         *     possible object is
         *     {@link Message.Success.Request }
         *     
         */
        public Message.Success.Request getRequest() {
            return request;
        }

        /**
         * Sets the value of the request property.
         * 
         * @param value
         *     allowed object is
         *     {@link Message.Success.Request }
         *     
         */
        public void setRequest(Message.Success.Request value) {
            this.request = value;
        }

        /**
         * Gets the value of the response property.
         * 
         * @return
         *     possible object is
         *     {@link Message.Success.Response }
         *     
         */
        public Message.Success.Response getResponse() {
            return response;
        }

        /**
         * Sets the value of the response property.
         * 
         * @param value
         *     allowed object is
         *     {@link Message.Success.Response }
         *     
         */
        public void setResponse(Message.Success.Response value) {
            this.response = value;
        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence>
         *         &lt;element name="index" type="{http://www.w3.org/2001/XMLSchema}int"/>
         *         &lt;element name="value" type="{urn:generated:nonstandard:notification}notificationInfo"/>
         *       &lt;/sequence>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "index",
            "value"
        })
        public static class Request {

            protected int index;
            @XmlElement(required = true)
            protected NotificationInfo value;

            /**
             * Gets the value of the index property.
             * 
             */
            public int getIndex() {
                return index;
            }

            /**
             * Sets the value of the index property.
             * 
             */
            public void setIndex(int value) {
                this.index = value;
            }

            /**
             * Gets the value of the value property.
             * 
             * @return
             *     possible object is
             *     {@link NotificationInfo }
             *     
             */
            public NotificationInfo getValue() {
                return value;
            }

            /**
             * Sets the value of the value property.
             * 
             * @param value
             *     allowed object is
             *     {@link NotificationInfo }
             *     
             */
            public void setValue(NotificationInfo value) {
                this.value = value;
            }

        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence>
         *         &lt;element name="acceptorsFirstUnchosenIndex" type="{http://www.w3.org/2001/XMLSchema}int"/>
         *       &lt;/sequence>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "acceptorsFirstUnchosenIndex"
        })
        public static class Response {

            protected int acceptorsFirstUnchosenIndex;

            /**
             * Gets the value of the acceptorsFirstUnchosenIndex property.
             * 
             */
            public int getAcceptorsFirstUnchosenIndex() {
                return acceptorsFirstUnchosenIndex;
            }

            /**
             * Sets the value of the acceptorsFirstUnchosenIndex property.
             * 
             */
            public void setAcceptorsFirstUnchosenIndex(int value) {
                this.acceptorsFirstUnchosenIndex = value;
            }

        }

    }

}
