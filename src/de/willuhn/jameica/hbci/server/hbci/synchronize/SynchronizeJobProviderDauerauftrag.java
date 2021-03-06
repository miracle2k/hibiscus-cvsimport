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

package de.willuhn.jameica.hbci.server.hbci.synchronize;

import java.rmi.RemoteException;
import java.util.ArrayList;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.SynchronizeOptions;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SynchronizeJob;
import de.willuhn.jameica.hbci.rmi.SynchronizeJobProvider;

/**
 * Implementierung eines Job-Providers zum Abrufen und Ausfuehren von Dauerauftraegen.
 */
public class SynchronizeJobProviderDauerauftrag implements SynchronizeJobProvider
{

  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJobProvider#getSynchronizeJobs(de.willuhn.jameica.hbci.rmi.Konto)
   */
  public GenericIterator getSynchronizeJobs(Konto k) throws RemoteException
  {
    if (k == null)
      return null;
    
    final SynchronizeOptions options = new SynchronizeOptions(k);

    if (!options.getSyncDauerauftraege())
      return null;
    
    ArrayList jobs = new ArrayList();

    // Senden der neuen Dauerauftraege
    DBIterator list = k.getDauerauftraege();
    while (list.hasNext())
    {
      Dauerauftrag d = (Dauerauftrag) list.next();
      if (d.isActive())
        continue; // Der wurde schon gesendet
      jobs.add(new SynchronizeDauerauftragStoreJob(d));
    }

    // Abrufen der existierenden Dauerauftraege.
    jobs.add(new SynchronizeDauerauftragListJob(k));

    return PseudoIterator.fromArray((SynchronizeJob[])jobs.toArray(new SynchronizeJob[jobs.size()]));
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o)
  {
    // Reihenfolge egal.
    return 0;
  }

}


/*********************************************************************
 * $Log$
 * Revision 1.1  2007-05-16 11:32:30  willuhn
 * @N Redesign der SynchronizeEngine. Ermittelt die HBCI-Jobs jetzt ueber generische "SynchronizeJobProvider". Damit ist die Liste der Sync-Jobs erweiterbar
 *
 **********************************************************************/