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
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;
import java.util.Date;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.KontoFetchFromPassport;
import de.willuhn.jameica.hbci.gui.action.KontoNew;
import de.willuhn.jameica.hbci.gui.action.UmsatzDetail;
import de.willuhn.jameica.hbci.gui.dialogs.PassportAuswahlDialog;
import de.willuhn.jameica.hbci.gui.dialogs.SynchronizeOptionsDialog;
import de.willuhn.jameica.hbci.gui.input.BLZInput;
import de.willuhn.jameica.hbci.gui.input.PassportInput;
import de.willuhn.jameica.hbci.gui.parts.ProtokollList;
import de.willuhn.jameica.hbci.gui.parts.UmsatzChart;
import de.willuhn.jameica.hbci.gui.parts.UmsatzList;
import de.willuhn.jameica.hbci.messaging.SaldoMessage;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller der fuer den Dialog "Bankverbindungen" zustaendig ist.
 */
public class KontoControl extends AbstractControl {

	// Fachobjekte
	private Konto konto 			 		= null;
	
	// Eingabe-Felder
	private TextInput kontonummer  		= null;
  private TextInput unterkonto      = null;
	private TextInput blz          		= null;
	private Input name				 		    = null;
	private Input bezeichnung	 		    = null;
	private Input passportAuswahl     = null;
  private Input kundennummer 		    = null;
  private Input kommentar           = null;
  
  private TextInput bic             = null;
  private TextInput iban            = null;
  
  private LabelInput saldo			        = null;
  private SaldoMessageConsumer consumer = null;
  
  private Button synchronizeOptions = null;

	private TablePart kontoList						= null;
	private TablePart protokoll						= null;
  private UmsatzList umsatzList         = null;
  private UmsatzChart umsatzChart       = null;

	private I18N i18n;

  /**
   * ct.
   * @param view
   */
  public KontoControl(AbstractView view) {
    super(view);
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

	/**
	 * Liefert die aktuelle Bankverbindung.
   * @return Bankverbindung.
   * @throws RemoteException
   */
  public Konto getKonto() throws RemoteException
	{
		if (konto != null)
			return konto;
		
		try {
			konto = (Konto) getCurrentObject();
			if (konto != null)
				return konto;
		}
		catch (ClassCastException e)
		{
			// Falls wir von 'nem anderen Dialog kommen, kann es durchaus sein,
			// das getCurrentObject() was falsches liefert. Das ist aber nicht
			// weiter schlimm. Wir erstellen dann einfach ein neues.
		}
		
		// Kein Konto verfuegbar - wir bauen ein neues.
		konto = (Konto) Settings.getDBService().createObject(Konto.class,null);
		return konto;
	}

	/**
	 * Liefert eine Tabelle mit dem Protokoll des Kontos.
   * @return Tabelle.
   * @throws RemoteException
   */
  public Part getProtokoll() throws RemoteException
	{
		if (protokoll != null)
			return protokoll;

		protokoll = new ProtokollList(getKonto(),null);
		return protokoll;
	}

  /**
   * Liefert eine Tabelle mit den Umsaetzen des Kontos.
   * @return Tabelle.
   * @throws RemoteException
   */
  public Part getUmsatzList() throws RemoteException
  {
    if (umsatzList != null)
      return umsatzList;

    umsatzList = new UmsatzList(getKonto(),HBCIProperties.UMSATZ_DEFAULT_DAYS,new UmsatzDetail());
    umsatzList.setFilterVisible(false);
    return umsatzList;
  }

  /**
   * Liefert einen Chart mit den Umsaetzen des Kontos.
   * @return Tabelle.
   * @throws RemoteException
   */
  public Part getUmsatzChart() throws RemoteException
  {
    if (umsatzChart != null)
      return umsatzChart;

    umsatzChart = new UmsatzChart(getKonto());
    return umsatzChart;
  }

  /**
	 * Liefert das Eingabe-Feld fuer die Kontonummer.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getKontonummer() throws RemoteException
	{
		if (kontonummer != null)
			return kontonummer;
		kontonummer = new TextInput(getKonto().getKontonummer(),HBCIProperties.HBCI_KTO_MAXLENGTH_HARD);
    // BUGZILLA 280
    kontonummer.setValidChars(HBCIProperties.HBCI_KTO_VALIDCHARS);
    kontonummer.setMandatory(true);
		return kontonummer;
	}

  /**
   * Liefert das Eingabe-Feld fuer die Unterkontonummer.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getUnterkonto() throws RemoteException
  {
    if (unterkonto != null)
      return unterkonto;
    unterkonto = new TextInput(getKonto().getUnterkonto(),HBCIProperties.HBCI_KTO_MAXLENGTH_HARD);
    unterkonto.setValidChars(HBCIProperties.HBCI_KTO_VALIDCHARS);
    unterkonto.setComment(i18n.tr("Kann meist frei gelassen werden"));
    return unterkonto;
  }

  /**
   * Liefert einen Button, ueber den die Synchronisierungsdetails konfiguriert
   * werden.
   * @return Button.
   * @throws RemoteException
   */
  public Button getSynchronizeOptions() throws RemoteException
  {
    if (this.synchronizeOptions != null)
      return this.synchronizeOptions;

    this.synchronizeOptions = new Button(i18n.tr("Synchronisierungsoptionen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          SynchronizeOptionsDialog d = new SynchronizeOptionsDialog(getKonto(),SynchronizeOptionsDialog.POSITION_CENTER);
          d.open();
        }
        catch (OperationCanceledException oce)
        {
          // ignore
        }
        catch (ApplicationException ae)
        {
          throw ae;
        }
        catch (Exception e)
        {
          Logger.error("unable to configure synchronize options");
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Konfigurieren der Synchronisierungsoptionen"),StatusBarMessage.TYPE_ERROR));
        }
        
      }
    },getKonto(),false,"document-properties.png");
    this.synchronizeOptions.setEnabled((getKonto().getFlags() & Konto.FLAG_DISABLED) != Konto.FLAG_DISABLED);
    return this.synchronizeOptions;
  }
  
  /**
	 * Liefert das Eingabe-Feld fuer die Bankleitzahl.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getBlz() throws RemoteException
	{
		if (blz != null)
			return blz;
		blz = new BLZInput(getKonto().getBLZ());
    blz.setMandatory(true);
    blz.addListener(new Listener()
    {
      /**
       * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
       */
      public void handleEvent(Event event)
      {
        completeBic();
      }
    });
		return blz;
	}

	/**
	 * Liefert den Namen des Konto-Inhabers.
   * @return Name des Konto-Inhabers.
   * @throws RemoteException
   */
  public Input getName() throws RemoteException
	{
		if (name != null)
			return name;
		name = new TextInput(getKonto().getName(),HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH);
    name.setMandatory(true);
		return name;
	}

	/**
	 * Liefert die Bezeichnung des Kontos.
	 * @return Bezeichnung des Kontos.
	 * @throws RemoteException
	 */
	public Input getBezeichnung() throws RemoteException
	{
		if (bezeichnung != null)
			return bezeichnung;
		bezeichnung = new TextInput(getKonto().getBezeichnung(),255);
		return bezeichnung;
	}

	/**
	 * Liefert das Eingabefeld fuer die Kundennummer.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getKundennummer() throws RemoteException
	{
		if (kundennummer != null)
			return kundennummer;
		kundennummer = new TextInput(getKonto().getKundennummer());
    kundennummer.setMandatory(true);
		return kundennummer;
	}

	/**
	 * Liefert das Auswahl-Feld fuer das Sicherheitsmedium.
   * @return Eingabe-Feld.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public Input getPassportAuswahl() throws RemoteException, ApplicationException
	{
		if (passportAuswahl != null)
			return passportAuswahl;

		passportAuswahl = new PassportInput(getKonto());
		return passportAuswahl;
	}

	/**
	 * Liefert ein Feld zur Anzeige des Saldos.
   * @return Anzeige-Feld.
   * @throws RemoteException
   */
  public Input getSaldo() throws RemoteException
	{
		if (saldo != null)
			return saldo;
			
		saldo = new LabelInput("");
    saldo.setComment(""); // Platz fuer Kommentar reservieren
    // Einmal ausloesen, damit das Feld mit Inhalt gefuellt wird.
    this.consumer = new SaldoMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.consumer);
    Application.getMessagingFactory().sendMessage(new SaldoMessage(getKonto()));
    return saldo;
	}
  
  /**
   * Liefert ein Eingabe-Feld fuer einen Kommentar.
   * @return Kommentar.
   * @throws RemoteException
   */
  public Input getKommentar() throws RemoteException
  {
    if (this.kommentar != null)
      return this.kommentar;
    this.kommentar = new TextAreaInput(getKonto().getKommentar());
    return this.kommentar;
  }

  /**
   * Liefert das Eingabe-Feld fuer die IBAN.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getIban() throws RemoteException
  {
    if (this.iban == null)
    {
      this.iban = new TextInput(getKonto().getIban(),HBCIProperties.HBCI_IBAN_MAXLENGTH + 5); // max. 5 Leerzeichen
      this.iban.setValidChars(HBCIProperties.HBCI_IBAN_VALIDCHARS + " ");
      this.iban.addListener(new Listener()
      {
        public void handleEvent(Event event)
        {
          String s = (String) iban.getValue();
          if (s == null || s.length() == 0 || s.indexOf(" ") == -1)
            return;
          iban.setValue(s.replaceAll(" ",""));
        }
      });
    }
    return this.iban;
  }

  /**
   * Liefert das Eingabe-Feld fuer die BIC.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getBic() throws RemoteException
  {
    if (this.bic == null)
    {
      this.bic = new TextInput(getKonto().getBic(),HBCIProperties.HBCI_BIC_MAXLENGTH);
      this.bic.setValidChars(HBCIProperties.HBCI_BIC_VALIDCHARS);
      this.completeBic();
    }
    return this.bic;
  }

  /**
   * Liefert einen Saldo-MessageConsumer.
   * @return Consumer.
   */
  public MessageConsumer getSaldoMessageConsumer()
  {
    return this.consumer;
  }
  
  /**
	 * Liefert eine Tabelle mit allen vorhandenen Bankverbindungen.
   * @return Tabelle mit Bankverbindungen.
   * @throws RemoteException
   */
  public Part getKontoListe() throws RemoteException
	{
		if (kontoList != null)
			return kontoList;

    kontoList = new de.willuhn.jameica.hbci.gui.parts.KontoList(new KontoNew());
    // BUGZILLA 108 http://www.willuhn.de/bugzilla/show_bug.cgi?id=108
    kontoList.addColumn(i18n.tr("Saldo aktualisiert am"),"saldo_datum", new DateFormatter(HBCI.LONGDATEFORMAT));
    // BUGZILLA 81 http://www.willuhn.de/bugzilla/show_bug.cgi?id=81
    kontoList.addColumn(i18n.tr("Ums�tze"),"numumsaetze");
		return kontoList;
	}

  /**
   * Speichert das Konto.
   */
  public synchronized void handleStore() {
		try {

			Passport p = (Passport) getPassportAuswahl().getValue();

			if (p == null)
			{
			  Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Bitte w�hlen Sie ein Sicherheitsmedium aus"), StatusBarMessage.TYPE_ERROR));
				return;
			}
			
			getKonto().setPassportClass(p.getClass().getName());

			getKonto().setKontonummer((String)getKontonummer().getValue());
      getKonto().setUnterkonto((String)getUnterkonto().getValue());
			getKonto().setBLZ((String)getBlz().getValue());
			getKonto().setName((String)getName().getValue());
			getKonto().setBezeichnung((String)getBezeichnung().getValue());
      getKonto().setKundennummer((String)getKundennummer().getValue());
      getKonto().setKommentar((String) getKommentar().getValue());
      getKonto().setIban((String)getIban().getValue());
      getKonto().setBic((String)getBic().getValue());
      
      // und jetzt speichern wir.
			getKonto().store();
			GUI.getStatusBar().setSuccessText(i18n.tr("Bankverbindung gespeichert."));
      GUI.getView().setSuccessText("");
		}
		catch (ApplicationException e1)
		{
			GUI.getView().setErrorText(i18n.tr(e1.getLocalizedMessage()));
		}
		catch (RemoteException e)
		{
			Logger.error("unable to store konto",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern der Bankverbindung."));
		}

  }

	/**
   * Liest alle ueber das Sicherheitsmedium verfuegbaren Konten
   * aus und speichert sie (insofern Konten mit identischer kto-Nummer/BLZ nicht schon existieren).
   */
  public synchronized void handleReadFromPassport()
	{

		try 
		{
      PassportAuswahlDialog d = new PassportAuswahlDialog(PassportAuswahlDialog.POSITION_CENTER);
      Passport p = (Passport) d.open();

      new KontoFetchFromPassport().handleAction(p);
		}
    catch (OperationCanceledException oce)
    {
      // ignore
    }
		catch (ApplicationException ae)
		{
			GUI.getStatusBar().setErrorText(ae.getMessage());
		}
		catch (Exception e)
		{
			Logger.error("error while reading passport from select box",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Auslesen der Konto-Informationen"));
		}
	}

  /**
   * Laedt die Tabelle mit den Umsaetzen neu.
   */
  public void handleReload()
  {
    GUI.startSync(new Runnable() {
      public void run()
      {
        try
        {
          UmsatzList list = ((UmsatzList)getUmsatzList());
          list.removeAll();
          Konto k = getKonto();
          DBIterator i = k.getUmsaetze(HBCIProperties.UMSATZ_DEFAULT_DAYS);
          while (i.hasNext())
            list.addItem(i.next());
          list.sort();
          Application.getMessagingFactory().sendMessage(new SaldoMessage(getKonto()));
        }
        catch (IllegalArgumentException iae)
        {
          // Fliegt, wenn der Dialog zwischenzeitlich verlassen
          // wurde und die Tabelle disposed ist.
          // Dann brechen wir ab und ignorieren den Fehler.
          Logger.warn("umsatz table has be disposed in the meantime, skip reload");
          return;
        }
        catch (RemoteException e)
        {
          Logger.error("error while reloading umsatz list",e);
        }
      }
    });
  }
  
  /**
   * Auto-vervollstaendigt die BIC, falls sie noch nicht eingegeben wurde aber die BLZ bekannt ist.
   */
  private void completeBic()
  {
    try
    {
      String bic = (String) getBic().getValue();
      if (bic != null && bic.length() > 0)
        return; // schon eingegeben
      
      String blz = (String) getBlz().getValue();
      if (blz == null || blz.length() == 0)
        return; // keine BLZ bekannt
      
      bic = HBCIUtils.getBICForBLZ(blz);
      if (bic != null && bic.length() > 0)
        getBic().setValue(bic);
    }
    catch (Exception e)
    {
      Logger.write(Level.WARN,"unable to autocomplete BIC",e);
    }
  }

  /**
   * Wird beim Eintreffen neuer Salden benachrichtigt und aktualisiert ggf die Anzeige.
   */
  private class SaldoMessageConsumer implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{SaldoMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(final Message message) throws Exception
    {
      GUI.getDisplay().syncExec(new Runnable() {
        
        public void run()
        {
          try
          {
            if (saldo == null)
            {
              // Eingabe-Feld existiert nicht. Also abmelden
              Application.getMessagingFactory().unRegisterMessageConsumer(SaldoMessageConsumer.this);
              return;
            }
            
            SaldoMessage msg = (SaldoMessage) message;
            Konto k = (Konto)msg.getObject();
            if (k == null || !k.equals(getKonto()))
              return; // Kein Konto oder nicht unseres

            double s = k.getSaldo();
            Date   d = k.getSaldoDatum();
            if (d == null)
            {
              // Kein Datum, kein Saldo
              saldo.setValue("");
              saldo.setComment(i18n.tr("noch kein Saldo abgerufen"));
            }
            else
            {
              saldo.setValue(HBCI.DECIMALFORMAT.format(s) + " " + getKonto().getWaehrung());
              if (s < 0)
                saldo.setColor(Color.ERROR);
              else if (s > 0)
                saldo.setColor(Color.SUCCESS);
              else
                saldo.setColor(Color.WIDGET_FG);

              saldo.setComment(i18n.tr("vom {0}",HBCI.LONGDATEFORMAT.format(d)));
            }
          }
          catch (Exception e)
          {
            // Wenn hier ein Fehler auftrat, deregistrieren wir uns wieder
            Logger.error("unable to refresh saldo",e);
            Application.getMessagingFactory().unRegisterMessageConsumer(SaldoMessageConsumer.this);
          }
        }
      
      });
    }
    
  }


}


/**********************************************************************
 * $Log$
 * Revision 1.86  2010-04-14 17:09:56  willuhn
 * @C Max. 255 Zeichen zulassen - BUGZILLA 567
 *
 * Revision 1.85  2010/04/05 21:19:34  willuhn
 * @N Leerzeichen in IBAN zulassen - und nach Eingabe automatisch abschneiden (wie bei BLZ) - siehe http://www.willuhn.de/blog/index.php?/archives/506-Beta-Phase-fuer-Jameica-1.9Hibiscus-1.11-eroeffnet.html#c1079
 *
 * Revision 1.84  2009/10/20 23:12:58  willuhn
 * @N Support fuer SEPA-Ueberweisungen
 * @N Konten um IBAN und BIC erweitert
 *
 * Revision 1.83  2009/09/15 00:23:34  willuhn
 * @N BUGZILLA 745
 *
 * Revision 1.82  2009/05/07 13:36:57  willuhn
 * @R Hilfsobjekt "PassportObject" entfernt
 * @C Cleanup in PassportInput (insb. der weisse Hintergrund hinter dem "Konfigurieren..."-Button hat gestoert
 *
 * Revision 1.81  2009/01/26 23:17:46  willuhn
 * @R Feld "synchronize" aus Konto-Tabelle entfernt. Aufgrund der Synchronize-Optionen pro Konto ist die Information redundant und ergibt sich implizit, wenn fuer ein Konto irgendeine der Synchronisations-Optionen aktiviert ist
 *
 * Revision 1.80  2009/01/20 10:51:46  willuhn
 * @N Mehr Icons - fuer Buttons
 *
 * Revision 1.79  2009/01/04 17:43:30  willuhn
 * @N BUGZILLA 532
 **********************************************************************/