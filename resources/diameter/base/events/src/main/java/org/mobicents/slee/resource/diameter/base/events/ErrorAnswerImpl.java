package org.mobicents.slee.resource.diameter.base.events;

import net.java.slee.resource.diameter.base.events.DiameterCommand;
import net.java.slee.resource.diameter.base.events.DiameterHeader;
import net.java.slee.resource.diameter.base.events.ErrorAnswer;
import net.java.slee.resource.diameter.base.events.avp.AvpList;
import net.java.slee.resource.diameter.base.events.avp.AvpNotAllowedException;
import net.java.slee.resource.diameter.base.events.avp.DiameterIdentityAvp;
import net.java.slee.resource.diameter.base.events.avp.ProxyInfoAvp;

public class ErrorAnswerImpl implements ErrorAnswer
{

  public DiameterIdentityAvp getErrorReportingHost()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public AvpList getExtensionAvps()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public DiameterIdentityAvp getOriginHost()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public DiameterIdentityAvp getOriginRealm()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public long getOriginStateId()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  public ProxyInfoAvp getProxyInfo()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public long getResultCode()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  public String getSessionId()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean hasErrorReportingHost()
  {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean hasOriginHost()
  {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean hasOriginRealm()
  {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean hasOriginStateId()
  {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean hasProxyInfo()
  {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean hasResultCode()
  {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean hasSessionId()
  {
    // TODO Auto-generated method stub
    return false;
  }

  public void setErrorReportingHost( DiameterIdentityAvp errorReportingHost )
  {
    // TODO Auto-generated method stub

  }

  public void setExtensionAvps( AvpList avps ) throws AvpNotAllowedException
  {
    // TODO Auto-generated method stub

  }

  public void setOriginHost( DiameterIdentityAvp originHost )
  {
    // TODO Auto-generated method stub

  }

  public void setOriginRealm( DiameterIdentityAvp originRealm )
  {
    // TODO Auto-generated method stub

  }

  public void setOriginStateId( long originStateId )
  {
    // TODO Auto-generated method stub

  }

  public void setProxyInfo( ProxyInfoAvp proxyInfo )
  {
    // TODO Auto-generated method stub

  }

  public void setResultCode( long resultCode )
  {
    // TODO Auto-generated method stub

  }

  public void setSessionId( String sessionId )
  {
    // TODO Auto-generated method stub

  }

  public AvpList getAvps()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public DiameterCommand getCommand()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public DiameterIdentityAvp getDestinationHost()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public DiameterIdentityAvp getDestinationRealm()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public DiameterHeader getHeader()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public void setDestinationHost( DiameterIdentityAvp destinationHost )
  {
    // TODO Auto-generated method stub

  }

  public void setDestinationRealm( DiameterIdentityAvp destinationRealm )
  {
    // TODO Auto-generated method stub

  }

  public Object clone()
  {
    // TODO Auto-generated method stub
    return null;
  }

}
