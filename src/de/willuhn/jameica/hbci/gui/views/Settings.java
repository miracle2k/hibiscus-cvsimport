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

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.LabelGroup;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.gui.controller.SettingsControl;
import de.willuhn.jameica.hbci.gui.dialogs.NewPassportDialog;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Einstellungs-Dialog.
 */
public class Settings extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

		GUI.setTitleText(I18N.tr("Einstellungen"));
		final SettingsControl control = new SettingsControl(this);
		
		LabelGroup settings = new LabelGroup(getParent(),I18N.tr("Grundeinstellungen"));

		try {

			// Einstellungen
			settings.addCheckbox(control.getOnlineMode(),I18N.tr("Keine Nachfrage vor Verbindungsaufbau"));
			settings.addCheckbox(control.getCheckPin(),I18N.tr("PIN-Eingabe via Check-Summe pr�fen"));

			ButtonArea buttons = settings.createButtonArea(2);
			buttons.addCustomButton(I18N.tr("gespeicherte Check-Summe l�schen"),new MouseAdapter() {
				public void mouseUp(MouseEvent e) {
					control.handleDeleteCheckSum();
				}
			});
			buttons.addStoreButton(control);


			// Passports
			LabelGroup passports = new LabelGroup(getParent(),I18N.tr("Sicherheitsmedien"));

			passports.addTable(control.getPassportListe());
			
			ButtonArea buttons2 = passports.createButtonArea(1);
			buttons2.addCustomButton(I18N.tr("Neues Sicherheitsmedium anlegen"),new MouseAdapter() {
        public void mouseUp(MouseEvent e) {
        	NewPassportDialog d = new NewPassportDialog(NewPassportDialog.POSITION_MOUSE);
					try {
						GUI.startView(PassportDetails.class.getName(),d.open());
					}
					catch (Exception e2)
					{
						// Dialog wurde abgebrochen
						Application.getLog().info(e2.getMessage());
					}
        }
      });


			// Hinweise
			LabelGroup comments = new LabelGroup(getParent(),I18N.tr("Hinweise"));
			comments.addText(
				I18N.tr("Wenn Sie �ber eine dauerhafte Internetverbindung verf�gen," +					"k�nnen Sie die Option \"keine Nachfrage vor Verbindungsaufbau " +					"aktivieren."),
				true
			);
			comments.addText(
				I18N.tr("Bei aktivierter PIN-Pr�fung wird aus der von Ihnen eingegebene PIN " +					"eine Check-Summe gebildet und diese mit der Check-Summe Ihrer ersten PIN-Eingabe " +					"verglichen. Hierbei wird nicht die PIN selbst gespeichert sondern lediglich die " +					"Pr�fsumme mit der ermittelt werden kann, ob Ihre aktuelle " +					"Eingabe mit der Erst-Eingabe �bereinstimmt."),
				true
			);

			ButtonArea buttons3 = new ButtonArea(getParent(),1);
			buttons3.addCancelButton(control);

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
 * Revision 1.8  2004-02-27 01:13:09  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/02/27 01:12:22  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/02/27 01:11:53  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 * Revision 1.4  2004/02/25 23:11:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/02/21 19:49:04  willuhn
 * @N PINDialog
 *
 * Revision 1.2  2004/02/20 20:45:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/