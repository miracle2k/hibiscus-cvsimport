/**********************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.csv;

import de.willuhn.jameica.hbci.rmi.Ueberweisung;

/**
 * Implementierung des CSV-Formats fuer den Import von Ueberweisungen.
 */
public class UeberweisungFormat extends AbstractBaseUeberweisungFormat<Ueberweisung>
{
  /**
   * @see de.willuhn.jameica.hbci.io.csv.Format#getType()
   */
  public Class<Ueberweisung> getType()
  {
    return Ueberweisung.class;
  }
}



/**********************************************************************
 * $Log$
 * Revision 1.1  2010-03-16 13:43:56  willuhn
 * @N CSV-Import von Ueberweisungen und Lastschriften
 * @N Versionierbarkeit von serialisierten CSV-Profilen
 *
 **********************************************************************/