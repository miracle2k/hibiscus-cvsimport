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
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.SammelTransferBuchungDelete;
import de.willuhn.jameica.hbci.gui.controller.SammelUeberweisungBuchungControl;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bearbeitung einer Buchung in einer Sammel-Lastschriften.
 */
public class SammelUeberweisungBuchungNew extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {

		final SammelUeberweisungBuchungControl control = new SammelUeberweisungBuchungControl(this);


    SammelTransfer l = control.getBuchung().getSammelTransfer();
    GUI.getView().setTitle(i18n.tr("Sammel-Überweisung {0}: Buchung bearbeiten",l.getBezeichnung()));
		
    SimpleContainer group = new SimpleContainer(getParent());
    group.addHeadline(i18n.tr("Empfänger"));
    group.addLabelPair(i18n.tr("Name"),                      control.getGegenkontoName());
		group.addLabelPair(i18n.tr("Kontonummer"),               control.getGegenKonto());
		group.addLabelPair(i18n.tr("BLZ"),			                 control.getGegenkontoBLZ());
		group.addCheckbox(control.getStoreAddress(),i18n.tr("In Adressbuch übernehmen"));

    SimpleContainer details = new SimpleContainer(getParent());
    details.addHeadline(i18n.tr("Details"));
		details.addLabelPair(i18n.tr("Verwendungszweck"),					control.getZweck());
		details.addLabelPair(i18n.tr("weiterer Verwendungszweck"),control.getZweck2());
		details.addLabelPair(i18n.tr("Betrag"),										control.getBetrag());
    details.addLabelPair(i18n.tr("Textschlüssel"),            control.getTextSchluessel());

		ButtonArea buttonArea = new ButtonArea();
    
    Button delete = new Button(i18n.tr("Löschen"), new SammelTransferBuchungDelete(),control.getBuchung(),false,"user-trash-full.png");
    delete.setEnabled(!l.ausgefuehrt());
    buttonArea.addButton(delete);

    Button store = new Button(i18n.tr("Speichern"), new Action() {
      public void handleAction(Object context) throws ApplicationException {
        control.handleStore(false);
      }
    },null,false,"document-save.png");
    store.setEnabled(!l.ausgefuehrt());
    buttonArea.addButton(store);
    
    // BUGZILLA 116 http://www.willuhn.de/bugzilla/show_bug.cgi?id=116
    Button store2 = new Button(i18n.tr("Speichern und nächste Buchung"), new Action() {
      public void handleAction(Object context) throws ApplicationException {
        control.handleStore(true);
      }
    },null,!l.ausgefuehrt(),"go-next.png");
    store2.setEnabled(!l.ausgefuehrt());
    buttonArea.addButton(store2);
    
    buttonArea.paint(getParent());
  }
}


/**********************************************************************
 * $Log$
 * Revision 1.10  2011-04-08 15:19:14  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 **********************************************************************/