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

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Back;
import de.willuhn.jameica.hbci.gui.action.KontoFetchUmsaetze;
import de.willuhn.jameica.hbci.gui.controller.UmsatzControl;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Listet alle Umsaetze auf.
 */
public class UmsatzList extends AbstractView
{
  
  private UmsatzControl control = null;
  
  /**
   * ct.
   */
  public UmsatzList()
  {
    control = new UmsatzControl(this);
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();


    // BUGZILLA 38 http://www.willuhn.de/bugzilla/show_bug.cgi?id=38
    Konto k = control.getKonto();

    String s1 = k.getBezeichnung();
    if (s1 == null) s1 = "";

    String s2 = k.getKontonummer();
    
    double d = k.getSaldo();
    String s3 = null;
    if (k.getSaldoDatum() != null)
      s3 = HBCI.DECIMALFORMAT.format(d) + " " + k.getWaehrung(); // Saldo wurde schonmal abgerufen

    if (s3 == null)
  		GUI.getView().setTitle(i18n.tr("Kontoausz�ge: {0} [Kto.-Nr.: {1}]",new String[]{s1,s2}));
    else
      GUI.getView().setTitle(i18n.tr("Kontoausz�ge: {0} [Kto.-Nr.: {1}, Saldo: {2}]",new String[]{s1,s2,s3}));
		

		try {
			
			Part list = control.getUmsatzListe();
			list.paint(getParent());
			
			ButtonArea buttons = new ButtonArea(getParent(),2);
			buttons.addButton(i18n.tr("Zur�ck"),new Back(),null,true);
			buttons.addButton(i18n.tr("Ums�tze abrufen"), new KontoFetchUmsaetze(),control.getKonto());
		}
		catch (RemoteException e)
		{
			Logger.error("error while loading umsatz list",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Laden der Ums�tze"));
		}

  }
  
  /**
   * @see de.willuhn.jameica.gui.AbstractView#reload()
   */
  public void reload() throws ApplicationException
  {
    control.handleReload();
    super.reload();
  }
}


/**********************************************************************
 * $Log$
 * Revision 1.7  2006-05-10 12:51:37  willuhn
 * @B typo s/Ktr/Kto/
 *
 * Revision 1.6  2006/01/18 00:51:00  willuhn
 * @B bug 65
 *
 * Revision 1.5  2005/06/07 22:41:09  web0
 * @B bug 70
 *
 * Revision 1.4  2005/04/05 22:13:30  web0
 * @B bug 38
 *
 * Revision 1.3  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.2  2005/02/06 17:46:17  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/11/13 17:12:15  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/10/20 12:34:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/10/17 16:28:46  willuhn
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