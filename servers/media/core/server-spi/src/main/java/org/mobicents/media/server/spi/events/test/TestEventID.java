/**
 * Mobicents Media Gateway
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
package org.mobicents.media.server.spi.events.test;

import org.mobicents.media.server.spi.events.dtmf.*;
import org.mobicents.media.server.spi.events.*;

/**
 *
 * @author Oleg Kulikov
 */
public enum TestEventID implements EventID {

    SINE_WAVE("SINE_WAVE"),
    SPECTRA("SPECTRA");
    
    private final static String packageName = "org.mobicents.media.server.evt.test";
    private String eventName;

    private TestEventID(String eventName) {
        this.eventName = eventName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getEventName() {
        return eventName;
    }

    @Override
    public String toString() {
        return packageName + "." + eventName;
    }
    
}
