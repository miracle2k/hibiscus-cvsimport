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

import de.willuhn.datasource.rmi.DBObject;

/**
 * Dieses Objekt repraesentiert eine Liste der unterstuetzten Passport-Typen.
 */
public interface PassportType extends DBObject {

	/**
	 * Liefert den Namen des Passports.
   * @return Name des Passports.
   * @throws RemoteException
   */
  public String getName() throws RemoteException;

	/**
	 * Liefert den Namen der Java-Klasse, die diesen Passport implementiert.
   * @return Name der Klasse.
   * @throws RemoteException
   */
  public String getImplementor() throws RemoteException;
}


/**********************************************************************
 * $Log$
 * Revision 1.1  2004-02-27 01:10:18  willuhn
 * @N passport config refactored
 *
 **********************************************************************/