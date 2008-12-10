/*
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.mobicents.servlet.sip.startup;

import gov.nist.javax.sip.SipStackImpl;

import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.SipStack;

import net.java.stun4j.StunAddress;
import net.java.stun4j.client.NetworkConfigurationDiscoveryProcess;
import net.java.stun4j.client.StunDiscoveryReport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.coyote.Adapter;
import org.apache.coyote.ProtocolHandler;
import org.apache.tomcat.util.modeler.Registry;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.core.DNSAddressResolver;
import org.mobicents.servlet.sip.core.ExtendedListeningPoint;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;

/**
 * This is the sip protocol handler that will get called upon creation of the
 * tomcat connector defined in the server.xml.<br/> To use a sip connector, one
 * need to specify a new connector in server.xml with
 * org.mobicents.servlet.sip.startup.SipProtocolHandler as the value for the
 * protocol attribute.<br/>
 * 
 * Some of the fields (representing the sip stack propeties) get populated
 * automatically by the container.<br/>
 * 
 * @author Jean Deruelle
 * 
 */
public class SipProtocolHandler implements ProtocolHandler, MBeanRegistration {
	// the logger
	private static transient Log logger = LogFactory.getLog(SipProtocolHandler.class.getName());
	// *
    protected ObjectName tpOname = null;
    // *
    protected ObjectName rgOname = null;
	/**
     * The random port number generator that we use in getRandomPortNumer()
     */
    private static Random portNumberGenerator = new Random();
    
	private Adapter adapter = null;

	private Map<String, Object> attributes = new HashMap<String, Object>();

	private SipStack sipStack;

	/*
	 * the extended listening point with global ip address and port for it
	 */
	public ExtendedListeningPoint extendedListeningPoint;

	// sip stack attributes defined by the server.xml
	private String sipStackPropertiesFile;
	// defining sip stack properties
	private Properties sipStackProperties;
	/**
	 * the sip stack signaling transport
	 */
	private String signalingTransport;

	/**
	 * the sip stack listening port
	 */
	private int port;
	/*
	 * IP address for this protocol handler.
	 */
	private String ipAddress;	
	/*
	 * use Stun
	 */
	private boolean useStun;
	/*
	 * Stun Server Address
	 */
	private String stunServerAddress;
	/*
	 * Stun Server Port
	 */
	private int stunServerPort;
	

	/**
	 * {@inheritDoc}
	 */
	public void destroy() throws Exception {
		if(logger.isDebugEnabled()) {
			logger.debug("Stopping a sip protocol handler");
		}
		//Jboss specific unloading case
		SipApplicationDispatcher sipApplicationDispatcher = (SipApplicationDispatcher)
			getAttribute(SipApplicationDispatcher.class.getSimpleName());
		if(sipApplicationDispatcher != null) {
			if(logger.isDebugEnabled()) {
				logger.debug("Removing the Sip Application Dispatcher as a sip listener for listening point " + extendedListeningPoint);
			}
			extendedListeningPoint.getSipProvider().removeSipListener(sipApplicationDispatcher);
			sipApplicationDispatcher.getSipNetworkInterfaceManager().removeExtendedListeningPoint(extendedListeningPoint);
		}
		// removing listening point and sip provider
		if(logger.isDebugEnabled()) {
			logger.debug("Removing the following Listening Point " + extendedListeningPoint);
		}
		sipStack.deleteSipProvider(extendedListeningPoint.getSipProvider());
		if(logger.isDebugEnabled()) {
			logger.debug("Removing the sip provider");
		}
		sipStack.deleteListeningPoint(extendedListeningPoint.getListeningPoint());
		extendedListeningPoint = null;
		// stopping the sip stack
		if(!sipStack.getListeningPoints().hasNext() && !sipStack.getSipProviders().hasNext()) {
			sipStack.stop();
			sipStack = null;
			logger.info("Sip stack stopped");
		}		
		if (tpOname!=null)
            Registry.getRegistry(null, null).unregisterComponent(tpOname);
        if (rgOname != null)
            Registry.getRegistry(null, null).unregisterComponent(rgOname);
	}

	public Adapter getAdapter() {		
		return adapter;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getAttribute(String attribute) {
		return attributes.get(attribute);
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator getAttributeNames() {
		return attributes.keySet().iterator();
	}

	/**
	 * {@inheritDoc}
	 */
	public void init() throws Exception {		
		SipFactories.initialize("gov.nist");
		setAttribute("isSipConnector",Boolean.TRUE);
	}

	public void pause() throws Exception {
		//This is optionnal, no implementation there

	}

	public void resume() throws Exception {
		// This is optionnal, no implementation there

	}

	public void setAdapter(Adapter adapter) {
		this.adapter = adapter;

	}

	/**
	 * {@inheritDoc}
	 */
	public void setAttribute(String arg0, Object arg1) {
		attributes.put(arg0, arg1);
	}		
	
	public void start() throws Exception {
		try {
			if(logger.isDebugEnabled()) {
				logger.debug("Starting a sip protocol handler");
			}						
			
			String catalinaHome = System.getProperty("catalina.home");
	        if (catalinaHome == null) {
	        	catalinaHome = System.getProperty("catalina.base");
	        }
	        if(catalinaHome == null) {
	        	catalinaHome = ".";
	        }
	        if(sipStackPropertiesFile != null && !sipStackPropertiesFile.startsWith("file:///")) {
				sipStackPropertiesFile = "file:///" + catalinaHome.replace(File.separatorChar, '/') + "/" + sipStackPropertiesFile;
	 		}	
	        sipStackProperties = new Properties();
	        boolean isPropsLoaded = false;
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("Loading SIP stack properties from following file : " + sipStackPropertiesFile);
				}
				if(sipStackPropertiesFile != null) {
					FileInputStream sipStackPropertiesInputStream = new FileInputStream(new File(new URI(sipStackPropertiesFile)));
					sipStackProperties.load(sipStackPropertiesInputStream);
					String debugLog = sipStackProperties.getProperty("gov.nist.javax.sip.DEBUG_LOG");
					if(debugLog != null && debugLog.length() > 0 && !debugLog.startsWith("file:///")) {				
						sipStackProperties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
							catalinaHome + "/" + debugLog);
					}
					String serverLog = sipStackProperties.getProperty("gov.nist.javax.sip.SERVER_LOG");
					if(serverLog != null && serverLog.length() > 0 && !serverLog.startsWith("file:///")) {
						sipStackProperties.setProperty("gov.nist.javax.sip.SERVER_LOG",
							catalinaHome + "/" + serverLog);
					}
					// The whole MSS is built upon this assumption, so this property is not overrideable
					sipStackProperties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", "off");
					isPropsLoaded = true;
				} else {
					logger.warn("no sip stack properties file defined ");		
				}
			} catch (Exception e) {
				logger.warn("Could not find or problem when loading the sip stack properties file : " + sipStackPropertiesFile, e);		
			}
			
			if(!isPropsLoaded) {
				logger.warn("loading default Mobicents Sip Servlets sip stack properties");
				// Silently set default values
				sipStackProperties.setProperty("gov.nist.javax.sip.LOG_MESSAGE_CONTENT",
						"true");
				sipStackProperties.setProperty("gov.nist.javax.sip.TRACE_LEVEL",
						"32");
				sipStackProperties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
						catalinaHome + "/" + "mss-jsip-" + ipAddress + "-" + port+"-debug.txt");
				sipStackProperties.setProperty("gov.nist.javax.sip.SERVER_LOG",
						catalinaHome + "/" + "mss-jsip-" + ipAddress + "-" + port+"-messages.xml");
				sipStackProperties.setProperty("javax.sip.STACK_NAME", "mss-" + ipAddress + "-" + port);
				sipStackProperties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", "off");		
				sipStackProperties.setProperty("gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY", "true");
				sipStackProperties.setProperty("gov.nist.javax.sip.THREAD_POOL_SIZE", "64");
				sipStackProperties.setProperty("gov.nist.javax.sip.REENTRANT_LISTENER", "true");
			}
			
			//checking the external ip address if stun enabled			
			String globalIpAddress = null;			
			int globalPort = -1;
			if (useStun) {
				if(InetAddress.getByName(ipAddress).isLoopbackAddress()) {
					logger.warn("The Ip address provided is the loopback address, stun won't be enabled for it");
				} else {
					//chooses stun port randomly
					DatagramSocket randomSocket = initRandomPortSocket();
					int randomPort = randomSocket.getLocalPort();
					randomSocket.disconnect();
					randomSocket.close();
					randomSocket = null;
					StunAddress localStunAddress = new StunAddress(ipAddress,
							randomPort);
	
					StunAddress serverStunAddress = new StunAddress(
							stunServerAddress, stunServerPort);
	
					NetworkConfigurationDiscoveryProcess addressDiscovery = new NetworkConfigurationDiscoveryProcess(
							localStunAddress, serverStunAddress);
					addressDiscovery.start();
					StunDiscoveryReport report = addressDiscovery
							.determineAddress();
					if(report.getPublicAddress() != null) {
						globalIpAddress = report.getPublicAddress().getSocketAddress().getAddress().getHostAddress();
						globalPort = report.getPublicAddress().getPort();
						//TODO set a timer to retry the binding and provide a callback to update the global ip address and port
					} else {
						useStun = false;
						logger.error("Stun discovery failed to find a valid public ip address, disabling stun !");
					}
					logger.info("Stun report = " + report);
					addressDiscovery.shutDown();
				}
			}
			if(logger.isInfoEnabled()) {
				logger.info("Mobicents Sip Servlets sip stack properties : " + sipStackProperties);
			}
			// Create SipStack object
			sipStack = SipFactories.sipFactory.createSipStack(sipStackProperties);

			ListeningPoint listeningPoint = sipStack.createListeningPoint(ipAddress,
					port, signalingTransport);
			if(useStun) {
//				listeningPoint.setSentBy(globalIpAddress + ":" + globalPort);
				listeningPoint.setSentBy(globalIpAddress + ":" + port);
			}
			SipProvider sipProvider = sipStack.createSipProvider(listeningPoint);
			sipStack.start();
			
			//creating the extended listening point
			extendedListeningPoint = new ExtendedListeningPoint(sipProvider, listeningPoint);
			extendedListeningPoint.setGlobalIpAddress(globalIpAddress);
			extendedListeningPoint.setGlobalPort(globalPort);
			//TODO add it as a listener for global ip address changes if STUN rediscover a new addess at some point
			
			//made the sip stack and the extended listening Point available to the service implementation
			setAttribute(SipStack.class.getSimpleName(), sipStack);					
			setAttribute(ExtendedListeningPoint.class.getSimpleName(), extendedListeningPoint);
			
			//Jboss specific loading case
			SipApplicationDispatcher sipApplicationDispatcher = (SipApplicationDispatcher)
				getAttribute(SipApplicationDispatcher.class.getSimpleName());
			if(sipApplicationDispatcher != null) {
				if(logger.isDebugEnabled()) {
					logger.debug("Adding the Sip Application Dispatcher as a sip listener for connector listening on port " + port);
				}
				sipProvider.addSipListener(sipApplicationDispatcher);
				sipApplicationDispatcher.getSipNetworkInterfaceManager().addExtendedListeningPoint(extendedListeningPoint);
				// for nist sip stack set the DNS Address resolver allowing to make DNS SRV lookups
				if(sipStack instanceof SipStackImpl) {
					if(logger.isDebugEnabled()) {
						logger.debug(sipStack.getStackName() +" will be using DNS SRV lookups as AddressResolver");
					}
					((SipStackImpl) sipStack).setAddressResolver(new DNSAddressResolver(sipApplicationDispatcher));
				}
			}
			
			logger.info("Sip Connector started on ip address : " + ipAddress
					+ ",port " + port + ", useStun " + useStun + ", stunAddress " + stunServerAddress + ", stunPort : " + stunServerPort);
			
			if (this.domain != null) {
//	            try {
//	                tpOname = new ObjectName
//	                    (domain + ":" + "type=ThreadPool,name=" + getName());
//	                Registry.getRegistry(null, null)
//	                    .registerComponent(endpoint, tpOname, null );
//	            } catch (Exception e) {
//	                logger.error("Can't register endpoint");
//	            }
	            rgOname=new ObjectName
	                (domain + ":type=GlobalRequestProcessor,name=" + getName());
	            Registry.getRegistry(null, null).registerComponent
	                ( sipStack, rgOname, null );
	        }
		} catch (Exception ex) {
			logger.fatal(
					"Bad shit happened -- check server.xml for tomcat. ", ex);
			throw ex;
		}
	}	
	
	/**
     * Initializes and binds a socket that on a random port number. The method
     * would try to bind on a random port and retry 5 times until a free port
     * is found.
     *
     * @return the socket that we have initialized on a randomport number.
     */
    private DatagramSocket initRandomPortSocket() {
        int bindRetries = 5;
        int currentlyTriedPort = 
        	getRandomPortNumber(JainSipUtils.MIN_PORT_NUMBER, JainSipUtils.MAX_PORT_NUMBER);

        DatagramSocket resultSocket = null;
        //we'll first try to bind to a random port. if this fails we'll try
        //again (bindRetries times in all) until we find a free local port.
        for (int i = 0; i < bindRetries; i++) {
            try {
                resultSocket = new DatagramSocket(currentlyTriedPort);
                //we succeeded - break so that we don't try to bind again
                break;
            }
            catch (SocketException exc) {
                if (exc.getMessage().indexOf("Address already in use") == -1) {
                    logger.fatal("An exception occurred while trying to create"
                                 + "a local host discovery socket.", exc);                    
                    return null;
                }
                //port seems to be taken. try another one.
                logger.debug("Port " + currentlyTriedPort + " seems in use.");
                currentlyTriedPort = 
                	getRandomPortNumber(JainSipUtils.MIN_PORT_NUMBER, JainSipUtils.MAX_PORT_NUMBER);
                logger.debug("Retrying bind on port " + currentlyTriedPort);
            }
        }

        return resultSocket;
    }
	
	/**
     * Returns a random local port number, greater than min and lower than max.
     *
     * @param min the minimum allowed value for the returned port number.
     * @param max the maximum allowed value for the returned port number.
     *
     * @return a random int located between greater than min and lower than max.
     */
    public static int getRandomPortNumber(int min, int max) {
        return portNumberGenerator.nextInt(max - min) + min;
    }
			

	/**
	 * @return the signalingTransport
	 */
	public String getSignalingTransport() {
		return signalingTransport;
	}

	/**
	 * @param signalingTransport
	 *            the signalingTransport to set
	 */
	public void setSignalingTransport(String transport) {
		this.signalingTransport = transport;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}
		
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * @return the stunServerAddress
	 */
	public String getStunServerAddress() {
		return stunServerAddress;
	}

	/**
	 * @param stunServerAddress the stunServerAddress to set
	 */
	public void setStunServerAddress(String stunServerAddress) {
		this.stunServerAddress = stunServerAddress;
	}

	/**
	 * @return the stunServerPort
	 */
	public int getStunServerPort() {
		return stunServerPort;
	}

	/**
	 * @param stunServerPort the stunServerPort to set
	 */
	public void setStunServerPort(int stunServerPort) {
		this.stunServerPort = stunServerPort;
	}

	/**
	 * @return the useStun
	 */
	public boolean isUseStun() {
		return useStun;
	}

	/**
	 * @param useStun the useStun to set
	 */
	public void setUseStun(boolean useStun) {
		this.useStun = useStun;
	}	

	public InetAddress getAddress() {
		try {
			return InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			logger.error("unexpected exception while getting the ipaddress of the sip protocol handler", e);
			return null;
		}
	}
    public void setAddress(InetAddress ia) { ipAddress = ia.getHostAddress(); }

    public String getName() {
        String encodedAddr = "";
        if (getAddress() != null) {
            encodedAddr = "" + getAddress();
            if (encodedAddr.startsWith("/"))
                encodedAddr = encodedAddr.substring(1);
            encodedAddr = URLEncoder.encode(encodedAddr) + "-";
        }
        return ("sip-" + encodedAddr + getPort());
    }
    
    /**
	 * @param sipStackPropertiesFile the sipStackPropertiesFile to set
	 */
	public void setSipStackPropertiesFile(String sipStackPropertiesFile) {
		this.sipStackPropertiesFile = sipStackPropertiesFile;
	}

	/**
	 * @return the sipStackPropertiesFile
	 */
	public String getSipStackPropertiesFile() {
		return sipStackPropertiesFile;
	}
    
    // -------------------- JMX related methods --------------------

    // *
    protected String domain;
    protected ObjectName oname;
    protected MBeanServer mserver;

    public ObjectName getObjectName() {
        return oname;
    }

    public String getDomain() {
        return domain;
    }

    public ObjectName preRegister(MBeanServer server,
                                  ObjectName name) throws Exception {
        oname=name;
        mserver=server;
        domain=name.getDomain();
        return name;
    }

    public void postRegister(Boolean registrationDone) {
    }

    public void preDeregister() throws Exception {
    }

    public void postDeregister() {
    }	
}
