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
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Empfaenger;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.util.ApplicationException;

/**
 * Action fuer neuen Dauerauftrag.
 */
public class DauerauftragNeu implements Action
{

  /**
   * Als Context kann ein Konto, ein Empfaenger oder ein Dauerauftrag angegeben werden.
   * Abhaengig davon wird das eine oder andere Feld in dem Dauerauftrag 
   * vorausgefuellt oder der uebergebene Dauerauftrag geladen.
   * Wenn nichts angegeben ist, wird ein leerer Dauerauftrag erstellt und angezeigt.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		Dauerauftrag d = null;

		if (context instanceof Dauerauftrag)
		{
			d = (Dauerauftrag) context;
		}
		else if (context instanceof Konto)
		{
			try {
				Konto k = (Konto) context;
				d = (Dauerauftrag) Settings.getDBService().createObject(Dauerauftrag.class,null);
				d.setKonto(k);
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
				d = (Dauerauftrag) Settings.getDBService().createObject(Dauerauftrag.class,null);
				d.setEmpfaenger(e);
			}
			catch (RemoteException e)
			{
				// Dann halt nicht
			}
		}

  	GUI.startView(de.willuhn.jameica.hbci.gui.views.DauerauftragNeu.class.getName(),d);
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.1  2004-10-19 23:33:31  willuhn
 * *** empty log message ***
 *
 **********************************************************************/