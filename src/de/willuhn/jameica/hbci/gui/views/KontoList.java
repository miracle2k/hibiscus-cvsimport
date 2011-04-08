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
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.KontoNew;
import de.willuhn.jameica.hbci.gui.action.PassportDetail;
import de.willuhn.jameica.hbci.gui.controller.KontoControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste mit den vorhandenen Bankverbindungen an.
 */
public class KontoList extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();


  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {

		final KontoControl control = new KontoControl(this);
		GUI.getView().setTitle(i18n.tr("Vorhandene Bankverbindungen"));

		control.getKontoListe().paint(getParent());
		
		ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Neue HBCI-Konfiguration anlegen..."),new PassportDetail(),null,false,"document-properties.png");
    buttons.addButton(i18n.tr("Konten aus HBCI-Konfiguration laden..."), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleReadFromPassport();
      }
    },null,false,"mail-send-receive.png");
		buttons.addButton(i18n.tr("Konto manuell anlegen"),new KontoNew(),null,false,"system-file-manager.png");

		buttons.paint(getParent());
  }
}


/**********************************************************************
 * $Log$
 * Revision 1.8  2011-04-08 15:19:13  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 * Revision 1.7  2010-09-29 23:43:34  willuhn
 * @N Automatisches Abgleichen und Anlegen von Konten aus KontoFetchFromPassport in KontoMerge verschoben
 * @N Konten automatisch (mit Rueckfrage) anlegen, wenn das Testen der HBCI-Konfiguration erfolgreich war
 * @N Config-Test jetzt auch bei Schluesseldatei
 * @B in PassportHandleImpl#getKonten() wurder der Converter-Funktion seit jeher die falsche Passport-Klasse uebergeben. Da gehoerte nicht das Interface hin sondern die Impl
 *
 * Revision 1.6  2009/05/06 23:11:23  willuhn
 * @N Mehr Icons auf Buttons
 *
 * Revision 1.5  2009/01/20 10:51:45  willuhn
 * @N Mehr Icons - fuer Buttons
 *
 * Revision 1.4  2006/01/18 00:51:00  willuhn
 * @B bug 65
 *
 * Revision 1.3  2005/06/21 21:48:24  web0
 * @B bug 80
 *
 * Revision 1.2  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.1  2004/11/13 17:12:15  willuhn
 * *** empty log message ***
 *
 * Revision 1.20  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.19  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2004/10/20 12:34:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/10/08 13:37:48  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/07/25 17:15:05  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.15  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/07/20 22:53:03  willuhn
 * @C Refactoring
 *
 * Revision 1.13  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 * Revision 1.12  2004/06/30 20:58:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/06/03 00:23:43  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/05/04 23:07:23  willuhn
 * @C refactored Passport stuff
 *
 * Revision 1.9  2004/04/27 22:23:56  willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
 *
 * Revision 1.8  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.7  2004/03/30 22:07:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/03/04 00:26:24  willuhn
 * @N Ueberweisung
 *
 * Revision 1.5  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.4  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 * Revision 1.3  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.2  2004/02/20 20:45:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 **********************************************************************/