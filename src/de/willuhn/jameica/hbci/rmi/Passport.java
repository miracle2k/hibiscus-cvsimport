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
 * Bildet einen Passport ab.
 * Ein Passport ist ein HBCI-Sicherheitsmedium - z.Bsp. Chipkarte (DDV).
 */
public interface Passport extends DBObject {

	public final static int TYPE_DDV = 1;

	/**
	 * Liefert den Namen des Passports.
   * @return Name des Passports.
   * @throws RemoteException
   */
  public String getName() throws RemoteException;
	
	/**
	 * Liefert den Typ des Passports.
	 * Zur Codierung des Int-Wertes existieren in <code>Passport</code>
	 * Konstanten mit den Namen TYPE_*.
	 * @return Typ des Passports.
	 * @throws RemoteException
	 */
	public int getType() throws RemoteException;
	
	/**
	 * Speichert den Namen des Passports.
   * @param name Name des Passports.
   * @throws RemoteException
   */
  public void setName(String name) throws RemoteException;
  
  /**
   * Oeffnet den Passport.
   * @throws RemoteException muss geworfen werden, wenn die Initialisierung fehlschlaegt.
   * Die Exeption sollte einen sinnvollen Fehlertext enthalten. 
   */
  public void open() throws RemoteException;

	/**
	 * Schliesst den Passport.
   * @throws RemoteException
   */
  public void close() throws RemoteException;
  
  /**
   * Prueft, ob der Passport offen ist.
   * @return true, wenn er offen ist.
   * @throws RemoteException
   */
  public boolean isOpen() throws RemoteException;
  
}


/**********************************************************************
 * $Log$
 * Revision 1.3  2004-02-12 23:46:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/12 00:38:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 **********************************************************************/