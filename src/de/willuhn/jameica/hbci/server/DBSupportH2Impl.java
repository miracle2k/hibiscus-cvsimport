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

package de.willuhn.jameica.hbci.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Locale;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.plugin.PluginResources;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.Base64;

/**
 * Implementierung des Datenbank-Supports fuer H2-Database (http://www.h2database.com).
 */
public class DBSupportH2Impl extends AbstractDBSupportImpl
{
  /**
   * ct.
   */
  public DBSupportH2Impl()
  {
    // H2-Datenbank verwendet uppercase Identifier
    Logger.info("switching dbservice to uppercase");
    System.setProperty(HBCIDBServiceImpl.class.getName() + ".uppercase","true");    
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getJdbcDriver()
   */
  public String getJdbcDriver()
  {
    return "org.h2.Driver";
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getJdbcPassword()
   */
  public String getJdbcPassword()
  {
    String password = HBCIDBService.SETTINGS.getString("database.driver.h2.encryption.encryptedpassword",null);
    try
    {
      // Existiert noch nicht. Also neu erstellen.
      if (password == null)
      {
        // Wir koennen als Passwort nicht so einfach das Masterpasswort
        // nehmen, weil der User es aendern kann. Wir koennen zwar
        // das Passwort der Datenbank aendern. Allerdings kriegen wir
        // hier nicht mit, wenn sich das Passwort geaendert hat.
        // Daher erzeugen wir ein selbst ein Passwort.
        Logger.info("generating new random password for database");
        byte[] data = new byte[8];
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed((long) (new Date().getTime()));
        random.nextBytes(data);
        
        // Jetzt noch verschluesselt abspeichern
        Logger.info("encrypting password with system certificate");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Application.getSSLFactory().encrypt(new ByteArrayInputStream(data),bos);

        // Verschluesseltes Passwort als Base64 speichern
        HBCIDBService.SETTINGS.setAttribute("database.driver.h2.encryption.encryptedpassword",Base64.encode(bos.toByteArray()));
        
        // Entschluesseltes Passwort als Base64 zurueckliefern, damit keine Binaer-Daten drin sind.
        // Die Datenbank will es doppelt mit Leerzeichen getrennt haben.
        // Das erste ist fuer den User. Das zweite fuer die Verschluesselung.
        String encoded = Base64.encode(data);
        return encoded + " " + encoded;
      }

      Logger.debug("decrypting database password");
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      Application.getSSLFactory().decrypt(new ByteArrayInputStream(Base64.decode(password)),bos);
      
      String encoded = Base64.encode(bos.toByteArray());
      return encoded + " " + encoded;
    }
    catch (Exception e)
    {
      throw new RuntimeException("error while determining database password",e);
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getJdbcUrl()
   */
  public String getJdbcUrl()
  {
    String url = "jdbc:h2:" + Application.getPluginLoader().getPlugin(HBCI.class).getResources().getWorkPath() + "/h2db/hibiscus";

    if (HBCIDBService.SETTINGS.getBoolean("database.driver.h2.encryption",true))
      url += ";CIPHER=" + HBCIDBService.SETTINGS.getString("database.driver.h2.encryption.algorithm","XTEA");
    return url;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getJdbcUsername()
   */
  public String getJdbcUsername()
  {
    return "hibiscus";
  }

  /**
   * @see de.willuhn.jameica.hbci.server.AbstractDBSupportImpl#checkConsistency(java.sql.Connection)
   */
  public void checkConsistency(Connection conn) throws RemoteException, ApplicationException
  {

    ////////////////////////////////////////////////////////////////////////////
    // Damit wir die Updates nicht immer haendisch nachziehen muessen, rufen wir
    // das letzte Update-Script ggf. nochmal auf.
    if (!Application.inClientMode())
    {
      try
      {
        PluginResources res = Application.getPluginLoader().getPlugin(HBCI.class).getResources();
        de.willuhn.jameica.system.Settings s = res.getSettings();
        double size = s.getDouble("sql-update-size",-1);
        
        DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(Locale.ENGLISH); // Punkt als Dezimal-Trenner
        df.setMaximumFractionDigits(1);
        df.setMinimumFractionDigits(1);
        df.setGroupingUsed(false);

        double version    = Application.getPluginLoader().getManifest(HBCI.class).getVersion();
        double oldVersion = version - 0.1d;

        File _f = new File(res.getPath() + File.separator + "sql",
            "update_" + df.format(oldVersion) + "-" + df.format(version) + ".sql");

        File f = new File(_f.getParent(),HBCIDBService.SETTINGS.getString("database.driver.h2.scriptprefix","h2-") + _f.getName());

        if (f.exists())
        {
          long length = f.length();
          if (length != size)
          {
            s.setAttribute("sql-update-size",(double)f.length());
            execute(conn, _f); // wir uebergeben den Original-Namen weil execute den Prefix selbst davorschreibt
          }
          else
            Logger.info("database up to date");
        }
      }
      catch (Exception e2)
      {
        Logger.error("unable to execute sql update script",e2);
      }
    }
    ////////////////////////////////////////////////////////////////////////////
  }

  /**
   * Ueberschrieben, weil SQL-Scripts bei H2 mit einem Prefix versehen werden.
   * Das soll der Admin sicherheitshalber manuell durchfuehren. Wir hinterlassen stattdessen
   * nur einen Hinweistext mit den auszufuehrenden SQL-Scripts.
   * @see de.willuhn.jameica.hbci.server.AbstractDBSupportImpl#execute(java.sql.Connection, java.io.File)
   */
  public void execute(Connection conn, File sqlScript) throws RemoteException
  {
    if (sqlScript == null)
      return; // Ignore

    // Wir schreiben unseren Prefix davor.
    String prefix = HBCIDBService.SETTINGS.getString("database.driver.h2.scriptprefix","h2-");
    sqlScript = new File(sqlScript.getParent(),prefix + sqlScript.getName());
    if (!sqlScript.exists())
    {
      Logger.debug("file " + sqlScript + " does not exist, skipping");
      return;
    }
    super.execute(conn,sqlScript);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getSQLTimestamp(java.lang.String)
   */
  public String getSQLTimestamp(String content) throws RemoteException
  {
    // Nicht noetig
    // return MessageFormat.format("DATEDIFF('MS','1970-01-01 00:00',{0})", new Object[]{content});
    return content;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getInsertWithID()
   */
  public boolean getInsertWithID() throws RemoteException
  {
    return false;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#checkConnection(java.sql.Connection)
   */
  public void checkConnection(Connection conn) throws RemoteException
  {
    // brauchen wir bei nicht, da Embedded
  }
}


/*********************************************************************
 * $Log$
 * Revision 1.7  2007-12-06 17:57:21  willuhn
 * @N Erster Code fuer das neue Versionierungs-System
 *
 * Revision 1.6  2007/10/27 13:31:41  willuhn
 * @C Checksummen-Pruefung temporaer deaktiviert
 *
 * Revision 1.5  2007/10/02 16:08:55  willuhn
 * @C Bugfix mit dem falschen Spaltentyp nochmal ueberarbeitet
 *
 * Revision 1.4  2007/10/01 09:37:42  willuhn
 * @B H2: Felder vom Typ "TEXT" werden von H2 als InputStreamReader geliefert. Felder umsatz.kommentar und protokoll.nachricht auf "VARCHAR(1000)" geaendert und fuer Migration in den Gettern beides beruecksichtigt
 *
 * Revision 1.3  2007/08/23 13:07:38  willuhn
 * @C Uppercase-Verhalten nicht global sondern pro DBService konfigurierbar. Verhindert Fehler, wenn mehrere Plugins installiert sind
 *
 * Revision 1.2  2007/07/18 09:45:18  willuhn
 * @B Neue Version 1.8 in DB-Checks nachgezogen
 *
 * Revision 1.1  2007/06/25 11:21:19  willuhn
 * @N Support fuer H2-Datenbank (http://www.h2database.com/)
 *
 **********************************************************************/