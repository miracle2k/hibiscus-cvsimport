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

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.UmsatzControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 */
public class UmsatzListe extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Ums�tze des Kontos"));
		
		final UmsatzControl control = new UmsatzControl(this);

		try {
			
			Part list = control.getUmsatzListe();
			list.paint(getParent());
			
			ButtonArea buttons = new ButtonArea(getParent(),3);
			buttons.addCustomButton(i18n.tr("Ums�tze abrufen"), new Listener()
      {
        public void handleEvent(Event event)
        {
					control.handleFetchUmsaetze();
        }
      });
			buttons.addCustomButton(i18n.tr("alle Ums�tze l�schen"), new Listener()
      {
        public void handleEvent(Event event)
        {
					control.handleDeleteUmsaetze();
        }
      });
			buttons.addCancelButton(control);
		}
		catch (RemoteException e)
		{
			Logger.error("error while loading umsatz list",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Laden der Ums�tze"));
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
 * Revision 1.11  2004-10-17 16:28:46  willuhn
 * @N Die ersten Dauerauftraege abgerufen ;)
 *
 * Revision 1.10  2004/10/08 13:37:48  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/07/25 17:15:05  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.8  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/07/20 22:53:03  willuhn
 * @C Refactoring
 *
 * Revision 1.6  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 * Revision 1.5  2004/06/30 20:58:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.3  2004/04/04 18:30:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/03/30 22:07:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/03/05 00:04:10  willuhn
 * @N added code for umsatzlist
 *
 **********************************************************************/