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
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.server.hbci.HBCIDauerauftragDeleteJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Action fuer Loeschen eines Dauerauftrages.
 * Existiert der Auftrag auch bei der Bank, wird er dort ebenfalls geloescht.
 */
public class DauerauftragDelete implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Dauerauftrag</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
  	final I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null || !(context instanceof Dauerauftrag))
			throw new ApplicationException(i18n.tr("Kein Dauerauftrag ausgew�hlt"));

		try {

			final Dauerauftrag da = (Dauerauftrag) context;

			YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
			d.setTitle(i18n.tr("Dauerauftrag l�schen"));
			if (da.isActive())
				d.setText(i18n.tr("Wollen Sie diesen Dauerauftrag wirklich l�schen?\nDer Auftrag wird auch bei der Bank gel�scht."));
			else
				d.setText(i18n.tr("Wollen Sie diesen Dauerauftrag wirklich l�schen?"));

			try {
				Boolean choice = (Boolean) d.open();
				if (!choice.booleanValue())
					return;
			}
			catch (Exception e)
			{
				Logger.error("error while deleting dauerauftrag",e);
				return;
			}

			if (da.isActive())
			{

				// Uh, der wird auch online geloescht
				GUI.startSync(new Runnable()
				{
					public void run()
					{
						try
						{
							GUI.getStatusBar().startProgress();
							GUI.getStatusBar().setStatusText(i18n.tr("L�sche Dauerauftrag bei Bank..."));
							HBCIFactory factory = HBCIFactory.getInstance();
							factory.addJob(new HBCIDauerauftragDeleteJob(da));
							factory.executeJobs(da.getKonto().getPassport().getHandle()); 
							GUI.getStatusBar().setSuccessText(i18n.tr("...Dauerauftrag erfolgreich gel�scht"));
						}
						catch (OperationCanceledException oce)
						{
							GUI.getStatusBar().setErrorText(i18n.tr("Vorgang abgebrochen"));
						}
						catch (ApplicationException ae)
						{
							GUI.getStatusBar().setErrorText(ae.getMessage());
						}
						catch (RemoteException e)
						{
							Logger.error("error while deleting dauerauftrag",e);
							GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim L�schen des Dauerauftrages"));
						}
						finally
						{
							GUI.getStatusBar().stopProgress();
							GUI.getStatusBar().setStatusText("");
						}
					}
				});
			}
			else
			{
				// nur lokal loeschen
				da.delete();
			}
			GUI.getStatusBar().setSuccessText(i18n.tr("Dauerauftrag gel�scht."));
		}
		catch (RemoteException e)
		{
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim L�schen des Dauerauftrages."));
			Logger.error("unable to delete dauerauftrag",e);
		}
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.3  2004-10-25 22:39:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.1  2004/10/24 17:19:02  willuhn
 * *** empty log message ***
 *
 **********************************************************************/