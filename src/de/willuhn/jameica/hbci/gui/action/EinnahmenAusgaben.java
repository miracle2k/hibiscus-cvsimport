/**********************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 * $Locker$
 * $State$
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

/**
 * Action zum Oeffnen der Einnahmen/Ausgaben-Übersicht.
 */
public class EinnahmenAusgaben implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    GUI.startView(de.willuhn.jameica.hbci.gui.views.EinnahmenAusgaben.class,context);
  }

}


/*********************************************************************
 * $Log$
 * Revision 1.1  2007-06-04 15:57:39  jost
 * Neue Auswertung: Einnahmen/Ausgaben
 *
 **********************************************************************/