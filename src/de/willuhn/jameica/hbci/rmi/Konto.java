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

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;

/**
 * Bildet eine Bankverbindung in HBCI ab.
 */
public interface Konto extends DBObject {

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
	 * Liefert den fuer dieses Konto zu verwendende Passport.
   * @return Passport.
   * @throws RemoteException
   */
  public Passport getPassport() throws RemoteException;
	
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
	 * Speichert den zu verwendenden Passport.
   * @param passport Passport.
   * @throws RemoteException
   */
  public void setPassport(Passport passport) throws RemoteException;

	/**
	 * Liefert einen Iterator mit Objekten des Typs <code>PassportParam</code>, welche
	 * zur Initialisierung des Passports benoetigt werden.
   * @return DBIterator mit PassportParams.
   * @throws RemoteException
   */
  public DBIterator getPassportParams() throws RemoteException;

}


/**********************************************************************
 * $Log$
 * Revision 1.1  2004-02-11 00:11:20  willuhn
 * *** empty log message ***
 *
 **********************************************************************/