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
package de.willuhn.jameica.hbci.gui.dialogs;

import java.rmi.RemoteException;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.SearchDialog;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Empfaenger;
import de.willuhn.util.I18N;

/**
 * Such-Dialog fuer Empfaenger.
 */
public class EmpfaengerSearchDialog extends SearchDialog {

	/**
	 * @param object
	 */
	public EmpfaengerSearchDialog()
	{
		// wir setzten die Liste, die der Dialog anzeigen soll.
		try {
			setList(Settings.getDatabase().createList(Empfaenger.class));
		}
		catch (RemoteException e)
		{
			Application.getLog().error("unable to init empfaenger search dialog.",e);
			GUI.setActionText(I18N.tr("Fehler beim �ffnen des Such-Dialogs."));
		}
    
		// und definieren noch die Spalten, die im Dialog angezeigt werden sollen.
		addColumn(I18N.tr("Name"),"name");
		addColumn(I18N.tr("Kontonummer"),"kontonummer");
		addColumn(I18N.tr("BLZ"),"blz");
    
		// und wir definieren noch einen passenden Titel
		setTitle(I18N.tr("Auswahl des Empf�ngers"));
	}

	/**
	 * @see de.willuhn.jameica.views.SearchDialog#load(java.lang.String)
	 */
	protected String load(String id)
	{
		if (id == null || id.length() == 0)
			return "";
			
		try {
			Empfaenger e = (Empfaenger) Settings.getDatabase().createObject(Empfaenger.class,id);
			return e.getKontonummer();
		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while loading choosen empfaenger",e);
			return "";
		}
	}

}


/**********************************************************************
 * $Log$
 * Revision 1.1  2004-02-22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/