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
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action fuer Loeschen einer Sammel-Lastschrift.
 */
public class SammelLastschriftDelete implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>SammelLastschrift</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
  	I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null || !(context instanceof SammelLastschrift))
			throw new ApplicationException(i18n.tr("Keine Sammel-Lastschrift ausgew�hlt"));

		try {

			SammelLastschrift u = (SammelLastschrift) context;
			if (u.isNewObject())
				return;

			YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
			d.setTitle(i18n.tr("Sammel-Lastschrift l�schen"));
			d.setText(i18n.tr("Wollen Sie diese Sammel-Lastschrift wirklich l�schen?\n" +        "Alle enthaltenen Buchungen werden hierbei ebenfalls gel�scht."));

			try {
				Boolean choice = (Boolean) d.open();
				if (!choice.booleanValue())
					return;
			}
			catch (Exception e)
			{
				Logger.error("error while deleting sammellastschrift",e);
				return;
			}

			// ok, wir loeschen das Objekt
			u.delete();
			GUI.getStatusBar().setSuccessText(i18n.tr("Sammel-Lastschrift gel�scht."));
		}
		catch (RemoteException e)
		{
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim L�schen der Sammel-Lastschrift."));
			Logger.error("unable to delete sammellastschrift",e);
		}
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.1  2005-03-01 18:51:04  web0
 * @N Dialoge fuer Sammel-Lastschriften
 *
 **********************************************************************/