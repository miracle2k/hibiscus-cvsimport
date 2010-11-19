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

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

/**
 * Startet die View mit den Terminen.
 */
public class Termine implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    GUI.startView(de.willuhn.jameica.hbci.gui.views.Termine.class,context);
  }
}



/**********************************************************************
 * $Log$
 * Revision 1.1  2010-11-19 18:37:19  willuhn
 * @N Erste Version der Termin-View mit Appointment-Providern
 *
 **********************************************************************/