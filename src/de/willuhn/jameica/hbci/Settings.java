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

import java.io.File;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.util.Date;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.security.Wallet;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.ServiceSettings;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Verwaltet die Einstellungen des Plugins.
 * @author willuhn
 */
public class Settings
{

  private static de.willuhn.jameica.system.Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
  private static DBService db = null;
	private static String workPath = null;
	private static String libPath = null;
	private static Wallet wallet = null;

	private static Color buchungSollForeground = null;
	private static Color buchungHabenForeground = null;
	private static Color ueberfaelligForeground = null;
	
  /**
   * Liefert den Datenbank-Service.
   * @return Datenbank.
   * @throws RemoteException
   */
  public static DBService getDBService() throws RemoteException
  {
    if (db != null)
      return db;
		try {
			db = (DBService) Application.getServiceFactory().lookup(HBCI.class,"database");
			return db;
		}
    catch (ConnectException ce)
    {
      // Die Exception fliegt nur bei RMI-Kommunikation mit fehlendem RMI-Server
      I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
      String host = ServiceSettings.getLookupHost(HBCI.class,"database");
      int    port = ServiceSettings.getLookupPort(HBCI.class,"database");
      String msg = i18n.tr("Hibiscus-Server \"{0}\" nicht erreichbar", (host + ":" + port));
      try
      {
        Application.getCallback().notifyUser(msg);
        throw new RemoteException(msg);
      }
      catch (Exception e)
      {
        Logger.error("error while notifying user",e);
        throw new RemoteException(msg);
      }
    }
		catch (Exception e)
		{
			throw new RemoteException("unable to open/create database",e);
		}
  }

	/**
	 * Liefert die Vordergrundfarbe fuer Soll-Buchungen in Tabellen.
   * @return Farbe.
   */
  public static Color getBuchungSollForeground()
	{
		if (buchungSollForeground != null)
			return buchungSollForeground;

		buchungSollForeground = new Color(GUI.getDisplay(),settings.getRGB("buchung.soll.fg",new RGB(147,33,33)));
		return buchungSollForeground;
	}

	/**
	 * Liefert die Vordergrundfarbe fuer Haben-Buchungen in Tabellen.
	 * @return Farbe.
	 */
	public static Color getBuchungHabenForeground()
	{
		if (buchungHabenForeground != null)
			return buchungHabenForeground;

		buchungHabenForeground = new Color(GUI.getDisplay(),settings.getRGB("buchung.haben.fg",new RGB(4,13,169)));
		return buchungHabenForeground;
	}

	/**
	 * Liefert die Vordergrundfarbe fuer ueberfaellige Ueberweisungen.
	 * @return Farbe.
	 */
	public static Color getUeberfaelligForeground()
	{
		if (ueberfaelligForeground != null)
			return ueberfaelligForeground;

		ueberfaelligForeground = new Color(GUI.getDisplay(),settings.getRGB("ueberfaellig.fg",new RGB(140,0,0)));
		return ueberfaelligForeground;
	}

	/**
	 * Speichert die Farben fuer den Vordergrund von Soll-Buchungen.
   * @param rgb
   */
  public static void setBuchungSollForeground(RGB rgb)
	{
		settings.setAttribute("buchung.soll.fg",rgb);
		buchungSollForeground = null;
	}

	/**
	 * Speichert die Farben fuer den Vordergrund von Haben-Buchungen.
	 * @param rgb
	 */
	public static void setBuchungHabenForeground(RGB rgb)
	{
		settings.setAttribute("buchung.haben.fg",rgb);
		buchungHabenForeground = null;
	}

	/**
	 * Speichert die Farben fuer den Vordergrund von ueberfaelligen Ueberweisungen.
	 * @param rgb
	 */
	public static void setUeberfaelligForeground(RGB rgb)
	{
		settings.setAttribute("ueberfaellig.fg",rgb);
		ueberfaelligForeground = null;
	}

	/**
	 * Liefert den Verzeichnis-Pfad zu den nativen Libs.
	 * @return Pfad der Libs.
	 */
	public static String getLibPath()
	{
		if (libPath != null)
			return libPath;
		libPath = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getPath() + "/lib";
    try
    {
      libPath = new File(libPath).getCanonicalPath();
    }
    catch (Exception e)
    {
      Logger.error("error while determining canonical path",e);
    }
		return libPath;
	}

	/**
	 * Liefert den Pfad zum Work-Verzeichnis.
	 * @return Pfad des Work-Verzeichnis.
	 */
	public static String getWorkPath()
	{
		if (workPath != null)
			return workPath;
		workPath = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getWorkPath();
		return workPath;
	}

  /**
   * Legt fest, ob die PIN gehasht gespeichert werden soll, um sie
   * bei erneuter Eingabe pruefen zu koennen.
   * @param checkPin true, wenn die Pin geprueft werden soll.
   */
  public static void setCheckPin(boolean checkPin)
  {
    settings.setAttribute("checkpin",checkPin);
  }

  /**
   * Prueft, ob ein Hash der PIN gespeichert werden soll, um sie bei
   * erneuter Eingabe auf Richtigkeit pruefen zu koennen.
   * @return true, wenn die Pin gehasht gespeichert werden soll.
   */
  public static boolean getCheckPin()
  {
    return settings.getBoolean("checkpin",true);
  }
  
  /**
   * Liefert einen ggf gespeicherten Sicherheitsmechanismus fuer das Konto.
   * @param konto zu testendes Konto.
   * @return ID des Sicherheitsmechanismus.
   */
  public static String getSecMech(Konto konto)
  {
    try
    {
      return settings.getString("secmech.konto." + konto.getID(),null);
    }
    catch (RemoteException e)
    {
      Logger.error("unable to check secmech for konto",e);
    }
    return null;
  }
  
  /**
   * Speichert einen Sicherheitsmechanismus fuer fuer das Konto.
   * @param konto zu testendes Konto.
   * @param s der Sicherheitsmechanismus.
   */
  public static void setSecMech(Konto konto, String s)
  {
    try
    {
      settings.setAttribute("secmech.konto." + konto.getID(),s);
    }
    catch (RemoteException e)
    {
      Logger.error("unable to check secmech for konto",e);
    }
  }
  

  /**
   * Prueft, ob die MD5-Checksumme der Datenbank geprueft werden soll.
   * @return true, wenn die Checksumme geprueft werden soll.
   */
  public static boolean getCheckDatabase()
  {
    return settings.getBoolean("checkdatabase",true);
  }
  
  /**
   * Prueft, ob die TAN waehrend der Eingabe angezeigt werden soll.
   * @return true, wenn die TANs angezeigt werden sollen.
   */
  public static boolean getShowTan()
  {
    return settings.getBoolean("showtan",false);
  }

  /**
   * Legt fest, ob die TANs bei der Eingabe angezeigt werden sollen.
   * @param show true, wenn sie angezeigt werden sollen.
   */
  public static void setShowTan(boolean show)
  {
    settings.setAttribute("showtan",show);
  }

  /**
   * Prueft, ob Tausender-Punkte bei Betraegen verwendet werden sollen.
   * @return true, wenn Tausender-Punkte verwendet werden sollen.
   */
  public static boolean getDecimalGrouping()
  {
    return settings.getBoolean("decimalgrouping",false);
  }

  /**
   * Legt fest, ob Tausender-Punkte bei Betraegen verwendet werden sollen.
   * @param grouping true, wenn Tausender-Punkte verwendet werden sollen.
   */
  public static void setDecimalGrouping(boolean grouping)
  {
    settings.setAttribute("decimalgrouping",grouping);
    HBCI.DECIMALFORMAT.setGroupingUsed(grouping);
  }

  /**
   * Speichert, ob wir eine permanente Online-Verbindung haben und daher
   * vom HBCI-Kernel nicht dauernd gefragt werden muessen, ob wir eine
   * Internetverbindung haben wollen.
   * @param online true, wenn wir dauernd online sind.
   */
  public static void setOnlineMode(boolean online)
  {
    settings.setAttribute("online",online);
  }

  /**
   * Liefert true, wenn die Kontonummern via Pruefsumme gecheckt werden sollen.
   * @return true, wenn die Pruefziffern-Kontrolle aktiviert ist.
   */
  public static boolean getKontoCheck()
  {
    return settings.getBoolean("kontocheck",true);
  }

  /**
   * Legt fest, ob die Kontonummern via Pruefsumme gecheckt werden sollen.
   * @param check true, wenn gecheckt werden soll.
   */
  public static void setKontoCheck(boolean check)
  {
    settings.setAttribute("kontocheck",check);
  }

  /**
   * Prueft, ob wir eine permanente Online-Verbindung haben und daher
   * vom HBCI-Kernel nicht dauernd gefragt werden muessen, ob wir eine
   * Internetverbindung haben wollen.
   * @return true, wenn wir dauernd online sind.
   */
  public static boolean getOnlineMode()
  {
    return settings.getBoolean("online",false);
  }

  /**
	 * Liefert das Limit bei Ueberweisungen.
	 * Soll den Benutzer davor schuetzen, versehentlich zu grosse Betraege bei
	 * einer Ueberweisung einzugeben.
   * @return Ueberweisungslimit.
   */
  public static double getUeberweisungLimit()
	{
		return settings.getDouble("ueberweisunglimit",1000.0);
	}
	
	/**
	 * Definiert ein Limit bei Ueberweisungen.
	 * Soll den Benutzer davor schuetzen, versehentlich zu grosse Betraege bei
	 * einer Ueberweisung einzugeben.
   * @param limit das Limit fuer Ueberweisungen.
   */
  public static void setUeberweisungLimit(double limit)
	{
		settings.setAttribute("ueberweisunglimit",limit);
	}
	
  /**
   * Liefert das von Hibiscus verwendete Wallet.
   * @return das Wallet.
   * @throws Exception
   */
  public static Wallet getWallet() throws Exception
  {
		if (wallet == null)
    {
      wallet = Application.getSSLFactory().getWallet(HBCI.class);

      // BUGZILLA 109 http://www.willuhn.de/bugzilla/show_bug.cgi?id=109
      if (wallet.get("migration") == null)
        wallet.set("migration",new Date().toString());
    }
		return wallet;
  }
}

/*********************************************************************
 * $Log$
 * Revision 1.41  2006-03-28 17:52:23  willuhn
 * @B bug 218
 *
 * Revision 1.40  2006/03/24 00:15:35  willuhn
 * @B Duplikate von Settings-Instanzen entfernt
 *
 * Revision 1.39  2005/11/28 11:15:49  willuhn
 * @C database check can be disabled
 *
 * Revision 1.38  2005/08/22 10:36:37  willuhn
 * @N bug 115, 116
 *
 * Revision 1.37  2005/08/04 22:15:14  willuhn
 * @B bug 109
 *
 * Revision 1.36  2005/07/24 22:26:42  web0
 * @B bug 101
 *
 * Revision 1.35  2005/06/27 11:26:30  web0
 * @N neuer Test bei Dauerauftraegen (zum Monatsletzten)
 * @N neue DDV-Lib
 *
 * Revision 1.34  2005/06/16 13:29:13  web0
 * *** empty log message ***
 *
 * Revision 1.33  2005/06/06 09:54:39  web0
 * *** empty log message ***
 *
 * Revision 1.32  2005/05/02 11:54:09  web0
 * *** empty log message ***
 *
 * Revision 1.31  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.30  2005/02/08 22:28:00  willuhn
 * *** empty log message ***
 *
 * Revision 1.29  2005/02/02 16:15:52  willuhn
 * @N Neue Dialoge fuer RDH
 *
 * Revision 1.28  2005/02/01 17:15:37  willuhn
 * *** empty log message ***
 *
 * Revision 1.27  2005/01/30 20:45:35  willuhn
 * *** empty log message ***
 *
 * Revision 1.26  2005/01/15 16:48:17  willuhn
 * *** empty log message ***
 *
 * Revision 1.25  2005/01/09 23:21:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.24  2004/12/06 22:45:06  willuhn
 * *** empty log message ***
 *
 * Revision 1.23  2004/11/12 18:25:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.22  2004/10/24 17:19:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.21  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.20  2004/07/23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.19  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2004/07/20 21:48:00  willuhn
 * @N ContextMenus
 *
 * Revision 1.17  2004/07/20 00:11:07  willuhn
 * @C Code sharing zwischen Ueberweisung und Dauerauftrag
 *
 * Revision 1.16  2004/06/30 20:58:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2004/06/10 20:56:33  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.14  2004/05/25 23:23:18  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 * Revision 1.13  2004/05/11 23:31:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/05/11 21:11:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/05/09 17:39:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/04/21 22:28:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/04/13 23:14:23  willuhn
 * @N datadir
 *
 * Revision 1.8  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.7  2004/04/05 23:28:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/03/17 00:06:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/02/21 19:49:04  willuhn
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