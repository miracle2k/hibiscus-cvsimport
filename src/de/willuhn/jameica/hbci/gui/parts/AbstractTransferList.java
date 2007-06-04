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

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;
import java.util.Date;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit Transfers.
 */
public abstract class AbstractTransferList extends AbstractFromToList
{
  private MessageConsumer mc = null;

  /**
   * ct.
   * @param action
   */
  public AbstractTransferList(Action action)
  {
    super(action);

    setFormatter(new TableFormatter() {
      public void format(TableItem item) {
        Terminable l = (Terminable) item.getData();
        if (l == null)
          return;

        try {
          if (l.getTermin().before(new Date()) && !l.ausgefuehrt())
          {
            item.setForeground(Settings.getUeberfaelligForeground());
          }
          if (l.ausgefuehrt())
            item.setForeground(Color.COMMENT.getSWTColor());
        }
        catch (RemoteException e) { /*ignore */}
      }
    });

    addColumn(i18n.tr("Konto"),"konto_id", new Formatter() {
      /**
       * @see de.willuhn.jameica.gui.formatter.Formatter#format(java.lang.Object)
       */
      public String format(Object o)
      {
        if (o == null || !(o instanceof Konto))
          return null;
        Konto k = (Konto) o;
        try
        {
          String s = k.getKontonummer();
          String name = k.getBezeichnung();
          if (name != null && name.length() > 0)
            s += " [" + name + "]";
          return s;
        }
        catch (RemoteException r)
        {
          Logger.error("unable to display konto",r);
          return null;
        }
      }
    
    });
    addColumn(i18n.tr("Gegenkonto Inhaber"),"empfaenger_name");
    addColumn(i18n.tr("Gegenkonto BLZ"),"empfaenger_blz", new Formatter() {
      /**
       * @see de.willuhn.jameica.gui.formatter.Formatter#format(java.lang.Object)
       */
      public String format(Object o)
      {
        if (o == null)
          return null;
        String blz = o.toString();
        String name = HBCIUtils.getNameForBLZ(blz);
        if (name != null && name.length() > 0)
          blz += " [" + name + "]";
        return blz;
      }
    
    });
    addColumn(i18n.tr("Verwendungszweck"),"zweck");
    addColumn(i18n.tr("Betrag"),"betrag", new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT));
    addColumn(i18n.tr("Termin"),"termin", new DateFormatter(HBCI.DATEFORMAT));
    addColumn(i18n.tr("Status"),"ausgefuehrt",new Formatter() {
      public String format(Object o) {
        try {
          int i = ((Integer) o).intValue();
          return i == 1 ? i18n.tr("ausgef�hrt") : i18n.tr("offen");
        }
        catch (Exception e) {}
        return ""+o;
      }
    });
    setMulti(true);

    // Damit werden wir informiert, wenn Ueberweisungen/Lastschriften
    // importiert wurden. Wir koennen sie dann gleich zur Tabelle hinzufuegen
    this.mc = new TransferMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.mc);

  }

  
  /**
   * @see de.willuhn.jameica.hbci.gui.parts.AbstractFromToList#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
    // Damit wir den MessageConsumer beim Schliessen wieder entfernen
    parent.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        Application.getMessagingFactory().unRegisterMessageConsumer(mc);
      }
    });
    super.paint(parent);
  }


  /**
   * @see de.willuhn.jameica.hbci.gui.parts.AbstractFromToList#getList(java.util.Date, java.util.Date)
   */
  protected GenericIterator getList(Date from, Date to) throws RemoteException
  {
    HBCIDBService service = (HBCIDBService) Settings.getDBService();
    
    DBIterator list = service.createList(getObjectType());
    if (from != null) list.addFilter("termin >= ?", new Object[]{new java.sql.Date(HBCIProperties.startOfDay(from).getTime())});
    if (to   != null) list.addFilter("termin <= ?", new Object[]{new java.sql.Date(HBCIProperties.endOfDay(to).getTime())});
    list.setOrder("ORDER BY " + service.getSQLTimestamp("termin") + " DESC");
    return list;
  }
  
  /**
   * Liefert die Art der zu ladenden Objekte zurueck.
   * @return Art der zu ladenden Objekte.
   */
  protected abstract Class getObjectType();


  /**
   * Hilfsklasse damit wir ueber importierte Transfers informiert werden.
   */
  public class TransferMessageConsumer implements MessageConsumer
  {
    private DelayedListener delayed = null;
    
    /**
     * ct.
     */
    private TransferMessageConsumer()
    {
      if (listener != null)
        this.delayed = new DelayedListener(listener);
    }
    
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{ImportMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      if (message == null || !(message instanceof ImportMessage))
        return;
      
      final GenericObject o = ((ImportMessage)message).getObject();
      
      if (o == null)
        return;
      
      // Checken, ob uns der Transfer-Typ interessiert
      if (!getObjectType().isAssignableFrom(o.getClass()))
        return;

      // Wir forcieren das Reload. Da in den Eingabefeldern
      // nichts geaendert wurde, wuerde das Reload sonst nicht
      // stattfinden.
      if (delayed != null)
        delayed.handleEvent(null);
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.21  2007-06-04 23:23:47  willuhn
 * @B error while saving transfer list date
 *
 * Revision 1.20  2007/04/26 23:08:13  willuhn
 * @C Umstellung auf DelayedListener
 *
 * Revision 1.19  2007/04/26 13:59:31  willuhn
 * @N Besseres Reload-Verhalten in Transfer-Listen
 *
 * Revision 1.18  2007/04/24 17:15:51  willuhn
 * @B Vergessen, "size" hochzuzaehlen, wenn Objekte vor paint() hinzugefuegt werden
 *
 * Revision 1.17  2007/04/24 16:55:00  willuhn
 * @N Aktualisierte Daten nur bei geaendertem Datum laden
 *
 * Revision 1.16  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.15  2007/04/18 14:51:09  willuhn
 * @C removed 2 warnings
 *
 * Revision 1.14  2007/03/21 18:47:36  willuhn
 * @N Neue Spalte in Kategorie-Tree
 * @N Sortierung des Kontoauszuges wie in Tabelle angezeigt
 * @C Code cleanup
 *
 * Revision 1.13  2007/03/16 14:40:02  willuhn
 * @C Redesign ImportMessage
 * @N Aktualisierung der Umsatztabelle nach Kategorie-Zuordnung
 *
 * Revision 1.12  2006/12/29 16:09:21  willuhn
 * @R Uhrzeit aus Termin entfernt
 *
 * Revision 1.11  2006/12/29 14:28:47  willuhn
 * @B Bug 345
 * @B jede Menge Bugfixes bei SQL-Statements mit Valuta
 *
 * Revision 1.10  2006/12/27 17:56:49  willuhn
 * @B Bug 341
 *
 * Revision 1.9  2006/11/20 23:07:54  willuhn
 * @N new package "messaging"
 * @C moved ImportMessage into new package
 *
 * Revision 1.8  2006/11/16 12:21:02  willuhn
 * @B NPE
 *
 * Revision 1.7  2006/11/15 00:47:39  willuhn
 * @C Bug 325
 *
 * Revision 1.6  2006/11/06 23:12:38  willuhn
 * @B Fehler bei Aktualisierung der Elemente nach Insert, Delete, Sort
 *
 * Revision 1.5  2006/10/31 22:59:03  willuhn
 * @B Bis-Datum wurde nicht korrekt uebernommen
 *
 * Revision 1.4  2006/10/31 22:54:46  willuhn
 * @N Ausgefuehrte Transfers eingrauen
 *
 * Revision 1.3  2006/10/17 23:50:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2006/10/17 01:01:21  willuhn
 * @N Filter fuer Ueberweisungen und Lastschriften
 *
 * Revision 1.1  2006/10/17 00:04:31  willuhn
 * @N new Formatters in Transfer-Listen
 * @N merged UeberweisungList + LastschriftList into AbstractTransferList
 *
 **********************************************************************/