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
 * Kann versendet werden, wenn ein Objekt geloescht wurde.
 */
public class ObjectDeletedMessage extends ObjectMessage
{

  /**
   * ct.
   * @param object
   */
  public ObjectDeletedMessage(GenericObject object)
  {
    super(object);
  }

}


/*********************************************************************
 * $Log$
 * Revision 1.1  2010-03-05 15:24:53  willuhn
 * @N BUGZILLA 686
 *
 **********************************************************************/