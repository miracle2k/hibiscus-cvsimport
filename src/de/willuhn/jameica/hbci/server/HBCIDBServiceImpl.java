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

package de.willuhn.jameica.hbci.server;

import java.io.File;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Locale;

import de.willuhn.datasource.db.DBServiceImpl;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.DBSupport;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.plugin.PluginResources;
import de.willuhn.jameica.plugin.Version;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.sql.version.UpdateProvider;
import de.willuhn.sql.version.Updater;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.MultipleClassLoader;
import de.willuhn.util.ProgressMonitor;

/**
 * @author willuhn
 */
public class HBCIDBServiceImpl extends DBServiceImpl implements HBCIDBService
{
  private DBSupport driver = null;
  
  /**
   * @throws RemoteException
   */
  public HBCIDBServiceImpl() throws RemoteException
  {
    this(SETTINGS.getString("database.driver",DBSupportH2Impl.class.getName()));
  }
  
  /**
   * Konstruktor mit expliziter Angabe des Treibers.
   * @param driverClass der zu verwendende Treiber.
   * @throws RemoteException
   */
  public HBCIDBServiceImpl(String driverClass) throws RemoteException
  {
    super();
    MultipleClassLoader cl = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getClassLoader();
    this.setClassloader(cl);
    this.setClassFinder(cl.getClassFinder());
    if (driverClass == null)
      throw new RemoteException("no driver given");
    Logger.info("loading database driver: " + driverClass);
    try
    {
      Class c = cl.load(driverClass);
      this.driver = (DBSupport) c.newInstance();
    }
    catch (Throwable t)
    {
      throw new RemoteException("unable to load database driver " + driverClass,t);
    }
  }

  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName() throws RemoteException
  {
		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    return i18n.tr("Datenbank-Service f�r Hibiscus");
  }

  /**
   * @see de.willuhn.datasource.db.DBServiceImpl#getAutoCommit()
   */
  protected boolean getAutoCommit() throws RemoteException
  {
    return SETTINGS.getBoolean("autocommit",super.getAutoCommit());
  }

  /**
   * @see de.willuhn.datasource.db.DBServiceImpl#getJdbcDriver()
   */
  protected String getJdbcDriver() throws RemoteException
  {
    return this.driver.getJdbcDriver();
  }

  /**
   * @see de.willuhn.datasource.db.DBServiceImpl#getJdbcPassword()
   */
  protected String getJdbcPassword() throws RemoteException
  {
    return this.driver.getJdbcPassword();
  }

  /**
   * @see de.willuhn.datasource.db.DBServiceImpl#getJdbcUrl()
   */
  protected String getJdbcUrl() throws RemoteException
  {
    return this.driver.getJdbcUrl();
  }

  /**
   * @see de.willuhn.datasource.db.DBServiceImpl#getJdbcUsername()
   */
  protected String getJdbcUsername() throws RemoteException
  {
    return this.driver.getJdbcUsername();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HBCIDBService#checkConsistency()
   */
  public void checkConsistency() throws RemoteException, ApplicationException
  {
    Logger.info("init update provider");
    UpdateProvider provider = new HBCIUpdateProvider(getConnection(),VersionUtil.getVersion(this,"db"));
    Updater updater = new Updater(provider);
    updater.execute();
    Logger.info("updates finished");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HBCIDBService#install()
   */
  public void install() throws RemoteException
  {
    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    ProgressMonitor monitor = Application.getCallback().getStartupMonitor();
    monitor.setStatusText(i18n.tr("Installiere Hibiscus"));
    this.driver.install();
    
    PluginResources res = Application.getPluginLoader().getPlugin(HBCI.class).getResources();
    File file = new File(res.getPath() + File.separator + "sql","create.sql");
    this.driver.execute(getConnection(),file);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HBCIDBService#update(de.willuhn.jameica.plugin.Version, de.willuhn.jameica.plugin.Version)
   */
  public void update(Version oldVersion, Version newVersion) throws RemoteException
  {
    Logger.info("starting update process for hibiscus");

    DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(Locale.ENGLISH); // Punkt als Dezimal-Trenner
    df.setMaximumFractionDigits(1);
    df.setMinimumFractionDigits(1);
    df.setGroupingUsed(false);

    PluginResources res = Application.getPluginLoader().getPlugin(HBCI.class).getResources();

    double target = Double.parseDouble(newVersion.getMajor() + "." + newVersion.getMinor());
    double old    = Double.parseDouble(oldVersion.getMajor() + "." + oldVersion.getMinor());
    double newV   = target;

    try
    {
      // Wir wiederholen die Updates solange, bis wir bei der aktuellen
      // Versionsnummer angekommen sind.
      while (old < target)
      {
        newV = old + 0.1d;

        File f = new File(res.getPath() + File.separator + "sql",
            "update_" + df.format(old) + "-" + df.format(newV) + ".sql");

        I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
        ProgressMonitor monitor = Application.getCallback().getStartupMonitor();
        monitor.setStatusText(i18n.tr("F�hre Hibiscus-Update durch: von {0} zu {1}", new String[]{df.format(old),df.format(newV)}));

        this.driver.execute(getConnection(),f);
        
        // OK, naechster Durchlauf
        old = newV;
      }
      
      Logger.info("Update completed");
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to perform database update from " + oldVersion + " to " + newVersion,e);
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HBCIDBService#getSQLTimestamp(java.lang.String)
   */
  public String getSQLTimestamp(String content) throws RemoteException
  {
    return this.driver.getSQLTimestamp(content);
  }

  /**
   * @see de.willuhn.datasource.db.DBServiceImpl#getInsertWithID()
   */
  protected boolean getInsertWithID() throws RemoteException
  {
    return this.driver.getInsertWithID();
  }

  /**
   * @see de.willuhn.datasource.db.DBServiceImpl#checkConnection(java.sql.Connection)
   */
  protected void checkConnection(Connection conn) throws SQLException
  {
    try
    {
      this.driver.checkConnection(conn);
    }
    catch (RemoteException re)
    {
      throw new SQLException(re.getMessage());
    }
    super.checkConnection(conn);
  }
  
  /**
   * @see de.willuhn.datasource.db.DBServiceImpl#getTransactionIsolationLevel()
   */
  protected int getTransactionIsolationLevel() throws RemoteException
  {
    // BUGZILLA 447
    return this.driver.getTransactionIsolationLevel();
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.HBCIDBService#getDriver()
   */
  public DBSupport getDriver() throws RemoteException
  {
    return this.driver;
  }
}


/*********************************************************************
 * $Log$
 * Revision 1.28  2008-12-30 15:21:40  willuhn
 * @N Umstellung auf neue Versionierung
 *
 * Revision 1.27  2008/05/30 14:23:48  willuhn
 * @N Vollautomatisches und versioniertes Speichern der BPD und UPD in der neuen Property-Tabelle
 *
 * Revision 1.26  2008/05/06 10:10:56  willuhn
 * @N Diagnose-Dialog, mit dem man die JDBC-Verbindungsdaten (u.a. auch das JDBC-Passwort) ausgeben kann
 *
 * Revision 1.25  2007/12/11 15:23:53  willuhn
 * @N Class-Update fuer neue Tabellen "op" und "op_buchung"
 *
 * Revision 1.24  2007/12/11 00:33:35  willuhn
 * @N Scharfschaltung des neuen Update-Prozesses
 *
 * Revision 1.23  2007/10/04 23:39:49  willuhn
 * @N Datenmigration McKoi->H2 (in progress)
 *
 * Revision 1.22  2007/08/23 12:43:07  willuhn
 * @C BUGZILLA 275 - Umstellung der Default-Datenbank auf H2
 *
 * Revision 1.21  2007/07/28 15:51:26  willuhn
 * @B Bug 447
 *
 * Revision 1.20  2007/07/26 23:55:21  willuhn
 * @B Changed transaction isolation level
 *
 * Revision 1.19  2007/05/07 09:27:25  willuhn
 * @N Automatisches Neuerstellen der JDBC-Connection bei MySQL
 *
 * Revision 1.18  2007/04/24 19:31:25  willuhn
 * @N Neuer Konstruktor
 *
 * Revision 1.17  2007/04/23 18:07:15  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.16  2007/04/20 14:49:05  willuhn
 * @N Support fuer externe Adressbuecher
 * @N Action "EmpfaengerAdd" "aufgebohrt"
 *
 * Revision 1.15  2007/04/19 18:12:21  willuhn
 * @N MySQL-Support (GUI zum Konfigurieren fehlt noch)
 *
 * Revision 1.14  2007/04/18 17:03:06  willuhn
 * @N Erster Code fuer Unterstuetzung von MySQL
 *
 * Revision 1.13  2006/12/27 11:52:36  willuhn
 * @C ResultsetExtractor moved into datasource
 *
 * Revision 1.12  2006/11/20 23:00:57  willuhn
 * @N ability to configure autocommit behaviour
 *
 * Revision 1.11  2006/04/25 23:25:12  willuhn
 * @N bug 81
 *
 * Revision 1.10  2005/02/01 17:15:37  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2005/01/30 20:45:35  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2005/01/05 15:17:50  willuhn
 * @N Neues Service-System in Jameica
 *
 * Revision 1.7  2004/11/17 19:02:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/11/03 18:42:55  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/09/15 22:31:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/08/31 18:13:51  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.2  2004/07/23 16:23:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
 **********************************************************************/