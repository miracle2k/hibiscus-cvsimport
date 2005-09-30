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
package de.willuhn.jameica.hbci.server.hbci;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
import de.willuhn.util.ApplicationException;

/**
 * Job fuer "Sammel-Ueberweisung".
 */
public class HBCISammelUeberweisungJob extends AbstractHBCISammelTransferJob
{

  /**
	 * ct.
   * @param ueberweisung die auszufuehrende Sammel-Ueberweisung.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public HBCISammelUeberweisungJob(SammelUeberweisung ueberweisung) throws ApplicationException, RemoteException
	{
    super(ueberweisung);
	}

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getIdentifier()
   */
  String getIdentifier() {
    return "MultiUeb";
  }
}


/**********************************************************************
 * $Log$
 * Revision 1.1  2005-09-30 00:08:51  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 **********************************************************************/