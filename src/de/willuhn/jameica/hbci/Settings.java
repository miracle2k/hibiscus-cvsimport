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
package de.willuhn.jameica.hbci;

import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import sun.misc.BASE64Encoder;

import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.Application;

/**
 * Verwaltet die Einstellungen des Plugins.
 * @author willuhn
 */
public class Settings
{

  private static de.willuhn.jameica.Settings settings = new de.willuhn.jameica.Settings(HBCI.class);
  private static DBService db = null;
  private static String path = null;
  private static String passphrase = null;

	/**
	 * Liefert den Datenbank-Service.
	 * @return Datenbank.
	 * @throws RemoteException
	 */
	public static DBService getDatabase() throws RemoteException
	{
		return db;
	}

	/**
	 * Speichert die zu verwendende Datenbank.
	 * @param db die Datenbank.
	 */
	protected static void setDatabase(DBService db)
	{
		Settings.db = db;
	}

	/**
	 * Speichert den Verzeichnis-Pfad zu diesem Plugin.
   * @param path Pfad zu diesem Plugin.
   */
  protected static void setPath(String path)
	{
		Settings.path = path;
	}

	/**
	 * Liefert den Verzeichnis-Pfad in dem sich das Plugin befindet.
   * @return Pfad des Plugins.
   */
  public static String getPath()
	{
		return Settings.path;
	}

	/**
	 * Legt fest, ob die PIN gehasht gespeichert werden soll, um sie
	 * bei erneuter Eingabe pruefen zu koennen.
	 * @param checkPin true, wenn die Pin geprueft werden soll.
	 */
	public static void setCheckPin(boolean checkPin)
	{
		settings.setAttribute("checkpin", checkPin ? "true" : "false");
	}

	/**
	 * Prueft, ob ein Hash der PIN gespeichert werden soll, um sie bei
	 * erneuter Eingabe auf Richtigkeit pruefen zu koennen.
   * @return true, wenn die Pin gehasht gespeichert werden soll.
   */
  public static boolean getCheckPin()
	{
		return "true".equals(settings.getAttribute("checkpin","false"));
	}

	/**
	 * Liefert die Check-Summe der PIN oder <code>null</code> wenn sie nie
	 * gespeichert wurde.
   * @return Check-Summe der Pin.
   */
  public static String getCheckSum()
	{
		return settings.getAttribute("checksum",null);
	}

	/**
	 * Speichert die Check-Summe der PIN.
   * @param checksum Check-Summe der Pin.
   */
  public static void setCheckSum(String checksum)
	{
		settings.setAttribute("checksum",checksum);
	}

	/**
	 * Speichert, ob wir eine permanente Online-Verbindung haben und daher
	 * vom HBCI-Kernel nicht dauernd gefragt werden muessen, ob wir eine
	 * Internetverbindung haben wollen.
   * @param online true, wenn wir dauernd online sind.
   */
  public static void setOnlineMode(boolean online)
	{
		settings.setAttribute("online", online ? "true" : "false");
	}

	/**
	 * Prueft, ob wir eine permanente Online-Verbindung haben und daher
	 * vom HBCI-Kernel nicht dauernd gefragt werden muessen, ob wir eine
	 * Internetverbindung haben wollen.
   * @return true, wenn wir dauernd online sind.
   */
  public static boolean getOnlineMode()
	{
		return "true".equals(settings.getAttribute("online","false"));
	}

	/**
	 * Liefert das Passwort die lokalen Daten verschluesselt werden.
   * @return Passphrase.
   */
  protected static String getPassphrase()
	{
		if (passphrase != null)
			return passphrase;

		MessageDigest md = null;
		byte[] hashed = null;
		try {
			md = MessageDigest.getInstance("SHA1");
			hashed = md.digest(getPath().getBytes());
		}
		catch (NoSuchAlgorithmException nsae)
		{
			Application.getLog().warn("algorithm SHA1 not found, trying MD5");
			try {
				md = MessageDigest.getInstance("MD5");
				hashed = md.digest(getPath().getBytes());
			}
			catch (NoSuchAlgorithmException nsae2)
			{
				Application.getLog().error("no such algorithm SHA1/MD5",nsae2);
				hashed = getPath().getBytes();
			}
		}
		BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encode(hashed);
	}

}

/*********************************************************************
 * $Log$
 * Revision 1.5  2004-02-21 19:49:04  willuhn
 * @N PINDialog
 *
 * Revision 1.4  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.3  2004/02/12 00:38:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/09 22:09:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/09 13:06:03  willuhn
 * @C misc
 *
 * Revision 1.7  2004/01/28 00:37:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/01/28 00:31:34  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/01/25 19:44:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/01/03 18:07:22  willuhn
 * @N Exception logging
 *
 * Revision 1.3  2003/12/15 19:08:04  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/12/11 21:00:35  willuhn
 * @C refactoring
 *
 * Revision 1.1  2003/11/24 23:02:11  willuhn
 * @N added settings
 *
 **********************************************************************/