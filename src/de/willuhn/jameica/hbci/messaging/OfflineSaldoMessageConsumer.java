/**********************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 * $Locker$
 * $State$
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.messaging;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;

/**
 * Pr�ft eingehende oder geloeschte Ums�tze, ob sich diese auf ein Offline-Konto
 * beziehen und aktualisiert den Saldo in das Offline-Konto.
 */
public class OfflineSaldoMessageConsumer implements MessageConsumer
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
    return new Class[]{ImportMessage.class,ObjectDeletedMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    // Wenn es keine Import-Message ist ignorieren wir die folgenden
    if (message == null)
      return;

    // Andernfalls wurde der Umsatz geloescht
    boolean isImport = (message instanceof ImportMessage);
    
    GenericObject o = ((ObjectMessage)message).getObject();
    
    if (!(o instanceof Umsatz))
      return; // interessiert uns nicht
    
    // wir haben einen Umsatz, den es zu bearbeiten gilt...
    Umsatz u = (Umsatz) o;

    Konto k = u.getKonto();
    if (k == null)
      return;
    
    // Offline-Konto?
    if ((k.getFlags() & Konto.FLAG_OFFLINE) != Konto.FLAG_OFFLINE)
      return;

    // Betrag der Buchung
    double betrag = u.getBetrag();
    if (Double.isNaN(betrag))
      return;

    // neuen Saldo ausrechnen
    double saldo = k.getSaldo();
    if (Double.isNaN(saldo))
      saldo = 0.0d;

    // Neuen Saldo uebernehmen
    if (isImport)
      k.setSaldo(saldo + betrag);
    else
      k.setSaldo(saldo - betrag);

    k.store();
    
    // Geaendertes Konto bekanntmachen
    Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(k));
  }
}

/*******************************************************************************
 * $Log$
 * Revision 1.1  2010-11-08 10:37:00  willuhn
 * @N BUGZILLA 945
 *
 ******************************************************************************/