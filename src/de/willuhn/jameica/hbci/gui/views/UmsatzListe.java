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
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.Table;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.UmsatzControl;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 */
public class UmsatzListe extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();

		GUI.setTitleText(i18n.tr("Ums�tze des Kontos"));
		
		final UmsatzControl control = new UmsatzControl(this);

		try {
			
			Table list = control.getUmsatzListe();
			list.paint(getParent());
			
			ButtonArea buttons = new ButtonArea(getParent(),2);
			buttons.addCustomButton(i18n.tr("Ums�tze abrufen"), new MouseAdapter() {
        public void mouseUp(MouseEvent e) {
        	control.handleGetUmsaetze();
        }
      });
			buttons.addCancelButton(control);
		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while loading umsatz list",e);
			GUI.setActionText(i18n.tr("Fehler beim Laden der Ums�tze"));
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
 * Revision 1.1  2004-03-05 00:04:10  willuhn
 * @N added code for umsatzlist
 *
 **********************************************************************/