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
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.views.UmsatzDetail;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Liefert ein vorgefertigtes Kontext-Menu, welches an Listen von Umsaetzen
 * angehaengt werden kann.
 */
public class UmsatzList extends ContextMenu
{

	private I18N i18n;

	public UmsatzList()
	{
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		addItem(new CheckedContextMenuItem(i18n.tr("�ffnen"),new Listener()
		{
			public void handleEvent(Event event)
			{
				GUI.startView(UmsatzDetail.class.getName(),event.data);
			}
		}));

	}

}


/**********************************************************************
 * $Log$
 * Revision 1.3  2004-07-25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.2  2004/07/21 23:54:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/20 21:48:00  willuhn
 * @N ContextMenus
 *
 **********************************************************************/