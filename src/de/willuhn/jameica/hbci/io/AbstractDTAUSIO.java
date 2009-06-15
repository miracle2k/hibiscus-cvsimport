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

import java.rmi.RemoteException;

import de.jost_net.OBanToo.Dtaus.CSatz;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisungBuchung;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Klasse fuer DTAUS-Import/Export.
 */
public abstract class AbstractDTAUSIO implements IO
{
  static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  public String getName()
  {
    return i18n.tr("DTAUS-Format");
  }
  
  /**
   * Mappt den Textschluessel von Hibiscus zu DTAUS.
   * @param buchung Buchung.
   * @return Textschluessel.
   * @throws RemoteException
   */
  protected int mapTextschluesselToDtaus(SammelTransferBuchung buchung) throws RemoteException
  {
    if (buchung == null)
      return CSatz.TS_UEBERWEISUNGSGUTSCHRIFT;

    int ts = (buchung instanceof SammelUeberweisungBuchung) ? CSatz.TS_UEBERWEISUNGSGUTSCHRIFT : CSatz.TS_LASTSCHRIFT_ABBUCHUNGSVERFAHREN;
    String textschluessel = buchung.getTextSchluessel();
    if (textschluessel != null)
    {
      if (textschluessel.equals("05"))
        ts = CSatz.TS_LASTSCHRIFT_EINZUGSERMAECHTIGUNGSVERFAHREN;
      else if (textschluessel.equals("53"))
        ts = CSatz.TS_UEBERWEISUNG_LOHN_GEHALT_RENTE;
    }
    return ts;
  }
  
  /**
   * Mappt den DTAUS-Textschluessel von DTAUS zu Hibiscus.
   * @param buchung die Buchung.
   * @param ts Textschluessel aus DTAUS.
   * @return Textschluessel in Hibiscus.
   */
  protected String mapDtausToTextschluessel(SammelTransferBuchung buchung, long ts)
  {
    if (ts == CSatz.TS_LASTSCHRIFT_EINZUGSERMAECHTIGUNGSVERFAHREN)
      return "05";
    if (ts == CSatz.TS_UEBERWEISUNG_LOHN_GEHALT_RENTE)
      return "53";
    return (buchung instanceof SammelUeberweisungBuchung) ? "51" : "04";
  }
 
  /**
   * Liefert eine Liste von Objekt-Typen, die von diesem Importer
   * unterstuetzt werden.
   * @return Liste der unterstuetzten Formate.
   */
  abstract Class[] getSupportedObjectTypes();
  
  /**
   * @see de.willuhn.jameica.hbci.io.IO#getIOFormats(java.lang.Class)
   */
  public IOFormat[] getIOFormats(Class objectType)
  {
    // Kein Typ angegeben?
    if (objectType == null)
      return null;

    Class[] supported = getSupportedObjectTypes();
    if (supported == null || supported.length == 0)
      return null;
    
    for (int i=0;i<supported.length;++i)
    {
      if (objectType.equals(supported[i]))
        return new IOFormat[] { new MyIOFormat(objectType) };
    }
    return null;
  }

  /**
   * Hilfsklasse, damit wir uns den Objekt-Typ merken koennen.
   * @author willuhn
   */
  class MyIOFormat implements IOFormat
  {
    Class type = null;
    
    /**
     * ct.
     * @param type
     */
    private MyIOFormat(Class type)
    {
      this.type = type;
    }

    /**
     * @see de.willuhn.jameica.hbci.io.IOFormat#getName()
     */
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
  }
}


/*********************************************************************
 * $Log$
 * Revision 1.4  2009-06-15 08:51:16  willuhn
 * @N BUGZILLA 736
 *
 * Revision 1.3  2008/12/17 23:24:23  willuhn
 * @N Korrektes Mapping der Textschluessel beim Export/Import von Sammelauftraegen von/nach DTAUS
 *
 * Revision 1.2  2006/06/08 17:40:59  willuhn
 * @N Vorbereitungen fuer DTAUS-Import von Sammellastschriften und Umsaetzen
 *
 * Revision 1.1  2006/06/07 22:42:00  willuhn
 * @N DTAUSExporter
 * @N Abstrakte Basis-Klasse fuer Export und Import
 *
 **********************************************************************/