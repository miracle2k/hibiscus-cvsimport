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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import sun.misc.BASE64Encoder;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.PasswordDialog;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.server.JobFactory;
import de.willuhn.util.I18N;

/**
 * Dialog f�r die PIN-Eingabe.
 * Es muss weder Text, noch Titel oder LabelText gesetzt werden.
 * Das ist alles schon drin.
 */
public class PINDialog extends PasswordDialog {

  /**
   * ct.
   * @param position Position des Dialogs.
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#POSITION_CENTER
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#POSITION_MOUSE
   */
  public PINDialog(int position) {
    super(position);
    setTitle(I18N.tr("PIN-Eingabe"));
    setLabelText(I18N.tr("PIN"));
    setText(I18N.tr("Bitte geben Sie Ihre PIN ein."));
  }

	/**
   * @see de.willuhn.jameica.gui.dialogs.PasswordDialog#checkPassword(java.lang.String)
   */
  protected boolean checkPassword(String password)
	{
		if (password == null && password.length() == 0)
		{
			setErrorText(I18N.tr("Fehler: Bitte geben Sie Ihre PIN ein"));
			return false;
		}

		if (password.length() != 5)
		{
			setErrorText(I18N.tr("Fehler: PIN muss f�nf-stellig sein"));
			return false;
		}

		// TODO: Dialog schliesst sich nicht sofort.
		if (!Settings.getCheckPin())
			return true;

		// PIN-Ueberpruefung aktiv. Also checken wir die Pruef-Summe
		String checkSum = Settings.getCheckSum();
		if (checkSum == null || checkSum.length() == 0)
		{
			// Das ist die erste Eingabe. Dann koennen wir nur
			// eine neue Check-Summe bilden, sie abspeichern und
			// hoffen, dass sie richtig eingegeben wurd.
			try {
				Settings.setCheckSum(createCheckSum(password));
			}
			catch (NoSuchAlgorithmException e)
			{
				Application.getLog().error("hash algorithm not found",e);
				GUI.setActionText(I18N.tr("Pr�fsumme konnte nicht ermittelt werden. Option wurde deaktiviert."));
				Settings.setCheckPin(false);
				Settings.setCheckSum(null);
			}
			return true;
		}
		// Check-Summe existiert, dann vergleichen wir die Eingabe
		else {
			String n = null;
			try {
				n = createCheckSum(password);
			}
			catch (NoSuchAlgorithmException e)
			{
				Application.getLog().error("hash algorithm not found",e);
				GUI.setActionText(I18N.tr("Pr�fsumme konnte nicht verglichen werden. Option wurde deaktiviert."));
				Settings.setCheckPin(false);
				Settings.setCheckSum(null);
				return true;
			}
			if (n != null && checkSum != null && n.length() > 0 && checkSum.length() > 0 && n.equals(checkSum))
			{
				// Eingabe korrekt
				return true;
			}
    }
		setErrorText(I18N.tr("PIN falsch. Noch ") + getRemainingRetries() + " " + I18N.tr("Versuche"));
		return false;
	}

  /**
	 * Erzeugt eine Check-Summe aus dem uebergebenen String.
   * @param s der String.
   * @return erzeugte Check-Summe.
   * @throws NoSuchAlgorithmException Wenn die benoetigten Hash-Algorithmen nicht zur Verfuegung stehen.
   */
  private static String createCheckSum(String s) throws NoSuchAlgorithmException
	{
		// Es ist vielleicht etwas uebertrieben - ich weiss, aber ich
		// will auf Nummer sicher gehen. Deswegen mach ich aus dem String
		// erst einen MD5-Hash und bilde aus dem dann einen SHA1-Hash.
		// Hintergrund: Kriegt jemand die Datei mit dem Hash in die
		// Finger, wird er sich mit einer Woertbuch-Attacke lange versuchen,
		// da er wahrscheinlich nicht weiss, dass das Passwort doppelt
		// gehasht ist.
		MessageDigest md = null;
		byte[] hashed = null;
		md = MessageDigest.getInstance("MD5");
		byte[] b = md.digest(s.getBytes());
		md = MessageDigest.getInstance("SHA1");
		hashed = md.digest(b);

		BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encode(hashed);
	}

  /**
   * @see de.willuhn.jameica.gui.dialogs.PasswordDialog#cancel()
   */
  protected void cancel() {
		JobFactory.close();
  }
}


/**********************************************************************
 * $Log$
 * Revision 1.1  2004-02-21 19:49:04  willuhn
 * @N PINDialog
 *
 **********************************************************************/