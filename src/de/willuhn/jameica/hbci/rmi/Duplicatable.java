/*****************************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 * $Locker$
 * $State$
 *
****************************************************************************/
package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;

/**
 * Basis-Interface fuer Objekte, die duplizierbar sind.
 * Eine Ueberweisung kann damit zum Beispiel dupliziert werden,
 * um eine neue Ueberweisung mit den gleichen Eigenschaften zu erzeugen.
 * @author willuhn
 */
public interface Duplicatable
{
  /**
   * Dupliziert das Objekt.
   * @return neues Objekt mit den gleichen Eigenschaften.
   * @throws RemoteException
   */
  public Duplicatable duplicate() throws RemoteException;

}

/*****************************************************************************
 * $Log$
 * Revision 1.1  2005-03-02 17:59:30  web0
 * @N some refactoring
 *
*****************************************************************************/