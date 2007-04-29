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

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Exportiert Umsaetze im PDF-Format.
 * Der Exporter kann Umsaetze mehrerer Konten exportieren. Sie werden
 * hierbei nach Konto gruppiert.
 */
public class PDFUmsatzExporter implements Exporter
{
  private I18N i18n = null;

  /**
   * ct.
   */
  public PDFUmsatzExporter()
  {
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.Exporter#doExport(java.lang.Object[], de.willuhn.jameica.hbci.io.IOFormat, java.io.OutputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doExport(Object[] objects, IOFormat format,
      OutputStream os, ProgressMonitor monitor) throws RemoteException,
      ApplicationException
  {
    
    if (objects == null || objects.length == 0)
      throw new ApplicationException(i18n.tr("Bitte w�hlen Sie die zu exportierenden Ums�tze aus"));

    Umsatz u = (Umsatz) objects[0];

    Date startDate     = u.getDatum();
    Date endDate       = u.getDatum();
    Hashtable umsaetze = new Hashtable();
    
    if (monitor != null) 
    {
      monitor.setStatusText(i18n.tr("Ermittle zu exportierende Ums�tze und Konten"));
      monitor.addPercentComplete(1);
    }

    for (int i=0;i<objects.length;++i)
    {
      u = (Umsatz) objects[i];
      Konto k  = u.getKonto();

      // Wir ermitteln bei der Gelegenheit das Maximal- und Minimal-Datum
      Date date = u.getDatum();
      if (date != null)
      {
        if (endDate != null && date.after(endDate))    endDate = date;
        if (startDate != null && date.before(startDate)) startDate = date;
      }

      // Wir gruppieren die Umsaetze nach Konto.
      ArrayList list = (ArrayList) umsaetze.get(k.getID());
      if (list == null)
      {
        list = new ArrayList();
        umsaetze.put(k.getID(),list);
      }
      list.add(u);
    }

    // Falls wir die Datumsfelder als optionale Parameter erhalten haben,
    // nehmen wir die.
    Date d = (Date) Exporter.SESSION.get("pdf.start");
    if (d != null) startDate = d;
    d = (Date) Exporter.SESSION.get("pdf.end");
    if (d != null) endDate = d;
    
    try
    {
      // Der Export
      String subTitle = i18n.tr("{0} - {1}", new String[]{startDate == null ? "" : HBCI.DATEFORMAT.format(startDate),endDate == null ? "" : HBCI.DATEFORMAT.format(endDate)});
      Reporter reporter = new Reporter(os,monitor, "Kontoauszug", subTitle, objects.length  );

      reporter.addHeaderColumn("Valuta / Buchungs- datum",Element.ALIGN_CENTER,30,  Color.LIGHT_GRAY);
      reporter.addHeaderColumn("Empf�nger/Einzahler",     Element.ALIGN_CENTER,100, Color.LIGHT_GRAY);
      reporter.addHeaderColumn("Zahlungsgrund",           Element.ALIGN_CENTER,120, Color.LIGHT_GRAY);
      reporter.addHeaderColumn("Betrag",                  Element.ALIGN_CENTER,30,  Color.LIGHT_GRAY);
      reporter.addHeaderColumn("Saldo",                   Element.ALIGN_CENTER,30,  Color.LIGHT_GRAY);
      reporter.createHeader();

      
      // Iteration ueber Umsaetze
      Enumeration konten = umsaetze.keys();
      while (konten.hasMoreElements())
      {
        String id = (String) konten.nextElement();
        Konto konto = (Konto) Settings.getDBService().createObject(Konto.class,id);
        
        PdfPCell cell = reporter.getDetailCell(konto.getLongName(), Element.ALIGN_CENTER,Color.LIGHT_GRAY);
        cell.setColspan(5);
        reporter.addColumn(cell);

        ArrayList list = (ArrayList) umsaetze.get(id);

        if (list.size() == 0)
        {
          PdfPCell empty = reporter.getDetailCell(i18n.tr("Keine Ums�tze"), Element.ALIGN_CENTER,Color.LIGHT_GRAY);
          empty.setColspan(5);
          reporter.addColumn(empty);
          continue;
        }
        
        for (int i=0;i<list.size();++i)
        {

          u = (Umsatz)list.get(i);
          reporter.addColumn(reporter.getDetailCell((u.getValuta() != null ? HBCI.DATEFORMAT.format(u.getValuta()) : "" ) + "\n"
                                    + (u.getDatum() != null ? HBCI.DATEFORMAT.format(u.getDatum()) : ""), Element.ALIGN_LEFT));
          reporter.addColumn(reporter.getDetailCell(reporter.notNull(u.getGegenkontoName()) + "\n"
                                    + reporter.notNull(u.getArt()), Element.ALIGN_LEFT));
          reporter.addColumn(reporter.getDetailCell(reporter.notNull(u.getZweck()) + "\n"
                                   + reporter.notNull(u.getZweck2()), Element.ALIGN_LEFT));
          reporter.addColumn(reporter.getDetailCell(u.getBetrag()));
          reporter.addColumn(reporter.getDetailCell(u.getSaldo()));
          reporter.setNextRecord();
        }

      }
      reporter.close();
    
    }
    catch (DocumentException e)
    {
      Logger.error("error while creating report",e);
      throw new ApplicationException(i18n.tr("Fehler beim Erzeugen des Reports"),e);
    }
    catch (IOException e)
    {
      Logger.error("error while creating report",e);
      throw new ApplicationException(i18n.tr("Fehler beim Erzeugen des Reports"),e);
    }

  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  public String getName()
  {
    return i18n.tr("PDF-Format");
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getIOFormats(java.lang.Class)
   */
  public IOFormat[] getIOFormats(Class objectType)
  {
    if (!Umsatz.class.equals(objectType))
      return null;
    
    IOFormat format = new IOFormat() {
      public String getName()
      {
        return i18n.tr("PDF-Format");
      }

      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
       */
      public String[] getFileExtensions()
      {
        return new String[]{"pdf"};
      }
    };
    
    return new IOFormat[]{format};
  }

}


/*********************************************************************
 * $Log$
 * Revision 1.8  2007-04-29 10:21:20  jost
 * Umstellung auf neue Reporter-Klasse
 *
 * Revision 1.7  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.6  2006/10/22 19:52:07  jost
 * Bug #301
 *
 * Revision 1.5  2006/10/16 22:53:54  willuhn
 * @N noch Checks auf Nullpointer hinzugefuegt
 *
 * Revision 1.4  2006/10/16 17:34:08  jost
 * Nochmals Randkorrektur
 *
 * Revision 1.3  2006/10/16 17:12:14  jost
 * 1. Randeintellungen korrigiert
 * 2. Korrekte Datumsangabe bei Aufruf des Exports aus der Umsatzliste
 *
 * Revision 1.2  2006/10/16 12:51:32  willuhn
 * @B Uebernahme des originalen Datums aus dem Kontoauszug
 *
 * Revision 1.1  2006/07/03 23:04:32  willuhn
 * @N PDF-Reportwriter in IO-API gepresst, damit er auch an anderen Stellen (z.Bsp. in der Umsatzliste) mitverwendet werden kann.
 *
 **********************************************************************/