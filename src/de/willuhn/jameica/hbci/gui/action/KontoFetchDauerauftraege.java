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
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Action, die die Dauerauftraege eines Kontos abruft.
 * Er erwartet ein Objekt vom Typ <code>Konto</code> als Context.
 */
public class KontoFetchDauerauftraege implements Action
{

  /**
   * ct.
   */
  public KontoFetchDauerauftraege()
  {
    super();
  }

  /**
	 * Erwartet ein Objekt vom Typ <code>Konto</code> als Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		final I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null || !(context instanceof Konto))
			throw new ApplicationException(i18n.tr("Bitte w�hlen Sie ein Konto aus"));

		GUI.getStatusBar().startProgress();

		try
		{
			final Konto k = (Konto) context;

			GUI.startSync(new Runnable() {
				public void run() {
					try {
						GUI.getStatusBar().setSuccessText(i18n.tr("Dauerauftr�ge werden abgerufen..."));
						k.refreshDauerauftraege();
						GUI.getStatusBar().setSuccessText(i18n.tr("...Ums�tze erfolgreich �bertragen"));

						new DauerauftragListe().handleAction(k);
					}
					catch (ApplicationException e2)
					{
						GUI.getView().setErrorText(i18n.tr(e2.getMessage()));
					}
					catch (Throwable t)
					{
						Logger.error("error while reading dauerauftraege",t);
						GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Abrufen der Dauerauftr�ge."));
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
 * Revision 1.2  2004-10-23 18:13:45  willuhn
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