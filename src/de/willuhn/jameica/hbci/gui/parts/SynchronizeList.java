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

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.TableItem;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.SynchronizeJob;
import de.willuhn.jameica.hbci.server.hbci.synchronize.SynchronizeEngine;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Vorgefertigte Liste mit den offenen Synchronisierungs-TODOs fuer ein Konto.
 */
public class SynchronizeList extends TablePart
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * ct.
   * @throws RemoteException
   */
  public SynchronizeList() throws RemoteException
  {
    super(SynchronizeEngine.getInstance().getSynchronizeJobs(),new MyAction());
    addColumn(i18n.tr("Offene Synchronisierungsaufgaben"),"name");
    
    this.setSummary(false);
    this.setCheckable(true);

    // BUGZILLA 583
    this.setFormatter(new TableFormatter() {
      public void format(TableItem item)
      {
        try
        {
          if (item == null)
            return;
          SynchronizeJob job = (SynchronizeJob) item.getData();
          if (job == null)
            return;
          item.setFont(job.isRecurring() ? Font.DEFAULT.getSWTFont() : Font.BOLD.getSWTFont());
        }
        catch (Exception e)
        {
          Logger.error("unable to format text",e);
        }
      }
    });
  }
  
  /**
   * Hilfsklasse zum Reagieren auf Doppelklicks in der Liste.
   * Dort stehen naemlich ganz verschiedene Datensaetze drin.
   * Daher muss der Datensatz selbst entscheiden, was beim
   * Klick auf ihn gesehen soll.
   */
  private static class MyAction implements Action
  {

    /**
     * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
     */
    public void handleAction(Object context) throws ApplicationException
    {
      if (context == null || !(context instanceof SynchronizeJob))
        return;
      try
      {
        ((SynchronizeJob)context).configure();
      }
      catch (RemoteException e)
      {
        Logger.error("unable to configure synchronize job",e);
        GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim �ffnen des Synchronisierungs-Auftrags"));
      }
    }
    
  }

}


/*********************************************************************
 * $Log$
 * Revision 1.7  2008-04-13 04:20:41  willuhn
 * @N Bug 583
 *
 * Revision 1.6  2007/05/16 11:32:30  willuhn
 * @N Redesign der SynchronizeEngine. Ermittelt die HBCI-Jobs jetzt ueber generische "SynchronizeJobProvider". Damit ist die Liste der Sync-Jobs erweiterbar
 *
 * Revision 1.5  2006/07/05 22:18:16  willuhn
 * @N Einzelne Sync-Jobs koennen nun selektiv auch einmalig direkt in der Sync-Liste deaktiviert werden
 *
 * Revision 1.4  2006/04/18 22:38:16  willuhn
 * @N bug 227
 *
 * Revision 1.3  2006/03/21 00:43:14  willuhn
 * @B bug 209
 *
 * Revision 1.2  2006/03/19 23:04:49  willuhn
 * @B bug 209
 *
 * Revision 1.1  2006/03/17 00:51:25  willuhn
 * @N bug 209 Neues Synchronisierungs-Subsystem
 *
 **********************************************************************/