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

import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.DialogFactory;
import de.willuhn.jameica.hbci.gui.controller.KontoControl;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste mit den vorhandenen Bankverbindungen an.
 */
public class KontoListe extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

		// Bevor hier irgendwas angezeigt wird, muss sicher sein, dass
		// wir einen Passport haben
		if (!DialogFactory.checkPassport()) return;


		final KontoControl control = new KontoControl(this);

		I18N i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Vorhandene Bankverbindungen"));

		try {

			control.getKontoListe().paint(getParent());
			ButtonArea buttons = new ButtonArea(getParent(),1);
			buttons.addCreateButton(i18n.tr("Neue Bankverbindung"),control);


			LabelGroup group = new LabelGroup(getParent(),i18n.tr("Konten aus Medium lesen"));
			group.addLabelPair(i18n.tr("Sicherheitsmedium"),control.getPassportAuswahl());

			ButtonArea c = group.createButtonArea(1);
			c.addCustomButton(i18n.tr("Daten aus Medium lesen"), new MouseAdapter() {
				public void mouseUp(MouseEvent e) {
					control.handleReadFromPassport();
				}
			});

		}
		catch (Exception e)
		{
			Application.getLog().error("error while loading konto list",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Lesen der Bankverbindungen."));
		}
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.9  2004-04-27 22:23:56  willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
 *
 * Revision 1.8  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.7  2004/03/30 22:07:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/03/04 00:26:24  willuhn
 * @N Ueberweisung
 *
 * Revision 1.5  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.4  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 * Revision 1.3  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.2  2004/02/20 20:45:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 **********************************************************************/