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
 * Action fuer die Kontoauszüge.
 */
public class KontoauszugRpt implements Action
{
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    GUI
        .startView(de.willuhn.jameica.hbci.gui.views.KontoauszugList.class,
            null);
  }
}

/*******************************************************************************
 * $Log$
 * Revision 1.1  2006-05-14 19:49:24  jost
 * Prerelease Kontoauszug-Report
 *
 ******************************************************************************/
