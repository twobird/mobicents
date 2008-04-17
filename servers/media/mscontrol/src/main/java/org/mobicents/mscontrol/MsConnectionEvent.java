/*
 * MsConnectionEvent.java
 *
 * The Simple Media API RA
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

import org.mobicents.media.msc.common.events.MsConnectionEventCause;
import org.mobicents.media.msc.common.events.MsConnectionEventID;

/**
 *
 * @author Oleg Kulikov
 */
public interface MsConnectionEvent extends Serializable {
    

    
    public MsConnection getConnection();
    public MsConnectionEventID getEventID();
    public MsConnectionEventCause getCause();
    public String getMessage();
}
