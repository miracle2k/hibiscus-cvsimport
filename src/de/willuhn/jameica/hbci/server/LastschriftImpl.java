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
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.rmi.Transfer;

/**
 * Bildet eine Lastschrift ab. Ist fast das gleiche wie eine
 * Ueberweisung. Nur dass wir in eine andere Tabelle speichern
 * und der Empfaenger hier nicht das Geld sondern die Forderung
 * erhaelt.
 */
public class LastschriftImpl extends AbstractBaseUeberweisungImpl implements Lastschrift
{

  /**
   * @throws RemoteException
   */
  public LastschriftImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName()
  {
    return "lastschrift";
  }

	/**
	 * @see de.willuhn.jameica.hbci.rmi.Transfer#duplicate()
	 */
	public Transfer duplicate() throws RemoteException {
		Lastschrift u = (Lastschrift) Settings.getDBService().createObject(Lastschrift.class,null);
		u.setBetrag(getBetrag());
		u.setEmpfaengerBLZ(getEmpfaengerBLZ());
		u.setEmpfaengerKonto(getEmpfaengerKonto());
		u.setEmpfaengerName(getEmpfaengerName());
		u.setKonto(getKonto());
		u.setTermin(getTermin());
		u.setZweck(getZweck());
		u.setZweck2(getZweck2());
		return u;
	}

}


/**********************************************************************
 * $Log$
 * Revision 1.2  2005-02-04 18:27:54  willuhn
 * @C Refactoring zwischen Lastschrift und Ueberweisung
 *
 * Revision 1.1  2005/01/19 00:16:05  willuhn
 * @N Lastschriften
 *
 **********************************************************************/