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

/**
 * Bildet einen Dauerauftrag in Hibiscus ab.
 */
public interface Dauerauftrag extends Transfer
{

	/**
	 * Liefert das Datum der ersten Zahlung.
   * @return erste Zahlung.
   * @throws RemoteException
   */
  public Date getErsteZahlung() throws RemoteException;
	
	/**
	 * Liefert das Datum der letzten Zahlung oder <code>null</code>, wenn kein Zahlungsende definiert ist.
   * @return Datum der letzten Zahlung oder <code>null</code>.
   * @throws RemoteException
   */
  public Date getLetzteZahlung() throws RemoteException;

	/**
	 * Liefert den Zahlungsturnus fuer diesen Dauerauftrag.
   * @return Zahlungsturnus des Dauerauftrags.
   * @throws RemoteException
   */
  public Turnus getTurnus() throws RemoteException;

	/**
	 * Legt das Datum fuer die erste Zahlung fest.
   * @param datum Datum fuer die erste Zahlung.
   * @throws RemoteException
   */
  public void setErsteZahlung(Date datum) throws RemoteException;

	/**
	 * Legt das Datum fuer die letzte Zahlung fest.
   * @param datum Datum fuer die letzte Zahlung. Kann <code>null</code> sein, wenn kein End-Datum definiert ist.
   * @throws RemoteException
   */
  public void setLetzteZahlung(Date datum) throws RemoteException;

	/**
	 * Legt den Zahlungsturnus fest.
   * @param turnus Zahlungsturnus des Dauerauftrags.
   * @throws RemoteException
   */
  public void setTurnus(Turnus turnus) throws RemoteException;

	/**
	 * Liefert <code>true</code> wenn der Dauerauftrag bei der Bank aktiv ist.
	 * Ob dieser nun von der Bank abgerufen oder lokal erstellt und dann
	 * eingereicht wurde, spielt keine Rolle. Entscheidend ist lediglich, dass
	 * er bei der Bank vorliegt und aktiv ist.
	 * @return true, wenn der Dauerauftrag bei der Bank aktiv ist.
	 * @throws RemoteException
	 */
	public boolean isActive() throws RemoteException;

	/**
	 * Markiert einen Dauerauftrag als "liegt bei der Bank vor" und
	 * ist somit aktiv.
	 * Diese Funktion wird intern verwendet.
   * @throws RemoteException
   */
  public void activate() throws RemoteException;
	
	/**
	 * Markiert einen Dauerauftrag als "liegt nicht bei der Bank vor" und
	 * ist somit inaktiv.
	 * Diese Funktion wird intern verwendet.
   * @throws RemoteException
   */
  public void deactivate() throws RemoteException;
}


/**********************************************************************
 * $Log$
 * Revision 1.4  2004-10-23 17:34:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/10/17 16:28:46  willuhn
 * @N Die ersten Dauerauftraege abgerufen ;)
 *
 * Revision 1.2  2004/07/15 23:39:22  willuhn
 * @N TurnusImpl
 *
 * Revision 1.1  2004/07/11 16:14:29  willuhn
 * @N erster Code fuer Dauerauftraege
 *
 **********************************************************************/