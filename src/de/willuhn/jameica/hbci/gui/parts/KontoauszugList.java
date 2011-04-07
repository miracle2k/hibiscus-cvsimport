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
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Text;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.internal.buttons.Back;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.gui.util.Headline;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.action.UmsatzDetail;
import de.willuhn.jameica.hbci.gui.action.UmsatzExport;
import de.willuhn.jameica.hbci.gui.dialogs.AdresseAuswahlDialog;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.BLZInput;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.gui.input.UmsatzTypInput;
import de.willuhn.jameica.hbci.gui.parts.columns.KontoColumn;
import de.willuhn.jameica.hbci.io.Exporter;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.hbci.server.UmsatzUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Vorkonfigurierte Liste fuer Kontoauszuege.
 */
public class KontoauszugList extends UmsatzList
{
  private static Date endDate          = null;
  private static Integer currentTab    = null;
  
  // Konto/Zeitraum/Suchbegriff/nur geprueft
  private TextInput text               = null;
  private CheckboxInput unchecked      = null;
  private KontoInput kontoAuswahl      = null;
  private DateInput start              = null;
  private DateInput end                = null;
  private UmsatzTypInput kategorie     = null;

  // Gegenkonto/Betrag
  private DialogInput gegenkontoNummer = null;
  private TextInput gegenkontoName     = null;
  private TextInput gegenkontoBLZ      = null;
  private DecimalInput betragFrom      = null;
  private DecimalInput betragTo        = null;

  private Listener listener            = null;
  
  private boolean disposed = false; // BUGZILLA 462
  private boolean changed = false;

  private I18N i18n = null;
  
  /**
   * ct.
   * @throws RemoteException
   */
  public KontoauszugList() throws RemoteException
  {
    super((GenericIterator)null,new UmsatzDetail());

    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
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
    addColumn(new KontoColumn("konto_id")); // BUGZILLA 723
    addColumn(i18n.tr("GK Konto"), "empfaenger_konto");
    addColumn(i18n.tr("GK BLZ"),   "empfaenger_blz");
    addColumn(i18n.tr("Art"),      "art");

    /////////////////////////////////////////////////////////////////
    // Tab-Container
    final TabFolder folder = new TabFolder(parent, SWT.NONE);
    folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    folder.setBackground(Color.BACKGROUND.getSWTColor());

    {
      TabGroup tab = new TabGroup(folder,i18n.tr("Konto/Zeitraum/Suchbegriff"));

      ColumnLayout columns = new ColumnLayout(tab.getComposite(),2);
      
      Container left = new SimpleContainer(columns.getComposite());
      left.addLabelPair(i18n.tr("Zweck/Notiz/Art enth�lt"), this.getText());
      left.addLabelPair(i18n.tr("Konto"),                   this.getKontoAuswahl());
      left.addLabelPair(i18n.tr("Kategorie"),               this.getKategorie());
      
      Container right = new SimpleContainer(columns.getComposite());
      right.addCheckbox(this.getUnChecked(),i18n.tr("Nur ungepr�fte Ums�tze"));
      right.addLabelPair(i18n.tr("Start-Datum"),            this.getStart());
      right.addLabelPair(i18n.tr("End-Datum"),              this.getEnd());
    }
    
    {
      TabGroup tab = new TabGroup(folder,i18n.tr("Gegenkonto/Betrag"));
      tab.addLabelPair(i18n.tr("Kontonummer enth�lt"), this.getGegenkontoNummer());

      ColumnLayout columns = new ColumnLayout(tab.getComposite(),2);
      Container left = new SimpleContainer(columns.getComposite());
      left.addLabelPair(i18n.tr("BLZ enth�lt"),                    this.getGegenkontoBLZ());
      left.addLabelPair(i18n.tr("Name des Kontoinhabers enth�lt"), this.getGegenkontoName());

      Container right = new SimpleContainer(columns.getComposite());
      right.addLabelPair(i18n.tr("Mindest-Betrag"), this.getMindestBetrag());
      right.addLabelPair(i18n.tr("H�chst-Betrag"),        this.getHoechstBetrag());
    }
    
    // Wir merken uns das aktive Tab.
    if (currentTab != null) folder.setSelection(currentTab);
    folder.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        currentTab = folder.getSelectionIndex();
      }
    });
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(new Back(false));
    buttons.addButton(i18n.tr("Exportieren..."), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        handlePrint();
      }
    },null,false,"document-save.png");
    buttons.addButton(i18n.tr("Filter zur�cksetzen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        handleReset();
      }
    },null,false,"edit-undo.png");
    buttons.addButton(i18n.tr("Aktualisieren"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        handleReload();
      }
    },null,true,"view-refresh.png");
    
    buttons.paint(parent);
    

    new Headline(parent,i18n.tr("Gefundene Ums�tze"));

    removeAll();
    List<Umsatz> list = getUmsaetze();
    for(Umsatz u:list)
      addItem(u);
    
    parent.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        GUI.getView().setLogoText(""); // Hinweis-Test wieder ausblenden BUGZILLA 449
        disposed = true;
      }
    });
    super.paint(parent);

    // Zum Schluss Sortierung aktualisieren
    sort();
    
    // Machen wir explizit nochmal, weil wir die paint()-Methode ueberschrieben haben
    restoreState();
  }

  /**
   * Liefert eine Auswahlbox fuer das Konto.
   * @return Auswahlbox.
   * @throws RemoteException
   */
  public KontoInput getKontoAuswahl() throws RemoteException
  {
    if (this.kontoAuswahl != null)
      return this.kontoAuswahl;

    this.kontoAuswahl = new KontoInput(null,KontoFilter.ALL);
    this.kontoAuswahl.setComment(null);
    
    Konto preset = null;

    /////////////////////
    // Preset laden
    String s = mySettings.getString("kontoauszug.list.konto",null);
    if (s != null && s.length() > 0)
    {
      try
      {
        preset = (Konto) de.willuhn.jameica.hbci.Settings.getDBService().createObject(Konto.class,s);
      }
      catch (Exception e)
      {
        // Konto existiert wohl nicht mehr - dann loeschen wir die Vorauswahl
        mySettings.setAttribute("kontoauszug.list.konto",(String)null);
      }
    }
    //
    /////////////////////
    
    // Wir ueberschreiben hier uebrigens bewusst das Default-Konto von
    // Hibiscus, weil es hier eher nervt
    this.kontoAuswahl.setPreselected(preset);
    
    this.kontoAuswahl.setPleaseChoose(i18n.tr("<Alle Konten>"));
    this.kontoAuswahl.addListener(this.listener);
    return this.kontoAuswahl;
  }

  /**
   * Liefert eine Checkbox, um nur die ungeprueften Umsaetze anzuzeigen.
   * @return Checkbox.
   */
  public CheckboxInput getUnChecked()
  {
    if (this.unchecked != null)
      return this.unchecked;
    
    this.unchecked = new CheckboxInput(mySettings.getBoolean("kontoauszug.list.unchecked",false));
    this.unchecked.addListener(this.listener);
    return this.unchecked;
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

    this.end = new DateInput(endDate, HBCI.DATEFORMAT);
    this.end.setComment(i18n.tr("Sp�testes Valuta-Datum"));
    this.end.addListener(this.listener);
    return this.end;
  }
  
  /**
   * Liefert ein Auswahl-Feld fuer die Kategorie. 
   * @return Auswahl-Feld.
   * @throws RemoteException
   */
  public UmsatzTypInput getKategorie() throws RemoteException
  {
    if (this.kategorie != null)
      return this.kategorie;
    
    UmsatzTyp preset = null;

    /////////////////////
    // Preset laden
    String s = mySettings.getString("kontoauszug.list.kategorie",null);
    if (s != null && s.length() > 0)
    {
      try
      {
        preset = (UmsatzTyp) de.willuhn.jameica.hbci.Settings.getDBService().createObject(UmsatzTyp.class,s);
      }
      catch (Exception e)
      {
        // Kategorie existiert wohl nicht mehr - dann loeschen wir die Vorauswahl
        mySettings.setAttribute("kontoauszug.list.kategorie",(String)null);
      }
    }
    //
    /////////////////////

    this.kategorie = new UmsatzTypInput(preset,UmsatzTyp.TYP_EGAL);
    this.kategorie.setPleaseChoose(i18n.tr("<Alle Kategorien>"));
    this.kategorie.setComment(null);
    this.kategorie.addListener(this.listener);
    return this.kategorie;
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
    this.gegenkontoNummer = new DialogInput(mySettings.getString("kontoauszug.list.gegenkonto.nummer",null),d);
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
    
    this.gegenkontoBLZ = new BLZInput(mySettings.getString("kontoauszug.list.gegenkonto.blz",null));
    this.gegenkontoBLZ.setComment(null);
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
    this.gegenkontoName = new TextInput(mySettings.getString("kontoauszug.list.gegenkonto.name",null),HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH);
    this.gegenkontoName.setValidChars(HBCIProperties.HBCI_DTAUS_VALIDCHARS);
    this.gegenkontoName.addListener(this.listener);
    return this.gegenkontoName;
  }
  
  /**
   * Liefert das Eingabe-Feld fuer Verwendungszweck/Kommentar.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getText() throws RemoteException
  {
    if (this.text != null)
      return this.text;
    this.text = new TextInput(mySettings.getString("kontoauszug.list.text",null),HBCIProperties.HBCI_TRANSFER_USAGE_MAXLENGTH);
    this.text.addListener(this.listener);
    return this.text;
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
    
    this.betragFrom = new DecimalInput(mySettings.getDouble("kontoauszug.list.betrag.from",Double.NaN), HBCI.DECIMALFORMAT);
    this.betragFrom.setComment(HBCIProperties.CURRENCY_DEFAULT_DE);
    this.betragFrom.addListener(this.listener);
    this.betragFrom.addListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        try
        {
          // Nur bei Focus Out
          if (event.type == SWT.FocusOut)
          {
            // Wenn beim Hoechstbetrag noch nichts eingegeben ist, uebernehmen
            // wird dort automatisch den Mindestbetrag
            // Vorschlag von Roberto aus Mail vom 30.08.2008
            Input i = getHoechstBetrag();
            Double value = (Double) i.getValue();
            if (value == null || value.isNaN())
            {
              i.setValue(betragFrom.getValue());
              ((Text) i.getControl()).selectAll();
            }
          }
        }
        catch (Exception e)
        {
          Logger.error("error while auto applying max value",e);
        }
      }
    });
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
    
    this.betragTo = new DecimalInput(mySettings.getDouble("kontoauszug.list.betrag.to",Double.NaN), HBCI.DECIMALFORMAT);
    this.betragTo.setComment(HBCIProperties.CURRENCY_DEFAULT_DE);
    this.betragTo.addListener(this.listener);
    return this.betragTo;
  }

  /**
   * Liefert die Liste der Umsaetze basierend auf der aktuellen Auswahl.
   * @return Liste der Umsaetze.
   * @throws RemoteException
   */
  private synchronized List<Umsatz> getUmsaetze() throws RemoteException
  {
    Konto k           = (Konto) getKontoAuswahl().getValue();
    Date start        = (Date) getStart().getValue();
    Date end          = (Date) getEnd().getValue();
    String gkName     = (String) getGegenkontoName().getValue();
    String gkBLZ      = (String) getGegenkontoBLZ().getValue();
    String gkNummer   = (String) getGegenkontoNummer().getText();
    Double min        = (Double) getMindestBetrag().getValue();
    Double max        = (Double) getHoechstBetrag().getValue();
    String zk         = (String) getText().getValue();
    UmsatzTyp typ     = (UmsatzTyp) getKategorie().getValue();
    boolean unchecked = ((Boolean) getUnChecked().getValue()).booleanValue();
    
    DBIterator umsaetze = UmsatzUtil.getUmsaetzeBackwards();
    
    // BUGZILLA 449
    boolean hasFilter = false;

    /////////////////////////////////////////////////////////////////
    // Zeitraum
    // Der Warnhinweis wird nicht fuer den Zeitraum angezeigt, da der
    // immer vorhanden ist
    if (start != null) umsaetze.addFilter("valuta >= ?", new Object[]{new java.sql.Date(DateUtil.startOfDay(start).getTime())});
    if (end != null)   umsaetze.addFilter("valuta <= ?", new Object[]{new java.sql.Date(DateUtil.endOfDay(end).getTime())});
    /////////////////////////////////////////////////////////////////
    // Gegenkonto
    if (gkBLZ    != null && gkBLZ.length() > 0)    {umsaetze.addFilter("empfaenger_blz like ?",new Object[]{"%" + gkBLZ + "%"});hasFilter = true;}
    if (gkNummer != null && gkNummer.length() > 0) {umsaetze.addFilter("empfaenger_konto like ?",new Object[]{"%" + gkNummer + "%"});hasFilter = true;}
    if (gkName   != null && gkName.length() > 0)   {umsaetze.addFilter("LOWER(empfaenger_name) like ?",new Object[]{"%" + gkName.toLowerCase() + "%"});hasFilter = true;}
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Konto
    if (k != null) umsaetze.addFilter("konto_id = " + k.getID());
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Betrag
    if (min != null && !(Double.isNaN(min.doubleValue())))
    {
      umsaetze.addFilter("betrag >= ?",new Object[]{min});
      hasFilter = true;
    }
    if (max != null && (!Double.isNaN(max.doubleValue())))
    {
      umsaetze.addFilter("betrag <= ?",new Object[]{max});
      hasFilter = true;
    }
    /////////////////////////////////////////////////////////////////
    
    /////////////////////////////////////////////////////////////////
    // Zweck/Kommentar
    if (zk != null && zk.length() > 0) {
      zk = "%" + zk.toLowerCase() + "%";
      umsaetze.addFilter("(LOWER(zweck) like ? OR LOWER(zweck2) like ? OR LOWER(zweck3) like ? OR LOWER(kommentar) like ? OR LOWER(art) like ?)",new Object[]{zk,zk,zk,zk,zk});
    }
    /////////////////////////////////////////////////////////////////


    GUI.getView().setLogoText(hasFilter ? i18n.tr("Hinweis: Aufgrund von Suchkriterien werden m�glicherweise nicht alle Ums�tze angezeigt") : "");
    
    List<Umsatz> result = new LinkedList<Umsatz>();
    while (umsaetze.hasNext())
    {
      Umsatz u = (Umsatz) umsaetze.next();
      if (typ != null && !typ.matches(u))
        continue;
      
      if (unchecked)
      {
        // Nur ungepruefte anzeigen
        if ((u.getFlags() & Umsatz.FLAG_CHECKED) != 0)
          continue;
      }
      result.add(u);
    }
    return result;
  }

  /**
   * Startet den Export.
   */
  private synchronized void handlePrint()
  {
    try
    {
      // Vorher machen wir nochmal ein UNVERZOEGERTES Reload,
      // denn es muss sichergestellt sein, dass die Tabelle
      // aktuell ist, wenn wir als naechstes getItems()
      // aufrufen
      handleReload();

      // Wir laden die Umsaetze direkt aus der Tabelle.
      // Damit werden genau die ausgegeben, die gerade
      // angezeigt werden und wir sparen uns das erneute
      // Laden aus der Datenbank
      List list = getItems();

      if (list == null || list.size() == 0)
      {
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Keine zu exportierenden Ums�tze"), StatusBarMessage.TYPE_ERROR));
        return;
      }

      // Start- und End-Datum als Contextparameter an Exporter uebergeben
      Exporter.SESSION.put("pdf.start",getStart().getValue());
      Exporter.SESSION.put("pdf.end",getEnd().getValue());

      Umsatz[] u = (Umsatz[]) list.toArray(new Umsatz[list.size()]);
      new UmsatzExport().handleAction(u);
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getLocalizedMessage(),StatusBarMessage.TYPE_ERROR));
    }
    catch (RemoteException re)
    {
      Logger.error("error while reloading table",re);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Exportieren der Ums�tze"), StatusBarMessage.TYPE_ERROR));
    }
  }
  
  /**
   * Resettet alle Filter-Eingaben.
   */
  private synchronized void handleReset()
  {
    try
    {
      getStart().setValue(null);
      getEnd().setValue(null);
      getMindestBetrag().setValue(Double.NaN);
      getHoechstBetrag().setValue(Double.NaN);
      getKontoAuswahl().setValue(null);
      getKategorie().setValue(null);
      getGegenkontoNummer().setText("");
      getGegenkontoBLZ().setValue(null);
      getGegenkontoName().setValue(null);
      getText().setValue(null);
      getUnChecked().setValue(Boolean.FALSE);
      this.changed = true;
      handleReload();
    }
    catch (Exception e)
    {
      Logger.error("unable to reset filters",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Zur�cksetzen der Filter"), StatusBarMessage.TYPE_ERROR));
    }
  }

  /**
   * Aktualisiert die Tabelle der angezeigten Umsaetze.
   */
  private synchronized void handleReload()
  {
    if (!hasChanged() || disposed)
      return;
    
    GUI.startSync(new Runnable() // Sanduhr einblenden
    {
      public void run()
      {
        try
        {
          removeAll();
          
          List<Umsatz> list = getUmsaetze();
          for(Umsatz u:list)
            addItem(u);

          // Aktuelle Werte speichern
          try
          {
            // Nur fuer die Dauer der Sitzung speichern
            endDate = (Date) getEnd().getValue(); // BUGZILLA 951
            
            // Wir speichern hier alle eingegebenen Suchbegriffe fuer's naechste mal
            Date from         = (Date) getStart().getValue();
            Konto k           = (Konto) getKontoAuswahl().getValue();
            UmsatzTyp typ     = (UmsatzTyp) getKategorie().getValue();
            Double bFrom      = (Double) getMindestBetrag().getValue();
            Double bTo        = (Double) getHoechstBetrag().getValue();
            boolean unchecked = ((Boolean) getUnChecked().getValue()).booleanValue();
            mySettings.setAttribute("kontoauszug.list.from",from == null ? null : HBCI.DATEFORMAT.format(from));
            mySettings.setAttribute("kontoauszug.list.gegenkonto.nummer",getGegenkontoNummer().getText());
            mySettings.setAttribute("kontoauszug.list.gegenkonto.blz",(String) getGegenkontoBLZ().getValue());
            mySettings.setAttribute("kontoauszug.list.gegenkonto.name",(String) getGegenkontoName().getValue());
            mySettings.setAttribute("kontoauszug.list.konto",k == null ? null : k.getID());
            mySettings.setAttribute("kontoauszug.list.kategorie",typ == null ? null : typ.getID());
            mySettings.setAttribute("kontoauszug.list.text",(String) getText().getValue());
            mySettings.setAttribute("kontoauszug.list.betrag.from",bFrom == null ? Double.NaN : bFrom.doubleValue());
            mySettings.setAttribute("kontoauszug.list.betrag.to",bTo == null ? Double.NaN : bTo.doubleValue());
            mySettings.setAttribute("kontoauszug.list.unchecked",unchecked);
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
      boolean b = this.changed;
      this.changed = false;
      return b || getStart().hasChanged() ||
                getEnd().hasChanged() ||
                getUnChecked().hasChanged() ||
                getKontoAuswahl().hasChanged() ||
                getGegenkontoName().hasChanged() ||
                getGegenkontoNummer().hasChanged() ||
                getGegenkontoBLZ().hasChanged() ||
                getMindestBetrag().hasChanged() ||
                getHoechstBetrag().hasChanged() ||
                getKategorie().hasChanged() ||
                getText().hasChanged();
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
        getGegenkontoBLZ().setValue(address.getBlz());
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
 * Revision 1.40  2011-04-07 17:52:07  willuhn
 * @N BUGZILLA 1014
 *
 * Revision 1.39  2011-01-20 17:13:21  willuhn
 * @C HBCIProperties#startOfDay und HBCIProperties#endOfDay nach Jameica in DateUtil verschoben
 *
 * Revision 1.38  2011-01-11 22:44:40  willuhn
 * @N BUGZILLA 978
 *
 * Revision 1.37  2010-12-10 12:38:45  willuhn
 * *** empty log message ***
 *
 * Revision 1.36  2010-12-09 15:56:54  willuhn
 * @C Filter nach Kategorie
 *
 * Revision 1.35  2010-11-24 14:54:45  willuhn
 * @C BUGZILLA 951
 *
 * Revision 1.34  2010/06/01 12:12:19  willuhn
 * @C Umsaetze in "Kontoauszuege" und "Umsatze nach Kategorien" per Default in umgekehrt chronologischer Reihenfolge liefern - also neue zuerst
 *
 * Revision 1.33  2010/05/30 23:08:32  willuhn
 * @N Auch in Spalte "art" suchen (BUGZILLA 731)
 *
 * Revision 1.32  2009/10/20 23:12:58  willuhn
 * @N Support fuer SEPA-Ueberweisungen
 * @N Konten um IBAN und BIC erweitert
 *
 * Revision 1.31  2009/10/08 22:45:16  willuhn
 * @N Button "Geprueft" in Umsatz-Details, um einen Umsatz auch dort als geprueft markieren zu koennen
 * @N Button "Filter zuruecksetzen" in Kontoauszug
 *
 * Revision 1.30  2009/10/07 23:08:55  willuhn
 * @N BUGZILLA 745: Deaktivierte Konten in Auswertungen zwar noch anzeigen, jedoch mit "[]" umschlossen. Bei der Erstellung von neuen Auftraegen bleiben sie jedoch ausgeblendet. Bei der Gelegenheit wird das Default-Konto jetzt mit ">" markiert
 *
 * Revision 1.29  2009/09/23 11:47:58  willuhn
 * @N Auch im erweiterten Verwendungszweck suchen
 *
 * Revision 1.28  2009/07/16 10:41:05  willuhn
 * @C Bis-Datum wird nun gar nicht mehr gespeichert oder mit einem Wert vorausgefuellt
 *
 * Revision 1.27  2009/05/19 21:55:57  willuhn
 * @B Selektion und Markierung auch bei angepasster Sortierung wiederherstellen
 *
 * Revision 1.26  2009/05/11 14:39:54  willuhn
 * @C Es werden jetzt wieder alle Filterkriterien gespeichert, da auch ein Warnhinweis angezeigt wird, wenn Filter aktiv sind
 *
 * Revision 1.25  2009/05/08 14:22:55  willuhn
 * @C Default-Konto in Kontoauszuegen nicht verwenden - nervt hier eher nur
 *
 * Revision 1.24  2009/05/08 13:58:30  willuhn
 * @N Icons in allen Menus und auf allen Buttons
 * @N Fuer Umsatz-Kategorien koennen nun benutzerdefinierte Farben vergeben werden
 *
 * Revision 1.23  2009/05/06 23:11:22  willuhn
 * @N Mehr Icons auf Buttons
 *
 * Revision 1.22  2009/04/20 11:11:10  willuhn
 * @N BUGZILLA 723
 *
 * Revision 1.21  2009/04/20 11:07:06  willuhn
 * @N BUGZILLA 723
 *
 * Revision 1.20  2009/01/20 10:51:46  willuhn
 * @N Mehr Icons - fuer Buttons
 *
 * Revision 1.19  2009/01/12 00:46:50  willuhn
 * @N Vereinheitlichtes KontoInput in den Auswertungen
 *
 * Revision 1.18  2009/01/04 16:18:22  willuhn
 * @N BUGZILLA 404 - Kontoauswahl via SelectBox
 *
 * Revision 1.17  2008/11/17 23:29:59  willuhn
 * @C Aufrufe der depeicated BLZ-Funktionen angepasst
 *
 * Revision 1.16  2008/09/04 09:34:16  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2008/09/03 08:27:04  willuhn
 * @C Test - "selectAll" gegen "setSelection" ersetzt
 *
 * Revision 1.14  2008/09/02 08:55:47  willuhn
 * @N Beei FocusOut Min-Betrag in Max-Betrag uebernehmen, wenn dort noch nichts drin steht
 *
 * Revision 1.13  2008/08/31 13:59:54  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2008/08/31 13:50:42  willuhn
 * @N Automatische Betragsformatierung
 *
 * Revision 1.11  2008/04/06 23:21:43  willuhn
 * @C Bug 575
 * @N Der Vereinheitlichung wegen alle Buttons in den Auswertungen nach oben verschoben. Sie sind dann naeher an den Filter-Controls -> ergonomischer
 *
 * Revision 1.10  2007/08/23 12:37:32  willuhn
 * @N Neues Filterkriterium "Verwendungszweck/Kommentar"
 * @C "Betrag von", "Betrag bis" nicht mehr speichern -> zu verwirrend
 *
 * Revision 1.9  2007/08/09 12:19:55  willuhn
 * @N Bug 449 - Filterkriterien auf dem ersten Tab werden fuer Warnhinweis nicht beruecksichtigt weil sie offensichtlich sind
 *
 * Revision 1.8  2007/08/09 12:04:39  willuhn
 * @N Bug 302
 *
 * Revision 1.7  2007/08/09 11:38:59  willuhn
 * @N Bug 449
 *
 * Revision 1.6  2007/08/09 11:01:24  willuhn
 * @B Bug 462
 *
 * Revision 1.5  2007/08/07 23:54:15  willuhn
 * @B Bug 394 - Erster Versuch. An einigen Stellen (z.Bsp. konto.getAnfangsSaldo) war ich mir noch nicht sicher. Heiner?
 *
 * Revision 1.4  2007/05/02 13:01:12  willuhn
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