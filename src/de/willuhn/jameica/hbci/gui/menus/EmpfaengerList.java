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
package de.willuhn.jameica.hbci.gui.menus;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.listener.UeberweisungCreate;
import de.willuhn.jameica.hbci.gui.views.EmpfaengerNeu;
import de.willuhn.jameica.plugin.PluginLoader;
import de.willuhn.util.I18N;

/**
 * Liefert ein vorgefertigtes Kontext-Menu, welches an Listen von Empfaenger-Adressen
 * angehaengt werden kann.
 */
public class EmpfaengerList extends ContextMenu
{

	private I18N i18n;

	public EmpfaengerList()
	{
		i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();

		addItem(new CheckedContextMenuItem(i18n.tr("�ffnen"),new Listener()
		{
			public void handleEvent(Event event)
			{
				GUI.startView(EmpfaengerNeu.class.getName(),event.data);
			}
		}));

		addItem(new CheckedContextMenuItem(i18n.tr("Neue �berweisung mit diesem Empf�nger..."), new UeberweisungCreate()));
		addItem(ContextMenuItem.SEPARATOR);

		addItem(new ContextMenuItem(i18n.tr("Neue Adresse..."), new Listener()
		{
			public void handleEvent(Event event)
			{
				GUI.startView(EmpfaengerNeu.class.getName(),null);
			}
		}));
	}

}


/**********************************************************************
 * $Log$
 * Revision 1.3  2004-07-21 23:54:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/07/20 22:53:03  willuhn
 * @C Refactoring
 *
 * Revision 1.1  2004/07/20 21:48:00  willuhn
 * @N ContextMenus
 *
 **********************************************************************/