package org.mobicents.media.msc.common.events;
/**
 * <ul>
 * <li>SESSION_CREATED indicates that the MsSession object has been created and is in the MsSessionState.IDLE state.</li> 
 * <li>SESSION_ACTIVE indicates that the state of the MsSession object has changed to MsSessionState.ACTIVE</li>
 * <li>SESSION_INVALID indicates that the state of the MsSession object has changed to MsSessionState.INVALID</li>
 * </ul>
 */
public enum MsSessionEventID {

	/**
	 * <ul>
	 * <li>SESSION_CREATED indicates that the MsSession object has been created and is in the MsSessionState.IDLE state.</li> 
	 * <li>SESSION_ACTIVE indicates that the state of the MsSession object has changed to MsSessionState.ACTIVE</li>
	 * <li>SESSION_INVALID indicates that the state of the MsSession object has changed to MsSessionState.INVALID</li>
	 * </ul>
	 */
	SESSION_CREATED, SESSION_ACTIVE, SESSION_INVALID;

}
