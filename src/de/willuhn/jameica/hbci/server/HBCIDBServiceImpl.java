/**********************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 * $Locker$
 * $State$
 *
 * Copyright (c) by  bbv AG
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;

import de.willuhn.datasource.db.DBServiceImpl;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.plugin.PluginLoader;

/**
 * @author willuhn
 */
public class HBCIDBServiceImpl extends DBServiceImpl implements DBService
{

  /**
   * @throws RemoteException
   */
  public HBCIDBServiceImpl() throws RemoteException
  {
    super("com.mckoi.JDBCDriver",
          ":jdbc:mckoi:local://" + PluginLoader.getPlugin(HBCI.class).getResources().getWorkPath() + "/db/db.conf?user=hibiscus&password=hibiscus");
  }

}


/*********************************************************************
 * $Log$
 * Revision 1.1  2004-07-23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
 **********************************************************************/