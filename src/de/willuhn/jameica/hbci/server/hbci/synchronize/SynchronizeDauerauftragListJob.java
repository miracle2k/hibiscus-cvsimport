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

import de.willuhn.jameica.hbci.gui.action.DauerauftragNew;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIDauerauftragListJob;
import de.willuhn.util.ApplicationException;

/**
 * Ein Synchronize-Job fuer das Abrufen der Dauerauftraege eines Kontos.
 */
public class SynchronizeDauerauftragListJob extends AbstractSynchronizeJob
{

  /**
   * ct.
   * @param konto
   */
  public SynchronizeDauerauftragListJob(Konto konto)
  {
    super(konto);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#createHBCIJob()
   */
  public AbstractHBCIJob createHBCIJob() throws RemoteException, ApplicationException
  {
    return new HBCIDauerauftragListJob((Konto)getContext());
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#getName()
   */
  public String getName() throws RemoteException
  {
    Konto k = (Konto) getContext();
    return i18n.tr("Konto {0}: Daeuerauftr�ge abrufen",k.getLongName());
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#configure()
   */
  public void configure() throws RemoteException, ApplicationException
  {
    new DauerauftragNew().handleAction(getContext());
  }
}


/*********************************************************************
 * $Log$
 * Revision 1.1  2006-03-17 00:51:24  willuhn
 * @N bug 209 Neues Synchronisierungs-Subsystem
 *
 **********************************************************************/