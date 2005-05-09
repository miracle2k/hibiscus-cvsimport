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
import de.willuhn.jameica.hbci.rmi.Nachricht;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action fuer Loeschen von System-Nachrichten.
 */
public class NachrichtDelete implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Nachricht</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
  	I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null)
      throw new ApplicationException(i18n.tr("Keine System-Nachricht ausgew�hlt"));

    if (!(context instanceof Nachricht) && !(context instanceof Nachricht[]))
			throw new ApplicationException(i18n.tr("Keine System-Nachricht ausgew�hlt"));

    boolean array = (context instanceof Nachricht[]);
    // Sicherheitsabfrage
    YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
    if (array)
    {
      d.setTitle(i18n.tr("Nachrichten l�schen"));
      d.setText(i18n.tr("Wollen Sie diese {0} System-Nachrichten wirklich l�schen?",""+((Nachricht[])context).length));
    }
    else
    {
      d.setTitle(i18n.tr("Nachricht l�schen"));
      d.setText(i18n.tr("Wollen Sie diese System-Nachricht wirklich l�schen?"));
    }
    try {
      Boolean choice = (Boolean) d.open();
      if (!choice.booleanValue())
        return;
    }
    catch (Exception e)
    {
      Logger.error("error while deleting system message",e);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim L�schen der System-Nachricht"));
      return;
    }

    Nachricht[] list = null;
    if (array)
      list = (Nachricht[]) context;
    else
      list = new Nachricht[]{(Nachricht)context}; // Array mit einem Element

		try {

      for (int i=0;i<list.length;++i)
      {
        if (list[i].isNewObject())
          continue; // muss nicht geloescht werden

        // ok, wir loeschen das Objekt
        list[i].delete();
      }
      if (array)
        GUI.getStatusBar().setSuccessText(i18n.tr("{0} System-Nachrichten gel�scht.",""+list.length));
      else
        GUI.getStatusBar().setSuccessText(i18n.tr("System-Nachricht gel�scht."));

		}
		catch (RemoteException e)
		{
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim L�schen der System-Nachrichten."));
			Logger.error("unable to delete system message",e);
		}
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.1  2005-05-09 17:26:56  web0
 * @N Bugzilla 68
 *
 **********************************************************************/