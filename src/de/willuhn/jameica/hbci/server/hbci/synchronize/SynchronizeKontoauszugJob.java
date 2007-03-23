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

package de.willuhn.jameica.hbci.server.hbci.synchronize;

import java.rmi.RemoteException;
import java.util.ArrayList;

import de.willuhn.jameica.hbci.SynchronizeOptions;
import de.willuhn.jameica.hbci.gui.action.KontoNew;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.server.hbci.HBCISaldoJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIUmsatzJob;
import de.willuhn.util.ApplicationException;

/**
 * Ein Synchronize-Job fuer das Abrufen der Umsaetze und des Saldos eines Kontos.
 */
public class SynchronizeKontoauszugJob extends AbstractSynchronizeJob
{
  /**
   * ct.
   * @param konto
   */
  public SynchronizeKontoauszugJob(Konto konto)
  {
    super(konto);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#createHBCIJobs()
   */
  public AbstractHBCIJob[] createHBCIJobs() throws RemoteException, ApplicationException
  {
    // BUGZILLA 346: Das bleibt weiterhin
    // ein Sync-Job, der aber je nach Konfiguration ggf.
    // nur Saldo oder nur Umsaetze abruft
    Konto k = (Konto) getContext();
    SynchronizeOptions o = new SynchronizeOptions(k);
    ArrayList jobs = new ArrayList();
    if (o.getSyncSaldo()) jobs.add(new HBCISaldoJob(k));
    if (o.getSyncKontoauszuege()) jobs.add(new HBCIUmsatzJob(k));

    return (AbstractHBCIJob[]) jobs.toArray(new AbstractHBCIJob[jobs.size()]);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#getName()
   */
  public String getName() throws RemoteException
  {
    Konto k = (Konto) getContext();
    SynchronizeOptions o = new SynchronizeOptions(k);
    
    String s = "Konto {0}: ";
    
    if (o.getSyncKontoauszuege())
      s += "Kontoausz�ge";
    if (o.getSyncSaldo())
    {
      if (o.getSyncKontoauszuege())
        s += "/";
      s += "Salden";
    }
    s += " abrufen";
    return i18n.tr(s,k.getLongName());
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#configure()
   */
  public void configure() throws RemoteException, ApplicationException
  {
    new KontoNew().handleAction(getContext());
  }
}


/*********************************************************************
 * $Log$
 * Revision 1.2  2007-03-23 00:11:51  willuhn
 * @N Bug 346
 *
 * Revision 1.1  2006/10/09 21:43:26  willuhn
 * @N Zusammenfassung der Geschaeftsvorfaelle "Umsaetze abrufen" und "Saldo abrufen" zu "Kontoauszuege abrufen" bei der Konto-Synchronisation
 *
 * Revision 1.1  2006/03/17 00:51:24  willuhn
 * @N bug 209 Neues Synchronisierungs-Subsystem
 *
 **********************************************************************/