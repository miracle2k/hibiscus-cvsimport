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
import de.willuhn.jameica.hbci.gui.action.DauerauftragChange;
import de.willuhn.jameica.hbci.gui.action.DauerauftragDelete;
import de.willuhn.jameica.hbci.gui.action.DauerauftragExecute;
import de.willuhn.jameica.hbci.gui.controller.DauerauftragControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 */
public class DauerauftragNeu extends AbstractView
{

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception
  {
		final DauerauftragControl control = new DauerauftragControl(this);

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("�berweisung bearbeiten"));
		
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

		group.addSeparator();

		group.addLabelPair(i18n.tr("Auftragsnummer"),						control.getOrderID());

		boolean active = control.getDauerauftrag().isActive();
		ButtonArea buttonArea = new ButtonArea(getParent(),4);
		if (active)
		{
			buttonArea.addButton(i18n.tr("�nderungen zur Bank senden"), new DauerauftragChange(), control.getDauerauftrag());
		}
		else
		{
			buttonArea.addButton(i18n.tr("Bei Bank einreichen"), new DauerauftragExecute(), control.getDauerauftrag());
		}
		buttonArea.addButton(i18n.tr("Zur�ck"), 				 new Back());
		buttonArea.addButton(i18n.tr("L�schen"),				 new DauerauftragDelete(), control.getDauerauftrag());
		buttonArea.addButton(i18n.tr("Speichern"), new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
				control.handleStore();
			}
		});
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException
  {
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.4  2004-10-24 17:19:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/10/23 17:34:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/10/08 13:37:48  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/13 23:08:37  willuhn
 * @N Views fuer Dauerauftrag
 *
 **********************************************************************/