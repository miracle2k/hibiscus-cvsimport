/**********************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.messaging;

import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.system.Application;

/**
 * Leitet das Event "Saldo-Update bei einem Konto" an das Scripting-Plugin weiter.
 */
public class ScriptingSaldoMessageConsumer implements MessageConsumer
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
    return new Class[]{SaldoMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    SaldoMessage m = (SaldoMessage) message;
    Application.getMessagingFactory().getMessagingQueue("jameica.scripting").sendMessage(new QueryMessage("hibiscus_kontoSaldoChanged",m.getObject()));
  }
}



/**********************************************************************
 * $Log$
 * Revision 1.1  2010-07-25 23:11:59  willuhn
 * @N Erster Code fuer Scripting-Integration
 *
 **********************************************************************/