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
import de.willuhn.jameica.hbci.gui.dialogs.SammelLastschriftDialog;
import de.willuhn.jameica.hbci.gui.views.SammelLastschriftNew;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.hbci.server.hbci.HBCISammelLastschriftJob;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, die zur Ausfuehrung einer Sammel-Lastschrift verwendet werden kann.
 * Er erwartet ein Objekt vom Typ <code>SammelLastschrift</code> als Context.
 */
public class SammelLastschriftExecute implements Action
{

  /**
	 * Erwartet ein Objekt vom Typ <code>SammelLastschrift</code> als Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		final I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null || !(context instanceof SammelLastschrift))
			throw new ApplicationException(i18n.tr("Keine Sammel-Lastschrift angegeben"));

		try
		{
			final SammelLastschrift u = (SammelLastschrift) context;
			
			if (u.ausgefuehrt())
				throw new ApplicationException(i18n.tr("Sammel-Lastschrift wurde bereits ausgef�hrt"));

			if (u.getBuchungen().size() == 0)
				throw new ApplicationException(i18n.tr("Sammel-Lastschrift enth�lt keine Buchungen"));
			if (u.isNewObject())
				u.store(); // wir speichern bei Bedarf selbst.

			SammelLastschriftDialog d = new SammelLastschriftDialog(u,SammelLastschriftDialog.POSITION_CENTER);
			try
			{
				if (!((Boolean)d.open()).booleanValue())
					return;
			}
			catch (Exception e)
			{
				Logger.error("error while showing confirm dialog",e);
				GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausf�hren der Sammel-Lastschrift"));
				return;
			}

			GUI.startSync(new Runnable()
      {
        public void run()
        {
        	try
        	{
						GUI.getStatusBar().startProgress();
						GUI.getStatusBar().setStatusText(i18n.tr("F�hre Sammel-Lastschrift aus..."));
						HBCIFactory factory = HBCIFactory.getInstance();
						factory.addJob(new HBCISammelLastschriftJob(u));
						factory.executeJobs(u.getKonto().getPassport().getHandle()); 
						GUI.getStatusBar().setSuccessText(i18n.tr("Sammel-Lastschrift erfolgreich ausgef�hrt"));
            // BUGZILLA 31 http://www.willuhn.de/bugzilla/show_bug.cgi?id=31
            GUI.startView(SammelLastschriftNew.class,u);
        	}
					catch (OperationCanceledException oce)
					{
						GUI.getStatusBar().setErrorText(i18n.tr("Ausf�hrung der Sammel-Lastschrift abgebrochen"));
					}
					catch (ApplicationException ae)
					{
						GUI.getStatusBar().setErrorText(ae.getMessage());
					}
					catch (RemoteException e)
					{
						Logger.error("error while executing sammellastschrift",e);
						GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausf�hren der Sammel-Lastschrift") + " [" + e.getMessage() + "]");
					}
					finally
					{
						GUI.getStatusBar().stopProgress();
						GUI.getStatusBar().setStatusText("");
					}
        }
      });

		}
		catch (RemoteException e)
		{
			Logger.error("error while executing sammellastschrift",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausf�hren der Sammel-Lastschrift"));
		}
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.5  2005-03-30 23:28:13  web0
 * @B bug 31
 *
 * Revision 1.4  2005/03/30 23:26:28  web0
 * @B bug 29
 * @B bug 30
 *
 * Revision 1.3  2005/03/05 19:19:48  web0
 * *** empty log message ***
 *
 * Revision 1.2  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.1  2005/03/02 00:22:05  web0
 * @N first code for "Sammellastschrift"
 *
 **********************************************************************/