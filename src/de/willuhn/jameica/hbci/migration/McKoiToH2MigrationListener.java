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

package de.willuhn.jameica.hbci.migration;

import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.SystemMessage;


/**
 * Wird benachrichtigt, wenn die Anwendung gestartet wurde
 * und zeigt ggf. ein Dialog fuer die Datenmigration von
 * Mckoi zu H2 an.
 */
public class McKoiToH2MigrationListener implements MessageConsumer
{

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    return false;
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{SystemMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
//    if (message == null || !(message instanceof SystemMessage))
//      return;
//    
//    if (((SystemMessage) message).getStatusCode() != SystemMessage.SYSTEM_STARTED)
//      return;
//    
//    // Checken, ob Migration schon lief
//    if (DatabaseMigrationTask.SETTINGS.getString("migration.mckoi-to-h2",null) != null)
//      return; // lief bereits
//    
//    // Checken, ob ueberhaupt die McKoi-Datenbank genutzt wird
//    String driver = HBCIDBService.SETTINGS.getString("database.driver",null);
//    if (driver == null || !driver.equals(DBSupportMcKoiImpl.class.getName()))
//      return;
//    
//    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
//    
//    String text = i18n.tr("Das Datenbank-Format von Hibiscus wurde umgestellt.\nM�chten Sie jetzt die �bernahme der Daten in das neue Format durchf�hren?");
//    if (!Application.getCallback().askUser(text))
//      return;
//    
//    Logger.warn("starting database migration from mckoi to h2");
//    Application.getController().start(new McKoiToH2MigrationTask());
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.1  2007-10-04 23:39:49  willuhn
 * @N Datenmigration McKoi->H2 (in progress)
 *
 **********************************************************************/