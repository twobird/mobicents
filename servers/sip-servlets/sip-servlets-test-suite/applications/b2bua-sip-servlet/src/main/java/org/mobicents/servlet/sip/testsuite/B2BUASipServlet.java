package org.mobicents.servlet.sip.testsuite;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.SipErrorEvent;
import javax.servlet.sip.SipErrorListener;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

public class B2BUASipServlet extends SipServlet implements SipErrorListener,
		Servlet {

	private static Log logger = LogFactory.getLog(B2BUASipServlet.class);

	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {
		logger.info("Got request:\n" + request);
		SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(
				SIP_FACTORY);
		B2buaHelper helper = request.getB2buaHelper();
		SipServletRequest forkedRequest = helper.createRequest(request, true,
				null);
		SipURI sipUri = (SipURI) sipFactory.createURI("sip:aa@127.0.0.1:5059");
		if (logger.isDebugEnabled()) {
			logger.debug("forkedRequest = " + forkedRequest);

		}
		forkedRequest.setRequestURI(sipUri);
		forkedRequest.send();

	}

	/**
	 * {@inheritDoc}
	 */
	protected void doResponse(SipServletResponse sipServletResponse)
			throws ServletException, IOException {
		logger.info("Got : " + sipServletResponse.getStatus() + " "
				+ sipServletResponse.getMethod());
//		int status = sipServletResponse.getStatus();
//		if (status == SipServletResponse.SC_OK) {
//			SipServletRequest ackRequest = sipServletResponse.createAck();
//			ackRequest.send();
//		} else {
			super.doResponse(sipServletResponse);
//		}
	}
	
	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {

		SipServletResponse response = request.createResponse(200);
		response.send();

		SipSession session = request.getSession();
		B2buaHelper helper = request.getB2buaHelper();
		SipSession linkedSession = helper.getLinkedSession(session);
		SipServletRequest newRequest = linkedSession.createRequest("BYE");
		newRequest.send();

	}

	public void noAckReceived(SipErrorEvent ee) {
		// TODO Auto-generated method stub

	}

	public void noPrackReceived(SipErrorEvent ee) {
		// TODO Auto-generated method stub

	}

}
