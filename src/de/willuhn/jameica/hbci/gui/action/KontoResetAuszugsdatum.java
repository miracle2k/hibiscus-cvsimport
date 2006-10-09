/**********************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 * $Locker$
 * $State$
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zum Reset des Kontoauszugsdatums
 */
public class KontoResetAuszugsdatum implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Konto</code> im Context.
   * 
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class)
        .getResources().getI18N();

    if (context == null || !(context instanceof Konto))
    {
      throw new ApplicationException(i18n.tr("Kein Konto ausgew�hlt"));
    }

    try
    {
      Konto k = (Konto) context;
      if (k.isNewObject())
      {
        return;
      }
      YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
      d.setTitle(i18n.tr("Reset Kontoauszugsdatum"));
      d.setText(i18n
          .tr("Soll das Kontoauszugsdatum wirklich zur�ckgesetzt werden?\n"
              + "Bei der n�chsten Synchronisierung werden alle bei der Bank \n"
              + "verf�gbaren Ums�tze abgeholt."));

      try
      {
        Boolean choice = (Boolean) d.open();
        if (!choice.booleanValue())
          return;
      }
      catch (Exception e)
      {
        Logger.error("error while resetting saldo_date", e);
        return;
      }

      k.resetSaldoDatum();
      k.store();
      GUI.getStatusBar().setSuccessText(
          i18n.tr("Kontoauszugsdatum zur�ckgesetzt."));
    }
    catch (RemoteException e)
    {
      GUI.getStatusBar().setErrorText(
          i18n.tr("Fehler beim Zur�cksetzen des Kontoauszugsdatums."));
      Logger.error("unable to reset saldo_datum", e);
    }
  }

}

/*******************************************************************************
 * $Log$
 * Revision 1.1  2006-10-09 16:55:51  jost
 * Bug #284
 *
 ******************************************************************************/
