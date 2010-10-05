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
package de.willuhn.jameica.hbci.gui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.internal.action.Program;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.DonateView;
import de.willuhn.jameica.hbci.rmi.Version;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * About-Dialog.
 */
public class About extends AbstractDialog
{

  /**
   * ct.
   * @param position
   */
  public About(int position)
  {
    super(position);
    setTitle("About...");
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    AbstractPlugin plugin = Application.getPluginLoader().getPlugin(HBCI.class);
    final I18N i18n = plugin.getResources().getI18N();

    DBIterator list = Settings.getDBService().createList(Version.class);
    list.addFilter("name = ?", new String[]{"db"});
    Version version = (Version) list.next();
    
    setTitle(i18n.tr("About"));

    Label l = GUI.getStyleFactory().createLabel(parent,SWT.BORDER);
    l.setImage(SWTUtil.getImage("hibiscus.jpg"));

    Container container = new LabelGroup(parent,i18n.tr("About"),true);
    
    FormTextPart text = new FormTextPart();
    text.setText("<form>" +
      "<p><b>Hibiscus - HBCI-Onlinebanking f�r Jameica</b></p>" +
      "<p>Lizenz: GPL [<a href=\"" + Program.class.getName() + "\">http://www.gnu.org/copyleft/gpl.html</a>]</p>" +
      "<p>Copyright by Olaf Willuhn [<a href=\"" + Program.class.getName() + "\">mailto:hibiscus@willuhn.de</a>]</p>" +
      "<p><a href=\"" + Program.class.getName() + "\">http://www.willuhn.de/projects/hibiscus/</a></p>" +
      "<p>Software-Version: " + plugin.getManifest().getVersion() + "</p>" +
      "<p>Datenbank-Version: " + version.getVersion() + "</p>" +
      "<p>Build: " + plugin.getManifest().getBuildnumber() + " [Datum " + plugin.getManifest().getBuildDate() + "]</p>" +
      "</form>");

    container.addPart(text);

    ButtonArea buttons = container.createButtonArea(3);
    buttons.addButton(i18n.tr("Diagnose-Informationen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          new DebugDialog(DebugDialog.POSITION_CENTER).open();
        }
        catch (Exception e)
        {
          Logger.error("unable to display debug dialog",e);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Anzeigen der Diagnose-Informationen"), StatusBarMessage.TYPE_ERROR));
        }
      }
    
    });
    buttons.addButton(" " + i18n.tr("Spenden") + " ",new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
        new DonateView().handleAction(null);
      }
    },null,false,"emblem-special.png");
    buttons.addButton("   " + i18n.tr("Schlie�en") + "   ",new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,true);
    setSize(SWT.DEFAULT,460); // BUGZILLA 269
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.9  2010-10-05 21:39:18  willuhn
 * @C Doppelte Spenden-Funktion entfernt - jetzt nur noch ueber die DonateView
 *
 * Revision 1.8  2010-09-06 20:44:57  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2008/07/07 15:37:17  willuhn
 * @B Typo
 *
 * Revision 1.6  2008/05/06 10:10:56  willuhn
 * @N Diagnose-Dialog, mit dem man die JDBC-Verbindungsdaten (u.a. auch das JDBC-Passwort) ausgeben kann
 *
 * Revision 1.5  2008/04/15 16:16:34  willuhn
 * @B BUGZILLA 584
 *
 * Revision 1.4  2007/12/12 11:17:41  willuhn
 * @N Datenbank-Version in About-Dialog anzeigen
 *
 * Revision 1.3  2006/10/07 19:35:09  willuhn
 * @B Zugriff auf buildnumber hatte sich mit neuem Pluginloader geaendert
 *
 * Revision 1.2  2006/08/29 11:16:56  willuhn
 * @B Bug 269
 *
 * Revision 1.1  2005/11/07 18:51:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2005/07/14 20:24:34  web0
 * *** empty log message ***
 *
 * Revision 1.10  2005/07/14 18:03:54  web0
 * @N buildnumber/date in About-Dialog
 *
 * Revision 1.9  2005/03/31 23:05:46  web0
 * @N geaenderte Startseite
 * @N klickbare Links
 *
 * Revision 1.8  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.7  2004/10/11 22:41:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/10/08 13:37:48  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/07/25 17:15:05  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.4  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/06/08 22:28:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/05/18 22:45:24  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/05/18 22:40:59  willuhn
 * @N added about screen
 *
 **********************************************************************/