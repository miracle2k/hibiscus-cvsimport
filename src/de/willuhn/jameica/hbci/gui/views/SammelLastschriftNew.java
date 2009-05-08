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
import de.willuhn.jameica.gui.internal.buttons.Back;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Headline;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.DBObjectDelete;
import de.willuhn.jameica.hbci.gui.action.SammelLastBuchungNew;
import de.willuhn.jameica.hbci.gui.action.SammelLastschriftExecute;
import de.willuhn.jameica.hbci.gui.controller.SammelLastschriftControl;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bearbeitung der Sammel-Lastschriften.
 */
public class SammelLastschriftNew extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

		final SammelLastschriftControl control = new SammelLastschriftControl(this);
    SammelTransfer transfer = control.getTransfer();

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Sammel-Lastschrift bearbeiten"));
		
		LabelGroup group = new LabelGroup(getParent(),i18n.tr("Eigenschaften"));
    group.addLabelPair(i18n.tr("Gutschriftskonto"),control.getKontoAuswahl());
    group.addLabelPair(i18n.tr("Bezeichnung"),control.getName());
    group.addLabelPair(i18n.tr("Termin"),control.getTermin());
		
		group.addSeparator();
    group.addLabelPair(i18n.tr("Summe der Buchungen"),control.getSumme());

    new Headline(getParent(),i18n.tr("Enthaltene Buchungen"));
    control.getBuchungen().paint(getParent());

		final SammelLastschrift l = (SammelLastschrift) control.getTransfer();

    ButtonArea buttons = new ButtonArea(getParent(),5);
    buttons.addButton(new Back(transfer.ausgefuehrt()));
    buttons.addButton(i18n.tr("L�schen"),new DBObjectDelete(),control.getTransfer(),false,"user-trash-full.png");

    Button add = new Button(i18n.tr("Neue Buchungen hinzuf�gen"), new Action() {
      public void handleAction(Object context) throws ApplicationException {
        if (control.handleStore())
          new SammelLastBuchungNew().handleAction(l);
      }
    },null,false,"text-x-generic.png");
    add.setEnabled(!transfer.ausgefuehrt());
    
		Button execute = new Button(i18n.tr("Jetzt ausf�hren..."), new Action() {
			public void handleAction(Object context) throws ApplicationException {
        if (control.handleStore())
  				new SammelLastschriftExecute().handleAction(l);
			}
		},null,false,"emblem-important.png");
    execute.setEnabled(!transfer.ausgefuehrt());
    
    Button store = new Button(i18n.tr("Speichern"),new Action() {
      public void handleAction(Object context) throws ApplicationException {
        control.handleStore();
      }
    },null,!transfer.ausgefuehrt(),"document-save.png");
    store.setEnabled(!transfer.ausgefuehrt());
    
    buttons.addButton(add);
    buttons.addButton(execute);
    buttons.addButton(store);

  }
}


/**********************************************************************
 * $Log$
 * Revision 1.19  2009-05-08 13:58:30  willuhn
 * @N Icons in allen Menus und auf allen Buttons
 * @N Fuer Umsatz-Kategorien koennen nun benutzerdefinierte Farben vergeben werden
 *
 * Revision 1.18  2009/05/06 23:11:23  willuhn
 * @N Mehr Icons auf Buttons
 *
 * Revision 1.17  2009/03/11 23:40:45  willuhn
 * @B Kleineres Bugfixing in Sammeltransfer-Control
 *
 * Revision 1.16  2009/02/13 14:17:01  willuhn
 * @N BUGZILLA 700
 *
 * Revision 1.15  2009/01/20 10:51:46  willuhn
 * @N Mehr Icons - fuer Buttons
 *
 * Revision 1.14  2008/05/30 12:02:08  willuhn
 * @N Erster Code fuer erweiterte Verwendungszwecke - NOCH NICHT FREIGESCHALTET!
 *
 * Revision 1.13  2006/08/07 14:31:59  willuhn
 * @B misc bugfixing
 * @C Redesign des DTAUS-Imports fuer Sammeltransfers
 *
 * Revision 1.12  2006/06/13 20:09:06  willuhn
 * @R Text "Bemerkung" entfernt
 *
 * Revision 1.11  2006/06/08 22:29:47  willuhn
 * @N DTAUS-Import fuer Sammel-Lastschriften und Sammel-Ueberweisungen
 * @B Eine Reihe kleinerer Bugfixes in Sammeltransfers
 * @B Bug 197 besser geloest
 *
 * Revision 1.10  2006/01/18 00:51:00  willuhn
 * @B bug 65
 *
 * Revision 1.9  2005/09/30 00:08:51  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 * Revision 1.8  2005/08/22 10:36:37  willuhn
 * @N bug 115, 116
 *
 * Revision 1.7  2005/08/02 20:09:33  web0
 * @B bug 106
 *
 * Revision 1.6  2005/07/04 11:36:53  web0
 * @B bug 89
 *
 * Revision 1.5  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.4  2005/03/05 19:19:48  web0
 * *** empty log message ***
 *
 * Revision 1.3  2005/03/02 00:22:05  web0
 * @N first code for "Sammellastschrift"
 *
 * Revision 1.2  2005/03/01 18:51:04  web0
 * @N Dialoge fuer Sammel-Lastschriften
 *
 * Revision 1.1  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
 **********************************************************************/