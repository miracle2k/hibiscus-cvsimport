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
package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

/**
 * Action fuer die Detail-Ansicht eines Umsatzes.
 */
public class UmsatzDetail implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Umsatz</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		GUI.startView(de.willuhn.jameica.hbci.gui.views.UmsatzDetail.class.getName(),context);
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.1  2004-10-18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 **********************************************************************/