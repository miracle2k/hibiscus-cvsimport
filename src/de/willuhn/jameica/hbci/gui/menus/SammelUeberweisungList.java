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
package de.willuhn.jameica.hbci.gui.menus;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.SammelTransferDelete;
import de.willuhn.jameica.hbci.gui.action.SammelTransferDuplicate;
import de.willuhn.jameica.hbci.gui.action.SammelUeberweisungBuchungExport;
import de.willuhn.jameica.hbci.gui.action.SammelUeberweisungExecute;
import de.willuhn.jameica.hbci.gui.action.SammelUeberweisungNew;
import de.willuhn.jameica.hbci.gui.action.TerminableMarkExecuted;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Kontext-Menu, welches an Listen mit SammelUeberweisungen gehangen werden kann.
 * Es ist fix und fertig vorkonfiguriert und mit Elementen gefuellt.
 */
public class SammelUeberweisungList extends ContextMenu
{
	private I18N i18n	= null;

	/**
	 * Erzeugt ein Kontext-Menu fuer eine Liste von Sammel-Ueberweisungen.
	 */
	public SammelUeberweisungList()
	{
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		addItem(new CheckedContextMenuItem(i18n.tr("�ffnen"), new SammelUeberweisungNew()));
    addItem(new ContextMenuItem(i18n.tr("Neue Sammel-�berweisung..."), new SNeu()));
		addItem(new NotActiveMenuItem(i18n.tr("Jetzt ausf�hren..."), new SammelUeberweisungExecute()));
    // BUGZILLA 115 http://www.willuhn.de/bugzilla/show_bug.cgi?id=115
    addItem(new CheckedContextMenuItem(i18n.tr("Duplizieren"), new SammelTransferDuplicate()));
		addItem(ContextMenuItem.SEPARATOR);
		addItem(new CheckedContextMenuItem(i18n.tr("L�schen..."), new SammelTransferDelete()));
    addItem(new ContextMenuItem(i18n.tr("Als \"ausgef�hrt\" markieren..."), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        new TerminableMarkExecuted().handleAction(context);
        GUI.startView(GUI.getCurrentView().getClass(),GUI.getCurrentView().getCurrentObject());
      }
    }){
      public boolean isEnabledFor(Object o)
      {
        if (o == null || (!(o instanceof Terminable) && !(o instanceof Terminable[])))
          return false;
        try
        {
          if (o instanceof Terminable)
            return !((Terminable)o).ausgefuehrt();

          Terminable[] t = (Terminable[]) o;
          for (int i=0;i<t.length;++i)
          {
            if (t[i].ausgefuehrt())
              return false;
          }
          return true;
        }
        catch (RemoteException e)
        {
          Logger.error("unable to check if terminable is allready executed",e);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Pr�fen, ob Auftrag bereits ausgef�hrt wurde"),StatusBarMessage.TYPE_ERROR));
          return false;
        }
      }
    });
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedContextMenuItem(i18n.tr("Buchungen exportieren..."),new SammelUeberweisungBuchungExport()));
		
	}

	/**
	 * Ueberschreiben wir, um <b>grundsaetzlich</b> eine neue Sammel-Lastschrift
	 * anzulegen - auch wenn der Focus auf einer existierenden liegt.
   */
  private class SNeu extends SammelUeberweisungNew
	{
    /**
     * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
     */
    public void handleAction(Object context) throws ApplicationException
    {
    	super.handleAction(null);
    }
	} 
	
	/**
	 * Ueberschreiben wir, damit das Item nur dann aktiv ist, wenn die
	 * Sammel-Ueberweisung noch nicht ausgefuehrt wurde.
   */
  private class NotActiveMenuItem extends ContextMenuItem
	{
		
    /**
     * ct.
     * @param text anzuzeigender Text.
     * @param a auszufuehrende Action.
     */
    public NotActiveMenuItem(String text, Action a)
    {
      super(text, a);
    }

	  /**
     * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
    	if (o == null)
    		return false;
    	try
    	{
				SammelUeberweisung u = (SammelUeberweisung) o;
    		return !u.ausgefuehrt();
    	}
    	catch (Exception e)
    	{
    		Logger.error("error while enable check in menu item",e);
    	}
    	return false;
    }
	}
}


/**********************************************************************
 * $Log$
 * Revision 1.2  2006-03-30 22:56:46  willuhn
 * @B bug 216
 *
 * Revision 1.1  2005/09/30 00:08:51  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 **********************************************************************/