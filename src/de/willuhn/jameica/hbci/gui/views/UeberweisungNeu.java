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

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.LabelGroup;
import de.willuhn.jameica.gui.views.AbstractView;
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
		GUI.setTitleText(I18N.tr("�berweisung bearbeiten"));
		
		UeberweisungControl control = new UeberweisungControl(this);
		LabelGroup group = new LabelGroup(getParent(),I18N.tr("Eigenschaften"));
		
		group.addLabelPair(I18N.tr("Konto"),									control.getKonto());		
		group.addLabelPair(I18N.tr("Konto des Empf�ngers"),		control.getEmpfaengerKonto());		
		group.addLabelPair(I18N.tr("BLZ des Empf�ngers"),			control.getEmpfaengerBlz());		
		group.addLabelPair(I18N.tr("Name des Empf�ngers"),		control.getEmpfaengerName());		

		ButtonArea buttonArea = new ButtonArea(getParent(),3);
		buttonArea.addCancelButton(control);
		buttonArea.addDeleteButton(control);
		buttonArea.addStoreButton(control);
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
    // TODO Auto-generated method stub

  }

}


/**********************************************************************
 * $Log$
 * Revision 1.1  2004-02-22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/