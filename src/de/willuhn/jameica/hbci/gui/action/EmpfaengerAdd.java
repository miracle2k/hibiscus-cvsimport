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

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Adresse;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, ueber die ein Empfaenger dem Adressbuch hinzugefuegt wird.
 * Als Parameter kann eine Ueberweisung oder ein Umsatz uebergeben werden.
 */
public class EmpfaengerAdd implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Umsatz</code> oder <code>Transfer</code>.
   * Die Empfaenger-Daten werden extrahiert und ein neuer Empfaenger
   * angelegt, falls dieser nicht schon existiert.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null)
			throw new ApplicationException(i18n.tr("Bitte w�hlen Sie eine �berweisung oder einen Umsatz aus"));

		if (!(context instanceof Transfer) && !(context instanceof Umsatz))
			throw new ApplicationException(i18n.tr("Bitte w�hlen Sie eine �berweisung oder einen Umsatz aus"));

		String blz 		= null;
		String konto 	= null;
		String name   = null;

		try {

			if (context instanceof Transfer)
			{
				Transfer t = (Transfer) context;
				blz   = t.getEmpfaengerBLZ();
				konto = t.getEmpfaengerKonto();
				name  = t.getEmpfaengerName();
			}
			else if (context instanceof Umsatz)
			{
				Umsatz u = (Umsatz) context;
				blz   = u.getEmpfaengerBLZ();
				konto = u.getEmpfaengerKonto();
				name  = u.getEmpfaengerName();
			}

			// wir checken erstmal, ob wir den schon haben.
			DBIterator list = Settings.getDBService().createList(Adresse.class);
			list.addFilter("kontonummer = '" + konto + "'");
			list.addFilter("blz = '" + blz + "'");
			if (list.hasNext())
			{
				YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
				d.setTitle(i18n.tr("Empf�nger existiert"));
				d.setText(i18n.tr("Ein Empf�nger mit dieser Kontonummer und BLZ existiert bereits. " +
						"M�chten Sie den Empf�nger dennoch zum Adressbuch hinzuf�gen?"));
				if (!((Boolean) d.open()).booleanValue()) return;
			}
			Adresse e = (Adresse) Settings.getDBService().createObject(Adresse.class,null);
			e.setBLZ(blz);
			e.setKontonummer(konto);
			e.setName(name);
			e.store();
			GUI.getStatusBar().setSuccessText(i18n.tr("Adresse gespeichert"));
		}
		catch (ApplicationException ae)
		{
			throw ae;
		}
		catch (Exception e)
		{
			Logger.error("error while storing empfaenger",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern des Empf�ngers"));
		}
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.3  2005-02-27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 * Revision 1.2  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/20 12:08:18  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 **********************************************************************/