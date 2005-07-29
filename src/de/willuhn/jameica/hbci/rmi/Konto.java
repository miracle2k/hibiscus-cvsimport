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
package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;

/**
 * Bildet eine Bankverbindung in HBCI ab.
 */
public interface Konto extends DBObject,Checksum
{

	/**
	 * Liefert die Kontonummer fuer diese Bankverbindung.
   * @return Kontonummer.
   * @throws RemoteException
   */
  public String getKontonummer() throws RemoteException;
	
	/**
	 * Liefert die Bankleitzahl fuer diese Bankverbindung.
   * @return Bankleitzahl.
   * @throws RemoteException
   */
  public String getBLZ() throws RemoteException;
	
	/**
	 * Liefert den Namen des Konto-Inhabers.
   * @return Name des Konto-Inhabers.
   * @throws RemoteException
   */
  public String getName() throws RemoteException;
	
	/**
	 * Liefert die Bezeichnung des Kontos.
   * @return Bezeichnung des Kontos.
   * @throws RemoteException
   */
  public String getBezeichnung() throws RemoteException;

	/**
	 * Liefert die Kundennummer bei der Bank.
   * @return Kundennummer.
   * @throws RemoteException
   */
  public String getKundennummer() throws RemoteException;

	/**
	 * Liefert die Java-Klasse des zu verwendenden Passports.
   * Dieser kann anschliessend mittels <code>PassportRegistry#findByClass(String)</code> geladen werden.
   * @return Java-Klasse des Passports.
   * @throws RemoteException
   */
  public String getPassportClass() throws RemoteException;
	
  /**
   * Liefert die Waehrungs-Bezeichnung der Bankverbindung.
   * @return Waehrungsbezeichnung.
   * @throws RemoteException
   */
  public String getWaehrung() throws RemoteException;

	/**
	 * Speichert die Kontonummer der Bankverbindung.
   * @param kontonummer Kontonummer.
   * @throws RemoteException
   */
  public void setKontonummer(String kontonummer) throws RemoteException;
	
	/**
	 * Speichert die Bankleitzahl der Bankverbindung.
   * @param blz Bankleitzahl.
   * @throws RemoteException
   */
  public void setBLZ(String blz) throws RemoteException;
	
	/**
	 * Speichert den Namen des Konto-Inhabers.
   * @param name Name des Konto-Inhaber.s
   * @throws RemoteException
   */
  public void setName(String name) throws RemoteException;

	/**
	 * Speichert die Bezeichnung des Kontos.
   * @param bezeichnung Bezeichnung.
   * @throws RemoteException
   */
  public void setBezeichnung(String bezeichnung) throws RemoteException;

  /**
   * Speichert die Waehrungsbezeichnung.
   * @param waehrung Bezeichnung.
   * @throws RemoteException
   */
  public void setWaehrung(String waehrung) throws RemoteException;

	/**
	 * Speichert den Namen der Java-Klasse des zu verwendenden Passports.
   * @param passport Passport.
   * @throws RemoteException
   */
  public void setPassportClass(String passport) throws RemoteException;

	/**
	 * Speichert die Kundennummer.
   * @param kundennummer Kundennummer.
   * @throws RemoteException
   */
  public void setKundennummer(String kundennummer) throws RemoteException;

	/**
	 * Liefert den Saldo des Kontos oder <code>0.0</code> wenn er noch nie
	 * abgefragt wurde.
   * @return Saldo des Kontos.
   * @throws RemoteException
   */
  public double getSaldo() throws RemoteException;

	/**
	 * Speichert den neuen Saldo.
   * @param saldo Neuer Saldo.
   * @throws RemoteException
   */
  public void setSaldo(double saldo) throws RemoteException;
	
	/**
	 * Liefert das Datum des aktuellen Saldos oder <code>null</code> wenn er
	 * noch nie abgefragt wurde.
   * @return Datum des Saldos.
   * @throws RemoteException
   */
  public Date getSaldoDatum() throws RemoteException;

  /**
   * Liefert true, wenn das Konto beim Synchronisieren mit einbezogen werden soll.
   * @return true, wenn es einbezogen werden soll.
   * @throws RemoteException
   */
  public boolean getSynchronize() throws RemoteException; 

  /**
   * Legt fest, ob das Konto beim Synchronisieren mit einbezogen werden soll.
   * @param b true, wenn es einbezogen werden soll.
   * @throws RemoteException
   */
  public void setSynchronize(boolean b) throws RemoteException;
  
  /**
	 * Liefert eine Liste aller Umsaetze fuer das Konto in umgekehrter chronologischer Reihenfolge.
   * Also die neuesten zuerst, die aeltesten zuletzt.
   * @return Umsatzliste.
   * @throws RemoteException
   */
  public DBIterator getUmsaetze() throws RemoteException;

  /**
   * Liefert den ersten Umsatz.
   * @return Umsatz.
   * @throws RemoteException
   */
  public Umsatz getFirstUmsatz() throws RemoteException;
  
  /**
   * Liefert den letzten Umsatz.
   * @return letzter Umsatz.
   * @throws RemoteException
   */
  public Umsatz getLastUmsatz() throws RemoteException;
  
  /**
   * Liefert eine Liste aller Umsaetze fuer die letzten x Tage.
   * @param days Anzahl der Tage.
   * @return Umsatzliste.
   * @throws RemoteException
   */
  public DBIterator getUmsaetze(int days) throws RemoteException;

	/**
	 * Liefert eine Liste aller Ueberweisungen, die ueber dieses Konto getaetigt wurden.
	 * @return Ueberweisungsliste.
	 * @throws RemoteException
	 */
	public DBIterator getUeberweisungen() throws RemoteException;
	
	/**
	 * Liefert alle Dauerauftraege, die fuer das Konto vorliegen.
	 * Dabei werden auch jene geliefert, die lokal erstellt, jedoch noch nicht
	 * zur Bank hochgeladen wurden.
   * @return Liste der Dauerauftraege.
   * @throws RemoteException
   */
  public DBIterator getDauerauftraege() throws RemoteException;

	/**
	 * Liefert alle Lastschriften, die fuer das Konto vorliegen.
   * @return Liste der Lastschriften.
   * @throws RemoteException
   */
  public DBIterator getLastschriften() throws RemoteException;

	/**
	 * Liefert alle Sammel-Lastschriften, die fuer das Konto vorliegen.
	 * @return Liste der Lastschriften.
	 * @throws RemoteException
	 */
	public DBIterator getSammelLastschriften() throws RemoteException;

	/**
	 * Liefert die HBCI-Protokollierung des Kontos in Form einer Liste von Protokoll-Objekten.
   * @return Liste von Protokoll-Objekten.
   * @throws RemoteException
   */
  public DBIterator getProtokolle() throws RemoteException;

	/**
	 * Fuegt den uebergebenen Text zum Konto-Protokoll hinzu.
   * @param kommentar der hinzuzufuegende Text.
   * @param protokollTyp Typ des Protokoll-Eintrags.
   * Siehe <code>de.willuhn.jameica.hbci.rmi.Protokoll</code>.
   * @throws RemoteException
   */
  public void addToProtokoll(String kommentar, int protokollTyp) throws RemoteException;
}


/**********************************************************************
 * $Log$
 * Revision 1.27  2005-07-29 16:48:13  web0
 * @N Synchronize
 *
 * Revision 1.26  2005/07/11 14:03:42  web0
 * *** empty log message ***
 *
 * Revision 1.25  2005/07/11 13:51:49  web0
 * *** empty log message ***
 *
 * Revision 1.24  2005/06/07 22:41:09  web0
 * @B bug 70
 *
 * Revision 1.23  2005/05/19 23:31:07  web0
 * @B RMI over SSL support
 * @N added handbook
 *
 * Revision 1.22  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 * Revision 1.21  2005/02/27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 * Revision 1.20  2005/02/03 23:57:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.19  2004/10/25 23:12:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2004/10/25 22:39:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/10/25 17:58:57  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.16  2004/10/24 17:19:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2004/10/17 16:28:46  willuhn
 * @N Die ersten Dauerauftraege abgerufen ;)
 *
 * Revision 1.14  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 * Revision 1.13  2004/05/25 23:23:17  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 * Revision 1.12  2004/05/05 22:14:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/04/14 23:53:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/04/05 23:28:45  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/04/04 18:30:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/03/05 00:04:10  willuhn
 * @N added code for umsatzlist
 *
 * Revision 1.7  2004/02/25 23:11:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/02/17 01:01:38  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.4  2004/02/12 23:46:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/02/12 00:38:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/11 15:40:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 **********************************************************************/