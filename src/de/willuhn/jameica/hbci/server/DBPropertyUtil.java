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

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.DBProperty;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Hilfsklasse zum Laden und Speichern der Properties.
 */
public class DBPropertyUtil
{
  /**
   * Speichert ein Property.
   * @param name Name des Property.
   * @param value Wert des Property.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public static void set(String name, String value) throws RemoteException, ApplicationException
  {
    DBProperty prop = find(name);
    if (prop == null)
    {
      Logger.warn("parameter name " + name + " invalid");
      return;
    }
    
    prop.setValue(value);
    prop.store();
  }
  
  /**
   * Liefert den Wert des Parameters.
   * @param name Name des Parameters.
   * @param defaultValue Default-Wert, wenn der Parameter nicht existiert oder keinen Wert hat.
   * @return Wert des Parameters.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public static String get(String name, String defaultValue) throws RemoteException, ApplicationException
  {
    DBProperty prop = find(name);
    if (prop == null)
      return defaultValue;
    String value = prop.getValue();
    return value != null ? value : defaultValue;
  }
  
  /**
   * Liefert den Parameter mit dem genannten Namen.
   * Wenn er nicht existiert, wird er automatisch angelegt.
   * @param name Name des Parameters. Darf nicht <code>null</code> sein.
   * @return der Parameter oder <code>null</code>, wenn kein Name angegeben wurde.
   * @throws RemoteException
   * @throws ApplicationException
   */
  private static DBProperty find(String name) throws RemoteException, ApplicationException
  {
    if (name == null)
      return null;
    
    // Mal schauen, ob wir das Property schon haben
    DBService service = Settings.getDBService();
    DBIterator i = service.createList(DBProperty.class);
    i.addFilter("name = ?",new Object[]{name});
    if (i.hasNext())
      return (DBProperty) i.next();

    // Ne, dann neu anlegen
    DBProperty prop = (DBProperty) service.createObject(DBProperty.class,null);
    prop.setName(name);
    return prop;
  }
}


/*********************************************************************
 * $Log$
 * Revision 1.1  2008-05-30 14:23:48  willuhn
 * @N Vollautomatisches und versioniertes Speichern der BPD und UPD in der neuen Property-Tabelle
 *
 **********************************************************************/