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
public interface Konto extends DBObject, Checksum, Flaggable
{
  /**
   * Flag "kein Flag".
   */
  public final static int FLAG_NONE    = 0;

  /**
   * Flag "Deaktiviert".
   */
  public final static int FLAG_DISABLED = 1 << 0;

  /**
   * Flag "Offline".
   */
  public final static int FLAG_OFFLINE = 1 << 1;

  /**
	 * Liefert die Kontonummer fuer diese Bankverbindung.
	 * 
	 * @return Kontonummer.
	 * @throws RemoteException
	 */
	public String getKontonummer() throws RemoteException;
  
  /**
   * Liefert die Unterkonto-Nummer.
   * BUGZILLA 355
   * @return Unterkonto-Nummer.
   * @throws RemoteException
   */
  public String getUnterkonto() throws RemoteException;

	/**
	 * Liefert die Bankleitzahl fuer diese Bankverbindung.
	 * 
	 * @return Bankleitzahl.
	 * @throws RemoteException
	 */
	public String getBLZ() throws RemoteException;

	/**
	 * Liefert den Namen des Konto-Inhabers.
	 * 
	 * @return Name des Konto-Inhabers.
	 * @throws RemoteException
	 */
	public String getName() throws RemoteException;

	/**
	 * Liefert eine ausfuehrliche Bezeichnung des Kontos bestehend aus
	 * Bezeichnung, Kto und BLZ.
	 * 
	 * @return ausfuehrliche Bezeichnung.
	 * @throws RemoteException
	 */
	public String getLongName() throws RemoteException;

	/**
	 * Liefert die Bezeichnung des Kontos.
	 * 
	 * @return Bezeichnung des Kontos.
	 * @throws RemoteException
	 */
	public String getBezeichnung() throws RemoteException;

	/**
	 * Liefert die Kundennummer bei der Bank.
	 * 
	 * @return Kundennummer.
	 * @throws RemoteException
	 */
	public String getKundennummer() throws RemoteException;

	/**
	 * Liefert die Java-Klasse des zu verwendenden Passports. Dieser kann
	 * anschliessend mittels <code>PassportRegistry#findByClass(String)</code>
	 * geladen werden.
	 * 
	 * @return Java-Klasse des Passports.
	 * @throws RemoteException
	 */
	public String getPassportClass() throws RemoteException;

	/**
	 * Liefert die Waehrungs-Bezeichnung der Bankverbindung.
	 * 
	 * @return Waehrungsbezeichnung.
	 * @throws RemoteException
	 */
	public String getWaehrung() throws RemoteException;

	/**
	 * Speichert die Kontonummer der Bankverbindung.
	 * 
	 * @param kontonummer
	 *          Kontonummer.
	 * @throws RemoteException
	 */
	public void setKontonummer(String kontonummer) throws RemoteException;

  /**
   * Speichert das Unterkonto.
   * @param unterkonto
   * @throws RemoteException
   */
  public void setUnterkonto(String unterkonto) throws RemoteException;
  
	/**
	 * Speichert die Bankleitzahl der Bankverbindung.
	 * 
	 * @param blz
	 *          Bankleitzahl.
	 * @throws RemoteException
	 */
	public void setBLZ(String blz) throws RemoteException;

	/**
	 * Speichert den Namen des Konto-Inhabers.
	 * 
	 * @param name
	 *          Name des Konto-Inhaber.s
	 * @throws RemoteException
	 */
	public void setName(String name) throws RemoteException;

	/**
	 * Speichert die Bezeichnung des Kontos.
	 * 
	 * @param bezeichnung
	 *          Bezeichnung.
	 * @throws RemoteException
	 */
	public void setBezeichnung(String bezeichnung) throws RemoteException;

	/**
	 * Speichert die Waehrungsbezeichnung.
	 * 
	 * @param waehrung
	 *          Bezeichnung.
	 * @throws RemoteException
	 */
	public void setWaehrung(String waehrung) throws RemoteException;

	/**
	 * Speichert den Namen der Java-Klasse des zu verwendenden Passports.
	 * 
	 * @param passport
	 *          Passport.
	 * @throws RemoteException
	 */
	public void setPassportClass(String passport) throws RemoteException;

	/**
	 * Speichert die Kundennummer.
	 * 
	 * @param kundennummer
	 *          Kundennummer.
	 * @throws RemoteException
	 */
	public void setKundennummer(String kundennummer) throws RemoteException;

	/**
	 * Liefert den Saldo des Kontos oder <code>0.0</code> wenn er noch nie
	 * abgefragt wurde.
	 * 
	 * @return Saldo des Kontos.
	 * @throws RemoteException
	 */
	public double getSaldo() throws RemoteException;
  
  /**
   * Liefert den Anfangssaldo eines Tages bzw. des 1. Tages nach diesem Datum mit Ums�tzen
   * oder <code>0.0</code> wenn er noch nie abgefragt wurde.
   * @param datum Datum.
   * @return der Saldo.
   * @throws RemoteException 
   */
  public double getAnfangsSaldo(Date datum) throws RemoteException;
  
  /**
   * Liefert den Endsaldo eines Tages bzw. des 1. Tages vor diesem Datum mit Ums�tzen oder
   * <code>0.0</code> wenn er noch nie abgefragt wurde.
   * @param datum Datum.
   * @return der Saldo.
   * @throws RemoteException 
   */
  public double getEndSaldo(Date datum) throws RemoteException;
  

	/**
	 * Speichert den neuen Saldo.
	 * 
	 * @param saldo
	 *          Neuer Saldo.
	 * @throws RemoteException
	 */
	public void setSaldo(double saldo) throws RemoteException;

	/**
	 * Liefert das Datum des aktuellen Saldos oder <code>null</code> wenn er
	 * noch nie abgefragt wurde.
	 * 
	 * @return Datum des Saldos.
	 * @throws RemoteException
	 */
	public Date getSaldoDatum() throws RemoteException;
  
  /**
   * Setzt das Saldo und Datum zur�ck
   * @throws RemoteException
   */
  public void reset() throws RemoteException;

	/**
	 * Liefert eine Liste aller Umsaetze fuer das Konto in umgekehrter
	 * chronologischer Reihenfolge. Also die neuesten zuerst, die aeltesten
	 * zuletzt.
	 * 
	 * @return Umsatzliste.
	 * @throws RemoteException
	 */
	public DBIterator getUmsaetze() throws RemoteException;

	/**
	 * BUGZILLA 81 http://www.willuhn.de/bugzilla/show_bug.cgi?id=81 Liefert die
	 * Anzahl der Umsaetze auf dem Konto.
	 * 
	 * @return Anzahl der Umsaetze auf dem Konto.
	 * @throws RemoteException
	 */
	public int getNumUmsaetze() throws RemoteException;

	/**
	 * Liefert eine Liste aller Umsaetze fuer die letzten x Tage.
   * Die neuesten zuerst, die aeltesten zuletzt.
	 * 
	 * @param days
	 *          Anzahl der Tage.
	 * @return Umsatzliste.
	 * @throws RemoteException
	 */
	public DBIterator getUmsaetze(int days) throws RemoteException;

	/**
	 * Liefert eine Liste aller Umsaetze fuer den vorgegebenen Zeitraum.
   * Die neuesten zuerst, die aeltesten zuletzt.
	 * 
	 * @param start Startdatum
	 * @param end EndeDatum
	 * @return Umsatzliste.
	 * @throws RemoteException
	 */
	public DBIterator getUmsaetze(Date start, Date end) throws RemoteException;

	/**
	 * Liefert eine Liste aller Ueberweisungen, die ueber dieses Konto getaetigt
	 * wurden.
	 * 
	 * @return Ueberweisungsliste.
	 * @throws RemoteException
	 */
	public DBIterator getUeberweisungen() throws RemoteException;

  /**
   * Liefert eine Liste aller Auslandsueberweisungen, die ueber dieses Konto getaetigt
   * wurden.
   * 
   * @return Liste der Auslandsueberweisungen.
   * @throws RemoteException
   */
  public DBIterator getAuslandsUeberweisungen() throws RemoteException;

	/**
	 * Liefert alle Dauerauftraege, die fuer das Konto vorliegen. Dabei werden
	 * auch jene geliefert, die lokal erstellt, jedoch noch nicht zur Bank
	 * hochgeladen wurden.
	 * 
	 * @return Liste der Dauerauftraege.
	 * @throws RemoteException
	 */
	public DBIterator getDauerauftraege() throws RemoteException;

	/**
	 * Liefert alle Lastschriften, die fuer das Konto vorliegen.
	 * 
	 * @return Liste der Lastschriften.
	 * @throws RemoteException
	 */
	public DBIterator getLastschriften() throws RemoteException;

	/**
	 * Liefert alle Sammel-Lastschriften, die fuer das Konto vorliegen.
	 * 
	 * @return Liste der Lastschriften.
	 * @throws RemoteException
	 */
	public DBIterator getSammelLastschriften() throws RemoteException;

	/**
	 * Liefert alle Sammel-Ueberweisungen, die fuer das Konto vorliegen.
	 * 
	 * @return Liste der Sammelueberweisungen.
	 * @throws RemoteException
	 */
	public DBIterator getSammelUeberweisungen() throws RemoteException;

	/**
	 * Liefert die HBCI-Protokollierung des Kontos in Form einer Liste von
	 * Protokoll-Objekten.
	 * 
	 * @return Liste von Protokoll-Objekten.
	 * @throws RemoteException
	 */
	public DBIterator getProtokolle() throws RemoteException;

	/**
	 * Fuegt den uebergebenen Text zum Konto-Protokoll hinzu.
	 * 
	 * @param kommentar
	 *          der hinzuzufuegende Text.
	 * @param protokollTyp
	 *          Typ des Protokoll-Eintrags. Siehe
	 *          <code>de.willuhn.jameica.hbci.rmi.Protokoll</code>.
	 * @throws RemoteException
	 */
	public void addToProtokoll(String kommentar, int protokollTyp) throws RemoteException;

	/**
	 * Liefert die Ausgaben auf dem Konto im angegebenen Zeitraum.
	 * 
	 * @param from
	 *          Start-Datum.
	 * @param to
	 *          End-Datum.
	 * @return Summe der Ausgaben.
	 * @throws RemoteException
	 */
	public double getAusgaben(Date from, Date to) throws RemoteException;

	/**
	 * Liefert die Einnahmen auf dem Konto im angegebenen Zeitraum.
	 * 
	 * @param from
	 *          Start-Datum.
	 * @param to
	 *          End-Datum.
	 * @return Summe der Einnahmen.
	 * @throws RemoteException
	 */
	public double getEinnahmen(Date from, Date to) throws RemoteException;

  /**
   * Speichert einen zusaetzlichen Kommentar fuer das Konto.
   * @param kommentar
   * @throws RemoteException
   */
  public void setKommentar(String kommentar) throws RemoteException;

  /**
   * Liefert einen zusaetzlichen Kommentar fuer das Konto.
   * @return Kommentar.
   * @throws RemoteException
   */
  public String getKommentar() throws RemoteException;
  
  /**
   * Liefert die BIC.
   * @return die BIC.
   * @throws RemoteException
   */
  public String getBic() throws RemoteException;
  
  /**
   * Speichert die BIC.
   * @param bic die BIC.
   * @throws RemoteException
   */
  public void setBic(String bic) throws RemoteException;
  
  /**
   * Liefert die IBAN.
   * @return die IBAN.
   * @throws RemoteException
   */
  public String getIban() throws RemoteException;
  
  /**
   * Speichert die IBAN.
   * @param iban die IBAN.
   * @throws RemoteException
   */
  public void setIban(String iban) throws RemoteException;
}

/*******************************************************************************
 * $Log$
 * Revision 1.44  2010-04-22 16:10:43  willuhn
 * @C Saldo kann bei Offline-Konten zwar nicht manuell bearbeitet werden, dafuer wird er aber beim Zuruecksetzen des Kontos (heisst jetzt "Saldo und Datum zuruecksetzen" statt "Kontoauszugsdatum zuruecksetzen") jetzt ebenfalls geloescht
 *
 * Revision 1.43  2010/04/22 12:42:02  willuhn
 * @N Erste Version des Supports fuer Offline-Konten
 *
 * Revision 1.42  2009/10/20 23:12:58  willuhn
 * @N Support fuer SEPA-Ueberweisungen
 * @N Konten um IBAN und BIC erweitert
 *
 * Revision 1.41  2009/09/15 00:23:34  willuhn
 * @N BUGZILLA 745
 *
 * Revision 1.40  2009/03/17 23:44:15  willuhn
 * @N BUGZILLA 159 - Auslandsueberweisungen. Erste Version
 *
 * Revision 1.39  2009/01/26 23:17:46  willuhn
 * @R Feld "synchronize" aus Konto-Tabelle entfernt. Aufgrund der Synchronize-Optionen pro Konto ist die Information redundant und ergibt sich implizit, wenn fuer ein Konto irgendeine der Synchronisations-Optionen aktiviert ist
 *
 * Revision 1.38  2009/01/04 17:43:29  willuhn
 * @N BUGZILLA 532
 *
 * Revision 1.37  2007/12/11 12:23:26  willuhn
 * @N Bug 355
 *
 * Revision 1.36  2007/06/04 17:37:00  willuhn
 * @D javadoc
 * @C java 1.4 compatibility
 * @N table colorized
 *
 * Revision 1.35  2007/06/04 15:59:03  jost
 * Neue Auswertung: Einnahmen/Ausgaben
 ******************************************************************************/
