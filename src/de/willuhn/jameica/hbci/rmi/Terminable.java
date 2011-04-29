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

import de.willuhn.util.ApplicationException;

/**
 * Interface fuer clientseitig terminierte Transfers.
 * Das sind alle Geld-Transfers, die im Hibiscus-eigenen Terminkalender verwaltet werden.
 */
public interface Terminable
{

	/**
	 * Liefert den Termin der Ueberweisung.
   * @return Termin der Ueberweisung.
   * @throws RemoteException
   */
  public Date getTermin() throws RemoteException;
	
	/**
	 * Speichert den Termin, an dem die Ueberweisung ausgefuehrt werden soll.
   * @param termin Termin der Ueberweisung.
   * @throws RemoteException
   */
  public void setTermin(Date termin) throws RemoteException;

  /**
   * Prueft, ob die Ueberweisung ueberfaellig ist.
   * @return true, wenn sie ueberfaellig ist.
   * @throws RemoteException
   */
  public boolean ueberfaellig() throws RemoteException;
	
  /**
   * Prueft, ob das Objekt ausgefuehrt wurde.
   * @return true, wenn das Objekt bereits ausgefuehrt wurde.
   * @throws RemoteException
   */
  public boolean ausgefuehrt() throws RemoteException;
  
  /**
   * Liefert das Datum, zu dem der Auftrag ausgefuehrt wurde.
   * @return das Datum zu dem der Auftrag ausgefuehrt wurde.
   * @throws RemoteException
   */
  public Date getAusfuehrungsdatum() throws RemoteException;
  
  /**
   * Markiert das Objekt als ausgefuehrt/nicht ausgefuehrt und speichert die Aenderung
   * unmittelbar.
   * @param b ausgefuehrt-Status.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public void setAusgefuehrt(boolean b) throws RemoteException, ApplicationException;

}


/**********************************************************************
 * $Log$
 * Revision 1.5  2011-04-29 15:33:28  willuhn
 * @N Neue Spalte "ausgefuehrt_am", in der das tatsaechliche Ausfuehrungsdatum von Auftraegen vermerkt wird
 *
 * Revision 1.4  2009/02/18 10:48:42  willuhn
 * @N Neuer Schalter "transfer.markexecuted.before", um festlegen zu koennen, wann ein Auftrag als ausgefuehrt gilt (wenn die Quittung von der Bank vorliegt oder wenn der Auftrag erzeugt wurde)
 *
 * Revision 1.3  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.2  2005/03/02 17:59:30  web0
 * @N some refactoring
 *
 * Revision 1.1  2005/02/19 16:49:32  willuhn
 * @B bugs 3,8,10
 *
 * Revision 1.1  2005/02/04 18:27:54  willuhn
 * @C Refactoring zwischen Lastschrift und Ueberweisung
 *
 **********************************************************************/