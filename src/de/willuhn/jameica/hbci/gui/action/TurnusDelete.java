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

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.SimpleDialog;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action fuer Loeschen eines Empfaengers.
 * @deprecated kann mit in Controller
 */
public class TurnusDelete implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Turnus</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
  	I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null || !(context instanceof Turnus))
			throw new ApplicationException(i18n.tr("Kein Turnus ausgew�hlt"));

		try {

			Turnus t = (Turnus) context;
			if (t.isNewObject())
				return;

			if (t.isInitial())
			{
				SimpleDialog d = new SimpleDialog(SimpleDialog.POSITION_MOUSE);
				d.setTitle(i18n.tr("Nicht l�schbar"));
				d.setText(i18n.tr("Dieser Zahlungsturnus kann nicht gel�scht werden, da er Bestandteil der Initialdaten von Hibiscus ist."));
				try
				{
					d.open();
				}
				catch (Exception e)
				{
					GUI.getStatusBar().setErrorText(i18n.tr("Dieser Zahlungsturnus kann nicht gel�scht werden, da er Bestandteil der Initialdaten von Hibiscus ist."));
				}
				return;
			}

			YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
			d.setTitle(i18n.tr("Turnus l�schen"));
			d.setText(i18n.tr("Wollen Sie diesen Zahlungsturnus wirklich l�schen?"));

			try {
				Boolean choice = (Boolean) d.open();
				if (!choice.booleanValue())
					return;
			}
			catch (Exception e)
			{
				Logger.error("error while deleting turnus",e);
				return;
			}

			// ok, wir loeschen das Objekt
			t.delete();
			GUI.getStatusBar().setSuccessText(i18n.tr("Zahlungsturnus gel�scht."));
		}
		catch (RemoteException e)
		{
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim L�schen des Zahlungsturnus."));
			Logger.error("unable to delete turnus",e);
		}
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.4  2004-11-18 23:46:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/11/15 00:38:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/11/13 17:12:15  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 **********************************************************************/