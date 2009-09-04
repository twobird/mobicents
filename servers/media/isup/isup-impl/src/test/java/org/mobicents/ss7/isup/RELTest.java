/**
 * Start time:15:07:07 2009-07-17<br>
 * Project: mobicents-isup-stack<br>
 * 
 * @author <a href="mailto:baranowb@gmail.com">Bartosz Baranowski </a>
 * 
 */
package org.mobicents.ss7.isup;

import java.util.Arrays;

import org.mobicents.ss7.isup.ReleaseMessageImpl;

/**
 * Start time:15:07:07 2009-07-17<br>
 * Project: mobicents-isup-stack<br>
 * 
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 */
public class RELTest extends MessageHarness{

	public void testOne() throws Exception
	{
		//FIXME: for now we strip MTP part
		byte[] message={
				//9F,
				//EB,
				//0D,
				//C5,
				//00,
				//B7,
				//D1,
				//8D,
				//08,
				//00,
				0x0C,
				0x02,
				0x00,
				0x02,
				(byte) 0x83,
				(byte) 0x9F


		};

		ReleaseMessageImpl iam=new ReleaseMessageImpl(this,message);
		byte[] encodedBody = iam.encodeElement();
		boolean equal = Arrays.equals(message, encodedBody);
		assertTrue(super.makeCompare(message, encodedBody),equal);
	}
	
}
