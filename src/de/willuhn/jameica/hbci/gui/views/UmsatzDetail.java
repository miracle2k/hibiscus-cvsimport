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

package de.willuhn.jameica.hbci.gui.views;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Back;
import de.willuhn.jameica.hbci.gui.action.EmpfaengerAdd;
import de.willuhn.jameica.hbci.gui.controller.UmsatzDetailControl;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bildet die Detailansicht einer Buchung ab.
 */
public class UmsatzDetail extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

    final UmsatzDetailControl control = new UmsatzDetailControl(this);
    final I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    // BUGZILLA 38 http://www.willuhn.de/bugzilla/show_bug.cgi?id=38
    Konto k = control.getUmsatz().getKonto();

    String s1 = k.getBezeichnung();
    if (s1 == null) s1 = "";

    String s2 = k.getKontonummer();
    
    double d = k.getSaldo();
    String s3 = null;
    if (k.getSaldoDatum() != null)
      s3 = HBCI.DECIMALFORMAT.format(d) + " " + k.getWaehrung(); // Saldo wurde schonmal abgerufen

    if (s3 == null)
      GUI.getView().setTitle(i18n.tr("Buchungsdetails. Konto: {0} [Kto.-Nr.: {1}]",new String[]{s1,s2}));
    else
      GUI.getView().setTitle(i18n.tr("Buchungsdetails. Konto: {0} [Kto.-Nr.: {1}, Saldo: {2}]",new String[]{s1,s2,s3}));

    LabelGroup detail = new LabelGroup(getParent(),i18n.tr("Details"));

		// BUGZILLA 23 http://www.willuhn.de/bugzilla/show_bug.cgi?id=23
    detail.addLabelPair(i18n.tr("pers�nliches Konto"),						control.getKonto());
    detail.addLabelPair(i18n.tr("Kontoinhaber des Gegenkontos"),	control.getEmpfaengerName());
    detail.addLabelPair(i18n.tr("Kontonummer des Gegenkontos"),		control.getEmpfaengerKonto());
    detail.addLabelPair(i18n.tr("Bankleitzahl des Gegenkontos"),  control.getEmpfaengerBLZ());

    detail.addHeadline(i18n.tr("Datum und Betrag"));
    detail.addLabelPair(i18n.tr("Betrag"),                        control.getBetrag());
    detail.addLabelPair(i18n.tr("Datum der Buchung"),             control.getDatum());
    detail.addLabelPair(i18n.tr("Valuta"),                        control.getValuta());
    detail.addLabelPair(i18n.tr("Neuer Saldo"),                   control.getSaldo());

    detail.addHeadline(i18n.tr("Sonstige Informationen"));
    detail.addLabelPair(i18n.tr("Art der Buchung"),               control.getArt());
    detail.addLabelPair(i18n.tr("Kundenreferenz"),                control.getCustomerRef());
    detail.addLabelPair(i18n.tr("Primanota-Kennzeichen"),         control.getPrimanota());
    detail.addLabelPair(i18n.tr("Umsatz-Kategorie"),              control.getUmsatzTyp());

    // BUGZILLA 30 http://www.willuhn.de/bugzilla/show_bug.cgi?id=30
    LabelGroup zweck = new LabelGroup(getParent(),i18n.tr("Verwendungszweck"));

    // BUGZILLA 75 http://www.willuhn.de/bugzilla/show_bug.cgi?id=75
    Umsatz u = control.getUmsatz();
    String z1 = u.getZweck();
    String z2 = u.getZweck2();

    // Wir erlauben das Bearbeiten des Verwendungszwecks1 in 3 Faellen:
    // 1) Der Umsatz hat bisher keinen
    // 2) Der Verwendungszweck ist kuerzer als 4 Zeichen
    // 3) Der Verwendungszweck wurde vom User vorher schonmal geaendert
    if (z1 == null || z1.length() < 4 || u.hasChangedByUser())
    {
      zweck.addLabelPair(i18n.tr("Verwendungszweck"),control.getZweck());

      // BUGZILLA 263 http://www.willuhn.de/bugzilla/show_bug.cgi?id=263
      if (z2 != null && z2.length() > 0)
      {
        zweck.addLabelPair(i18n.tr("Weiterer Verwendungszweck"),new LabelInput(z2));
      }
    }
    else
    {
      zweck.addText(z1,true);
      if (z2 != null && z2.length() > 0)
      {
        zweck.addText(z2,true);
      }
    }
   
    LabelGroup comment = new LabelGroup(getParent(),i18n.tr("Kommentar zu dieser Buchung"),true);
    comment.addPart(control.getKommentar());

    ButtonArea buttons = new ButtonArea(getParent(),3);
		buttons.addButton(i18n.tr("Zur�ck"),new Back(),null,true);
    buttons.addButton(i18n.tr("Speichern"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleStore();
      }
    });
    
    Button ab = null;

    final Address found = control.getAddressbookEntry();
    if (found != null)
    {
      ab = new Button(i18n.tr("Gegenkonto In Adressbuch �ffnen"),new de.willuhn.jameica.hbci.gui.action.EmpfaengerNew(),found);
    }
    else
    {
      ab = new Button(i18n.tr("Gegenkonto in Adressbuch �bernehmen"),new Action()
      {
        public void handleAction(Object context) throws ApplicationException
        {
          new EmpfaengerAdd().handleAction(control.getUmsatz());
        }
      });
    }
    buttons.addButton(ab);
  }
}


/**********************************************************************
 * $Log$
 * Revision 1.28  2007-04-24 17:52:17  willuhn
 * @N Bereits in den Umsatzdetails erkennen, ob die Adresse im Adressbuch ist
 * @C Gross-Kleinschreibung in Adressbuch-Suche
 *
 * Revision 1.27  2006/11/30 23:48:40  willuhn
 * @N Erste Version der Umsatz-Kategorien drin
 *
 * Revision 1.26  2006/10/06 15:14:38  willuhn
 * @B bug 263
 *
 * Revision 1.25  2006/08/05 20:44:39  willuhn
 * @B Bug 256
 *
 * Revision 1.24  2006/05/10 12:51:37  willuhn
 * @B typo s/Ktr/Kto/
 *
 * Revision 1.23  2006/01/18 00:51:00  willuhn
 * @B bug 65
 *
 * Revision 1.22  2005/06/30 21:48:56  web0
 * @B bug 75
 *
 * Revision 1.21  2005/06/27 14:18:49  web0
 * @B bug 75
 *
 * Revision 1.20  2005/06/17 17:36:34  web0
 * @B bug 75
 *
 * Revision 1.19  2005/06/13 23:11:01  web0
 * *** empty log message ***
 *
 * Revision 1.18  2005/04/05 22:49:02  web0
 * @B bug 32
 *
 * Revision 1.17  2005/04/05 22:13:30  web0
 * @B bug 38
 *
 * Revision 1.16  2005/03/30 23:51:16  web0
 * @B bug 30
 *
 * Revision 1.15  2005/03/21 23:09:34  web0
 * @B bug 23
 *
 * Revision 1.14  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.13  2005/02/06 17:46:17  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/10/20 12:08:18  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 * Revision 1.10  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 * Revision 1.9  2004/10/08 13:37:48  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/07/25 17:15:05  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.7  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/07/20 22:53:03  willuhn
 * @C Refactoring
 *
 * Revision 1.5  2004/05/11 23:31:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/04/27 22:23:56  willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
 *
 * Revision 1.3  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.2  2004/03/30 22:07:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/03/11 08:55:42  willuhn
 * @N UmsatzDetails
 *
 **********************************************************************/