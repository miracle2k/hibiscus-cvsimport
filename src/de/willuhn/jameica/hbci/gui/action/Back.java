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
 * Allgemeine Action fuer "Einen Schritt in der History der Navigation zurueck.
 */
public class Back implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
  	GUI.startPreviousView();
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.1  2004-10-19 23:33:31  willuhn
 * *** empty log message ***
 *
 **********************************************************************/