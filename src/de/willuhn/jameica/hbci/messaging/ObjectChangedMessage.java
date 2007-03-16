/**********************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 * $Locker$
 * $State$
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.messaging;

import de.willuhn.datasource.GenericObject;

/**
 * Kann versendet werden, wenn sich ein Objekt geaendert hat.
 */
public class ObjectChangedMessage extends ObjectMessage
{

  /**
   * ct.
   * @param object
   */
  public ObjectChangedMessage(GenericObject object)
  {
    super(object);
  }

}


/*********************************************************************
 * $Log$
 * Revision 1.1  2007-03-16 14:40:02  willuhn
 * @C Redesign ImportMessage
 * @N Aktualisierung der Umsatztabelle nach Kategorie-Zuordnung
 *
 **********************************************************************/