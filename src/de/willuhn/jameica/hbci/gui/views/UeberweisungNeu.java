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

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.UeberweisungControl;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bearbeitung der Ueberweisungen.
 */
public class UeberweisungNeu extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("�berweisung bearbeiten"));
		
		final UeberweisungControl control = new UeberweisungControl(this);
		LabelGroup group = new LabelGroup(getParent(),i18n.tr("Eigenschaften"));
		
		group.addLabelPair(i18n.tr("Konto"),										control.getKontoAuswahl());		
		group.addLabelPair(i18n.tr("Konto des Empf�ngers"),			control.getEmpfaengerKonto());		
		group.addLabelPair(i18n.tr("BLZ des Empf�ngers"),				control.getEmpfaengerBlz());		
		group.addLabelPair(i18n.tr("Name des Empf�ngers"),			control.getEmpfaengerName());
		group.addCheckbox(control.getStoreEmpfaenger(),i18n.tr("Empf�ngerdaten im Adressbuch speichern"));

		group.addSeparator();

		group.addLabelPair(i18n.tr("Verwendungszweck"),					control.getZweck());
		group.addLabelPair(i18n.tr("weiterer Verwendungszweck"),control.getZweck2());
		group.addLabelPair(i18n.tr("Betrag"),										control.getBetrag());
		group.addLabelPair(i18n.tr("Termin"),										control.getTermin());


		ButtonArea buttonArea = new ButtonArea(getParent(),4);
		buttonArea.addCustomButton(i18n.tr("sofort ausf�hren"), new MouseAdapter() {
      public void mouseUp(MouseEvent e) {
      	control.handleExecute();
      }
    });
		buttonArea.addCancelButton(control);
		buttonArea.addDeleteButton(control);
		buttonArea.addStoreButton(control);
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.8  2004-04-21 22:28:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.6  2004/04/05 23:28:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/03/30 22:07:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/03/04 00:35:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/03/04 00:26:24  willuhn
 * @N Ueberweisung
 *
 * Revision 1.2  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.1  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/