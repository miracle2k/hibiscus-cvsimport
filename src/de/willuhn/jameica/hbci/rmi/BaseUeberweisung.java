/**********************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 * $Locker$
 * $State$
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;

/**
 * Basis-Interface fuer Einzelueberweisungen und Einzellastschriften.
 */
public interface BaseUeberweisung extends HibiscusTransfer, Terminable
{
  /**
   * Liefert den Textschluessel des Auftrags.
   * @return Textschluessel.
   * @throws RemoteException
   */
  public String getTextSchluessel() throws RemoteException;

  /**
   * Speichert den Textschluessel.
   * @param schluessel
   * @throws RemoteException
   */
  public void setTextSchluessel(String schluessel) throws RemoteException;
}


/*********************************************************************
 * $Log$
 * Revision 1.3  2008-08-01 11:05:14  willuhn
 * @N BUGZILLA 587
 *
 **********************************************************************/