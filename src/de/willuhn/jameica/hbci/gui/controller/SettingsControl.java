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

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.controller.AbstractControl;
import de.willuhn.jameica.gui.parts.CheckboxInput;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.views.Welcome;
import de.willuhn.util.I18N;

/**
 * Controller fuer die Einstellungen.
 */
public class SettingsControl extends AbstractControl {

	// Eingabe-Felder
	private CheckboxInput onlineMode = null;
	private CheckboxInput checkPin = null;

  /**
   * @param view
   */
  public SettingsControl(AbstractView view) {
    super(view);
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
	 * Liefert eine Checkbox 
   * @return
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
			Settings.setOnlineMode(CheckboxInput.ENABLED.equals(getOnlineMode().getValue()));
			Settings.setCheckPin(CheckboxInput.ENABLED.equals(getCheckPin().getValue()));

			// Wir gehen nochmal auf Nummer sicher, dass die Pruefsummen-Algorithmen vorhanden sind
			new CheckPinListener().handleEvent(null);
			GUI.setActionText(I18N.tr("Einstellungen gespeichert."));
		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while storing settings",e);
			GUI.setActionText(I18N.tr("Fehler beim Speichern der Einstellungen"));
		}
  }

	/**
   * Loescht den ggf. vorhandenen gespeicherten Pin-Hash.
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
				MessageDigest md = MessageDigest.getInstance("MD5");
				md = MessageDigest.getInstance("SHA1");
			}
			catch (NoSuchAlgorithmException e)
			{
				Settings.setCheckPin(false);
				try {
					getCheckPin().disable();
				}
				catch (RemoteException e1) {/*useless*/}
				GUI.setActionText(I18N.tr("Algorithmen zur Pr�fsummenbildung auf diesem System nicht vorhanden"));
			}
    }
	}
}


/**********************************************************************
 * $Log$
 * Revision 1.5  2004-02-24 22:47:04  willuhn
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