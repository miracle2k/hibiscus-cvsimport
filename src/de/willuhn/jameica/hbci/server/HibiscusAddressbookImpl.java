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
import java.rmi.server.UnicastRemoteObject;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.Addressbook;
import de.willuhn.jameica.hbci.rmi.Adresse;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Implementierung des Hibiscus-Adressbuches.
 */
public class HibiscusAddressbookImpl extends UnicastRemoteObject implements Addressbook
{
  private transient I18N i18n = null;

  /**
   * ct.
   * @throws RemoteException
   */
  public HibiscusAddressbookImpl() throws RemoteException
  {
    super();
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Addressbook#contains(de.willuhn.jameica.hbci.rmi.Address)
   */
  public Address contains(Address address) throws RemoteException
  {
    if (address == null)
      return null;
    
    DBIterator list = Settings.getDBService().createList(Adresse.class);
    list.addFilter("kontonummer like ?", new Object[]{"%" + address.getKontonummer()}); // Fuehrende Nullen ignorieren
    list.addFilter("blz=?",              new Object[]{address.getBLZ()});
    list.addFilter("name=?",             new Object[]{address.getName()});
    if (list.hasNext())
      return (Address) list.next();
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Addressbook#findAddresses(java.lang.String)
   */
  public GenericIterator findAddresses(String text) throws RemoteException
  {
    DBIterator list = Settings.getDBService().createList(Adresse.class);
    if (text != null && text.length() > 0)
    {
      // Gross-Kleinschreibung ignorieren wir
      text = "%" + text.toLowerCase() + "%";
      list.addFilter("LOWER(kontonummer) LIKE ? OR " +
                     " LOWER(blz) LIKE ? OR " +
                     " LOWER(name) LIKE ? OR " +
                     " LOWER(kommentar) LIKE ?",new Object[]{text,text,text,text});
    }
    return list;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Addressbook#getName()
   */
  public String getName() throws RemoteException
  {
    return i18n.tr("Hibiscus-Adressbuch");
  }

}


/*********************************************************************
 * $Log$
 * Revision 1.2  2007-04-20 14:55:31  willuhn
 * @C s/findAddress/findAddresses/
 *
 * Revision 1.1  2007/04/20 14:49:05  willuhn
 * @N Support fuer externe Adressbuecher
 * @N Action "EmpfaengerAdd" "aufgebohrt"
 *
 **********************************************************************/