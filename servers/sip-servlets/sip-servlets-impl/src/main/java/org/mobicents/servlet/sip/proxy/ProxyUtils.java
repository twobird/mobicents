package org.mobicents.servlet.sip.proxy;

import gov.nist.javax.sip.header.HeaderFactoryImpl;
import gov.nist.javax.sip.header.ims.PathHeader;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Vector;

import javax.sip.ClientTransaction;
import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.address.Address;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletResponseImpl;

public class ProxyUtils {
	
	private SipProvider provider;
	private ProxyImpl proxy;
	
	public ProxyUtils(SipProvider provider, ProxyImpl proxy)
	{
		this.provider = provider;
		this.proxy = proxy;
	}
	
	public SipServletRequestImpl createProxiedRequest(SipServletRequestImpl originalRequest, ProxyBranchImpl proxyBranch, ProxyParams params)
	{
		try {
			Request clonedRequest = (Request) originalRequest.getMessage().clone();

			javax.sip.address.SipURI target = SipFactories.addressFactory.createSipURI(
					params.target.getUser(), params.target.getHost());
			clonedRequest.setRequestURI(target);

			// Decrease max forwards if available
			MaxForwardsHeader mf = (MaxForwardsHeader) clonedRequest
				.getHeader(MaxForwardsHeader.NAME);
			if (mf == null) {
				mf = SipFactories.headerFactory.createMaxForwardsHeader(70);
				clonedRequest.addHeader(mf);
			} else {
				mf.setMaxForwards(mf.getMaxForwards() - 1);
			}

			// Add route header
			javax.sip.address.SipURI routeUri = SipFactories.addressFactory.createSipURI(
					params.target.getUser(), params.target.getHost());
			routeUri.setPort(params.target.getPort());
			routeUri.setLrParam();
			javax.sip.address.Address address = SipFactories.addressFactory.createAddress(params.target.getUser(),
					routeUri);
			RouteHeader rheader = SipFactories.headerFactory.createRouteHeader(address);
			
			clonedRequest.setHeader(rheader);
			
			//Add via header
			Iterator lps = provider.getSipStack().getListeningPoints();
			ListeningPoint lp = (ListeningPoint) lps.next();
			String stackIPAddress = params.stack.getHost() == null ? lp.getIPAddress() : params.stack.getHost();

			ViaHeader viaHeader = null;

			if (clonedRequest.getMethod().equals(Request.CANCEL)) {
				// Branch Id will be assigned by the stack.
				viaHeader = SipFactories.headerFactory.createViaHeader(stackIPAddress,
						lp.getPort(), lp.getTransport(), null);

				if (clonedRequest.getMethod().equals(Request.CANCEL)) {
					// Cancel is hop by hop so remove all other via headers.
					clonedRequest.removeHeader(ViaHeader.NAME);
				}
			} else {

				viaHeader = SipFactories.headerFactory.createViaHeader(stackIPAddress,
						lp.getPort(), lp.getTransport(), generateBranchId());
			}

			if (viaHeader != null) {
				clonedRequest.addHeader(viaHeader);
				
			}
			
			//Add route-record header, if enabled and if needed (if not null)
			if(params.routeRecord != null)
			{
				javax.sip.address.SipURI rrURI = SipFactories.addressFactory.createSipURI(null,
						stackIPAddress);
				rrURI.setPort(lp.getPort());
				rrURI.setTransportParam(lp.getTransport());
				
				Iterator<String> paramNames = params.routeRecord.getParameterNames();
				
				// Copy the parameters set by the user
				while(paramNames.hasNext())
				{
					String paramName = paramNames.next();
					rrURI.setParameter(paramName,
							params.routeRecord.getParameter(paramName));
				}
				
				Address rraddress = SipFactories.addressFactory
				.createAddress(null, rrURI);
				RecordRouteHeader recordRouteHeader = SipFactories.headerFactory
				.createRecordRouteHeader(rraddress);
				
				ListIterator recordRouteHeaders = clonedRequest
				.getHeaders(RecordRouteHeader.NAME);
				clonedRequest.removeHeader(RecordRouteHeader.NAME);
				Vector v = new Vector();
				v.addElement(recordRouteHeader);
				// add the other record route headers.
				while (recordRouteHeaders != null
						&& recordRouteHeaders.hasNext()) {
					recordRouteHeader = (RecordRouteHeader) recordRouteHeaders
					.next();
					v.addElement(recordRouteHeader);
				}
				for (int j = 0; j < v.size(); j++) {
					recordRouteHeader = (RecordRouteHeader) v.elementAt(j);
					clonedRequest.addHeader(recordRouteHeader);
				}
			}
			
			// Add path header
			if(params.path != null)
			{
				javax.sip.address.SipURI pathURI = SipFactories.addressFactory.createSipURI(null,
						stackIPAddress);
				pathURI.setPort(lp.getPort());
				pathURI.setTransportParam(lp.getTransport());

				Iterator<String> paramNames = params.path.getParameterNames();
				
				// Copy the parameters set by the user
				while(paramNames.hasNext())
				{
					String paramName = paramNames.next();
					pathURI.setParameter(paramName,
							params.path.getParameter(paramName));
				}
				
				Address pathAddress = SipFactories.addressFactory
					.createAddress(null, pathURI);
				
				// Here I need to reference the header factory impl class because can't create path header otherwise
				PathHeader pathHeader = ((HeaderFactoryImpl)SipFactories.headerFactory)
					.createPathHeader(pathAddress);
				
				ListIterator pathHeaders = clonedRequest
					.getHeaders(PathHeader.NAME);
				clonedRequest.removeHeader(PathHeader.NAME);
				Vector v = new Vector();
				v.addElement(pathHeader);
				// add the other record route headers.
				while (pathHeaders != null
						&& pathHeaders.hasNext()) {
					pathHeader = (PathHeader) pathHeaders
					.next();
					v.addElement(pathHeader);
				}
				for (int j = 0; j < v.size(); j++) {
					pathHeader = (PathHeader) v.elementAt(j);
					clonedRequest.addHeader(pathHeader);
				}
			}
			
			ClientTransaction tx = provider.getNewClientTransaction(clonedRequest);
			SipServletRequestImpl ret = new	SipServletRequestImpl(
					clonedRequest,
					provider,
					originalRequest.getSession(),
					tx, null, false);
			
			ret.getTrasactionApplicationData().setProxyBranch(proxyBranch);
			tx.setApplicationData(ret.getTrasactionApplicationData());

			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public SipServletResponseImpl createProxiedResponse(SipServletResponseImpl sipServetResponse)
	{
		Response response = (Response)sipServetResponse.getMessage();
		Response clonedResponse = (Response)  response.clone();

		// 1. TODO: Update timer C for provisional responses


			
		// 2. Remove topmost via
		Iterator viaHeaderIt = clonedResponse.getHeaders(ViaHeader.NAME);
		viaHeaderIt.next();
		viaHeaderIt.remove();
		if (!viaHeaderIt.hasNext())
			return null; // response was meant for this proxy
		
		SipServletRequestImpl originalRequest =
			(SipServletRequestImpl) this.proxy.getOriginalRequest();
		
		return new SipServletResponseImpl(clonedResponse,
				this.provider,
				originalRequest.getTransaction(),
				originalRequest.getSession(),
				null);


	}
	public static synchronized String generateBranchId() throws Exception {
		StringBuffer ret = new StringBuffer();
		StringBuffer b = new StringBuffer();

		b.append(new Integer((int) (Math.random() * 10000)).toString()
				+ System.currentTimeMillis());

		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			byte[] bytes = messageDigest.digest(b.toString().getBytes());
			String hex = toHexString(bytes);

			ret.append("z9hG4bK" + hex);
		} catch (NoSuchAlgorithmException ex) {
			throw ex;
		}

		return ret.toString();
	}
	
	public static String toHexString(byte[] b) {
		int pos = 0;
		char[] c = new char[b.length * 2];

		for (int i = 0; i < b.length; i++) {
			c[pos++] = toHex[(b[i] >> 4) & 0x0F];
			c[pos++] = toHex[b[i] & 0x0f];
		}

		return new String(c);
	}
	
	private static final char[] toHex = { '0', '1', '2', '3', '4', '5', '6',
		'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
}
