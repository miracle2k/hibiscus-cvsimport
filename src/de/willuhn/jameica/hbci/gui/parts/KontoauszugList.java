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

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.gui.util.Headline;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.dialogs.AdresseAuswahlDialog;
import de.willuhn.jameica.hbci.gui.input.BLZInput;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;


/**
 * Vorkonfigurierte Liste fuer Kontoauszuege.
 */
public class KontoauszugList extends UmsatzList
{
  // Suche nach Konto/zeitraum
  private SelectInput kontoAuswahl     = null;
  private DateInput start              = null;
  private DateInput end                = null;

  // Suche nach Gegenkonto
  private DialogInput gegenkontoNummer = null;
  private TextInput gegenkontoName     = null;
  private TextInput gegenkontoBLZ      = null;
  
  // Suche nach Betrag
  private DecimalInput betragFrom      = null;
  private DecimalInput betragTo        = null;

  private Listener listener            = null;

  private de.willuhn.jameica.system.Settings mySettings = null;

  private I18N i18n = null;

  /**
   * ct.
   * @param action die auszufuehrende Aktion.
   * @throws RemoteException
   */
  public KontoauszugList(Action action) throws RemoteException
  {
    super((GenericIterator)null,action);

    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    this.mySettings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
    this.setFilterVisible(false);

    // bei Ausloesungen ueber SWT-Events verzoegern wir
    // das Reload, um schnell aufeinanderfolgende Updates
    // zu buendeln.
    this.listener = new DelayedListener(new Listener() {
      public void handleEvent(Event event)
      {
        handleReload();
      }
    });
  }

  /**
   * Ueberschrieben, um die Tabelle vorher noch mit Daten zu fuellen.
   * @see de.willuhn.jameica.hbci.gui.parts.UmsatzList#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
    /////////////////////////////////////////////////////////////////
    // Tab-Container
    TabFolder folder = new TabFolder(parent, SWT.NONE);
    folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    folder.setBackground(Color.BACKGROUND.getSWTColor());

    TabGroup zeitraum = new TabGroup(folder,i18n.tr("Konto/Zeitraum"));
    zeitraum.addLabelPair(i18n.tr("Konto"), getKontoAuswahl());
    zeitraum.addLabelPair(i18n.tr("Start-Datum"), getStart());
    zeitraum.addLabelPair(i18n.tr("End-Datum"), getEnd());
    
    TabGroup gegenkonto = new TabGroup(folder,i18n.tr("Gegenkonto"));
    gegenkonto.addLabelPair(i18n.tr("Kontonummer enth�lt"),           getGegenkontoNummer());
    gegenkonto.addLabelPair(i18n.tr("BLZ enth�lt"),                   getGegenkontoBLZ());
    gegenkonto.addLabelPair(i18n.tr("Name des Kontoinhabers enth�lt"),getGegenkontoName());

    TabGroup betrag = new TabGroup(folder,i18n.tr("Betr�ge"));
    betrag.addLabelPair(i18n.tr("Mindest-Betrag"),                    getMindestBetrag());
    betrag.addLabelPair(i18n.tr("H�chst-Betrag"),                     getHoechstBetrag());

    new Headline(parent,i18n.tr("Gefundene Ums�tze"));

    removeAll();
    GenericIterator list = getUmsaetze();
    while (list.hasNext())
      addItem(list.next());
    
    super.paint(parent);

    // Zum Schluss Sortierung aktualisieren
    sort();
  }

  /**
   * Liefert eine Auswahlbox fuer das Konto.
   * @return Auswahlbox.
   * @throws RemoteException
   */
  public Input getKontoAuswahl() throws RemoteException
  {
    if (this.kontoAuswahl != null)
      return this.kontoAuswahl;

    DBIterator it = Settings.getDBService().createList(Konto.class);
    it.setOrder("ORDER BY blz, kontonummer");
    String id = mySettings.getString("kontoauszug.list.konto",null);
    Konto k = null;
    if (id != null)
    {
      try
      {
        k = (Konto) Settings.getDBService().createObject(Konto.class,id);
      }
      catch (ObjectNotFoundException e)
      {
        // Das angegebene Konto existiert nicht mehr
        mySettings.setAttribute("kontoauszug.list.konto",(String)null);
      }
    }
    this.kontoAuswahl = new SelectInput(it, k);
    this.kontoAuswahl.setAttribute("longname");
    this.kontoAuswahl.setPleaseChoose(i18n.tr("Alle Konten"));
    this.kontoAuswahl.addListener(this.listener);
    return this.kontoAuswahl;
  }

  /**
   * Liefert ein Auswahl-Feld fuer das Start-Datum.
   * @return Auswahl-Feld.
   */
  public Input getStart()
  {
    if (this.start != null)
      return this.start;

    Date dStart = null;
    String s = mySettings.getString("kontoauszug.list.from",null);
    if (s != null)
    {
      try
      {
        dStart = HBCI.DATEFORMAT.parse(s);
      }
      catch (Exception e)
      {
        Logger.error("unable to restore start date",e);
        mySettings.setAttribute("kontoauszug.list.from",(String) null);
      }
    }
    else
    {
      // Ansonsten nehmen wir den ersten des aktuellen Monats
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.DAY_OF_MONTH, 1);
      dStart = cal.getTime();
    }

    this.start = new DateInput(dStart, HBCI.DATEFORMAT);
    this.start.setComment(i18n.tr("Fr�hestes Valuta-Datum"));
    this.start.addListener(this.listener);
    return this.start;
  }

  /**
   * Liefert ein Auswahl-Feld fuer das End-Datum.
   * @return Auswahl-Feld.
   */
  public Input getEnd()
  {
    if (this.end != null)
      return this.end;

    Date dEnd = null;
    String s = mySettings.getString("kontoauszug.list.to",null);
    if (s != null)
    {
      try
      {
        dEnd = HBCI.DATEFORMAT.parse(s);
      }
      catch (Exception e)
      {
        Logger.error("unable to restore end date",e);
        mySettings.setAttribute("kontoauszug.list.to",(String) null);
      }
    }
    else
    {
      // Ansonsten nehmen wir den letzten des aktuellen Monats
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
      dEnd = cal.getTime();
    }
    
    this.end = new DateInput(dEnd, HBCI.DATEFORMAT);
    this.end.setComment(i18n.tr("Sp�testes Valuta-Datum"));
    this.end.addListener(this.listener);
    return this.end;
  }
  
  /**
   * Liefert das Eingabe-Feld fuer die Kontonummer des Gegenkontos.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public DialogInput getGegenkontoNummer() throws RemoteException
  {
    if (this.gegenkontoNummer != null)
      return this.gegenkontoNummer;

    AdresseAuswahlDialog d = new AdresseAuswahlDialog(AdresseAuswahlDialog.POSITION_MOUSE);
    d.addCloseListener(new AddressListener());
    this.gegenkontoNummer = new DialogInput("",d);
    this.gegenkontoNummer.setValidChars(HBCIProperties.HBCI_KTO_VALIDCHARS);
    this.gegenkontoNummer.addListener(this.listener);
    return this.gegenkontoNummer;
  }

  /**
   * Liefert das Eingabe-Feld fuer die BLZ.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getGegenkontoBLZ() throws RemoteException
  {
    if (this.gegenkontoBLZ != null)
      return this.gegenkontoBLZ;
    
    this.gegenkontoBLZ = new BLZInput("");
    this.gegenkontoBLZ.addListener(this.listener);
    return this.gegenkontoBLZ;
  }

  /**
   * Liefert das Eingabe-Feld fuer den Namen des Kontoinhabers.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getGegenkontoName() throws RemoteException
  {
    if (this.gegenkontoName != null)
      return this.gegenkontoName;
    this.gegenkontoName = new TextInput("",HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH);
    this.gegenkontoName.setValidChars(HBCIProperties.HBCI_DTAUS_VALIDCHARS);
    this.gegenkontoName.addListener(this.listener);
    return this.gegenkontoName;
  }
  
  /**
   * Liefert ein Eingabe-Feld fuer die Eingabe eines Mindestbetrages.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getMindestBetrag() throws RemoteException
  {
    if (this.betragFrom != null)
      return this.betragFrom;
    
    this.betragFrom = new DecimalInput(mySettings.getDouble("kontoauszug.list.minbetrag",Double.NaN), HBCI.DECIMALFORMAT);
    this.betragFrom.setComment(HBCIProperties.CURRENCY_DEFAULT_DE);
    this.betragFrom.addListener(this.listener);
    return this.betragFrom;
  }

  /**
   * Liefert ein Eingabe-Feld fuer die Eingabe eines Hoechstbetrages.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getHoechstBetrag() throws RemoteException
  {
    if (this.betragTo != null)
      return this.betragTo;
    
    this.betragTo = new DecimalInput(mySettings.getDouble("kontoauszug.list.maxbetrag",Double.NaN), HBCI.DECIMALFORMAT);
    this.betragTo.setComment(HBCIProperties.CURRENCY_DEFAULT_DE);
    this.betragTo.addListener(this.listener);
    return this.betragTo;
  }

  /**
   * Liefert die Liste der Umsaetze basierend auf der aktuellen Auswahl.
   * @return Liste der Umsaetze.
   * @throws RemoteException
   */
  private synchronized GenericIterator getUmsaetze() throws RemoteException
  {
    Konto k         = (Konto) getKontoAuswahl().getValue();
    Date start      = (Date) getStart().getValue();
    Date end        = (Date) getEnd().getValue();
    String gkName   = (String) getGegenkontoName().getValue();
    String gkBLZ    = (String) getGegenkontoBLZ().getValue();
    String gkNummer = (String) getGegenkontoNummer().getText();
    Double min      = (Double) getMindestBetrag().getValue();
    Double max      = (Double) getHoechstBetrag().getValue();
    
    HBCIDBService service = (HBCIDBService) Settings.getDBService();

    DBIterator umsaetze = Settings.getDBService().createList(Umsatz.class);

    /////////////////////////////////////////////////////////////////
    // Konto und Zeitraum
    if (k != null)     umsaetze.addFilter("konto_id = " + k.getID());
    if (start != null) umsaetze.addFilter("valuta >= ?", new Object[]{new java.sql.Date(HBCIProperties.startOfDay(start).getTime())});
    if (end != null)   umsaetze.addFilter("valuta <= ?", new Object[]{new java.sql.Date(HBCIProperties.endOfDay(end).getTime())});
    /////////////////////////////////////////////////////////////////
    // Gegenkonto
    if (gkBLZ    != null && gkBLZ.length() > 0)    umsaetze.addFilter("empfaenger_blz like ?",new Object[]{"%" + gkBLZ + "%"});
    if (gkNummer != null && gkNummer.length() > 0) umsaetze.addFilter("empfaenger_konto like ?",new Object[]{"%" + gkNummer + "%"});
    if (gkName   != null && gkName.length() > 0)   umsaetze.addFilter("LOWER(empfaenger_name) like ?",new Object[]{"%" + gkName.toLowerCase() + "%"});
    /////////////////////////////////////////////////////////////////
    
    /////////////////////////////////////////////////////////////////
    // Betrag
    if (min != null)
    {
      if (!Double.isNaN(min.doubleValue()))
        umsaetze.addFilter("betrag >= ?",new Object[]{min});
    }
    if (max != null)
    {
      if (!Double.isNaN(max.doubleValue()))
        umsaetze.addFilter("betrag <= ?",new Object[]{max});
    }
    /////////////////////////////////////////////////////////////////

    umsaetze.setOrder("ORDER BY " + service.getSQLTimestamp("valuta") + " asc, id asc");
    return umsaetze;
  }

  /**
   * Aktualisiert die Tabelle der angezeigten Umsaetze.
   */
  public synchronized void handleReload()
  {
    if (!hasChanged())
      return;

    GUI.startSync(new Runnable() // Sanduhr einblenden
    {
      public void run()
      {
        try
        {
          removeAll();
          
          GenericIterator list = getUmsaetze();
          while (list.hasNext())
            addItem(list.next());

          // Aktuelle Werte speichern
          try
          {
            // Wir speichern hier alle eingegebenen Suchbegriffe fuer's naechste mal
            Date from = (Date) getStart().getValue();
            Date to   = (Date) getEnd().getValue();
            Konto k   = (Konto) getKontoAuswahl().getValue();
            Double min = (Double) getMindestBetrag().getValue();
            Double max = (Double) getHoechstBetrag().getValue();
            mySettings.setAttribute("kontoauszug.list.from",from == null ? null : HBCI.DATEFORMAT.format(from));
            mySettings.setAttribute("kontoauszug.list.to",to == null ? null : HBCI.DATEFORMAT.format(to));
            mySettings.setAttribute("kontoauszug.list.konto",k == null ? null : k.getID());
            
            mySettings.setAttribute("kontoauszug.list.minbetrag",min == null ? Double.NaN : min.doubleValue());
            mySettings.setAttribute("kontoauszug.list.maxbetrag",max == null ? Double.NaN : max.doubleValue());
          }
          catch (RemoteException re)
          {
            Logger.error("error while saving last filter settings",re);
          }
          
          // Zum Schluss Sortierung aktualisieren
          sort();
        }
        catch (Exception e)
        {
          Logger.error("error while reloading table",e);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Aktualisieren der Ums�tze"), StatusBarMessage.TYPE_ERROR));
        }
      }
    });
  }
  
  /**
   * Prueft, ob seit der letzten Aktion Eingaben geaendert wurden.
   * Ist das nicht der Fall, muss die Tabelle nicht neu geladen werden.
   * @return true, wenn sich die Daten geaendert haben.
   */
  private boolean hasChanged()
  {
    try
    {
      return getStart().hasChanged() ||
                getEnd().hasChanged() ||
                getKontoAuswahl().hasChanged() ||
                getGegenkontoName().hasChanged() ||
                getGegenkontoNummer().hasChanged() ||
                getGegenkontoBLZ().hasChanged() ||
                getMindestBetrag().hasChanged() ||
                getHoechstBetrag().hasChanged();
    }
    catch (Exception e)
    {
      Logger.error("unable to check change status",e);
      return false;
    }
  }
  
  /**
   * Listener, der bei Auswahl der Adresse die restlichen Daten vervollstaendigt.
   */
  private class AddressListener implements Listener
  {

    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event)
    {
      if (event == null || event.data == null)
        return;

      Address address = (Address) event.data;
      try
      {
        getGegenkontoNummer().setText(address.getKontonummer());
        getGegenkontoBLZ().setValue(address.getBLZ());
        getGegenkontoName().setValue(address.getName());
      }
      catch (RemoteException er)
      {
        Logger.error("error while choosing gegenkonto",er);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler bei der Auswahl des Gegenkontos"), StatusBarMessage.TYPE_ERROR));
      }
    }
  }

}


/*********************************************************************
 * $Log$
 * Revision 1.4  2007-05-02 13:01:12  willuhn
 * @N Zusaetzlicher Filter nach Mindest- und Hoechstbetrag
 *
 * Revision 1.3  2007/05/02 12:40:18  willuhn
 * @C UmsatzTree*-Exporter nur fuer Objekte des Typs "UmsatzTree" anbieten
 * @C Start- und End-Datum in Kontoauszug speichern und an PDF-Export via Session uebergeben
 *
 * Revision 1.2  2007/04/27 15:33:03  willuhn
 * @C Hier sind noch Nacharbeiten noetig
 *
 * Revision 1.1  2007/04/27 15:30:44  willuhn
 * @N Kontoauszug-Liste in TablePart verschoben
 *
 **********************************************************************/