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

import de.willuhn.jameica.hbci.rmi.SammelUeberweisungBuchung;

/**
 * Action, ueber die die Buchungen einer Sammellastschrift exportiert werden koennen.
 */
public class SammelUeberweisungBuchungExport extends AbstractSammelTransferBuchungExport
{
  /**
   * @see de.willuhn.jameica.hbci.gui.action.AbstractSammelTransferBuchungExport#getExportClass()
   */
  Class getExportClass()
  {
    return SammelUeberweisungBuchung.class;
  }

}

/**********************************************************************
 * $Log$
 * Revision 1.1  2005-09-30 00:08:50  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 **********************************************************************/