/**
 * Start time:09:06:33 2009-04-06<br>
 * Project: mobicents-isup-stack<br>
 * 
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski
 *         </a>
 * 
 */
package org.mobicents.ss7.isup.message.parameter;

import java.io.IOException;

import org.mobicents.ss7.isup.ParameterRangeInvalidException;

/**
 * Start time:09:06:33 2009-04-06<br>
 * Project: mobicents-isup-stack<br>
 * 
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski
 *         </a>
 */
public class ReturnToInvokingExchangeCallIdentifierImpl extends AbstractParameter implements ReturnToInvokingExchangeCallIdentifier{
	

	public ReturnToInvokingExchangeCallIdentifierImpl() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.mobicents.isup.ISUPComponent#decodeElement(byte[])
	 */
	public int decodeElement(byte[] b) throws ParameterRangeInvalidException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.mobicents.isup.ISUPComponent#encodeElement()
	 */
	public byte[] encodeElement() throws IOException {
		// TODO Auto-generated method stub
		return new byte[]{};
	}
	public int getCode() {

		return _PARAMETER_CODE;
	}
}
