/***************************************************
 *                                                 *
 *  Mobicents: The Open Source VoIP Platform       *
 *                                                 *
 *  Distributable under LGPL license.              *
 *  See terms of license at gnu.org.               *
 *                                                 *
 ***************************************************/
package org.mobicents.ant.tasks;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mobicents.ant.SubTask;

import org.mobicents.slee.container.management.jmx.SleeCommandInterface;

public class ChangeSleeStateTask implements SubTask {
	// Obtain a suitable logger.
    private static Logger logger = Logger.getLogger(org.mobicents.ant.tasks.ChangeSleeStateTask.class.getName());
	
    public void run(SleeCommandInterface slee) {	
    	String command = null;
        
		if (state.equals(START)) {
    		command = "-startSlee";
    	}
    	else if (state.equals(STOP)) {
    		command = "-stopSlee";
    	}
    	
		if (state.equals(NO_CHANGE)) {
			logger.warning("Bad result: state=start or state=stop");
		}
		else {
			try {
	    		// Invoke the operation
				Object result = slee.invokeOperation(command, null, null, null);
				
	    		if (result == null)
	    		{
	    			logger.info("No response");
	    		}
	    		else
	    		{
	    			logger.info(result.toString());
	    		}
			}
	    	
	    	catch (Exception ex)
			{
	    		// Log the error
	    		logger.log(Level.WARNING, "Bad result: " + slee.commandBean + "." + slee.commandString +
	            		"\n" + ex.getCause().toString());
			}
		}
    }
    
	// The setter for the "state" attribute
	public void setState(String state) {
		this.state = state;
	}
    
	private String state = NO_CHANGE;	
}