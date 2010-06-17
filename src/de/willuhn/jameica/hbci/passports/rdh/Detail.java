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
package de.willuhn.jameica.hbci.passports.rdh;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.buttons.Back;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.Headline;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.DialogFactory;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Detail-Ansicht eines Passports.
 */
public class Detail extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    try
    {
      final Controller control = new Controller(this);

      GUI.getView().setTitle(i18n.tr("Schl�ssel-Details"));

      ColumnLayout layout = new ColumnLayout(getParent(),2);

      {
        Container group = new SimpleContainer(layout.getComposite());
        group.addHeadline(i18n.tr("Verbindungsdaten zur Bank"));
        group.addInput(control.getHBCIUrl());
        group.addInput(control.getHBCIPort());
        group.addInput(control.getHBCIVersion());
      }
      
      {
        Container group = new SimpleContainer(layout.getComposite());
        group.addHeadline(i18n.tr("Benutzerdaten"));
        group.addInput(control.getBenutzerkennung());
        group.addInput(control.getKundenkennung());
        group.addInput(control.getBLZ());
      }
      
      {
        Container group = new SimpleContainer(getParent());
        group.addHeadline(i18n.tr("Erweiterte Einstellungen"));
        group.addInput(control.getAlias()); // BUGZILLA 72
        group.addInput(control.getPath()); // BUGZILLA 148
      }

      ButtonArea buttons = new ButtonArea(getParent(),6);
      buttons.addButton(new Back(true));
      buttons.addButton(i18n.tr("Passwort �ndern"),new Action()
      {
        public void handleAction(Object context) throws ApplicationException
        {
          control.changePassword();
        }
      },null,false,"seahorse-preferences.png");
      buttons.addButton(i18n.tr("BPD/UPD anzeigen"),new Action()
      {
        public void handleAction(Object context) throws ApplicationException
        {
          control.handleDisplayProperties();
        }
      },null,false,"text-x-generic.png");
      buttons.addButton(i18n.tr("Signatur-ID synchronisieren"),new Action()
      {
        public void handleAction(Object context) throws ApplicationException
        {
          control.syncSigId();
        }
      },null,false,"view-refresh.png");
      buttons.addButton(i18n.tr("INI-Brief anzeigen/erzeugen"),new Action()
      {
        public void handleAction(Object context) throws ApplicationException
        {
          control.startIniLetter();
        }
      },null,false,"stock_keyring.png");
      buttons.addButton(i18n.tr("Speichern"),new Action()
      {
        public void handleAction(Object context) throws ApplicationException
        {
          control.handleStore();
        }
      },null,false,"document-save.png");

      new Headline(getParent(),i18n.tr("Fest zugeordnete Konten"));
      control.getKontoAuswahl().paint(getParent());
      
      // Ggf. angezeigten Fehlertext von vorher loeschen
      Application.getMessagingFactory().sendMessage(new StatusBarMessage("",StatusBarMessage.TYPE_SUCCESS));
    }
    catch (Exception e)
    {
      Logger.error("unable to load key",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Laden des Schl�ssels: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
      DialogFactory.clearPINCache();
      GUI.startPreviousView();
    }
  }
}


/**********************************************************************
 * $Log$
 * Revision 1.1  2010-06-17 11:26:48  willuhn
 * @B In HBCICallbackSWT wurden die RDH-Passports nicht korrekt ausgefiltert
 * @C komplettes Projekt "hbci_passport_rdh" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 * @N BUGZILLA 312
 * @N Neue Icons in Schluesselverwaltung
 * @N GUI-Polish in Schluesselverwaltung
 *
 * Revision 1.24  2009/06/16 14:04:30  willuhn
 * @N Dialog zum Anzeigen der BPD/UPD
 *
 * Revision 1.23  2009/03/04 22:49:16  willuhn
 * @C INI-Brief anzeigen/drucken nur noch in Detail-Ansicht
 * @B falsche Button-Anzahl
 *
 * Revision 1.22  2009/03/04 22:37:05  willuhn
 * @N sync sig id (siehe http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?p=55851#55851)
 **********************************************************************/