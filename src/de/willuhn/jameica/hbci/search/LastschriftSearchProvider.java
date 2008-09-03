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

package de.willuhn.jameica.hbci.search;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.LastschriftNew;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.search.Result;
import de.willuhn.jameica.search.SearchProvider;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Implementierung einen Search-Provider fuer die Suche in Lastschriften.
 */
public class LastschriftSearchProvider implements SearchProvider
{
  /**
   * @see de.willuhn.jameica.search.SearchProvider#getName()
   */
  public String getName()
  {
    return Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N().tr("Lastschriften");
  }

  /**
   * @see de.willuhn.jameica.search.SearchProvider#search(java.lang.String)
   */
  public List search(String search) throws RemoteException,
      ApplicationException
  {
    if (search == null || search.length() == 0)
      return null;
    
    String text = "%" + search.toLowerCase() + "%";
    DBIterator list = Settings.getDBService().createList(Lastschrift.class);
    list.addFilter("LOWER(zweck) LIKE ? OR " +
                   "LOWER(zweck2) LIKE ? OR " +
                   "LOWER(empfaenger_name) LIKE ? OR " +
                   "empfaenger_konto LIKE ? OR " +
                   "empfaenger_blz LIKE ?",
                   new String[]{text,text,text,text,text});

    ArrayList results = new ArrayList();
    while (list.hasNext())
    {
      results.add(new MyResult((Lastschrift)list.next()));
    }
    return results;
  }
  
  /**
   * Hilfsklasse fuer die formatierte Anzeige der Ergebnisse.
   */
  private class MyResult implements Result
  {
    private Lastschrift l = null;
    
    /**
     * ct.
     * @param l
     */
    private MyResult(Lastschrift l)
    {
      this.l = l;
    }

    /**
     * @see de.willuhn.jameica.search.Result#execute()
     */
    public void execute() throws RemoteException, ApplicationException
    {
      new LastschriftNew().handleAction(this.l);
    }

    /**
     * @see de.willuhn.jameica.search.Result#getName()
     */
    public String getName()
    {
      try
      {
        Konto k = l.getKonto();
        String[] params = new String[] {
            k.getLongName(),
            l.getZweck(),
            HBCI.DECIMALFORMAT.format(l.getBetrag()),
            k.getWaehrung(),
            l.getGegenkontoName()
           };
        I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
        return i18n.tr("{0}: ({1}) {2} {3} von {4}",params);
      }
      catch (RemoteException re)
      {
        Logger.error("unable to determin result name",re);
        return null;
      }
    }
    
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.1  2008-09-03 11:13:51  willuhn
 * @N Mehr Suchprovider
 *
 * Revision 1.1  2008/09/03 00:12:06  willuhn
 * @N Erster Code fuer Searchprovider in Hibiscus
 *
 **********************************************************************/
