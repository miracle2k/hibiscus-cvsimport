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

import java.util.List;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.io.ser.DefaultSerializer;
import de.willuhn.jameica.hbci.io.ser.Serializer;
import de.willuhn.jameica.hbci.rmi.AddressbookService;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung des CSV-Formats fuer den Adressbuch-Import.
 */
public class AddressFormat implements Format<HibiscusAddress>
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private ImportListener listener        = null;
  private Profile profile                = null;
  
  /**
   * @see de.willuhn.jameica.hbci.io.csv.Format#getDefaultProfile()
   */
  public synchronized Profile getDefaultProfile()
  {
    if (this.profile == null)
    {
      this.profile = new Profile();
      this.profile.setSkipLines(1);
      
      Serializer s = new DefaultSerializer();
      List<Column> list = this.profile.getColumns();
      int i = 0;
      list.add(new Column("name",i18n.tr("Name des Kontoinhabers"),i++,s));
      list.add(new Column("kontonummer",i18n.tr("Kontonummer"),i++,s));
      list.add(new Column("blz",i18n.tr("Bankleitzahl"),i++,s));
      list.add(new Column("iban",i18n.tr("IBAN"),i++,s));
      list.add(new Column("bic",i18n.tr("BIC"),i++,s));
      list.add(new Column("kommentar",i18n.tr("Kommentar"),i++,s));
    }
    return this.profile;
  }

  /**
   * @see de.willuhn.jameica.hbci.io.csv.Format#getType()
   */
  public Class<HibiscusAddress> getType()
  {
    return HibiscusAddress.class;
  }

  /**
   * @see de.willuhn.jameica.hbci.io.csv.Format#getImportListener()
   */
  public ImportListener getImportListener()
  {
    if (this.listener == null)
    {
      this.listener = new ImportListener(){
        
        private AddressbookService addressbook = null;

        /**
         * @see de.willuhn.jameica.hbci.io.csv.ImportListener#beforeStore(de.willuhn.jameica.hbci.io.csv.ImportEvent)
         */
        public void beforeStore(ImportEvent event)
        {
          try
          {
            Object data = event.data;
            if (data == null || !(data instanceof HibiscusAddress))
              return;
            
            if (this.addressbook == null)
              this.addressbook = (AddressbookService) Application.getServiceFactory().lookup(HBCI.class,"addressbook");

            HibiscusAddress t = (HibiscusAddress) data; 
            if (this.addressbook.contains(t) != null)
            {
              if (event.monitor != null)
                event.monitor.log("  " + i18n.tr("Adresse (Kto {0}, BLZ {1}) existiert bereits, �berspringe Zeile", new String[]{t.getKontonummer(),t.getBlz()}));
              event.doit = false;
            }
          }
          catch (Exception e)
          {
            Logger.error("error while checking address",e);
          }
        }
        
      };
    }
    return this.listener;
  }
}



/**********************************************************************
 * $Log$
 * Revision 1.1  2010-03-16 00:44:18  willuhn
 * @N Komplettes Redesign des CSV-Imports.
 *   - Kann nun erheblich einfacher auch fuer andere Datentypen (z.Bsp.Ueberweisungen) verwendet werden
 *   - Fehlertoleranter
 *   - Mehrfachzuordnung von Spalten (z.Bsp. bei erweitertem Verwendungszweck) moeglich
 *   - modulare Deserialisierung der Werte
 *   - CSV-Exports von Hibiscus koennen nun 1:1 auch wieder importiert werden (Import-Preset identisch mit Export-Format)
 *   - Import-Preset wird nun im XML-Format nach ~/.jameica/hibiscus/csv serialisiert. Damit wird es kuenftig moeglich sein,
 *     CSV-Import-Profile vorzukonfigurieren und anschliessend zu exportieren, um sie mit anderen Usern teilen zu koennen
 *
 **********************************************************************/