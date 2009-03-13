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
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.buttons.Back;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.AuslandsUeberweisungNew;
import de.willuhn.jameica.hbci.gui.controller.AuslandsUeberweisungControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste mit den vorhandenen Auslandsueberweisungen an.
 */
public class AuslandsUeberweisungList extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Vorhandene Auslands�berweisungen"));
		
		final AuslandsUeberweisungControl control = new AuslandsUeberweisungControl(this);
		
		try {

			control.getAuslandsUeberweisungListe().paint(getParent());

			ButtonArea buttons = new ButtonArea(getParent(),2);
	    buttons.addButton(new Back(false));
			buttons.addButton(i18n.tr("Neue Auslands�berweisung"), new AuslandsUeberweisungNew(),null,true);

		}
		catch (Exception e)
		{
			Logger.error("error while loading transfer list",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Lesen der Auftr�ge."));
		}
  }
}


/**********************************************************************
 * $Log$
 * Revision 1.1  2009-03-13 00:25:12  willuhn
 * @N Code fuer Auslandsueberweisungen fast fertig
 *
 **********************************************************************/