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
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Empfaenger;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.util.ApplicationException;

/**
 * Action fuer neue Lastschrift.
 */
public class LastschriftNew implements Action
{

  /**
   * Als Context kann ein Konto, ein Empfaenger oder eine Lastschrift angegeben werden.
   * Abhaengig davon wird das eine oder andere Feld in der neuen Lastschrift
   * vorausgefuellt oder die uebergebene Lastschrift geladen.
   * Wenn nichts angegeben ist, wird eine leere Lastschrift erstellt und angezeigt.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		Lastschrift u = null;

		if (context instanceof Lastschrift)
		{
			u = (Lastschrift) context;
		}
		else if (context instanceof Konto)
		{
			try {
				Konto k = (Konto) context;
				u = (Lastschrift) Settings.getDBService().createObject(Lastschrift.class,null);
				u.setKonto(k);
			}
			catch (RemoteException e)
			{
				// Dann halt nicht
			}
		}
		else if (context instanceof Empfaenger)
		{
			try {
				Empfaenger e = (Empfaenger) context;
				u = (Lastschrift) Settings.getDBService().createObject(Lastschrift.class,null);
				u.setEmpfaenger(e);
			}
			catch (RemoteException e)
			{
				// Dann halt nicht
			}
		}

  	GUI.startView(de.willuhn.jameica.hbci.gui.views.LastschriftNew.class,u);
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.1  2005-01-19 00:16:04  willuhn
 * @N Lastschriften
 *
 **********************************************************************/