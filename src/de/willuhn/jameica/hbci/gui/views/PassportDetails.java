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
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.LabelGroup;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.controller.PassportControlDDV;
import de.willuhn.jameica.hbci.rmi.Passport;
import de.willuhn.jameica.hbci.rmi.PassportDDV;
import de.willuhn.jameica.hbci.rmi.PassportType;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog, ueber den die Passports konfiguriert werden koennen.
 */
public class PassportDetails extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Eigenschaften des Sicherheitsmediums"));

		Passport p = (Passport) getCurrentObject();
		
		// Das ist erstmal nur ein anonymer Passport. Wir muessen noch die
		// zugehoerige Impl suchen.
		// TODO: Das ist noch gar nicht schoen.
		PassportType pt = p.getPassportType();
		String clazz = pt.getImplementor();
		p = (Passport) Settings.getDatabase().createObject(Application.getClassLoader().load(clazz),p.getID());
		if (p.isNewObject())
			p.setPassportType(pt); // ist ein neuer Passport - der hat keinen Typ nach dem Laden
		setCurrentObject(p);


		///////////////////////////////////////////////////////////////////////////
		// DDV
  	if (p instanceof PassportDDV)
  	{
			final PassportControlDDV control = new PassportControlDDV(this);

			LabelGroup group = new LabelGroup(getParent(),i18n.tr("Eigenschaften"));
			
			group.addLabelPair(i18n.tr("Typ"),										control.getType());
			group.addLabelPair(i18n.tr("Bezeichnung"),						control.getName());
			group.addLabelPair(i18n.tr("Port des Lesers"),				control.getPort());
			group.addLabelPair(i18n.tr("Index des Lesers"),				control.getCTNumber());
			group.addLabelPair(i18n.tr("Index des HBCI-Zugangs"),	control.getEntryIndex());

			group.addCheckbox(control.getBio(), 		i18n.tr("Biometrische Verfahren verwenden"));
			group.addCheckbox(control.getSoftPin(), i18n.tr("Tastatur des PCs zur PIN-Eingabe verwenden"));
			
			// und noch die Abschicken-Knoepfe
			ButtonArea buttonArea = new ButtonArea(getParent(),4);
			buttonArea.addCustomButton(i18n.tr("Kartenleser testen"), new MouseAdapter() {
				public void mouseUp(MouseEvent e) {
					control.handleTest();
				}
			});
			buttonArea.addCancelButton(control);
			buttonArea.addDeleteButton(control);
			buttonArea.addStoreButton(control);

  	}
		//
		///////////////////////////////////////////////////////////////////////////
  	else {
			Application.getLog().error("choosen passport not found");
			GUI.getStatusBar().setErrorText(i18n.tr("Das ausgew�hlte Sicherheitsmedium konnte nicht geladen werden."));
  		
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
 * Revision 1.9  2004-03-30 22:07:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/03/19 01:44:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/03/04 00:26:24  willuhn
 * @N Ueberweisung
 *
 * Revision 1.6  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.5  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 * Revision 1.4  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.3  2004/02/20 20:45:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/12 00:38:41  willuhn
 * *** empty log message ***
 *
 **********************************************************************/