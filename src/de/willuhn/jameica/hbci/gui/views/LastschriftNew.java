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
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Back;
import de.willuhn.jameica.hbci.gui.action.LastschriftDelete;
import de.willuhn.jameica.hbci.gui.action.LastschriftExecute;
import de.willuhn.jameica.hbci.gui.controller.LastschriftControl;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bearbeitung der Lastschriften.
 */
public class LastschriftNew extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

		final LastschriftControl control = new LastschriftControl(this);
    final Transfer tranfer = control.getTransfer();

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Lastschrift bearbeiten"));
		
		LabelGroup konten = new LabelGroup(getParent(),i18n.tr("Konten"));
		
		konten.addLabelPair(i18n.tr("pers�nliches Konto (Empf�nger)"),	control.getKontoAuswahl());		
    konten.addLabelPair(i18n.tr("Name Zahlungspflichtiger"),        control.getEmpfaengerName());
		konten.addLabelPair(i18n.tr("Kontonummer Zahlungspflichtiger"),	control.getEmpfaengerKonto());		
		konten.addLabelPair(i18n.tr("BLZ Zahlungspflichtiger"),			    control.getEmpfaengerBlz());
		konten.addCheckbox(control.getStoreEmpfaenger(),i18n.tr("Adressdaten im Adressbuch speichern"));

		LabelGroup details = new LabelGroup(getParent(),i18n.tr("Details"));

		details.addLabelPair(i18n.tr("Verwendungszweck"),					control.getZweck());
		details.addLabelPair(i18n.tr("weiterer Verwendungszweck"),control.getZweck2());
		details.addLabelPair(i18n.tr("Betrag"),										control.getBetrag());
		details.addLabelPair(i18n.tr("Termin"),										control.getTermin());
		details.addLabelPair(i18n.tr("Typ"),											control.getTyp());

		details.addSeparator();

		details.addLabelPair(i18n.tr("Bemerkung"),								control.getComment());

		ButtonArea buttonArea = new ButtonArea(getParent(),4);
		buttonArea.addButton(i18n.tr("Zur�ck"), 				 				 new Back());
		buttonArea.addButton(i18n.tr("L�schen"),				 				 new LastschriftDelete(), tranfer);
		buttonArea.addButton(i18n.tr("Speichern und ausf�hren"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleStore();
        new LastschriftExecute().handleAction(tranfer);
      }
    },null,true);
    
		buttonArea.addButton(i18n.tr("Speichern"), 			     new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
      	control.handleStore();
      }
    });
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.6  2005-10-17 22:00:44  willuhn
 * @B bug 143
 *
 * Revision 1.5  2005/08/01 23:27:42  web0
 * *** empty log message ***
 *
 * Revision 1.4  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.3  2005/02/19 17:22:05  willuhn
 * @B Bug 8
 *
 * Revision 1.2  2005/02/03 18:57:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 **********************************************************************/