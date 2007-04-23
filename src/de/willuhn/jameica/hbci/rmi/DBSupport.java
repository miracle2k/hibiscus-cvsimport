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

import java.io.File;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.sql.Connection;

import de.willuhn.util.ApplicationException;

/**
 * Interface fuer eine unterstuetzte Datenbank.
 * Fuer den Suppoert einer neuen Datenbank (z.Bsp. MySQL)
 * in Hibiscus muss dieses Interface implementiert werden.
 */
public interface DBSupport extends Serializable
{
  /**
   * Liefert die JDBC-URL.
   * @return die JDBC-URL.
   */
  public String getJdbcUrl();

  /**
   * Liefert den Klassennamen des JDBC-Treibers.
   * @return der JDBC-Treiber.
   */
  public String getJdbcDriver();

  /**
   * Liefert den Usernamen des Datenbank-Users.
   * @return Username.
   */
  public String getJdbcUsername();

  /**
   * Liefert das Passwort des Datenbank-Users.
   * @return das Passwort.
   */
  public String getJdbcPassword();
  
  /**
   * Checkt die Konsistenz der Datenbank.
   * @param conn die Datenbank-Connection.
   * @throws RemoteException Wenn es beim Pruefen der Datenbank-Konsistenz zu einem Fehler kam.
   * @throws ApplicationException wenn die Datenbank-Konsistenz nicht gewaehrleistet ist.
   */
  public void checkConsistency(Connection conn) throws RemoteException, ApplicationException;
  
  /**
   * Richtet ggf. die Datenbank ein.
   * @throws RemoteException
   */
  public void install() throws RemoteException;
  
  /**
   * Fuehrt ein SQL-Update-Script auf der Datenbank aus.
   * @param conn die Datenbank-Connection.
   * @param sqlScript das SQL-Script.
   * @throws RemoteException
   */
  public void execute(Connection conn, File sqlScript) throws RemoteException;

  /**
   * Liefert den Namen der SQL-Funktion, mit der die Datenbank aus einem DATE-Feld einen UNIX-Timestamp macht.
   * Bei MySQL ist das z.Bsp. "UNIX_TIMESTAMP" und bei McKoi schlicht "TONUMBER".
   * @param content der Feld-Name.
   * @return Name der SQL-Funktion samt Parameter. Also zum Beispiel "TONUMBER(datum)".
   * @throws RemoteException
   */
  public String getSQLTimestamp(String content) throws RemoteException;
  
  /**
   * Legt fest, ob SQL-Insert-Queries mit oder ohne ID erzeugt werden sollen.
   * @return true, wenn die Insert-Queries mit ID erzeugt werden.
   * @throws RemoteException
   * @see de.willuhn.datasource.db.DBServiceImpl#getInsertWithID()
   */
  public boolean getInsertWithID() throws RemoteException;

}


/*********************************************************************
 * $Log$
 * Revision 1.3  2007-04-23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.2  2007/04/19 18:12:21  willuhn
 * @N MySQL-Support (GUI zum Konfigurieren fehlt noch)
 *
 * Revision 1.1  2007/04/18 17:03:06  willuhn
 * @N Erster Code fuer Unterstuetzung von MySQL
 *
 **********************************************************************/