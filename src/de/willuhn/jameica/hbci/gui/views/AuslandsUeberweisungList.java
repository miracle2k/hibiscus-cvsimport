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
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.parts.PanelButtonPrint;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.AuslandsUeberweisungImport;
import de.willuhn.jameica.hbci.gui.action.AuslandsUeberweisungNew;
import de.willuhn.jameica.hbci.gui.controller.AuslandsUeberweisungControl;
import de.willuhn.jameica.hbci.io.print.PrintSupportAuslandsUeberweisungList;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste mit den vorhandenen Auslandsueberweisungen an.
 */
public class AuslandsUeberweisungList extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    AuslandsUeberweisungControl control = new AuslandsUeberweisungControl(this);
    final de.willuhn.jameica.hbci.gui.parts.AuslandsUeberweisungList table = control.getAuslandsUeberweisungListe();

    GUI.getView().setTitle(i18n.tr("Vorhandene SEPA-�berweisungen"));
    GUI.getView().addPanelButton(new PanelButtonPrint(new PrintSupportAuslandsUeberweisungList(table))
    {
      public boolean isEnabled()
      {
        return table.getSelection() != null && super.isEnabled();
      }
    });
    
    table.paint(getParent());
		
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Importieren..."),new AuslandsUeberweisungImport(),null,false,"document-open.png");
    buttons.addButton(i18n.tr("Neue SEPA-�berweisung"), new AuslandsUeberweisungNew(),null,true,"text-x-generic.png");

    buttons.paint(getParent());
  }
}


/**********************************************************************
 * $Log$
 * Revision 1.5  2011-04-11 14:36:37  willuhn
 * @N Druck-Support fuer Lastschriften und SEPA-Ueberweisungen
 *
 * Revision 1.4  2011-04-08 15:19:14  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 * Revision 1.3  2009/10/20 23:12:58  willuhn
 * @N Support fuer SEPA-Ueberweisungen
 * @N Konten um IBAN und BIC erweitert
 *
 * Revision 1.2  2009/05/06 23:11:23  willuhn
 * @N Mehr Icons auf Buttons
 *
 * Revision 1.1  2009/03/13 00:25:12  willuhn
 * @N Code fuer Auslandsueberweisungen fast fertig
 *
 **********************************************************************/