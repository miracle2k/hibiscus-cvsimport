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

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.LastschriftNew;
import de.willuhn.jameica.hbci.gui.action.SammelLastBuchungNew;
import de.willuhn.jameica.hbci.gui.action.SammelLastschriftNew;
import de.willuhn.jameica.hbci.gui.action.SammelTransferBuchungDelete;
import de.willuhn.jameica.hbci.gui.parts.SammelTransferBuchungList;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Implementierung des Controllers fuer den Dialog "Liste der Sammellastschriften".
 * @author willuhn
 */
public class SammelLastschriftControl extends AbstractSammelTransferControl
{
  private SammelTransfer transfer = null;
  private TablePart table         = null;
  private TablePart buchungen     = null;

  /**
   * ct.
   * @param view
   */
  public SammelLastschriftControl(AbstractView view)
  {
    super(view);
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

    transfer = (SammelLastschrift) Settings.getDBService().createObject(SammelLastschrift.class,null);
    return transfer;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSammelTransferControl#getListe()
   */
  public TablePart getListe() throws RemoteException
  {
    if (table != null)
      return table;

    table = new de.willuhn.jameica.hbci.gui.parts.SammelLastschriftList(new SammelLastschriftNew());
    return table;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSammelTransferControl#getBuchungen()
   */
  public TablePart getBuchungen() throws RemoteException
  {
    if (this.buchungen != null)
      return this.buchungen;
    
    Action a = new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        new SammelLastBuchungNew().handleAction(context);
      }
    };
    
    this.buchungen = new SammelTransferBuchungList(getTransfer(),a);

    ContextMenu ctx = new ContextMenu();
    ctx.addItem(new CheckedContextMenuItem(i18n.tr("Buchung �ffnen"), new SammelLastBuchungNew(),"document-open.png"));
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
            new SammelLastBuchungNew().handleAction(getTransfer());
          }
          catch (RemoteException e)
          {
            Logger.error("unable to load sammellastschrift",e);
            throw new ApplicationException(i18n.tr("Fehler beim Laden der Sammel-Lastschrift"));
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
    ctx.addItem(new CheckedContextMenuItem(i18n.tr("In Einzellastschrift duplizieren"), new LastschriftNew(),"stock_previous.png"));
    this.buchungen.setContextMenu(ctx);
    return this.buchungen;
  }
}

/*****************************************************************************
 * $Log$
 * Revision 1.17  2010-12-13 11:01:08  willuhn
 * @B Wenn man einen Sammelauftrag in der Detailansicht loeschte, konnte man anschliessend noch doppelt auf die zugeordneten Buchungen klicken und eine ObjectNotFoundException ausloesen
 *
 * Revision 1.16  2009/11/26 12:00:21  willuhn
 * @N Buchungen aus Sammelauftraegen in Einzelauftraege duplizieren
 *
 * Revision 1.15  2009/05/08 13:58:30  willuhn
 * @N Icons in allen Menus und auf allen Buttons
 * @N Fuer Umsatz-Kategorien koennen nun benutzerdefinierte Farben vergeben werden
 *
 * Revision 1.14  2009/05/06 23:11:23  willuhn
 * @N Mehr Icons auf Buttons
*****************************************************************************/