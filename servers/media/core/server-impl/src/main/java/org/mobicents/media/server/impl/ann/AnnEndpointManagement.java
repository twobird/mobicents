/*
 * AnnEndpointManagement.java
 *
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

package org.mobicents.media.server.impl.ann;

import org.apache.log4j.Logger;
import org.mobicents.media.server.impl.jmx.EndpointManagement;
import org.mobicents.media.server.impl.jmx.EndpointManagementMBean;
import org.mobicents.media.server.spi.Endpoint;

/**
 * Provides implementation of the Announcement endpoint MBean.
 *
 * @author Oleg Kulikov
 */
public class AnnEndpointManagement extends EndpointManagement 
        implements AnnEndpointManagementMBean {
    
    private Logger logger = Logger.getLogger(AnnEndpointManagement.class);
    
    /**
     * Creates a new instance of AnnEndpointManagement
     */
    public AnnEndpointManagement() {
    }
    
    @Override
    public Endpoint createEndpoint() throws Exception {
        AnnEndpointImpl endpoint = new AnnEndpointImpl(getJndiName());
        return endpoint;
    }
    
    @Override
	public EndpointManagementMBean cloneEndpointManagementMBean() {
		AnnEndpointManagement clone = new AnnEndpointManagement();
		try {
			clone.setJndiName(this.getJndiName());
			clone.setBindAddress(this.getBindAddress());
			clone.setJitter(this.getJitter());
			clone.setPacketizationPeriod(this.getPacketizationPeriod());
			clone.setPortRange(this.getPortRange());
			clone.setPCMA(this.getPCMA());
			clone.setPCMU(this.getPCMU());
			clone.setSpeex(this.getSpeex());
			clone.setDTMF(this.getDTMF());
		} catch (Exception ex) {
			logger.error("AnnEndpointManagement clonning failed ", ex);
			return null;
		}
		return clone;
	}    
    
    
}
