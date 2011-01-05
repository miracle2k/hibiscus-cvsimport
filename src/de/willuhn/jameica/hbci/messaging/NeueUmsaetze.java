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

import java.rmi.RemoteException;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.UmsatzUtil;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.logging.Logger;

/**
 * Ueber die Klasse koennen die in der aktuellen Session
 * abgerufenen Umsaetze ermittelt werden.
 */
public class NeueUmsaetze implements MessageConsumer
{
  private static String first = null;

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
    return new Class[]{ImportMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    // Wenn es keine Import-Message ist oder wir schon den ersten Umsatz haben,
    // ignorieren wir die folgenden
    if (first != null || message == null || !(message instanceof ImportMessage))
      return;
    
    GenericObject o = ((ImportMessage)message).getObject();
    
    if (o == null || o.getID() == null || !(o instanceof Umsatz))
      return; // interessiert uns nicht
    
    
    first = o.getID();
  }
  
  /**
   * Liefert eine Liste mit allen in der aktuellen Sitzung hinzugekommenen Umsaetzen.
   * @return Liste der neuen Umsaetze.
   * @throws RemoteException
   */
  public static GenericIterator getNeueUmsaetze() throws RemoteException
  {
    if (first == null)
      return PseudoIterator.fromArray(new Umsatz[0]);

    DBIterator list = UmsatzUtil.getUmsaetzeBackwards();
    list.addFilter("id >= " + first);
    if (list.size() == 0)
      first = null; // Wenn nichts gefunden wurde, resetten wir uns
    return list;
  }
  
  /**
   * Liefert die ID des ersten in der aktuellen Sitzung eingetroffenen
   * Umsatzes oder <code>null</code>, wenn noch keine neuen Umsaetze hinzugekommen sind.
   * @return die ID des ersten neuen Umsatzes (alle Folge-Umsaetze haben groessere IDs) oder <code>null</code>.
   */
  public static String getID()
  {
    return first;
  }

  /**
   * Liefert true, wenn der Umsatz in der aktuellen Sitzung abgerufen wurde.
   * @param u der zu pruefende Umsatz.
   * @return true, wenn er neu ist.
   */
  public static boolean isNew(Umsatz u)
  {
    if (first == null || u == null)
      return false;

    try
    {
      return (((Integer)u.getAttribute("id-int")).compareTo(new Integer(first)) >= 0);
    }
    catch (Exception e)
    {
      Logger.error("unable to determine new state",e);
    }
    return false;
  }
  
}


/*********************************************************************
 * $Log$
 * Revision 1.6  2011-01-05 11:19:10  willuhn
 * @N Fettdruck (bei neuen Umsaetzen) und grauer Text (bei Vormerkbuchungen) jetzt auch in "Umsaetze nach Kategorien"
 * @N NeueUmsaetze.isNew(Umsatz) zur Pruefung, ob ein Umsatz neu ist
 *
 * Revision 1.5  2007/08/09 12:04:39  willuhn
 * @N Bug 302
 *
 * Revision 1.4  2007/08/07 23:54:15  willuhn
 * @B Bug 394 - Erster Versuch. An einigen Stellen (z.Bsp. konto.getAnfangsSaldo) war ich mir noch nicht sicher. Heiner?
 *
 * Revision 1.3  2007/04/19 18:12:21  willuhn
 * @N MySQL-Support (GUI zum Konfigurieren fehlt noch)
 *
 * Revision 1.2  2007/03/16 14:40:02  willuhn
 * @C Redesign ImportMessage
 * @N Aktualisierung der Umsatztabelle nach Kategorie-Zuordnung
 *
 * Revision 1.1  2007/01/30 18:25:33  willuhn
 * @N Bug 302
 *
 **********************************************************************/