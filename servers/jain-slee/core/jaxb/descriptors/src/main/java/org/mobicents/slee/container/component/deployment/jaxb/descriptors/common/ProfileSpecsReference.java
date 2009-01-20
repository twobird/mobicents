/**
 * Start time:10:34:43 2009-01-20<br>
 * Project: mobicents-jainslee-server-core<br>
 * 
 * @author <a href="mailto:baranowb@gmail.com">baranowb - Bartosz Baranowski
 *         </a>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
package org.mobicents.slee.container.component.deployment.jaxb.descriptors.common;

import org.mobicents.slee.container.component.ComponentKey;


/**
 * Start time:10:34:43 2009-01-20<br>
 * Project: mobicents-jainslee-server-core<br>
 * 
 * @author <a href="mailto:baranowb@gmail.com">baranowb - Bartosz Baranowski
 *         </a>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public class ProfileSpecsReference {

	//This is common, either we have 3 identical classes or this one common taking strings as args
	
	private String description=null;
	//used in sbbs
	private String alias=null;
	private ComponentKey referenceKey=null;
	public ProfileSpecsReference(String description, String alias, String name, String vendor, String version) {
		super();
		this.description = description;
		this.alias = alias;
		this.referenceKey=new ComponentKey(name,vendor,version);
	}
	public String getDescription() {
		return description;
	}
	public String getAlias() {
		return alias;
	}
	public ComponentKey getReferenceKey() {
		return referenceKey;
	}
	
	
	
}