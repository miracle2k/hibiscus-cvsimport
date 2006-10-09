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

package de.willuhn.jameica.hbci;

import java.io.Serializable;
import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;

/**
 * Container fuer die Synchronisierungsoptionen eines Kontos.
 */
public class SynchronizeOptions implements Serializable
{
  private String id = null;
  private final static Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();

  /**
   * ct.
   * @param k das Konto.
   * @throws RemoteException
   */
  public SynchronizeOptions(Konto k) throws RemoteException
  {
    super();
    this.id = k == null ? null : k.getID();
  }

  /**
   * Prueft, ob die Kontoauszuege abgerufen werden sollen.
   * @return true, wenn sie synchronisiert werden sollen.
   */
  public boolean getSyncKontoauszuege()
  {
    return settings.getBoolean("sync.konto." + id + ".kontoauszug",true);
  }
  
  /**
   * Prueft, ob offene und ueberfaellige Ueberweisungen abgesendet werden sollen.
   * @return true, wenn sie synchronisiert werden sollen.
   */
  public boolean getSyncUeberweisungen()
  {
    return settings.getBoolean("sync.konto." + id + ".ueb",false);
  }

  /**
   * Prueft, ob offene und ueberfaellige Lastschriften eingereicht werden sollen.
   * @return true, wenn sie synchronisiert werden sollen.
   */
  public boolean getSyncLastschriften()
  {
    return settings.getBoolean("sync.konto." + id + ".last",false);
  }

  /**
   * Prueft, ob die Dauerauftraege synchronisiert werden sollen.
   * @return true, wenn sie synchronisiert werden sollen.
   */
  public boolean getSyncDauerauftraege()
  {
    return settings.getBoolean("sync.konto." + id + ".dauer",false);
  }
  

  /**
   * Legt fest, ob die Kontoauszuege abgerufen werden sollen.
   * @param b true, wenn sie synchronisiert werden sollen.
   */
  public void setSyncKontoauszuege(boolean b)
  {
    settings.setAttribute("sync.konto." + id + ".kontoauszug",b);
  }
  
  /**
   * Legt fest, ob offene und ueberfaellige Ueberweisungen abgesendet werden sollen.
   * @param b true, wenn sie synchronisiert werden sollen.
   */
  public void setSyncUeberweisungen(boolean b)
  {
    settings.setAttribute("sync.konto." + id + ".ueb",b);
  }

  /**
   * Legt fest, ob offene und ueberfaellige Lastschriften eingereicht werden sollen.
   * @param b true, wenn sie synchronisiert werden sollen.
   */
  public void setSyncLastschriften(boolean b)
  {
    settings.setAttribute("sync.konto." + id + ".last",b);
  }

  /**
   * Legt fest, ob die Dauerauftraege synchronisiert werden sollen.
   * @param b true, wenn sie synchronisiert werden sollen.
   */
  public void setSyncDauerauftraege(boolean b)
  {
    settings.setAttribute("sync.konto." + id + ".dauer",b);
  }

}


/*********************************************************************
 * $Log$
 * Revision 1.2  2006-10-09 21:43:26  willuhn
 * @N Zusammenfassung der Geschaeftsvorfaelle "Umsaetze abrufen" und "Saldo abrufen" zu "Kontoauszuege abrufen" bei der Konto-Synchronisation
 *
 * Revision 1.1  2006/04/18 22:38:16  willuhn
 * @N bug 227
 *
 **********************************************************************/