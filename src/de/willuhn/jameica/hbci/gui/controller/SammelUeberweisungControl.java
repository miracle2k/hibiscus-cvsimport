/*****************************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 * $Locker$
 * $State$
 *
****************************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.SammelTransferBuchungDelete;
import de.willuhn.jameica.hbci.gui.action.SammelUeberweisungBuchungNew;
import de.willuhn.jameica.hbci.gui.action.SammelUeberweisungNew;
import de.willuhn.jameica.hbci.gui.action.UeberweisungNew;
import de.willuhn.jameica.hbci.gui.parts.SammelTransferBuchungList;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisungBuchung;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung des Controllers fuer den Dialog "Liste der Sammellastschriften".
 * @author willuhn
 */
public class SammelUeberweisungControl extends AbstractSammelTransferControl
{


  private I18N i18n                     	= null;

  private SammelTransfer transfer         = null;

  private TablePart table               	= null;

  /**
   * ct.
   * @param view
   */
  public SammelUeberweisungControl(AbstractView view)
  {
    super(view);
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSammelTransferControl#getTransfer()
   */
  public SammelTransfer getTransfer() throws RemoteException
  {
    if (transfer != null)
      return transfer;

    transfer = (SammelTransfer) getCurrentObject();
    if (transfer != null)
      return transfer;

    transfer = (SammelUeberweisung) Settings.getDBService().createObject(SammelUeberweisung.class,null);
    return transfer;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSammelTransferControl#getListe()
   */
  public TablePart getListe() throws RemoteException
  {
    if (table != null)
      return table;

    table = new de.willuhn.jameica.hbci.gui.parts.SammelUeberweisungList(new SammelUeberweisungNew());
    return table;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSammelTransferControl#getBuchungen()
   */
  public Part getBuchungen() throws RemoteException
  {
    Action a = new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        new SammelUeberweisungBuchungNew().handleAction(context);
      }
    };
    
    TablePart buchungen = new SammelTransferBuchungList(getTransfer(),a);

    ContextMenu ctx = new ContextMenu();
    ctx.addItem(new CheckedContextMenuItem(i18n.tr("Buchung �ffnen"), new SammelUeberweisungBuchungNew(),"document-open.png"));
    ctx.addItem(new NotActiveMenuItem(i18n.tr("Buchung l�schen..."), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        new SammelTransferBuchungDelete().handleAction(context);
        try
        {
          getSumme().setValue(HBCI.DECIMALFORMAT.format(getTransfer().getSumme()));
        }
        catch (RemoteException e)
        {
          Logger.error("unable to refresh summary",e);
        }
      }
    },"user-trash-full.png"));
    ctx.addItem(ContextMenuItem.SEPARATOR);
    ctx.addItem(new ContextMenuItem(i18n.tr("Neue Buchung..."),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        if (handleStore())
        {
          try
          {
            new SammelUeberweisungBuchungNew().handleAction(getTransfer());
          }
          catch (RemoteException e)
          {
            Logger.error("unable to load sammelueberweisung",e);
            throw new ApplicationException(i18n.tr("Fehler beim Laden der Sammel-�berweisung"));
          }
        }
      }
    },"text-x-generic.png")
    {
      /**
       * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
       */
      public boolean isEnabledFor(Object o)
      {
        if (o == null)
          return true;
        try
        {
          SammelTransferBuchung u = (SammelTransferBuchung) o;
          return !u.getSammelTransfer().ausgefuehrt();
        }
        catch (Exception e)
        {
          Logger.error("error while enable check in menu item",e);
        }
        return false;
      }
      
    });
    ctx.addItem(ContextMenuItem.SEPARATOR);
    ctx.addItem(new CheckedContextMenuItem(i18n.tr("In Einzel�berweisung duplizieren"), new UeberweisungNew(),"stock_next.png"));
    buchungen.setContextMenu(ctx);
    return buchungen;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSammelTransferControl#handleStore()
   */
  public synchronized boolean handleStore()
  {
    try {
      getTransfer().setKonto((Konto)getKontoAuswahl().getValue());
      getTransfer().setBezeichnung((String)getName().getValue());
      getTransfer().setTermin((Date)getTermin().getValue());
      getTransfer().store();
      GUI.getStatusBar().setSuccessText(i18n.tr("Sammel-�berweisung gespeichert"));
      return true;
    }
    catch (ApplicationException e2)
    {
      GUI.getView().setErrorText(e2.getMessage());
    }
    catch (RemoteException e)
    {
      Logger.error("error while storing sammelueberweisung",e);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern der Sammel-�berweisung"));
    }
    return false;
  }


  /**
   * Ueberschreiben wir, damit das Item nur dann aktiv ist, wenn die
   * Ueberweisung noch nicht ausgefuehrt wurde.
   */
  private class NotActiveMenuItem extends ContextMenuItem
  {
    
    /**
     * ct.
     * @param text anzuzeigender Text.
     * @param a auszufuehrende Action.
     * @param icon Icon des Menueintrages.
     */
    public NotActiveMenuItem(String text, Action a, String icon)
    {
      super(text, a, icon);
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
        SammelUeberweisungBuchung u = (SammelUeberweisungBuchung) o;
        return !u.getSammelTransfer().ausgefuehrt();
      }
      catch (Exception e)
      {
        Logger.error("error while enable check in menu item",e);
      }
      return false;
    }
  }

}

/*****************************************************************************
 * $Log$
 * Revision 1.6  2009-11-26 12:00:21  willuhn
 * @N Buchungen aus Sammelauftraegen in Einzelauftraege duplizieren
 *
 * Revision 1.5  2009/05/08 13:58:30  willuhn
 * @N Icons in allen Menus und auf allen Buttons
 * @N Fuer Umsatz-Kategorien koennen nun benutzerdefinierte Farben vergeben werden
 *
 * Revision 1.4  2009/05/06 23:11:23  willuhn
 * @N Mehr Icons auf Buttons
 *
 * Revision 1.3  2006/08/07 14:31:59  willuhn
 * @B misc bugfixing
 * @C Redesign des DTAUS-Imports fuer Sammeltransfers
 *
 * Revision 1.2  2006/06/08 22:29:47  willuhn
 * @N DTAUS-Import fuer Sammel-Lastschriften und Sammel-Ueberweisungen
 * @B Eine Reihe kleinerer Bugfixes in Sammeltransfers
 * @B Bug 197 besser geloest
 *
 * Revision 1.1  2005/09/30 00:08:51  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
*****************************************************************************/