/*
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
package org.mobicents.media.server.impl;

import java.util.ArrayList;
import java.util.List;

import org.jboss.util.id.UID;
import org.mobicents.media.MediaSink;
import org.mobicents.media.MediaSource;
import org.mobicents.media.server.spi.NotificationListener;
import org.mobicents.media.server.spi.events.NotifyEvent;

/**
 * 
 * @author Oleg Kulikov
 */
public abstract class AbstractSink extends AbstractWorkDataGatherer implements MediaSink {

    protected transient MediaSource mediaStream;
    private List<NotificationListener> listeners = new ArrayList();
    private String id = null;
    private String name = null;

    public AbstractSink(String name) {
        this.id = (new UID()).toString();
        this.name = name;

    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }
    
    /**
     * (Non Java-doc).
     * 
     * @see org.mobicents.MediaSink#connect(MediaStream).
     */
    public void connect(MediaSource mediaStream) {
        this.mediaStream = mediaStream;
        if (((AbstractSource) mediaStream).sink == null) {
            mediaStream.connect(this);
        }
    }

    /**
     * (Non Java-doc).
     * 
     * @see org.mobicents.MediaSink#disconnect(MediaStream).
     */
    public void disconnect(MediaSource mediaStream) {
        if (this.mediaStream != null) {
            this.mediaStream = null;
            ((AbstractSource) mediaStream).sink = null;
            mediaStream.disconnect(this);
        }
    }

    public void addListener(NotificationListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeListener(NotificationListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    protected void sendEvent(NotifyEvent evt) {
        synchronized (listeners) {
            for (NotificationListener listener : listeners) {
                listener.update(evt);
            }
        }
    }

    public void dispose() {
        synchronized (listeners) {
            listeners.clear();
        }
        if (this.mediaStream != null) {
            ((AbstractSource) mediaStream).sink = null;
        }
        mediaStream = null;
    }

    @Override
    public String toString() {
        return (new StringBuffer().append(this.name).append(" - ").append(this.id)).toString();

    }
}
