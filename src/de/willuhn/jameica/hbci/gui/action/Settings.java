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
 * Action fuer Settings.
 */
public class Settings implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
  	GUI.startView(de.willuhn.jameica.hbci.gui.views.Settings.class,null);
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.2  2005-01-19 00:16:04  willuhn
 * @N Lastschriften
 *
 * Revision 1.1  2004/10/12 23:48:39  willuhn
 * @N Actions
 *
 **********************************************************************/