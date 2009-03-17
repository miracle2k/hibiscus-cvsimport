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

import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;

/**
 * Exporter fuer Auslandsueberweisungen.
 */
public class AuslandsUeberweisungExport extends AbstractTransferExport
{

  /**
   * @see de.willuhn.jameica.hbci.gui.action.AbstractTransferExport#getExportClass()
   */
  Class getExportClass()
  {
    return AuslandsUeberweisung.class;
  }

}


/*********************************************************************
 * $Log$
 * Revision 1.1  2009-03-17 23:44:14  willuhn
 * @N BUGZILLA 159 - Auslandsueberweisungen. Erste Version
 *
 **********************************************************************/