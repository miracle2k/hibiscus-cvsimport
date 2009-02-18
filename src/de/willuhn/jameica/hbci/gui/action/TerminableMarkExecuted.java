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
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.hbci.rmi.HibiscusTransfer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Markiert eine Ueberweisung/Lastschrift als ausgefuehrt.
 */
public class TerminableMarkExecuted implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    if (context == null)
      return;

    Terminable t[] = null;
    if (context instanceof Terminable)
      t = new Terminable[]{(Terminable) context};
    else
      t = (Terminable[]) context;
    
    YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
    d.setTitle(i18n.tr("Sicher?"));
    if (t.length == 1)
      d.setText(i18n.tr("Sind Sie sicher, dass Sie diesen Auftrag als \"ausgef�hrt\" markieren wollen?\nDies kann nicht r�ckg�ngig gemacht werden."));
    else
      d.setText(i18n.tr("Sind Sie sicher, dass Sie diese {0} Auftr�ge als \"ausgef�hrt\" markieren wollen?\nDies kann nicht r�ckg�ngig gemacht werden.",""+t.length));
    
    try
    {
      Boolean b = (Boolean) d.open();
      if (b == null || !b.booleanValue())
        return;
      
      for (int i=0;i<t.length;++i)
      {
        t[i].setAusgefuehrt(true);
        if (t[i] instanceof HibiscusTransfer)
        {
          HibiscusTransfer tr = (HibiscusTransfer) t[i];
          Konto k = tr.getKonto();
          if (k != null)
            k.addToProtokoll(i18n.tr("Auftrag \"{0}\" [Gegenkonto {1}, BLZ {2}] manuell als \"ausgef�hrt\" markiert",new String[]{tr.getZweck(),tr.getGegenkontoName(),tr.getGegenkontoBLZ()}),Protokoll.TYP_SUCCESS);
          Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(tr));
        }
      }
      if (t.length == 1)
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Auftrag als \"ausgef�hrt\" markiert"),StatusBarMessage.TYPE_SUCCESS));
      else
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Auftr�ge als \"ausgef�hrt\" markiert"),StatusBarMessage.TYPE_SUCCESS));
    }
    catch (OperationCanceledException oce)
    {
      return;
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("unabel to mark transfers as executed",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Markieren als \"ausgef�hrt\""),StatusBarMessage.TYPE_ERROR));
    }
    
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.4  2009-02-18 10:48:42  willuhn
 * @N Neuer Schalter "transfer.markexecuted.before", um festlegen zu koennen, wann ein Auftrag als ausgefuehrt gilt (wenn die Quittung von der Bank vorliegt oder wenn der Auftrag erzeugt wurde)
 *
 * Revision 1.3  2007/10/25 15:47:21  willuhn
 * @N Einzelauftraege zu Sammel-Auftraegen zusammenfassen (BUGZILLA 402)
 *
 * Revision 1.2  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.1  2006/03/30 22:56:46  willuhn
 * @B bug 216
 *
 **********************************************************************/