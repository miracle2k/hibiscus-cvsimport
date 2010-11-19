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

package de.willuhn.jameica.hbci.gui.parts;

import de.willuhn.jameica.gui.calendar.AppointmentProvider;
import de.willuhn.jameica.gui.calendar.CalendarPart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ClassFinder;

/**
 * Zeigt die anstehenden Termine in Hibiscus an.
 */
public class Termine extends CalendarPart
{
  /**
   * ct.
   */
  public Termine()
  {
    super();

    // Wir laden automatisch die Termin-Provider.
    try
    {
      ClassFinder finder = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getClassLoader().getClassFinder();
      Class[] classes = finder.findImplementors(AppointmentProvider.class);
      for (Class c:classes)
      {
        try
        {
          addAppointmentProvider((AppointmentProvider)c.newInstance());
        }
        catch (Exception e)
        {
          Logger.error("unable to load appointment provider " + c +", skipping",e);
        }
      }
    }
    catch (ClassNotFoundException e)
    {
      Logger.debug("no appointment providers found");
    }
  }
}



/**********************************************************************
 * $Log$
 * Revision 1.1  2010-11-19 18:37:19  willuhn
 * @N Erste Version der Termin-View mit Appointment-Providern
 *
 **********************************************************************/