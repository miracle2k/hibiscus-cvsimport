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
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.SammelLastBuchung;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action fuer Loeschen einer Buchung in einer Sammel-Lastschrift.
 */
public class SammelLastBuchungDelete implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>SammelLastBuchung</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
  	I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null || !(context instanceof SammelLastBuchung))
			throw new ApplicationException(i18n.tr("Keine Buchung ausgew�hlt"));

		try {

			SammelLastBuchung u = (SammelLastBuchung) context;
			if (u.isNewObject())
				return;

			if (u.getSammelLastschrift().ausgefuehrt())
				throw new ApplicationException(i18n.tr("Lastschrift wurde bereits ausgef�hrt" +					"und kann daher nur noch als ganzes gel�scht werden"));

			YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
			d.setTitle(i18n.tr("Buchung l�schen"));
			d.setText(i18n.tr("Wollen Sie diese Buchung wirklich l�schen?"));

			try {
				Boolean choice = (Boolean) d.open();
				if (!choice.booleanValue())
					return;
			}
			catch (Exception e)
			{
				Logger.error("error while deleting sammellastbuchung",e);
				return;
			}

			// ok, wir loeschen das Objekt
			u.delete();
			GUI.getStatusBar().setSuccessText(i18n.tr("Buchung gel�scht."));
		}
		catch (RemoteException e)
		{
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim L�schen der Buchung."));
			Logger.error("unable to delete sammellastbuchung",e);
		}
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.1  2005-03-02 00:22:05  web0
 * @N first code for "Sammellastschrift"
 *
 **********************************************************************/