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
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Action, die die Funktionsfaehigkeit eines Passports via oeffnen und schliessen testet.
 */
public class PassportTest implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>de.willuhn.jameica.hbci.passport.Passport</code>.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		final I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null || !(context instanceof Passport))
			throw new ApplicationException(i18n.tr("Bitte w�hlen Sie ein Sicherheits-Medium aus."));

		GUI.getStatusBar().startProgress();
		GUI.getStatusBar().setSuccessText(i18n.tr("Teste Sicherheits-Medium..."));

		final Passport p = (Passport) context;
		try
		{
			GUI.startSync(new Runnable() {
				public void run() {
					try {
						PassportHandle handle = p.getHandle();
						handle.open();
						handle.close(); // nein, nicht im finally, denn wenn das Oeffnen
 													  // fehlschlaegt, ist nichts zum Schliessen da ;)
						GUI.getStatusBar().setSuccessText(i18n.tr("Sicherheits-Medium erfolgreich getestet."));
					}
					catch (RemoteException e)
					{
						GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Testen des Sicherheits-Mediums."));
						Logger.debug("error while testing passport: " + e.getMessage());
					}
				}
			});
		}
		finally
		{
			GUI.getStatusBar().stopProgress();
		}
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.1  2004-10-20 12:08:18  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 **********************************************************************/