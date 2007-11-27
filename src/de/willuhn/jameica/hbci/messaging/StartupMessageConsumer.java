/**********************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 * $Locker$
 * $State$
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.messaging;

import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.SystemMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Fuehrt Kommandos aus, die erst dann erfolgen sollen, wenn
 * Jameica vollstaendig gestartet ist.
 */
public class StartupMessageConsumer implements MessageConsumer
{

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    return true;
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{SystemMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    if (message == null || !(message instanceof SystemMessage))
      return;
    
    SystemMessage msg = (SystemMessage) message;
    if (msg.getStatusCode() != SystemMessage.SYSTEM_STARTED)
      return;

    Logger.info("register message consumers for query lookups");
    Application.getMessagingFactory().getMessagingQueue("hibiscus.query.bankname").registerMessageConsumer(new QueryBanknameMessageConsumer());
    Application.getMessagingFactory().getMessagingQueue("hibiscus.query.accountcrc").registerMessageConsumer(new QueryAccountCRCMessageConsumer());
  }

}


/*********************************************************************
 * $Log$
 * Revision 1.1  2007-11-27 16:41:48  willuhn
 * @C MessageConsumers fuer Query-Lookups wurden zu frueh registriert
 *
 **********************************************************************/