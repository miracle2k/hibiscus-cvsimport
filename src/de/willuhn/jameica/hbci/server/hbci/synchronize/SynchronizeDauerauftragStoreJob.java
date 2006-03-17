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
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIDauerauftragStoreJob;
import de.willuhn.util.ApplicationException;

/**
 * Ein Synchronize-Job fuer das Ausfuehren eines Dauerauftrages.
 */
public class SynchronizeDauerauftragStoreJob extends AbstractSynchronizeJob
{

  /**
   * ct.
   * @param dauer
   */
  public SynchronizeDauerauftragStoreJob(Dauerauftrag dauer)
  {
    super(dauer);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#createHBCIJob()
   */
  public AbstractHBCIJob createHBCIJob() throws RemoteException, ApplicationException
  {
    return new HBCIDauerauftragStoreJob((Dauerauftrag)getContext());
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#getName()
   */
  public String getName() throws RemoteException
  {
    Dauerauftrag dauer = (Dauerauftrag) getContext();
    Konto k = dauer.getKonto();
    return i18n.tr("Konto {0}: Dauerauftrag an {1} absenden",new String[]{k.getLongName(), dauer.getGegenkontoName()});
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