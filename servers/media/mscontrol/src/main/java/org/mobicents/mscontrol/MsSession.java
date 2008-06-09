/*
 * MsSession.java
 *
 * The Simple Media Server Control API
 *
 * The source code contained in this file is in in the public domain.
 * It can be used in any project or product without prior permission,
 * license or royalty payments. There is  NO WARRANTY OF ANY KIND,
 * EXPRESS, IMPLIED OR STATUTORY, INCLUDING, WITHOUT LIMITATION,
 * THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE,
 * AND DATA ACCURACY.  We do not warrant or make any representations
 * regarding the use of the software or the  results thereof, including
 * but not limited to the correctness, accuracy, reliability or
 * usefulness of the software.
 */

package org.mobicents.mscontrol;

import java.io.Serializable;
import java.util.List;

import org.mobicents.media.msc.common.MsLinkMode;
import org.mobicents.media.msc.common.MsSessionState;

/**
 * A <code>MsSession</code> is a transient association of (zero or more)
 * connection for the purposes of engaging in a real-time communications
 * interchange.
 * 
 * The session and its associated connection objects describe the control and
 * media flows taking place in a communication network. The
 * <code>MsProvider</code> adjusts the session, connection and link objects to
 * reflect the results of the combined command actions.
 * 
 * Applications create instances of a MsSession object with the
 * <code>MsProvider.createSession()</code> method, which returns a MsSession
 * object that has zero connections and is in the IDLE state. The
 * <code>MsSession</code> maintains a reference to its
 * <code>MsProvider for the 
 * life of that <code>MsSession</code> object. The <code>MsProvider</code> 
 * object instance does not change throughout the lifetime of the 
 * <code>MsSession</code> object. The <code>MsProvider</code> associated with a 
 * <code>MsSession</code> is obtained via the getProvider() method. 
 *
 * @author Oleg Kulikov
 */
public interface MsSession extends Serializable {

	/**
	 * Get the unique id of this session
	 * 
	 * @return
	 */
	public String getId();

	/**
	 * Retrieves the provider handling this session object. The Provider
	 * reference does not change once the MsSession object has been created,
	 * despite the state of the MsSession object.
	 * 
	 * @return MsProvider object managing this call.
	 */
	public MsProvider getProvider();

	/**
	 * Retrieves the state of the session. The state will be either IDLE, ACTIVE
	 * or INVALID.
	 * 
	 * @return enum representing the state of the session.
	 */
	public MsSessionState getState();

	/**
	 * Creates a new network connection and attaches it to this session. The
	 * MsConnection object is associated with an endpoint name corresponding to
	 * the string given as an input parameter.
	 * 
	 * Note that following this operation the returned MsConnection object must
	 * still be "modified" which can be accomplished using the
	 * MsConnection.modify(...)
	 * 
	 * @param endpointName
	 *            specifies the identifier of the media server endpoint.
	 */
	public MsConnection createNetworkConnection(String endpointName);

	/**
	 * Creates local link that joines two endpoints and attach it to this
	 * session.
	 * 
	 * In some networks, we may often have to set-up connections between
	 * endpoints that are located within the same media server. Examples of such
	 * connections may be:
	 * 
	 * <ul>
	 * <li>Connecting a call to an Interactive Voice-Response unit,</li>
	 * <li>Connecting a call to a Conferencing unit,</li>
	 * <li>Routing a call from one endpoint to another,something often
	 * described as a "hairpin" connection.</li>
	 * </ul>
	 * 
	 * Local connections are much simpler to establish than network connections.
	 * In most cases, the connection will be established through some local
	 * interconnecting device, such as for example a TDM bus.
	 * 
	 * When two endpoints are managed by the same media server, it is possible
	 * to specify the link in a single command that conveys the names of the two
	 * endpoints that will be connected.
	 * 
	 * @param mode
	 *            specifies the mode of the link. Valid modes are
	 *            <ul>
	 *            <li>MsLinkMode.HALF_DUPLEX</li>
	 *            <li>MsLinkMode.DUPLEX</li>
	 *            </ul>
	 * @return MsLink object managing this link.
	 */
	public MsLink createLink(MsLinkMode mode);

	/**
	 * Add a listener to this session. This also reports all state changes in
	 * the state of the session. The listener added with this method will report
	 * events on the session for as long as the listener receive INVALID event.
	 * 
	 * @param listener
	 *            object that receives the specified events
	 */
	public void addSessionListener(MsSessionListener listener);

	/**
	 * Removes a listener from this session.
	 * 
	 * @param listener
	 *            Listener object.
	 */
	public void removeSessionListener(MsSessionListener listener);

	/**
	 * Returns the list of MsSessionListener
	 * 
	 * @return
	 */
	public List<MsSessionListener> getSessionListeners();

	/**
	 * Returns the list of MsConnection associated with this MsSession
	 * 
	 * @return
	 */
	public List<MsConnection> getConnections();

	/**
	 * Explicitly sets the MsSession state to <code>MsSessionState.IDLE</code>
	 * and fires <code>MsSessionEventID.SESSION_CREATED</code> event for
	 * {@link MsSessionListener}
	 */
	public void setSessionStateIdle();

	/**
	 * Removes the instance of MsConnection from list of MsConnection's managed
	 * by this MsSession If there are no more MsConnection or MsLink's
	 * associated with this MsSession, it transitions to INVALID state
	 * 
	 * @param connection
	 */
	public void disassociateNetworkConnection(MsConnection connection);

	/**
	 * Removes the instance of MsLink from list of MsLink's managed by this
	 * MsSession If there are no more MsConnection or MsLink's associated with
	 * this MsSession, it transitions to INVALID state
	 * 
	 * @param link
	 */
	public void disassociateLink(MsLink link);

}
