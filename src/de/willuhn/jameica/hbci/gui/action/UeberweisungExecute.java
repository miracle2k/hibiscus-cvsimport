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
import de.willuhn.jameica.hbci.gui.dialogs.UeberweisungDialog;
import de.willuhn.jameica.hbci.gui.views.UeberweisungNew;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.hbci.server.hbci.HBCIUeberweisungJob;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, die zur Ausfuehrung einer Ueberweisung verwendet werden kann.
 * Er erwartet ein Objekt vom Typ <code>Ueberweisung</code> als Context.
 */
public class UeberweisungExecute implements Action
{

  /**
	 * Erwartet ein Objekt vom Typ <code>Ueberweisung</code> als Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		final I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null || !(context instanceof Ueberweisung))
			throw new ApplicationException(i18n.tr("Keine �berweisung angegeben"));

		try
		{
			final Ueberweisung u = (Ueberweisung) context;
			
			if (u.ausgefuehrt())
				throw new ApplicationException(i18n.tr("�berweisung wurde bereits ausgef�hrt"));

			if (u.isNewObject())
				u.store(); // wir speichern bei Bedarf selbst.

			UeberweisungDialog d = new UeberweisungDialog(u,UeberweisungDialog.POSITION_CENTER);
			try
			{
				if (!((Boolean)d.open()).booleanValue())
					return;
			}
			catch (Exception e)
			{
				Logger.error("error while showing confirm dialog",e);
				GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausf�hren der �berweisung"));
				return;
			}

			GUI.startSync(new Runnable()
      {
        public void run()
        {
        	try
        	{
						GUI.getStatusBar().startProgress();
						GUI.getStatusBar().setStatusText(i18n.tr("F�hre �berweisung aus..."));
						HBCIFactory factory = HBCIFactory.getInstance();
						factory.addJob(new HBCIUeberweisungJob(u));
						factory.executeJobs(u.getKonto().getPassport().getHandle()); 
						GUI.getStatusBar().setSuccessText(i18n.tr("�berweisung erfolgreich ausgef�hrt"));
            // BUGZILLA 30 http://www.willuhn.de/bugzilla/show_bug.cgi?id=30
            GUI.startView(UeberweisungNew.class,u);
        	}
					catch (OperationCanceledException oce)
					{
						GUI.getStatusBar().setErrorText(i18n.tr("Ausf�hrung der �berweisung abgebrochen"));
					}
					catch (ApplicationException ae)
					{
						GUI.getStatusBar().setErrorText(ae.getMessage());
					}
					catch (RemoteException e)
					{
						Logger.error("error while executing ueberweisung",e);
						GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausf�hren der �berweisung") + " [" + e.getMessage() + "]");
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
			Logger.error("error while executing ueberweisung",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausf�hren der �berweisung"));
		}
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.8  2005-03-30 23:26:28  web0
 * @B bug 29
 * @B bug 30
 *
 * Revision 1.7  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/10/29 16:16:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/10/25 22:39:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.3  2004/10/24 17:19:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/10/19 23:33:31  willuhn
 * *** empty log message ***
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