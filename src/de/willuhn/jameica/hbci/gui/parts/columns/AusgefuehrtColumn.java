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

package de.willuhn.jameica.hbci.gui.parts.columns;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Vorformatierte Spalte fuer den Ausfuehrungsstatus.
 */
public class AusgefuehrtColumn extends Column
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * ct.
   */
  public AusgefuehrtColumn()
  {
    super("ausgefuehrt_am",i18n.tr("Ausgeführt?"));
  }

  /**
   * @see de.willuhn.jameica.gui.parts.Column#getFormattedValue(java.lang.Object, java.lang.Object)
   */
  public String getFormattedValue(Object value, Object context)
  {
    if (context != null && (context instanceof Terminable))
    {
      try
      {
        Terminable t = (Terminable) context;

        // Nicht ausgefuehrt
        if (!t.ausgefuehrt())
          return i18n.tr("offen");
        
        // Das sind die neuen mit Ausfuehrungs-Datum
        if (value != null && (value instanceof Date))
          return HBCI.LONGDATEFORMAT.format((Date)value);
        
        // Das sind die alten ohne Ausfuehrungs-Datum
        return i18n.tr("ausgeführt");
      }
      catch (RemoteException re)
      {
        Logger.error("unable to format attribute " + value + " for bean " + context);
      }
    }
    return super.getFormattedValue(value,context);
  }
  
  
}


/**********************************************************************
 * $Log$
 * Revision 1.2  2011-04-29 15:33:28  willuhn
 * @N Neue Spalte "ausgefuehrt_am", in der das tatsaechliche Ausfuehrungsdatum von Auftraegen vermerkt wird
 *
 * Revision 1.1  2009/02/17 00:00:02  willuhn
 * @N BUGZILLA 159 - Erster Code fuer Auslands-Ueberweisungen
 *
 **********************************************************************/
