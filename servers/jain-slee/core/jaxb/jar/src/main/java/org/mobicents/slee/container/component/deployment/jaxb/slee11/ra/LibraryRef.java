//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-661 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.01.16 at 04:56:25 PM WET 
//


package org.mobicents.slee.container.component.deployment.jaxb.slee11.ra;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "description",
    "libraryName",
    "libraryVendor",
    "libraryVersion"
})
@XmlRootElement(name = "library-ref")
public class LibraryRef {

    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;
    protected Description description;
    @XmlElement(name = "library-name", required = true)
    protected LibraryName libraryName;
    @XmlElement(name = "library-vendor", required = true)
    protected LibraryVendor libraryVendor;
    @XmlElement(name = "library-version", required = true)
    protected LibraryVersion libraryVersion;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link Description }
     *     
     */
    public Description getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link Description }
     *     
     */
    public void setDescription(Description value) {
        this.description = value;
    }

    /**
     * Gets the value of the libraryName property.
     * 
     * @return
     *     possible object is
     *     {@link LibraryName }
     *     
     */
    public LibraryName getLibraryName() {
        return libraryName;
    }

    /**
     * Sets the value of the libraryName property.
     * 
     * @param value
     *     allowed object is
     *     {@link LibraryName }
     *     
     */
    public void setLibraryName(LibraryName value) {
        this.libraryName = value;
    }

    /**
     * Gets the value of the libraryVendor property.
     * 
     * @return
     *     possible object is
     *     {@link LibraryVendor }
     *     
     */
    public LibraryVendor getLibraryVendor() {
        return libraryVendor;
    }

    /**
     * Sets the value of the libraryVendor property.
     * 
     * @param value
     *     allowed object is
     *     {@link LibraryVendor }
     *     
     */
    public void setLibraryVendor(LibraryVendor value) {
        this.libraryVendor = value;
    }

    /**
     * Gets the value of the libraryVersion property.
     * 
     * @return
     *     possible object is
     *     {@link LibraryVersion }
     *     
     */
    public LibraryVersion getLibraryVersion() {
        return libraryVersion;
    }

    /**
     * Sets the value of the libraryVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link LibraryVersion }
     *     
     */
    public void setLibraryVersion(LibraryVersion value) {
        this.libraryVersion = value;
    }

}
