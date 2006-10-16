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

import de.willuhn.jameica.hbci.rmi.Ueberweisung;

/**
 * Exporter fuer Ueberweisungen.
 */
public class UeberweisungExport extends AbstractTransferExport
{

  /**
   * @see de.willuhn.jameica.hbci.gui.action.AbstractTransferExport#getExportClass()
   */
  Class getExportClass()
  {
    return Ueberweisung.class;
  }

}


/*********************************************************************
 * $Log$
 * Revision 1.1  2006-10-16 14:46:30  willuhn
 * @N CSV-Export von Ueberweisungen und Lastschriften
 *
 **********************************************************************/