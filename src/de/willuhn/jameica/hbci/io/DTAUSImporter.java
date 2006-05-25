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

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.dialogs.KontoAuswahlDialog;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Import-Format fuer DTAUS-Dateien.
 */
public class DTAUSImporter implements Importer
{
  private I18N i18n = null;

  /**
   * ct.
   */
  public DTAUSImporter()
  {
    super();
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }
  
  
  /**
   * @see de.willuhn.jameica.hbci.io.Importer#doImport(de.willuhn.datasource.GenericObject, de.willuhn.jameica.hbci.io.IOFormat, java.io.InputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doImport(GenericObject context, IOFormat format, InputStream is,
      ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    //TODO:[Heiner] Hier den DTAUS-Import vornehmen.
    /*
    try
    {
      DtausDateiParser parser = new DtausDateiParser(is);
      CSatz c = parser.next();
      
      // Im E-Satz steht die Anzahl der Datensaetze. Die brauchen wir, um
      // den Fortschrittsbalken mit sinnvollen Daten fuettern zu koennen.
      ESatz e = parser.getESatz();

      double factor = 100 / e.getAnzahlDatensaetze();
      int count = 0;
      
      DBService service = Settings.getDBService();
      while (c != null)
      {
        c = parser.next();

        try
        {
          // Mit diesem Factor sollte sich der Fortschrittsbalken
          // bis zum Ende der DTAUS-Datei genau auf 100% bewegen
          if (++count % factor == 1)
            monitor.addPercentComplete(1);
          
          monitor.log(i18n.tr("Importiere �berweisung an {0}",c.getNameEmpfaenger()));
         
          // Neue Ueberweisung erstellen
          Ueberweisung u = (Ueberweisung) service.createObject(Ueberweisung.class,null);

          // Konto suchen
          DBIterator konten = service.createList(Konto.class);
          konten.addFilter("kontonummer = '" + c.getKontoAuftraggeber() + "'");
          konten.addFilter("blz = '" + c.getBlzErstbeteiligt() + "'");

          Konto k = null;
          if (!konten.hasNext())
          {
            // Das Konto existiert nicht im Hibiscus-Datenbestand.
            // Also muss der User eins auswaehlen.
            monitor.log(i18n.tr("Konto {0} [BLZ {1}] nicht gefunden",new String[]{""+c.getKontoAuftraggeber(),""+c.getBlzErstbeteiligt()}));
            KontoAuswahlDialog d = new KontoAuswahlDialog(KontoAuswahlDialog.POSITION_CENTER);
            k = (Konto) d.open();
          }
          else
          {
            k = (Konto) konten.next();
          }
          u.setKonto(k);
          u.setBetrag(c.getBetrag());
          u.setGegenkontoBLZ(Long.toString(c.getBlzEndbeguenstigt()));
          u.setGegenkontoName(c.getNameEmpfaenger());
          u.setGegenkontoNummer(Long.toString(c.getKontonummer()));
          u.setZweck(c.getVerwendungszweck());
          // u.setZweck2(...);
          
          // Ueberweisung speichern
          u.store();
        }
        catch (ApplicationException ace)
        {
          monitor.log(ace.getMessage());
          monitor.log(i18n.tr("�berspringe Datensatz"));
        }
        catch (Exception e1)
        {
          Logger.error("unable to import transfer",e1);
          monitor.log(i18n.tr("Fehler beim Import der �berweisung, �berspringe Datensatz"));
        }
      }
      monitor.setStatusText(i18n.tr("{0} �berweisungen erfolgreich importiert",""+count));
    }
    catch (OperationCanceledException oce)
    {
      Logger.info("operation cancelled");
      monitor.setStatusText(i18n.tr("Import abgebrochen"));
      monitor.setStatus(ProgressMonitor.STATUS_CANCEL);
      return;
    }
    catch (Exception e)
    {
      throw new RemoteException(i18n.tr("Fehler beim Import der DTAUS-Daten"),e);
    }
    finally
    {
      if (is != null)
      {
        try
        {
          is.close();
        }
        catch (IOException ioe)
        {
          Logger.error("unable to close inputstream",ioe);
        }
      }
    }
    */
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  public String getName()
  {
    return i18n.tr("DTAUS-Format");
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getIOFormats(java.lang.Class)
   */
  public IOFormat[] getIOFormats(Class objectType)
  {
    // Wir unterstuetzen erstmal nur Ueberweisungen und Lastschriften
    if (objectType == null)
      return null;
    
    if (!objectType.equals(Ueberweisung.class) && !objectType.equals(Ueberweisung.class))
      return null;
    
    IOFormat f = new IOFormat() {
      public String getName()
      {
        return i18n.tr("DTAUS-Format");
      }

      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
       */
      public String[] getFileExtensions()
      {
        return new String[] {"*.dta"};
      }
    };
    return new IOFormat[] { f };
  }

}


/*********************************************************************
 * $Log$
 * Revision 1.2  2006-05-25 13:54:38  willuhn
 * @R removed imports (occurs compile errors in nightly build)
 *
 * Revision 1.1  2006/05/25 13:47:03  willuhn
 * @N Skeleton for DTAUS-Import
 *
 **********************************************************************/