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
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.controller.AbstractControl;
import de.willuhn.jameica.gui.parts.CheckboxInput;
import de.willuhn.jameica.gui.parts.Table;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.views.PassportDetails;
import de.willuhn.jameica.hbci.gui.views.Welcome;
import de.willuhn.jameica.hbci.rmi.Passport;
import de.willuhn.util.I18N;

/**
 * Controller fuer die Einstellungen.
 */
public class SettingsControl extends AbstractControl {

	// Eingabe-Felder
	private CheckboxInput onlineMode     		= null;
	private CheckboxInput checkPin     			= null;

	private Table passportList 							= null;
	
	private I18N i18n;

  /**
   * @param view
   */
  public SettingsControl(AbstractView view) {
    super(view);
		i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
  }

	/**
	 * Liefert eine Tabelle mit den existierenden Passports.
   * @return Tabelle mit den Passports.
   * @throws RemoteException
   */
  public Table getPassportListe() throws RemoteException
	{
    if (passportList != null)
      	return passportList;

    DBIterator list = Settings.getDatabase().createList(Passport.class);

		passportList = new Table(list,this);
		passportList.addColumn(i18n.tr("Bezeichnung"),"name");
		passportList.addColumn(i18n.tr("Typ"),"passport_type_id");
		return passportList;
	}

	/**
	 * Checkbox zur Auswahl des Online-Mode.
   * @return Checkbox.
   * @throws RemoteException
   */
  public CheckboxInput getOnlineMode() throws RemoteException
	{
		if (onlineMode != null)
			return onlineMode;
		onlineMode = new CheckboxInput(Settings.getOnlineMode());
		return onlineMode;
	}

	/**
	 * Liefert eine Checkbox zur Aktivierung oder Deaktivierung der Pin-Pruefung via Checksumme.
   * @return Checkbox.
   * @throws RemoteException
   */
  public CheckboxInput getCheckPin() throws RemoteException
	{
		if (checkPin != null)
			return checkPin;
		checkPin = new CheckboxInput(Settings.getCheckPin());
		checkPin.addListener(new CheckPinListener());
		return checkPin;
	}
  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleDelete()
   */
  public void handleDelete() {
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCancel()
   */
  public void handleCancel() {
  	GUI.startView(Welcome.class.getName(),null);
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleStore()
   */
  public void handleStore() {
		try {
			Settings.setOnlineMode(((Boolean)getOnlineMode().getValue()).booleanValue());
			Settings.setCheckPin(((Boolean)getCheckPin().getValue()).booleanValue());

			// Wir gehen nochmal auf Nummer sicher, dass die Pruefsummen-Algorithmen vorhanden sind
			new CheckPinListener().handleEvent(null);
			GUI.getStatusBar().setSuccessText(i18n.tr("Einstellungen gespeichert."));
		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while storing settings",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern der Einstellungen"));
		}
  }

	/**
   * Loescht den gegebenenfalls vorhandenen gespeicherten Pin-Hash.
   */
  public void handleDeleteCheckSum()
	{
		Settings.setCheckSum(null);
	}

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCreate()
   */
  public void handleCreate() {
  }

	/**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleOpen(java.lang.Object)
   */
  public void handleOpen(Object o)
	{
		GUI.startView(PassportDetails.class.getName(),o);
	}

	/**
	 * Listener, der prueft, ob die Hash-Algorithmen zur Checksummen-Bildung
	 * verfuegbar sind.
   */
  private class CheckPinListener implements Listener
	{

    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event) {
			try {
				MessageDigest.getInstance("MD5");
				MessageDigest.getInstance("SHA1");
			}
			catch (NoSuchAlgorithmException e)
			{
				Settings.setCheckPin(false);
				try {
					getCheckPin().disable();
				}
				catch (RemoteException e1) {/*useless*/}
				GUI.getStatusBar().setErrorText(i18n.tr("Algorithmen zur Pr�fsummenbildung auf diesem System nicht vorhanden"));
			}
    }
	}
}


/**********************************************************************
 * $Log$
 * Revision 1.10  2004-03-30 22:07:50  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.8  2004/03/05 00:19:23  willuhn
 * @D javadoc fixes
 * @C Converter moved into server package
 *
 * Revision 1.7  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.6  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 * Revision 1.5  2004/02/24 22:47:04  willuhn
 * @N GUI refactoring
 *
 * Revision 1.4  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.3  2004/02/21 19:49:04  willuhn
 * @N PINDialog
 *
 * Revision 1.2  2004/02/20 20:45:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/