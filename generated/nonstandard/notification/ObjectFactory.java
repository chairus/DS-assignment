//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.09.25 at 09:34:52 AM ACST 
//


package generated.nonstandard.notification;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the generated.nonstandard.notification package. 
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

    private final static QName _Notification_QNAME = new QName("urn:generated:nonstandard:notification", "notification");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: generated.nonstandard.notification
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link NotificationInfo }
     * 
     */
    public NotificationInfo createNotificationInfo() {
        return new NotificationInfo();
    }

    /**
     * Create an instance of {@link NotificationInfo.Timestamp }
     * 
     */
    public NotificationInfo.Timestamp createNotificationInfoTimestamp() {
        return new NotificationInfo.Timestamp();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotificationInfo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:generated:nonstandard:notification", name = "notification")
    public JAXBElement<NotificationInfo> createNotification(NotificationInfo value) {
        return new JAXBElement<NotificationInfo>(_Notification_QNAME, NotificationInfo.class, null, value);
    }

}
