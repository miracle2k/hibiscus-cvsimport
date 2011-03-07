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

import java.util.Date;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.DauerauftragDeleteDialog;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.server.hbci.HBCIDauerauftragDeleteJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIDauerauftragListJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Action fuer Loeschen eines Dauerauftrages.
 * Existiert der Auftrag auch bei der Bank, wird er dort ebenfalls geloescht.
 */
public class DauerauftragDelete implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * Erwartet ein Objekt vom Typ <code>Dauerauftrag</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		if (context == null || !(context instanceof Dauerauftrag))
			throw new ApplicationException(i18n.tr("Kein Dauerauftrag ausgew�hlt"));

    final Dauerauftrag da = (Dauerauftrag) context;

		try
		{
	    final CheckboxInput check = new CheckboxInput(true);
	    YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER)
	    {
	      // BUGZILLA #999
	      protected void extend(Container container) throws Exception
	      {
	        if (da.isActive()) // Nur bei aktiven Dauerauftraegen anzeigen
  	        container.addCheckbox(check,i18n.tr("Auftrag auch bei der Bank l�schen."));
	        super.extend(container);
	      }
	    };
	    d.setTitle(i18n.tr("Dauerauftrag l�schen"));
	    d.setText(i18n.tr("Wollen Sie diesen Dauerauftrag wirklich l�schen?"));

	    Boolean choice = (Boolean) d.open();
	    if (!choice.booleanValue())
	      return;

	    // Nur bei der Bank loeschen, wenn er aktiv ist und der User das will
      // BUGZILLA #15
		  if (da.isActive() && (Boolean) check.getValue())
	    {

	      DauerauftragDeleteDialog d2 = new DauerauftragDeleteDialog(DauerauftragDeleteDialog.POSITION_CENTER);
	      Date date = (Date) d2.open();

	      HBCIFactory factory = HBCIFactory.getInstance();
	      HBCIDauerauftragListJob job = new HBCIDauerauftragListJob(da.getKonto());
	      job.setExclusive(true);
	      factory.addJob(job);
	      factory.addJob(new HBCIDauerauftragDeleteJob(da,date));
	      factory.executeJobs(da.getKonto(), new Listener() {
	        public void handleEvent(Event event)
	        {
	          if (event.type == ProgressMonitor.STATUS_DONE)
	          {
	            try
	            {
	              da.delete(); // anschliessend lokal in der Datenbank loeschen
                Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Dauerauftrag gel�scht."),StatusBarMessage.TYPE_SUCCESS));
	            }
	            catch (ApplicationException e)
	            {
	              Application.getMessagingFactory().sendMessage(new StatusBarMessage(e.getMessage(),StatusBarMessage.TYPE_ERROR));
	              GUI.getStatusBar().setErrorText(e.getMessage());
	            }
	            catch (Exception e)
	            {
	              Logger.error("unable to delete local copy",e);
                Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Lokale Kopie des Auftrages konnte nicht gel�scht werden: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
	            }
	          }
	        }
	      }); 
	    }
	    else
	    {
	      // nur lokal loeschen
	      da.delete();
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Dauerauftrag gel�scht."),StatusBarMessage.TYPE_SUCCESS));
	    }
		}
    catch (OperationCanceledException oce)
    {
      Logger.info("operation cancelled");
      return;
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("error while deleting",e);
      throw new ApplicationException(i18n.tr("Fehler beim L�schen des Auftrages: {0}",e.getMessage()));
    }
  }
}


/**********************************************************************
 * $Log$
 * Revision 1.18  2011-03-07 10:33:54  willuhn
 * @N BUGZILLA 999
 *
 **********************************************************************/