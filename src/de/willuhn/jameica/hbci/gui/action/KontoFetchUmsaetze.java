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
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Action, die die Umsaetze eines Kontos aktualisiert.
 * Er erwartet ein Objekt vom Typ <code>Konto</code> als Context.
 */
public class KontoFetchUmsaetze implements Action
{

  /**
   * ct.
   */
  public KontoFetchUmsaetze()
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

		try {
			final Konto k = (Konto) context;
			if (k == null)
				return;

			if (k.isNewObject())
			{
				GUI.getView().setErrorText(i18n.tr("Bitte speichern Sie zuerst das Konto."));
				return;
			}

			GUI.getStatusBar().startProgress();

			GUI.startSync(new Runnable() {
				public void run() {
					try {
						GUI.getStatusBar().setSuccessText(i18n.tr("Ums�tze werden abgerufen..."));
						k.refreshSaldo();
						GUI.startView(UmsatzListe.class.getName(),k);
						GUI.getStatusBar().setSuccessText(i18n.tr("...Ums�tze erfolgreich �bertragen"));
					}
					catch (ApplicationException e2)
					{
						GUI.getStatusBar().setErrorText(e2.getMessage());
					}
					catch (Throwable t)
					{
						Logger.error("error while reading umsaetze",t);
						GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Abrufen des Ums�tze."));
					}
				}
			});
		}
		catch (RemoteException e)
		{
			Logger.error("error while refreshing umsaetze",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Abrufen der Ums�tze"));
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