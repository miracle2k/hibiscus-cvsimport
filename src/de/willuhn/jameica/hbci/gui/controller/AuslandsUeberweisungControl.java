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

package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;
import java.util.Date;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.AuslandsUeberweisungNew;
import de.willuhn.jameica.hbci.gui.action.EmpfaengerAdd;
import de.willuhn.jameica.hbci.gui.filter.AddressFilter;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.AddressInput;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.gui.parts.AuslandsUeberweisungList;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.rmi.HibiscusTransfer;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller fuer Auslandsueberweisungen.
 */
public class AuslandsUeberweisungControl extends AbstractControl
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private AuslandsUeberweisungList list      = null;

  private AuslandsUeberweisung transfer      = null;
  
  // Eingabe-Felder
  private Input kontoAuswahl                 = null;
  private Input betrag                       = null;
  private TextInput zweck                    = null;

  private AddressInput empfName              = null;
  private TextInput empfkto                  = null;
  private TextInput bic                      = null;

  private DateInput termin                   = null;

  private CheckboxInput storeEmpfaenger      = null;
  
  
  /**
   * ct.
   * @param view
   */
  public AuslandsUeberweisungControl(AbstractView view)
  {
    super(view);
  }
  
  /**
   * @return der Auftrag
   * @throws RemoteException
   */
  public AuslandsUeberweisung getTransfer() throws RemoteException
  {
    if (this.transfer != null)
      return this.transfer;
    
    Object o = getCurrentObject();
    if (o instanceof AuslandsUeberweisung)
    {
      this.transfer = (AuslandsUeberweisung) o;
      return this.transfer;
    }
    
    this.transfer = (AuslandsUeberweisung) Settings.getDBService().createObject(AuslandsUeberweisung.class,null);
    return this.transfer;
  }
  
  /**
   * Liefert die Liste der Auslandsueberweisungen.
   * @return Liste der Auslandsueberweisungen.
   * @throws RemoteException
   */
  public AuslandsUeberweisungList getAuslandsUeberweisungListe() throws RemoteException
  {
    if (this.list == null)
      this.list = new AuslandsUeberweisungList(new AuslandsUeberweisungNew());
    return this.list;
  }

  /**
   * Liefert ein Auswahlfeld fuer das Konto.
   * @return Auswahl-Feld.
   * @throws RemoteException
   */
  public Input getKontoAuswahl() throws RemoteException
  {
    if (this.kontoAuswahl != null)
      return this.kontoAuswahl;
    
    KontoListener kl = new KontoListener();
    this.kontoAuswahl = new KontoInput(getTransfer().getKonto(),KontoFilter.FOREIGN);
    this.kontoAuswahl.setMandatory(true);
    this.kontoAuswahl.addListener(kl);
    this.kontoAuswahl.setEnabled(!getTransfer().ausgefuehrt());

    // einmal ausloesen
    kl.handleEvent(null);

    return this.kontoAuswahl;
  }
  
  /**
   * Liefert das Eingabe-Feld fuer den Empfaenger-Namen.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public AddressInput getEmpfaengerName() throws RemoteException
  {
    if (empfName != null)
      return empfName;
    empfName = new AddressInput(getTransfer().getGegenkontoName(), AddressFilter.FOREIGN);
    empfName.setMandatory(true);
    empfName.addListener(new EmpfaengerListener());
    empfName.setEnabled(!getTransfer().ausgefuehrt());
    return empfName;
  }

  
  /**
   * Liefert das Eingabe-Feld fuer den Empfaenger.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public TextInput getEmpfaengerKonto() throws RemoteException
  {
    if (empfkto != null)
      return empfkto;

    empfkto = new TextInput(getTransfer().getGegenkontoNummer(),HBCIProperties.HBCI_IBAN_MAXLENGTH + 5); // max. 5 Leerzeichen
    empfkto.setValidChars(HBCIProperties.HBCI_IBAN_VALIDCHARS + " ");
    empfkto.setMandatory(true);
    empfkto.setEnabled(!getTransfer().ausgefuehrt());
    empfkto.addListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        String s = (String) empfkto.getValue();
        if (s == null || s.length() == 0 || s.indexOf(" ") == -1)
          return;
        empfkto.setValue(s.replaceAll(" ",""));
      }
    });
    return empfkto;
  }

  /**
   * Liefert das Eingabe-Feld fuer die BIC.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getEmpfaengerBic() throws RemoteException
  {
    if (this.bic == null)
    {
      this.bic = new TextInput(getTransfer().getGegenkontoBLZ(),HBCIProperties.HBCI_BIC_MAXLENGTH);
      this.bic.setValidChars(HBCIProperties.HBCI_BIC_VALIDCHARS);
      this.bic.setEnabled(!getTransfer().ausgefuehrt());
      this.bic.setMandatory(true);
    }
    return this.bic;
  }

  /**
   * Liefert das Eingabe-Feld fuer den Verwendungszweck.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getZweck() throws RemoteException
  {
    if (zweck != null)
      return zweck;
    zweck = new TextInput(getTransfer().getZweck(),HBCIProperties.HBCI_FOREIGNTRANSFER_USAGE_MAXLENGTH);
    zweck.setValidChars(HBCIProperties.HBCI_DTAUS_VALIDCHARS);
    zweck.setEnabled(!getTransfer().ausgefuehrt());
    return zweck;
  }

  /**
   * Liefert das Eingabe-Feld fuer den Betrag.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getBetrag() throws RemoteException
  {
    if (betrag != null)
      return betrag;
    HibiscusTransfer t = getTransfer();
    double d = t.getBetrag();
    if (d == 0.0d) d = Double.NaN;
    betrag = new DecimalInput(d,HBCI.DECIMALFORMAT);

    Konto k = t.getKonto();
    betrag.setComment(k == null ? "" : k.getWaehrung());
    betrag.setMandatory(true);
    betrag.setEnabled(!getTransfer().ausgefuehrt());
    
    new KontoListener().handleEvent(null);

    return betrag;
  }

  /**
   * Liefert das Eingabe-Feld fuer den Termin.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public DateInput getTermin() throws RemoteException
  {
    final Terminable bu = (Terminable) getTransfer();

    if (termin != null)
      return termin;
    
    Date d = bu.getTermin();
    if (d == null)
      d = new Date();

    this.termin = new DateInput(d,HBCI.DATEFORMAT);
    this.termin.setEnabled(!bu.ausgefuehrt());
    this.termin.setTitle(i18n.tr("Termin"));
    this.termin.setText(i18n.tr("Bitte w�hlen Sie einen Termin"));

    if (bu.ausgefuehrt())
      this.termin.setComment(i18n.tr("Der Auftrag wurde bereits ausgef�hrt"));
    else if (bu.ueberfaellig())
      this.termin.setComment(i18n.tr("Der Auftrag ist �berf�llig"));
    else
      this.termin.setComment(""); // Platzhalter

    this.termin.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        try {
          Date date = (Date) termin.getValue();
          if (date != null && !date.after(new Date()))
            termin.setComment(i18n.tr("Der Auftrag ist �berf�llig"));
          else
            termin.setComment("");
        }
        catch (Exception e) {
          Logger.error("unable to check overdue",e);
        }
      }
    });


    return termin;
  }

  /**
   * Liefert eine CheckBox ueber die ausgewaehlt werden kann,
   * ob der Empfaenger mitgespeichert werden soll.
   * @return CheckBox.
   * @throws RemoteException
   */
  public CheckboxInput getStoreEmpfaenger() throws RemoteException
  {
    if (storeEmpfaenger != null)
      return storeEmpfaenger;

    // Nur bei neuen Transfers aktivieren
    HibiscusTransfer t = getTransfer();
    // Checkbox nur setzen, wenn es eine neue Ueberweisung ist und
    // noch kein Gegenkonto definiert ist.
    boolean enabled = t.isNewObject() && t.getGegenkontoNummer() == null;
    
    // Per Hidden-Parameter kann die Checkbox komplett ausgeschaltet werden
    de.willuhn.jameica.system.Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
    enabled &= settings.getBoolean("transfer.addressbook.autoadd",true);
    storeEmpfaenger = new CheckboxInput(enabled);

    return storeEmpfaenger;
  }

  /**
   * Speichert den Geld-Transfer.
   * @return true, wenn das Speichern erfolgreich war, sonst false.
   */
  public synchronized boolean handleStore()
  {
    try {
      
      getTransfer().transactionBegin();

      Double d = (Double) getBetrag().getValue();
      getTransfer().setBetrag(d == null ? Double.NaN : d.doubleValue());
      
      getTransfer().setKonto((Konto)getKontoAuswahl().getValue());
      getTransfer().setZweck((String)getZweck().getValue());

      String kto  = (String)getEmpfaengerKonto().getValue();
      String name = getEmpfaengerName().getText();
      String bic  = (String) getEmpfaengerBic().getValue();

      getTransfer().setGegenkontoNummer(kto);
      getTransfer().setGegenkontoName(name);
      getTransfer().setGegenkontoBLZ(bic);

      Date termin = (Date) getTermin().getValue();
      if (termin == null)
      {
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Bitte geben Sie einen Termin ein."),StatusBarMessage.TYPE_ERROR));
        return false;
      }
      getTransfer().setTermin(termin);
      

      getTransfer().store();
      
      Boolean store = (Boolean) getStoreEmpfaenger().getValue();
      if (store.booleanValue())
      {
        HibiscusAddress e = (HibiscusAddress) Settings.getDBService().createObject(HibiscusAddress.class,null);
        e.setIban(kto);
        e.setName(name);
        e.setBic(bic);
        
        // Zu schauen, ob die Adresse bereits existiert, ueberlassen wir der Action
        new EmpfaengerAdd().handleAction(e);
      }
      GUI.getStatusBar().setSuccessText(i18n.tr("Auftrag gespeichert"));
      getTransfer().transactionCommit();

      if (getTransfer().getBetrag() > Settings.getUeberweisungLimit())
        GUI.getView().setErrorText(i18n.tr("Warnung: Auftragslimit �berschritten: {0} ", HBCI.DECIMALFORMAT.format(Settings.getUeberweisungLimit()) + " " + getTransfer().getKonto().getWaehrung()));
      
      return true;
    }
    catch (ApplicationException e)
    {
      try {
        getTransfer().transactionRollback();
      }
      catch (RemoteException re)
      {
        Logger.error("rollback failed",re);
      }
      GUI.getView().setErrorText(i18n.tr(e.getMessage()));
    }
    catch (Exception e2)
    {
      try {
        getTransfer().transactionRollback();
      }
      catch (RemoteException re)
      {
        Logger.error("rollback failed",re);
      }
      Logger.error("error while storing transfer",e2);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern des Auftrags"));
    }
    return false;
  }

  /**
   * Listener, der die Auswahl des Kontos ueberwacht und die Waehrungsbezeichnung
   * hinter dem Betrag abhaengig vom ausgewaehlten Konto anpasst.
   */
  private class KontoListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event) {

      try {
        Object o = getKontoAuswahl().getValue();
        if (o == null || !(o instanceof Konto))
        {
          getBetrag().setComment("");
          return;
        }

        Konto konto = (Konto) o;
        getBetrag().setComment(konto.getWaehrung());

        // Wird u.a. benoetigt, damit anhand des Auftrages ermittelt werden
        // kann, wieviele Zeilen Verwendungszweck jetzt moeglich sind
        getTransfer().setKonto(konto);
      }
      catch (RemoteException er)
      {
        Logger.error("error while updating currency",er);
        GUI.getStatusBar().setErrorText(i18n.tr("Fehler bei Ermittlung der W�hrung"));
      }
    }
  }

  /**
   * Listener, der bei Auswahl des Empfaengers die restlichen Daten vervollstaendigt.
   */
  private class EmpfaengerListener implements Listener
  {

    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event) {
      if (event == null)
        return;
      
      if (!(event.data instanceof Address))
        return;
      
      Address a = (Address) event.data;

      try {
        getEmpfaengerName().setText(a.getName());
        getEmpfaengerKonto().setValue(a.getIban());
        getEmpfaengerBic().setValue(a.getBic());

        // Wenn der Empfaenger aus dem Adressbuch kommt, deaktivieren wir die Checkbox
        getStoreEmpfaenger().setValue(Boolean.FALSE);
        
        try
        {
          String zweck = (String) getZweck().getValue();
          if ((zweck != null && zweck.length() > 0))
            return;
          
          DBIterator list = getTransfer().getList();
          list.addFilter("empfaenger_konto = ?",new Object[]{a.getKontonummer()});
          list.setOrder("order by id desc");
          if (list.hasNext())
          {
            HibiscusTransfer t = (HibiscusTransfer) list.next();
            getZweck().setValue(t.getZweck());
          }
        }
        catch (Exception e)
        {
          Logger.error("unable to autocomplete subject",e);
        }

          
      }
      catch (RemoteException er)
      {
        Logger.error("error while choosing empfaenger",er);
        GUI.getStatusBar().setErrorText(i18n.tr("Fehler bei der Auswahl des Empf�ngers"));
      }
    }
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.11  2011-04-11 14:36:38  willuhn
 * @N Druck-Support fuer Lastschriften und SEPA-Ueberweisungen
 *
 * Revision 1.10  2011-04-07 17:52:07  willuhn
 * @N BUGZILLA 1014
 *
 * Revision 1.9  2010/04/14 17:44:10  willuhn
 * @N BUGZILLA 83
 *
 * Revision 1.8  2010/04/11 22:05:40  willuhn
 * @N virtuelle Konto-Adressen in SEPA-Auftraegen beruecksichtigen
 *
 * Revision 1.7  2010/04/05 21:19:34  willuhn
 * @N Leerzeichen in IBAN zulassen - und nach Eingabe automatisch abschneiden (wie bei BLZ) - siehe http://www.willuhn.de/blog/index.php?/archives/506-Beta-Phase-fuer-Jameica-1.9Hibiscus-1.11-eroeffnet.html#c1079
 *
 * Revision 1.6  2009/10/29 12:26:04  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2009/10/20 23:12:58  willuhn
 * @N Support fuer SEPA-Ueberweisungen
 * @N Konten um IBAN und BIC erweitert
 *
 * Revision 1.4  2009/10/07 23:08:56  willuhn
 * @N BUGZILLA 745: Deaktivierte Konten in Auswertungen zwar noch anzeigen, jedoch mit "[]" umschlossen. Bei der Erstellung von neuen Auftraegen bleiben sie jedoch ausgeblendet. Bei der Gelegenheit wird das Default-Konto jetzt mit ">" markiert
 *
 * Revision 1.3  2009/05/07 15:13:37  willuhn
 * @N BIC in Auslandsueberweisung
 *
 * Revision 1.2  2009/03/17 23:44:14  willuhn
 * @N BUGZILLA 159 - Auslandsueberweisungen. Erste Version
 *
 * Revision 1.1  2009/03/13 00:25:12  willuhn
 * @N Code fuer Auslandsueberweisungen fast fertig
 *
 **********************************************************************/
