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
 * Klassen, die dieses Interface implementieren, besitzen eine
 * Funktion, welche eine fuer dieses Objekt eindeutige Checksumme
 * zurueckliefert.
 */
public interface Checksum
{

	/**
	 * Liefert die Checksumme des Objektes.
   * @return Checksumme.
   * @throws RemoteException
   */
  public long getChecksum() throws RemoteException;

}


/**********************************************************************
 * $Log$
 * Revision 1.2  2005-02-27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 * Revision 1.1  2004/10/17 16:28:46  willuhn
 * @N Die ersten Dauerauftraege abgerufen ;)
 *
 **********************************************************************/