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
import de.willuhn.jameica.hbci.gui.action.SammelLastBuchungNew;
import de.willuhn.jameica.hbci.gui.action.SammelLastschriftDelete;
import de.willuhn.jameica.hbci.gui.action.SammelLastschriftExecute;
import de.willuhn.jameica.hbci.gui.controller.SammelLastschriftControl;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bearbeitung der Sammel-Lastschriften.
 */
public class SammelLastschriftNew extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

		final SammelLastschriftControl control = new SammelLastschriftControl(this);

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Sammel-Lastschrift bearbeiten"));
		
		LabelGroup group = new LabelGroup(getParent(),i18n.tr("Eigenschaften"));
    group.addLabelPair(i18n.tr("pers�nliches Konto (Empf�nger)"),control.getKontoAuswahl());
    group.addLabelPair(i18n.tr("Bezeichnung"),control.getName());
    group.addLabelPair(i18n.tr("Termin"),control.getTermin());
		
		group.addSeparator();
		group.addLabelPair(i18n.tr("Bemerkung"),control.getComment());

    new Headline(getParent(),i18n.tr("Enthaltene Buchungen"));
    control.getBuchungen().paint(getParent());

		final SammelLastschrift l = control.getLastschrift();

    ButtonArea buttons = new ButtonArea(getParent(),5);
    buttons.addButton(i18n.tr("Zur�ck"),new Back());
    buttons.addButton(i18n.tr("Neue Buchung hinzuf�gen"), new SammelLastBuchungNew(),l);
    buttons.addButton(i18n.tr("L�schen"),new SammelLastschriftDelete(),control.getLastschrift());
		buttons.addButton(i18n.tr("Speichern und ausf�hren"), new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
				control.handleStore();
				new SammelLastschriftExecute().handleAction(l);
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

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.4  2005-03-05 19:19:48  web0
 * *** empty log message ***
 *
 * Revision 1.3  2005/03/02 00:22:05  web0
 * @N first code for "Sammellastschrift"
 *
 * Revision 1.2  2005/03/01 18:51:04  web0
 * @N Dialoge fuer Sammel-Lastschriften
 *
 * Revision 1.1  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
 **********************************************************************/