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
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Action fuer die Anzeige der Umsaetze eines Kontos.
 */
public class UmsatzListe implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Konto</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
  	I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  	if (context == null || !(context instanceof Konto))
  		throw new ApplicationException(i18n.tr("Bitte w�hlen Sie ein Konto aus."));

		Konto k = (Konto) context;
		
		try
		{
			if (k.isNewObject())
				throw new ApplicationException(i18n.tr("Bitte speichern Sie zun�chst das Konto"));
		}
		catch (RemoteException e)
		{
			Logger.error("error while loading umsaetze",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Laden der Ums�tze"));
		}
		GUI.startView(de.willuhn.jameica.hbci.gui.views.UmsatzListe.class.getName(),k);
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.2  2004-10-20 12:08:18  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 * Revision 1.1  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 * Revision 1.1  2004/10/12 23:48:39  willuhn
 * @N Actions
 *
 **********************************************************************/