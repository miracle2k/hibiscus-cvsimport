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

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.LabelGroup;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.gui.controller.SettingsControl;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Einstellungs-Dialog.
 */
public class Settings extends AbstractView {

  /**
   * @param parent
   */
  public Settings(Composite parent) {
    super(parent);
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {
		addHeadline("Einstellungen");
		SettingsControl control = new SettingsControl(this);
		
		LabelGroup settings = new LabelGroup(getParent(),I18N.tr("Einstellungen"));
		try {
			settings.addCheckbox(control.getOnlineMode(),I18N.tr("Keine Nachfrage vor Verbindungsaufbau"));

			LabelGroup comments = new LabelGroup(getParent(),I18N.tr("Hinweise"));
			comments.addText(
			I18N.tr("Wenn Sie �ber eine dauerhafte Internetverbindung verf�gen," +				"k�nnen Sie die Option \"keine Nachfrage vor Verbindungsaufbau " +				"aktivieren."),true);

			ButtonArea buttons = new ButtonArea(getParent(),2);
			buttons.addCancelButton(control);
			buttons.addStoreButton(control);
		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while showing settings",e);
			GUI.setActionText(I18N.tr("Fehler beim Laden der Einstellungen"));
		}
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
 * Revision 1.1  2004-02-17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/