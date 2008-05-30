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

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.server.DBSupportH2Impl;
import de.willuhn.jameica.hbci.server.DBSupportMcKoiImpl;
import de.willuhn.jameica.hbci.server.DBSupportMySqlImpl;
import de.willuhn.jameica.hbci.server.HBCIUpdateProvider;
import de.willuhn.logging.Logger;
import de.willuhn.sql.ScriptExecutor;
import de.willuhn.sql.version.Update;
import de.willuhn.sql.version.UpdateProvider;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Datenbank-Update fuer neue Tabelle "property"
 */
public class update0006 implements Update
{
  private Map statements = new HashMap();
  
  /**
   * ct
   */
  public update0006()
  {
    // Update fuer H2
    statements.put(DBSupportH2Impl.class.getName(),
        "CREATE TABLE property (" +
        "    id IDENTITY," +
        "    name varchar(255) NOT NULL," +
        "    content varchar(255) NULL," +
        "    UNIQUE (id)," +
        "    UNIQUE (name)," +
        "    PRIMARY KEY (id)" +
        ");\n");

    // Update fuer McKoi
    statements.put(DBSupportMcKoiImpl.class.getName(),
        "CREATE TABLE property (" +
        "    id NUMERIC default UNIQUEKEY('property')," +
        "    name varchar(255) NOT NULL," +
        "    content varchar(255) NULL," +
        "    UNIQUE (id)," +
        "    UNIQUE (name)," +
        "    PRIMARY KEY (id)" +
        ");\n");
    
    // Update fuer MySQL
    statements.put(DBSupportMySqlImpl.class.getName(),
        "CREATE TABLE property (" +
        "    id int(10) AUTO_INCREMENT," +
        "    name varchar(255) NOT NULL," +
        "    content varchar(255) NULL," +
        "    UNIQUE (id)," +
        "    UNIQUE (name)," +
        "    PRIMARY KEY (id)" +
        ")TYPE=InnoDB;\n");
  }

  /**
   * @see de.willuhn.sql.version.Update#execute(de.willuhn.sql.version.UpdateProvider)
   */
  public void execute(UpdateProvider provider) throws ApplicationException
  {
    HBCIUpdateProvider myProvider = (HBCIUpdateProvider) provider;
    I18N i18n = myProvider.getResources().getI18N();

    // Wenn wir eine Tabelle erstellen wollen, muessen wir wissen, welche
    // SQL-Dialekt wir sprechen
    String driver = HBCIDBService.SETTINGS.getString("database.driver",null);
    String sql = (String) statements.get(driver);
    if (sql == null)
      throw new ApplicationException(i18n.tr("Datenbank {0} wird unterst�tzt",driver));
    
    try
    {
      Logger.info("create sql table for update0006");
      ScriptExecutor.execute(new StringReader(sql),myProvider.getConnection(),myProvider.getProgressMonitor());
      myProvider.getProgressMonitor().log(i18n.tr("Tabelle 'property' erstellt"));
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("unable to execute update",e);
      throw new ApplicationException(i18n.tr("Fehler beim Ausf�hren des Updates"),e);
    }
  }

  /**
   * @see de.willuhn.sql.version.Update#getName()
   */
  public String getName()
  {
    return "Datenbank-Update f�r neue Tabelle \"property\"";
  }

}


/*********************************************************************
 * $Log$
 * Revision 1.2  2008-05-30 12:37:50  willuhn
 * @C Feld "name" unique
 *
 * Revision 1.1  2008/05/30 12:34:04  willuhn
 * @N Neue Tabelle "property" in der allgemein alle moeglichen Parameter datenbank-gestuetzt gespeichert werden koennen.
 *
 * Revision 1.1  2008/02/15 17:39:10  willuhn
 * @N BUGZILLA 188 Basis-API fuer weitere Zeilen Verwendungszweck. GUI fehlt noch
 * @N DB-Update 0005. Speichern des Textschluessels bei Sammelauftragsbuchungen in der Datenbank
 *
 **********************************************************************/