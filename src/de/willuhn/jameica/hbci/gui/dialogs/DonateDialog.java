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

package de.willuhn.jameica.hbci.gui.dialogs;

import java.rmi.RemoteException;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zur Erzeugung einer Spende.
 */
public class DonateDialog extends AbstractDialog
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private final static int WINDOW_WIDTH = 470;

  private DialogInput kontoAuswahl  = null;
  private Input betrag              = null;
  private SelectInput empfaenger    = null;
  private CheckboxInput bill        = null;
  private Input email               = null;
  
  private Ueberweisung ueberweisung = null;
  
  /**
   * @param position
   */
  public DonateDialog(int position)
  {
    super(position);
    setTitle(i18n.tr("Spenden"));
    setSize(WINDOW_WIDTH,SWT.DEFAULT);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    // BUGZILLA 577
    ArrayList list = new ArrayList();

    final HibiscusAddress hibiscus = (HibiscusAddress) Settings.getDBService().createObject(HibiscusAddress.class,null);
    hibiscus.setBlz(new String(new char[]{'5','0','5','3','0','0','0','0'}));
    hibiscus.setKontonummer(new String(new char[]{'3','2','5','4','0','6'}));
    hibiscus.setName("Olaf Willuhn");
    hibiscus.setKommentar("Autor Hibiscus");
    list.add(hibiscus);
    
    final HibiscusAddress hbci4java = (HibiscusAddress) Settings.getDBService().createObject(HibiscusAddress.class,null);
    hbci4java.setBlz(new String(new char[]{'8','6','0','5','5','5','9','2'}));
    hbci4java.setKontonummer(new String(new char[]{'1','8','0','0','2','1','4','2','1','5'}));
    hbci4java.setName("Stefan Palme");
    hbci4java.setKommentar("Autor HBCI4Java");
    list.add(hbci4java);

    empfaenger = new SelectInput(list,null);
    empfaenger.setComment(hibiscus.getKommentar());
    empfaenger.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        try
        {
          HibiscusAddress a = (HibiscusAddress) empfaenger.getValue();
          empfaenger.setComment(a.getKommentar());
          
          // Stefan schreibt meines Wissens nach keine 
          getBill().setEnabled(a.getName().equals(hibiscus.getName()));
        }
        catch (Exception e)
        {
          Logger.error("unable to display comment",e);
        }
      }
    });

    Container group = new SimpleContainer(parent);
    group.addText(i18n.tr("M�chten Sie die Weiterentwicklung von Hibiscus/HBCI4Java mit einer Spende unterst�tzen?\n" +
                          "Dann w�hlen Sie einfach Ihr Konto aus und geben Sie den gew�nschten Betrag ein.\n" +
                          "Wenn Sie die Option \"Spendenquittung\" aktivieren und Ihre eMail-Adresse eingeben,\n" +
                          "erhalten Sie eine Rechnung in H�he des Spendenbetrages f�r Ihre Buchhaltung."),true);
    
    group.addLabelPair(i18n.tr("Ihr Konto"), getKontoauswahl());
    group.addLabelPair(i18n.tr("Betrag"), getBetrag());
    group.addLabelPair(i18n.tr("Empf�nger"), this.empfaenger);
    group.addSeparator();
    group.addCheckbox(getBill(),"Beleg �ber Spende per Mail gew�nscht");
    group.addLabelPair("Ihre eMail-Adresse", getEMail());
    
    ButtonArea buttons = new ButtonArea(parent,2);
    buttons.addButton(i18n.tr("�bernehmen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          ueberweisung = (Ueberweisung) Settings.getDBService().createObject(Ueberweisung.class,null);
          Double betrag = (Double) getBetrag().getValue();
          ueberweisung.setBetrag(betrag == null ? Double.NaN : betrag.doubleValue());
          ueberweisung.setGegenkonto((HibiscusAddress)empfaenger.getValue());
          
          Boolean email = (Boolean) getBill().getValue();
          ueberweisung.setZweck("Spende - Hibiscus");
          
          if (email.booleanValue())
          {
            String s = (String) getEMail().getValue();
            if (s != null)
              s = s.replaceAll("@","*at*");
            ueberweisung.setZweck2(s);
          }
          ueberweisung.setKonto((Konto)getKontoauswahl().getValue());
        }
        catch (RemoteException e)
        {
          Logger.error("error while creating donation",e);
        }
        close();
      }
    },null,true);
    buttons.addButton(i18n.tr("Abbrechen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    });
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return ueberweisung;
  }

  private DialogInput getKontoauswahl()
  {
    if (kontoAuswahl != null)
      return kontoAuswahl;

    KontoAuswahlDialog d = new KontoAuswahlDialog(KontoAuswahlDialog.POSITION_MOUSE);
    d.addCloseListener(new KontoListener());
    kontoAuswahl = new DialogInput("",d);
    kontoAuswahl.setComment("");
    kontoAuswahl.disableClientControl();
    return kontoAuswahl;
  }

  private Input getBetrag()
  {
    if (betrag != null)
      return betrag;
    betrag = new DecimalInput(0.0,HBCI.DECIMALFORMAT);
    return betrag;
  }
  
  private CheckboxInput getBill()
  {
    if (bill != null)
      return this.bill;
    bill = new CheckboxInput(false);
    bill.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        Boolean b = (Boolean) bill.getValue();
        getEMail().setEnabled(b);
      }
    });
    return bill;
  }
  
  private Input getEMail()
  {
    if (email != null)
      return email;
    email = new TextInput(null);
    email.setEnabled(false);
    return email;
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
      if (event == null || event.data == null)
        return;
      Konto konto = (Konto) event.data;

      try {
        String b = konto.getBezeichnung();
        getKontoauswahl().setText(konto.getKontonummer());
        getKontoauswahl().setComment(b == null ? "" : b);
        getKontoauswahl().setValue(konto);
        getBetrag().setComment(konto.getWaehrung());
      }
      catch (RemoteException er)
      {
        Logger.error("error while updating konto",er);
        GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Uebernehmen des Kontos"));
      }
    }
  }
}


/*********************************************************************
 * $Log$
 * Revision 1.10  2010-08-19 11:00:54  willuhn
 * @N Plausi-Checks (Checkbox und eMail-Eingabe aktivieren/deaktivieren)
 * @C Kein Spenden-Beleg, wenn Stefan als Empfaenger ausgewaehlt ist - IMHO verschickt er sowas nicht
 *
 * Revision 1.9  2008/12/02 10:52:23  willuhn
 * @B DecimalInput kann NULL liefern
 * @B Double.NaN beruecksichtigen
 *
 * Revision 1.8  2008/11/17 23:30:00  willuhn
 * @C Aufrufe der depeicated BLZ-Funktionen angepasst
 *
 * Revision 1.7  2008/04/07 16:15:34  willuhn
 * @N Bug 577
 *
 * Revision 1.6  2007/04/23 18:07:15  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.5  2006/06/06 18:02:45  jost
 * Tippfehler beseitigt.
 *
 * Revision 1.4  2006/06/06 13:52:58  willuhn
 * @N Linewraps in DonateDialog (sucking windows SWT behaviour)
 *
 * Revision 1.3  2005/06/27 22:25:43  web0
 * *** empty log message ***
 *
 * Revision 1.2  2005/06/27 16:12:35  web0
 * *** empty log message ***
 *
 * Revision 1.1  2005/06/27 13:36:53  web0
 * @N added donate button
 *
 **********************************************************************/