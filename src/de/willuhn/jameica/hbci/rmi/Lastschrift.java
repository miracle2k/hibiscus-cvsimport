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

/**
 * Ne Lastschrift ist ja auch nur ne Ueberweisung. Nur andersrum ;).
 */
public interface Lastschrift extends Transfer, Terminable, Checksum
{

	/**
	 * Liefert den Typ der Lastschrift.
	 * Moegliche Werte: 04 oder 05.
   * @return Typ der Lastschrift.
   * @throws RemoteException
   */
  public String getTyp() throws RemoteException;
	
	/**
	 * Legt den Typ der Lastschrift fest.
	 * Moegliche Werte sind 04 oder 05.
   * @param typ Typ.
   * @throws RemoteException
   */
  public void setTyp(String typ) throws RemoteException;
}


/**********************************************************************
 * $Log$
 * Revision 1.3  2005-02-19 16:49:32  willuhn
 * @B bugs 3,8,10
 *
 * Revision 1.2  2005/02/04 18:27:54  willuhn
 * @C Refactoring zwischen Lastschrift und Ueberweisung
 *
 * Revision 1.1  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 **********************************************************************/