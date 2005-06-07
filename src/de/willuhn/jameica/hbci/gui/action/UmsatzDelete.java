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
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action fuer Loeschen von Umsaetzen.
 */
public class UmsatzDelete implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Umsatz</code> oder <code>Umsatz[]</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
  	I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null)
      throw new ApplicationException(i18n.tr("Keine zu l�schenden Ums�tze ausgew�hlt"));

    if (!(context instanceof Umsatz) && !(context instanceof Umsatz[]))
			throw new ApplicationException(i18n.tr("Keine Ums�tze ausgew�hlt"));

    boolean array = (context instanceof Umsatz[]);
    // Sicherheitsabfrage
    YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
    if (array)
    {
      d.setTitle(i18n.tr("Ums�tze l�schen"));
      d.setText(i18n.tr("Wollen Sie diese {0} Ums�tze wirklich l�schen?",""+((Umsatz[])context).length));
    }
    else
    {
      d.setTitle(i18n.tr("Umsatz l�schen"));
      d.setText(i18n.tr("Wollen Sie diesen Umsatz wirklich l�schen?"));
    }
    try {
      Boolean choice = (Boolean) d.open();
      if (!choice.booleanValue())
        return;
    }
    catch (Exception e)
    {
      Logger.error("error while deleting umsatz",e);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim L�schen des Umsatzes"));
      return;
    }

    Umsatz[] list = null;
    if (array)
      list = (Umsatz[]) context;
    else
      list = new Umsatz[]{(Umsatz)context}; // Array mit einem Element

		try {

      for (int i=0;i<list.length;++i)
      {
        if (list[i].isNewObject())
          continue; // muss nicht geloescht werden

        // ok, wir loeschen das Objekt
        list[i].delete();
      }
      if (array)
        GUI.getStatusBar().setSuccessText(i18n.tr("{0} Ums�tze gel�scht.",""+list.length));
      else
        GUI.getStatusBar().setSuccessText(i18n.tr("Umsatz gel�scht."));

		}
		catch (RemoteException e)
		{
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim L�schen der Ums�tze."));
			Logger.error("unable to delete umsaetze",e);
		}
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.1  2005-06-07 22:41:09  web0
 * @B bug 70
 *
 **********************************************************************/