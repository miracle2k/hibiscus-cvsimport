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
package de.willuhn.jameica.hbci.gui;

import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.dialogs.SimpleDialog;
import de.willuhn.jameica.hbci.gui.dialogs.PINDialog;
import de.willuhn.jameica.hbci.gui.dialogs.PassportLoadDialog;
import de.willuhn.jameica.hbci.gui.dialogs.PassportSaveDialog;
import de.willuhn.jameica.hbci.gui.dialogs.TANDialog;
import de.willuhn.logging.Logger;

/**
 * Hilfsklasse zur Erzeugung von Hilfs-Dialogen bei der HBCI-Kommunikation.
 */
public class DialogFactory {

	private static AbstractDialog dialog = null;

  /**
	 * Erzeugt einen simplen Dialog mit einem OK-Button.
   * @param headline Ueberschrift des Dialogs.
   * @param text Text des Dialogs.
   * @throws Exception
   */
  public static synchronized void openSimple(final String headline, final String text) throws Exception
	{
		check();
		SimpleDialog d = new SimpleDialog(AbstractDialog.POSITION_CENTER);
		d.setTitle(headline);
		d.setText(text);
		dialog = d;
		try
		{
			d.open();
		}
		finally
		{
			close();
		}
	}

  /**
	 * Erzeugt den PIN-Dialog.
	 * Hinweis: Wirft eine RuntimeException, wenn der PIN-Dialog abgebrochen
	 * oder die PIN drei mal falsch eingegeben wurde (bei aktivierter Checksummen-Pruefung).
	 * Hintergrund: Der Dialog wurde aus dem HBCICallBack heraus aufgerufen und soll im
	 * Fehlerfall den HBCI-Vorgang abbrechen.
	 * @return die eingegebene PIN.
   * @throws Exception
	 */
	public static synchronized String getPIN() throws Exception
	{
		check();
		dialog = new PINDialog(AbstractDialog.POSITION_CENTER);
		try {
			return (String) dialog.open();
		}
		finally
		{
			close();
		}
	}

	/**
	 * Dialog zur Eingabe des Passworts fuer das Sicherheitsmedium beim Laden.
   * @return eingegebenes Passwort.
   * @throws Exception
   */
  public static synchronized String loadPassport() throws Exception
	{
		check();
		dialog = new PassportLoadDialog(AbstractDialog.POSITION_CENTER);
		try {
			return (String) dialog.open();
		}
		finally
		{
			close();
		}
	}

	/**
	 * Dialog zur Eingabe des Passworts fuer das Sicherheitsmedium beim Speichern.
	 * @return eingegebenes Passwort.
	 * @throws Exception
	 */
	public static synchronized String savePassport() throws Exception
	{
		check();
		dialog = new PassportSaveDialog(AbstractDialog.POSITION_CENTER);
		try {
			return (String) dialog.open();
		}
		finally
		{
			close();
		}
	}

  /**
	 * Erzeugt einen TAN-Dialog.
	 * Hinweis: Wirft eine RuntimeException, wenn der TAN-Dialog abgebrochen wurde.
	 * Hintergrund: Der Dialog wurde aus dem HBCICallBack heraus aufgerufen und soll im
	 * Fehlerfall den HBCI-Vorgang abbrechen.
	 * @return die eingegebene TAN.
   * @throws Exception
	 */
	public static synchronized String getTAN() throws Exception
	{
		check();
		dialog = new TANDialog(AbstractDialog.POSITION_CENTER);
		try {
			return (String) dialog.open();
		}
		finally
		{
			close();
		}
	}

	/**
   * Prueft, ob der Dialog geoeffnet werden kann.
   */
  private static synchronized void check()
	{
		if (dialog == null)
			return;

		Logger.error("alert: there's another opened dialog");
		throw new RuntimeException("alert: there's another opened dialog");
	}

	/**
   * Schliesst den gerade offenen Dialog.
   */
  public static synchronized void close()
	{
		if (dialog == null)
			return;
		try {
			dialog.close();
		}
		finally
		{
			dialog = null;
		}
	}
	
}


/**********************************************************************
 * $Log$
 * Revision 1.18  2005-01-09 23:21:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/10/19 23:33:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2004/06/30 20:58:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/05/05 21:27:13  willuhn
 * @N added TAN-Dialog
 *
 * Revision 1.13  2004/05/04 23:58:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/05/04 23:07:24  willuhn
 * @C refactored Passport stuff
 *
 * Revision 1.11  2004/04/27 22:23:56  willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
 *
 * Revision 1.10  2004/03/30 22:07:50  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.8  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 * Revision 1.7  2004/02/24 22:47:05  willuhn
 * @N GUI refactoring
 *
 * Revision 1.6  2004/02/22 20:04:54  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.5  2004/02/21 19:49:04  willuhn
 * @N PINDialog
 *
 * Revision 1.4  2004/02/20 20:45:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/02/20 01:25:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/13 00:41:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/12 23:46:46  willuhn
 * *** empty log message ***
 *
 **********************************************************************/