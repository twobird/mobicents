/*
 * Mobicents, Communications Middleware
 * 
 * Copyright (c) 2008, Red Hat Middleware LLC or third-party
 * contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Middleware LLC.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 *
 * Boston, MA  02110-1301  USA
 */
package org.mobicents.media.server.ctrl.mgcp;

import jain.protocol.ip.JainIPFactory;
import jain.protocol.ip.mgcp.DeleteProviderException;
import jain.protocol.ip.mgcp.JainMgcpCommandEvent;
import jain.protocol.ip.mgcp.JainMgcpEvent;
import jain.protocol.ip.mgcp.JainMgcpListener;
import jain.protocol.ip.mgcp.JainMgcpProvider;
import jain.protocol.ip.mgcp.JainMgcpResponseEvent;
import jain.protocol.ip.mgcp.message.Constants;
import jain.protocol.ip.mgcp.message.CreateConnection;
import jain.protocol.ip.mgcp.message.DeleteConnection;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.mobicents.media.server.spi.Connection;
import org.mobicents.mgcp.stack.JainMgcpStackImpl;

/**
 * 
 * @author kulikov
 */
public class MgcpController implements JainMgcpListener, MgcpControllerMBean {

	private static final Logger logger = Logger.getLogger(MgcpController.class);

	private JainMgcpProvider mgcpProvider;
	private JainMgcpStackImpl mgcpStack;
	private JainIPFactory jainFactory;

	private InetAddress inetAddress = null;
	private String bindAddress = null;
	private int port = 2727;

	private ConcurrentHashMap<String, Call> calls = new ConcurrentHashMap<String, Call>();

	public MgcpController() {
	}

	public String getBindAddress() {
		return this.bindAddress;
	}

	public void setBindAddress(String bindAddress) throws UnknownHostException {
		this.bindAddress = bindAddress;
		this.inetAddress = InetAddress.getByName(bindAddress);
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void create() {
		logger.info("Starting MGCP Controller module for MMS");
	}

	/**
	 * Starts MGCP controller.
	 * 
	 * @throws java.lang.Exception
	 */
	public void start() throws Exception {

		// jainFactory = JainIPFactory.getInstance();
		// jainFactory.setPathName("org.mobicents");

		mgcpStack = new JainMgcpStackImpl(this.inetAddress, this.port);

		mgcpProvider = mgcpStack.createProvider();
		mgcpProvider.addJainMgcpListener(this);

		this.port = mgcpStack.getPort();
		logger.info("Started MGCP Controller module for MMS");
	}

	/**
	 * Stops MGCP controller.
	 * 
	 * @throws java.lang.Exception
	 */
	public void stop() {
		logger.info("Stoping MGCP Controller module for MMS. Listening at IP " + this.inetAddress + " port "
				+ this.port);
		mgcpProvider.removeJainMgcpListener(this);
		try {
			mgcpStack.deleteProvider(mgcpProvider);
		} catch (DeleteProviderException e) {			
			e.printStackTrace();
		}
	}

	public void destroy() {
		logger.info("Stopped MGCP Controller module for MMS");
	}

	/**
	 * Processes a Command Event object received from a JainMgcpProvider.
	 * 
	 * @param evt -
	 *            The JAIN MGCP Command Event Object that is to be processed.
	 */
	public void processMgcpCommandEvent(JainMgcpCommandEvent evt) {
		// define action to be performed
		Callable<JainMgcpResponseEvent> action = null;

		// construct object implementing requested action using
		// object identifier
		int eventID = evt.getObjectIdentifier();
		switch (eventID) {
		case Constants.CMD_CREATE_CONNECTION:
			action = new CreateConnectionAction(this, (CreateConnection) evt);
			break;
		case Constants.CMD_MODIFY_CONNECTION:
			break;
		case Constants.CMD_DELETE_CONNECTION:
			action = new DeleteConnectionAction(this, (DeleteConnection) evt);
			break;
		default:
			logger.error("Unknown message type: " + eventID);
			return;
		}

		// try to perform action and send response back.
		try {
			JainMgcpResponseEvent response = action.call();
			mgcpProvider.sendMgcpEvents(new JainMgcpEvent[] { response });
		} catch (Exception e) {
			logger.error("Unexpected error during processing,Caused by ", e);
		}

	}

	/**
	 * Processes a Response Event object (acknowledgment to a Command Event
	 * object) received from a JainMgcpProvider.
	 * 
	 * @param evt -
	 *            The JAIN MGCP Response Event Object that is to be processed.
	 */
	public void processMgcpResponseEvent(JainMgcpResponseEvent evt) {
	}

	protected Call getCall(String callID) {
		return calls.get(callID);
	}

	protected void addCall(Call call) {
		calls.put(call.getID(), call);
	}

	protected void removeCall(String callID) {
		calls.remove(callID);
	}

	protected Collection<ConnectionActivity> getActivities(String endpointName) {
		ArrayList<ConnectionActivity> list = new ArrayList();
		for (Call call : calls.values()) {
			Collection<ConnectionActivity> activities = call.getActivities();
			for (ConnectionActivity activity : activities) {
				if (activity.getMediaConnection().getEndpoint().getLocalName().equals(endpointName)) {
					list.add(activity);
				}
			}
		}
		return list;
	}

	protected ConnectionActivity getActivity(String endpointName, String connectionID) {
		for (Call call : calls.values()) {
			Collection<ConnectionActivity> activities = call.getActivities();
			for (ConnectionActivity activity : activities) {
				Connection connection = activity.getMediaConnection();
				if (connection.getEndpoint().getLocalName().equals(endpointName)
						&& connection.getId().equals(connectionID)) {
					return activity;
				}
			}
		}
		return null;
	}

}
