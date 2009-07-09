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

package de.willuhn.jameica.hbci.gui.action;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Markiert ein Konto als Default.
 */
public class KontoMarkDefault implements Action
{
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null || !(context instanceof Konto))
      return;
    
    // Wenn vorher ein anderes Konto als Default markiert war,
    // entfernen wir erst dieses
    Konto prev = Settings.getDefaultKonto();
    if (prev != null)
    {
      Settings.setDefaultKonto(null);
      Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(prev));
    }
    
    // Jetzt das neue zum Default machen
    Konto k = (Konto) context;
    
    // wenn es bereits das Default-Konto war, entfernen wir die
    // Markierung wieder.
    try
    {
      boolean isDefault = prev != null && prev.equals(k);
      Settings.setDefaultKonto(isDefault ? null : k);
      Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(k));
    }
    catch (RemoteException re)
    {
      Logger.error("unable to mark account as default",re);
    }
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.2  2009-07-09 13:02:56  willuhn
 * @N Durch erneutes Auswaehlen von "als Standardkonto festlegen" kann die Markierung wieder rueckgaengig gemacht werden
 *
 * Revision 1.1  2009/01/04 16:38:55  willuhn
 * @N BUGZILLA 523 - ein Konto kann jetzt als Default markiert werden. Das wird bei Auftraegen vorausgewaehlt und ist fett markiert
 *
 **********************************************************************/
