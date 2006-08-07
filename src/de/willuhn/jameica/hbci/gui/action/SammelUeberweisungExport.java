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

import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;

/**
 * Action zum Exportieren von Sammel-Ueberweisungen.
 */
public class SammelUeberweisungExport extends AbstractSammelTransferExport
{

  Class getExportClass()
  {
    return SammelUeberweisung.class;
  }

}


/*********************************************************************
 * $Log$
 * Revision 1.1  2006-08-07 14:31:59  willuhn
 * @B misc bugfixing
 * @C Redesign des DTAUS-Imports fuer Sammeltransfers
 *
 **********************************************************************/