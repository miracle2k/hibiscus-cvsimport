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

package de.willuhn.jameica.hbci.io;

import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Hashtable;

import de.jost_net.OBanToo.Dtaus.CSatz;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.dialogs.KontoAuswahlDialog;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * DTAUS-Importer fuer Ueberweisungen und Lastschriften.
 */
public class DTAUSTransferImporter extends AbstractDTAUSImporter
{
  private Hashtable kontenCache = null;
  
  /**
   * ct.
   */
  public DTAUSTransferImporter()
  {
    super();
  }
  
  
  /**
   * @see de.willuhn.jameica.hbci.io.Importer#doImport(de.willuhn.datasource.GenericObject, de.willuhn.jameica.hbci.io.IOFormat, java.io.InputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doImport(GenericObject context, IOFormat format, InputStream is,
      ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    // Wir merken uns die Konten, die der User schonmal ausgewaehlt
    // hat, um ihn nicht fuer jede Buchung mit immer wieder dem
    // gleichen Konto zu nerven
    this.kontenCache = new Hashtable();

    super.doImport(context,format,is,monitor);
  }

  /**
   * @see de.willuhn.jameica.hbci.io.AbstractDTAUSImporter#fill(de.willuhn.datasource.rmi.DBObject, de.willuhn.datasource.GenericObject, de.jost_net.OBanToo.Dtaus.CSatz)
   */
  void fill(DBObject skel, GenericObject context, CSatz csatz)
    throws RemoteException, ApplicationException
  {
    // Wir verlassen uns hier einfach drauf, dass es sich bei dem
    // Skelet um einen Transfer handelt. Schliesslich haben wir
    // in getSupportedObjectTypes nur solche angegeben
    Transfer t = (Transfer) skel;

    DBService service = Settings.getDBService();

    // Konto suchen
    String kontonummer = Long.toString(csatz.getKontoAuftraggeber());
    String blz         = Long.toString(csatz.getBlzErstbeteiligt());
    DBIterator konten = service.createList(Konto.class);
    konten.addFilter("kontonummer = '" + kontonummer + "'");
    konten.addFilter("blz = '" + blz + "'");

    Konto k = null;
    if (!konten.hasNext())
    {
      // Das Konto existiert nicht im Hibiscus-Datenbestand.

      // Erstmal schauen, ob der User das Konto schonmal ausgewaehlt hat:
      k = (Konto) kontenCache.get(kontonummer + blz);
      if (k == null)
      {
        // Ne, hat er noch nicht.
        // Also muss der User eins auswaehlen.
        KontoAuswahlDialog d = new KontoAuswahlDialog(KontoAuswahlDialog.POSITION_CENTER);
        d.setText(i18n.tr("Konto {0} [BLZ {1}] nicht gefunden\n" +
                          "Bitte w�hlen Sie das Konto aus, auf dem der Auftrag ausgef�hrt werden soll.",
                          new String[]{kontonummer,blz}));

        try
        {
          k = (Konto) d.open();
        }
        catch (OperationCanceledException oce)
        {
          throw new ApplicationException(i18n.tr("Auftrag wird �bersprungen"));
        }
        catch (Exception e)
        {
          Logger.error("unable to choose konto",e);
        }
        
        if (k != null)
          kontenCache.put(kontonummer + blz,k);
      }
    }
    else
    {
      k = (Konto) konten.next();
    }
    t.setKonto(k);
    t.setBetrag(csatz.getBetragInEuro());
    t.setGegenkontoBLZ(Long.toString(csatz.getBlzEndbeguenstigt()));
    t.setGegenkontoName(csatz.getNameEmpfaenger());
    t.setGegenkontoNummer(Long.toString(csatz.getKontonummer()));
    t.setZweck(csatz.getVerwendungszweck(1));
    
    int z = csatz.getAnzahlVerwendungszwecke();
    if (z > 1)
      t.setZweck2(csatz.getVerwendungszweck(2));
  }


  /**
   * @see de.willuhn.jameica.hbci.io.AbstractDTAUSIO#getSupportedObjectTypes()
   */
  Class[] getSupportedObjectTypes()
  {
    return new Class[]
      {
        Ueberweisung.class,
        Lastschrift.class
      };
  }
}


/*********************************************************************
 * $Log$
 * Revision 1.2  2006-06-08 22:29:47  willuhn
 * @N DTAUS-Import fuer Sammel-Lastschriften und Sammel-Ueberweisungen
 * @B Eine Reihe kleinerer Bugfixes in Sammeltransfers
 * @B Bug 197 besser geloest
 *
 * Revision 1.1  2006/06/08 17:40:59  willuhn
 * @N Vorbereitungen fuer DTAUS-Import von Sammellastschriften und Umsaetzen
 *
 * Revision 1.11  2006/06/07 22:42:00  willuhn
 * @N DTAUSExporter
 * @N Abstrakte Basis-Klasse fuer Export und Import
 *
 * Revision 1.10  2006/06/07 17:26:40  willuhn
 * @N DTAUS-Import fuer Lastschriften
 * @B Satusbar-Update in DTAUSImport gefixt
 *
 * Revision 1.9  2006/06/06 22:41:26  willuhn
 * @N Generische Loesch-Action fuer DBObjects (DBObjectDelete)
 * @N Live-Aktualisierung der Tabelle mit den importierten Ueberweisungen
 * @B Korrekte Berechnung des Fortschrittsbalken bei Import
 *
 * Revision 1.8  2006/06/06 21:37:55  willuhn
 * @R FilternEngine entfernt. Wird jetzt ueber das Jameica-Messaging-System abgewickelt
 *
 * Revision 1.7  2006/06/05 09:55:50  jost
 * Anpassung an obantoo 0.5
 *
 * Revision 1.6  2006/05/31 09:04:21  willuhn
 * @C Wir merken uns die vom User bereits ausgewaehlten Konten
 *
 * Revision 1.5  2006/05/29 21:20:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2006/05/29 20:41:21  willuhn
 * @N Import aller logischen Dateien
 *
 * Revision 1.3  2006/05/29 09:16:12  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2006/05/25 13:54:38  willuhn
 * @R removed imports (occurs compile errors in nightly build)
 *
 * Revision 1.1  2006/05/25 13:47:03  willuhn
 * @N Skeleton for DTAUS-Import
 *
 **********************************************************************/