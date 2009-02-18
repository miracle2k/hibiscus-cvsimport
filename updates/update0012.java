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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.rmi.SammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisungBuchung;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.Verwendungszweck;
import de.willuhn.jameica.hbci.server.DBSupportH2Impl;
import de.willuhn.jameica.hbci.server.DBSupportMcKoiImpl;
import de.willuhn.jameica.hbci.server.DBSupportMySqlImpl;
import de.willuhn.jameica.hbci.server.HBCIDBServiceImpl;
import de.willuhn.jameica.hbci.server.HBCIUpdateProvider;
import de.willuhn.logging.Logger;
import de.willuhn.sql.ScriptExecutor;
import de.willuhn.sql.version.Update;
import de.willuhn.sql.version.UpdateProvider;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Verschiebt die erweiterten Verwendungszwecke in die Spalte "zweck3".
 */
public class update0012 implements Update
{
  private Map statements = new HashMap();
  
  /**
   * ct
   */
  public update0012()
  {
    // Update fuer H2
    statements.put(DBSupportH2Impl.class.getName(),
        "ALTER TABLE ueberweisung ADD COLUMN zweck3 VARCHAR(1000) BEFORE termin;\n" +
        "ALTER TABLE umsatz ADD COLUMN zweck3 VARCHAR(1000) BEFORE datum;\n" +
        "ALTER TABLE dauerauftrag ADD COLUMN zweck3 VARCHAR(1000) BEFORE erste_zahlung;\n" +
        "ALTER TABLE lastschrift ADD COLUMN zweck3 VARCHAR(1000) BEFORE termin;\n" +
        "ALTER TABLE slastbuchung ADD COLUMN zweck3 VARCHAR(1000) BEFORE typ;\n" +
        "ALTER TABLE sueberweisungbuchung ADD COLUMN zweck3 VARCHAR(1000) BEFORE typ;\n"
    );

    // Update fuer McKoi
    statements.put(DBSupportMcKoiImpl.class.getName(),
        "ALTER TABLE ueberweisung ADD COLUMN zweck3 VARCHAR(1000);\n" +
        "ALTER TABLE umsatz ADD COLUMN zweck3 VARCHAR(1000);\n" +
        "ALTER TABLE dauerauftrag ADD COLUMN zweck3 VARCHAR(1000);\n" +
        "ALTER TABLE lastschrift ADD COLUMN zweck3 VARCHAR(1000);\n" +
        "ALTER TABLE slastbuchung ADD COLUMN zweck3 VARCHAR(1000);\n" +
        "ALTER TABLE sueberweisungbuchung ADD COLUMN zweck3 VARCHAR(1000);\n"
    );
    
    // Update fuer MySQL
    statements.put(DBSupportMySqlImpl.class.getName(),
        "ALTER TABLE ueberweisung ADD COLUMN zweck3 TEXT AFTER zweck2;\n" +
        "ALTER TABLE umsatz ADD COLUMN zweck3 TEXT AFTER zweck2;\n" +
        "ALTER TABLE dauerauftrag ADD COLUMN zweck3 TEXT AFTER zweck2;\n" +
        "ALTER TABLE lastschrift ADD COLUMN zweck3 TEXT AFTER zweck2;\n" +
        "ALTER TABLE slastbuchung ADD COLUMN zweck3 TEXT AFTER zweck2;\n" +
        "ALTER TABLE sueberweisungbuchung ADD COLUMN zweck3 TEXT AFTER zweck2;\n"
    );
  }

  /**
   * @see de.willuhn.sql.version.Update#execute(de.willuhn.sql.version.UpdateProvider)
   */
  public void execute(UpdateProvider provider) throws ApplicationException
  {
    boolean kontoCheck = Settings.getKontoCheck();
    Settings.setKontoCheck(false);
    
    int maxUsage = HBCIProperties.HBCI_TRANSFER_USAGE_MAXNUM;
    if (maxUsage < 14)
    {
      de.willuhn.jameica.system.Settings s = new de.willuhn.jameica.system.Settings(HBCIProperties.class);
      s.setAttribute("hbci.transfer.usage.maxnum",14);
    }
    
    HBCIUpdateProvider myProvider = (HBCIUpdateProvider) provider;
    I18N i18n = myProvider.getResources().getI18N();

    try
    {
      // Wenn wir eine Tabelle erstellen wollen, muessen wir wissen, welche
      // SQL-Dialekt wir sprechen
      String driver = HBCIDBService.SETTINGS.getString("database.driver",null);
      String sql = (String) statements.get(driver);
      if (sql == null)
        throw new ApplicationException(i18n.tr("Datenbank {0} nicht wird unterst�tzt",driver));

      HBCIDBService service = null;
      try
      {
        //////////////////////////////////////////////////////////////////////////
        // Schritt 1: Neue Spalten anlegen
        Logger.info("update sql tables");
        ScriptExecutor.execute(new StringReader(sql),myProvider.getConnection(),myProvider.getProgressMonitor());
        myProvider.getProgressMonitor().log(i18n.tr("Tabellen aktualisiert"));
        //////////////////////////////////////////////////////////////////////////
        
        //////////////////////////////////////////////////////////////////////////
        // Schritt 2: Kopieren der bisherigen Zeilen
        Logger.info("copying data");
        myProvider.getProgressMonitor().log(i18n.tr("Kopiere Daten"));
        service = new HBCIDBServiceImpl();
        service.start();
        DBIterator lines = service.createList(Verwendungszweck.class);
        lines.setOrder("order by typ,auftrag_id,id");
        
        List l = new ArrayList();
        Integer currentType = null;
        Integer currentId   = null;
        while (lines.hasNext())
        {
          Verwendungszweck z = (Verwendungszweck) lines.next();
          Integer type = (Integer) z.getAttribute("typ");
          Integer id   = (Integer) z.getAttribute("auftrag_id");
          
          // Erstes Objekt oder noch das gleiche
          if (currentId == null || currentType == null ||
              (id.equals(currentId)) && type.equals(currentType))
          {
            if (currentId == null || currentType == null)
            {
              currentId = id;
              currentType = type;
            }
            // Zeile sammeln
            l.add(z.getText());
            continue;
          }

          // Objekt-Wechsel -> flush
          if (l.size() > 0)
          {
            String[] sl = (String[]) l.toArray(new String[l.size()]);
            
            String s = currentId.toString();
            Logger.info("copying " + sl.length + " usage lines for type: " + currentType + ", id: " + s);
            for (int i=0;i<sl.length;++i)
              Logger.debug("  " + sl[i]);
            
            boolean ausgefuehrt = false;
            
            try
            {
              switch (currentType.intValue())
              {
              
                case Transfer.TYP_UEBERWEISUNG:
                  Ueberweisung tu  = (Ueberweisung) service.createObject(Ueberweisung.class,s);
                  tu.setWeitereVerwendungszwecke(sl);
                  ausgefuehrt = tu.ausgefuehrt();
                  if (ausgefuehrt) ((AbstractDBObject)tu).setAttribute("ausgefuehrt",new Integer(0));
                  tu.store();
                  if (ausgefuehrt) tu.setAusgefuehrt(true);
                  break;
                case Transfer.TYP_LASTSCHRIFT:
                  Lastschrift tl = (Lastschrift) service.createObject(Lastschrift.class,s);
                  tl.setWeitereVerwendungszwecke(sl);
                  ausgefuehrt = tl.ausgefuehrt();
                  if (ausgefuehrt) ((AbstractDBObject)tl).setAttribute("ausgefuehrt",new Integer(0));
                  tl.store();
                  if (ausgefuehrt) tl.setAusgefuehrt(true);
                  break;
                case Transfer.TYP_DAUERAUFTRAG:
                  Dauerauftrag td = (Dauerauftrag) service.createObject(Dauerauftrag.class,s);
                  td.setWeitereVerwendungszwecke(sl);
                  td.store();
                  break;
                case Transfer.TYP_UMSATZ:
                  Umsatz tum = (Umsatz) service.createObject(Umsatz.class,s);
                  tum.setWeitereVerwendungszwecke(sl);
                  tum.store();
                  break;
                case Transfer.TYP_SLAST_BUCHUNG:
                  SammelLastBuchung tsb = (SammelLastBuchung) service.createObject(SammelLastBuchung.class,s);
                  tsb.setWeitereVerwendungszwecke(sl);
                  tsb.store();
                  break;
                case Transfer.TYP_SUEB_BUCHUNG:
                  SammelUeberweisungBuchung tub = (SammelUeberweisungBuchung) service.createObject(SammelUeberweisungBuchung.class,s);
                  tub.setWeitereVerwendungszwecke(sl);
                  tub.store();
                  break;
              }
            }
            catch (ObjectNotFoundException onf)
            {
              Logger.warn(onf.getMessage() + ", skipping");
            }
          }
          currentId = null;
          currentType = null;
          l.clear();
        }
        //////////////////////////////////////////////////////////////////////////

        //////////////////////////////////////////////////////////////////////////
        // Schritt 3: Tabelle loeschen
        Logger.info("drop table");
        ScriptExecutor.execute(new StringReader("drop table verwendungszweck;\n"),myProvider.getConnection(),myProvider.getProgressMonitor());
        myProvider.getProgressMonitor().log(i18n.tr("Tabellen aktualisiert"));
        //////////////////////////////////////////////////////////////////////////
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
      finally
      {
        if (service != null)
        {
          try
          {
            service.stop(true);
          }
          catch (Exception e)
          {
            Logger.error("error while closing db service",e);
          }
        }
      }
    }
    catch (ApplicationException ae)
    {
      Logger.error("rollback update",ae);
      String sql = "ALTER TABLE ueberweisung DROP zweck3;\n" +
                   "ALTER TABLE umsatz DROP zweck3;\n" +
                   "ALTER TABLE dauerauftrag DROP zweck3;\n" +
                   "ALTER TABLE lastschrift DROP zweck3;\n" +
                   "ALTER TABLE slastbuchung DROP zweck3;\n" +
                   "ALTER TABLE sueberweisungbuchung DROP zweck3;\n";
      try
      {
        ScriptExecutor.execute(new StringReader(sql),myProvider.getConnection(),myProvider.getProgressMonitor());
      }
      catch (Exception e2)
      {
        Logger.error("rollback failed",e2);
      }
      throw ae;
    }
    finally
    {
      Settings.setKontoCheck(kontoCheck);
    }
  }

  /**
   * @see de.willuhn.sql.version.Update#getName()
   */
  public String getName()
  {
    return "Datenbank-Update f�r erweiterte Verwendungszwecke";
  }

}


/*********************************************************************
 * $Log$
 * Revision 1.3  2009-02-18 10:48:42  willuhn
 * @N Neuer Schalter "transfer.markexecuted.before", um festlegen zu koennen, wann ein Auftrag als ausgefuehrt gilt (wenn die Quittung von der Bank vorliegt oder wenn der Auftrag erzeugt wurde)
 *
 * Revision 1.2  2008/12/15 10:46:04  willuhn
 * @B Verwendungszwecke ueberspringen, wenn die zugehoerigen Objekte nicht mehr gefunden wurden
 * @N rollback des Updates im Fehlerfall durch Entfernen der Spalte "zweck3"
 *
 * Revision 1.1  2008/12/14 23:18:35  willuhn
 * @N BUGZILLA 188 - REFACTORING
 *
 **********************************************************************/