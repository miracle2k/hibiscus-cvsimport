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
package de.willuhn.jameica.hbci.gui.listener;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.plugin.PluginLoader;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Listener, der zur Ausfuehrung einer Ueberweisung verwendet werden kann.
 * Er erwartet ein Objekt vom Typ <code>Ueberweisung</code> im <code>data</code>-Member
 * des Events.
 */
public class UeberweisungExecute implements Listener
{

	private I18N i18n = null;

  /**
   * ct.
   */
  public UeberweisungExecute()
  {
    super();
    i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
	 * Erwartet ein Objekt vom Typ <code>Ueberweisung</code> im <code>data</code>-Member
	 * des Events.
   * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
   */
  public void handleEvent(Event event)
  {
		try {
			Ueberweisung u = (Ueberweisung) event.data;
			if (u == null)
				return;
			if (u.ausgefuehrt())
			{
				GUI.getView().setErrorText(i18n.tr("Die �berweisung wurde bereits ausgef�hrt."));
				return;
			}
			u.execute();
		}
		catch (ApplicationException e)
		{
			GUI.getStatusBar().setErrorText(e.getMessage());
		}
		catch (Exception e2)
		{
			Logger.error("error while executing ueberweisung",e2);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausf�hren der �berweisung"));
		}
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.2  2004-07-21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 **********************************************************************/