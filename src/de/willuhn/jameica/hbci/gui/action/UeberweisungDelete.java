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
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Action fuer Loeschen eine rUeberweisung.
 */
public class UeberweisungDelete implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Ueberweisung</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
  	I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null || !(context instanceof Ueberweisung))
			throw new ApplicationException(i18n.tr("Keine �berweisung ausgew�hlt"));

		try {

			Ueberweisung u = (Ueberweisung) context;
			if (u.isNewObject())
				return;

			YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
			d.setTitle(i18n.tr("�berweisung l�schen"));
			d.setText(i18n.tr("Wollen Sie diese �berweisung wirklich l�schen?"));

			try {
				Boolean choice = (Boolean) d.open();
				if (!choice.booleanValue())
					return;
			}
			catch (Exception e)
			{
				Logger.error("error while deleting ueberweisung",e);
				return;
			}

			// ok, wir loeschen das Objekt
			u.delete();
			GUI.getStatusBar().setSuccessText(i18n.tr("�berweisung gel�scht."));
		}
		catch (RemoteException e)
		{
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim L�schen der �berweisung."));
			Logger.error("unable to delete ueberweisung",e);
		}
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.4  2004-10-25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.3  2004/10/24 17:19:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/21 14:05:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/20 12:08:18  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 **********************************************************************/