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
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.hbci.server.hbci.HBCISaldoJob;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, die den Saldo eines Kontos aktualisiert.
 * Er erwartet ein Objekt vom Typ <code>Konto</code> als Context.
 */
public class KontoFetchSaldo implements Action
{

  /**
	 * Erwartet ein Objekt vom Typ <code>Konto</code> als Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		final I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null || !(context instanceof Konto))
			throw new ApplicationException(i18n.tr("Bitte w�hlen Sie ein Konto aus"));

		try {
			final Konto k = (Konto) context;

			if (k.isNewObject())
				k.store();

			HBCIFactory factory = HBCIFactory.getInstance();
			factory.addJob(new HBCISaldoJob(k));
      // Listener nicht mehr noetig, da die Aktualisierung ueber
      // die SaldoMessage in HBCISaldoJob geschieht
      factory.executeJobs(k,null);

		}
		catch (RemoteException e)
		{
			Logger.error("error while refreshing saldo",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Aktualisieren des Saldos"));
		}
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.12  2007-07-04 09:16:23  willuhn
 * @B Aktuelle View nach Ausfuehrung eines HBCI-Jobs nur noch dann aktualisieren, wenn sie sich zwischenzeitlich nicht geaendert hat
 *
 * Revision 1.11  2006/06/19 12:57:31  willuhn
 * @N DTAUS-Import fuer Umsaetze
 * @B Formatierungsfehler in Umsatzliste
 *
 * Revision 1.10  2005/07/26 23:57:18  web0
 * @N Restliche HBCI-Jobs umgestellt
 *
 * Revision 1.9  2005/07/26 23:00:03  web0
 * @N Multithreading-Support fuer HBCI-Jobs
 *
 * Revision 1.8  2005/05/10 22:26:15  web0
 * @B bug 71
 *
 * Revision 1.7  2004/11/13 17:02:03  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.6  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/10/29 16:16:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/10/25 22:39:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/10/24 17:19:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/10/23 18:13:45  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/20 12:08:18  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 * Revision 1.1  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 * Revision 1.3  2004/07/25 17:15:05  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.2  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 **********************************************************************/