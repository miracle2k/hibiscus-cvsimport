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
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Headline;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Back;
import de.willuhn.jameica.hbci.gui.action.SammelTransferBuchungImport;
import de.willuhn.jameica.hbci.gui.action.SammelTransferDelete;
import de.willuhn.jameica.hbci.gui.action.SammelUeberweisungBuchungNew;
import de.willuhn.jameica.hbci.gui.action.SammelUeberweisungExecute;
import de.willuhn.jameica.hbci.gui.controller.SammelUeberweisungControl;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bearbeitung der Sammel-Lastschriften.
 */
public class SammelUeberweisungNew extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

		final SammelUeberweisungControl control = new SammelUeberweisungControl(this);

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Sammel-�berweisung bearbeiten"));
		
		LabelGroup group = new LabelGroup(getParent(),i18n.tr("Eigenschaften"));
    group.addLabelPair(i18n.tr("Zu belastendes Konto"),control.getKontoAuswahl());
    group.addLabelPair(i18n.tr("Bezeichnung"),control.getName());
    group.addLabelPair(i18n.tr("Termin"),control.getTermin());
		
		group.addSeparator();
    group.addLabelPair(i18n.tr("Summe der Buchungen"),control.getSumme());
		group.addLabelPair("",control.getComment());

    new Headline(getParent(),i18n.tr("Enthaltene Buchungen"));
    control.getBuchungen().paint(getParent());

		final SammelUeberweisung l = (SammelUeberweisung) control.getTransfer();

    ButtonArea buttons = new ButtonArea(getParent(),6);
    buttons.addButton(i18n.tr("Zur�ck"),new Back());
    buttons.addButton(i18n.tr("L�schen"),new SammelTransferDelete(),control.getTransfer());
    buttons.addButton(i18n.tr("Buchungen importieren..."), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        // Erst speichern
        if (control.handleStore())
          new SammelTransferBuchungImport().handleAction(context);
      }
    },control.getTransfer());
    buttons.addButton(i18n.tr("Neue Buchungen hinzuf�gen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        if (control.handleStore())
          new SammelUeberweisungBuchungNew().handleAction(l);
      }
    });
		buttons.addButton(i18n.tr("Speichern und ausf�hren"), new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
        if (control.handleStore())
  				new SammelUeberweisungExecute().handleAction(l);
			}
		},null,true);
    buttons.addButton(i18n.tr("Speichern"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleStore();
      }
    },null,true);

  }
}


/**********************************************************************
 * $Log$
 * Revision 1.4  2006-06-13 20:09:05  willuhn
 * @R Text "Bemerkung" entfernt
 *
 * Revision 1.3  2006/06/08 22:29:47  willuhn
 * @N DTAUS-Import fuer Sammel-Lastschriften und Sammel-Ueberweisungen
 * @B Eine Reihe kleinerer Bugfixes in Sammeltransfers
 * @B Bug 197 besser geloest
 *
 * Revision 1.2  2006/01/18 00:51:00  willuhn
 * @B bug 65
 *
 * Revision 1.1  2005/09/30 00:08:51  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 **********************************************************************/