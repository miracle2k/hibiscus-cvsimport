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
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.LabelGroup;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.gui.controller.KontoControl;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bankverbindung bearbeiten.
 */
public class KontoNeu extends AbstractView {

  /**
   * ct.
   * @param parent
   */
  public KontoNeu(Composite parent) {
    super(parent);
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {
		
		addHeadline("Bankverbindung bearbeiten");
		
		final KontoControl control = new KontoControl(this);
		LabelGroup group = new LabelGroup(getParent(),I18N.tr("Eigenschaften"));

		LabelGroup saldo = new LabelGroup(getParent(),I18N.tr("Aktueller Saldo"));


		try {
			group.addLabelPair(I18N.tr("Kontonummer"),			    		control.getKontonummer());
			group.addLabelPair(I18N.tr("Bankleitzahl"),			    		control.getBlz());
			group.addLabelPair(I18N.tr("Kontoinhaber"),			    		control.getName());
			group.addLabelPair(I18N.tr("Kundennummer"),							control.getKundennummer());
      group.addLabelPair(I18N.tr("W�hrungsbezeichnung"),  		control.getWaehrung());
			group.addLabelPair(I18N.tr("Sicherheitsmedium"),    		control.getPassport());

			saldo.addLabelPair(I18N.tr("Saldo"),										control.getSaldo());
			saldo.addLabelPair(I18N.tr("letzte Aktualisierung"),		control.getSaldoDatum());

			control.init();
		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while reading konto",e);
			GUI.setActionText(I18N.tr("Fehler beim Lesen der Bankverbindungsdaten."));
		}

		// und noch die Abschicken-Knoepfe
		ButtonArea buttonArea = new ButtonArea(getParent(),6);
		buttonArea.addCustomButton(I18N.tr("Saldo aktualisieren"), new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				control.handleRefreshSaldo();
			}
		});
		buttonArea.addCustomButton(I18N.tr("Sicherheitsmedium konfigurieren"), new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				control.handleConfigurePassport();
			}
		});
		buttonArea.addCustomButton(I18N.tr("Sicherheitsmedium auslesen"), new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				control.handleReadFromPassport();
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
 * Revision 1.4  2004-02-17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.3  2004/02/12 23:46:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/11 15:40:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 **********************************************************************/