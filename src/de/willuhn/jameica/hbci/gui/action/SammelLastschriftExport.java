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

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.hbci.rmi.SammelLastschrift;

/**
 * Action zum Exportieren von Sammel-Lastschriften.
 */
public class SammelLastschriftExport extends AbstractSammelTransferExport
{

  Class getExportClass()
  {
    return SammelLastschrift.class;
  }

}


/*********************************************************************
 * $Log$
 * Revision 1.1  2006-08-07 14:31:59  willuhn
 * @B misc bugfixing
 * @C Redesign des DTAUS-Imports fuer Sammeltransfers
 *
 **********************************************************************/