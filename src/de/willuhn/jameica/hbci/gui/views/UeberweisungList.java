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
import de.willuhn.jameica.hbci.gui.action.UeberweisungImport;
import de.willuhn.jameica.hbci.gui.action.UeberweisungNew;
import de.willuhn.jameica.hbci.gui.controller.UeberweisungControl;
import de.willuhn.jameica.hbci.io.print.PrintSupportUeberweisungList;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste mit den vorhandenen Ueberweisungen an.
 */
public class UeberweisungList extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    UeberweisungControl control = new UeberweisungControl(this);
    final de.willuhn.jameica.hbci.gui.parts.UeberweisungList table = control.getUeberweisungListe();
    
		GUI.getView().setTitle(i18n.tr("Vorhandene �berweisungen"));
    GUI.getView().addPanelButton(new PanelButtonPrint(new PrintSupportUeberweisungList(table))
    {
      public boolean isEnabled()
      {
        return table.getSelection() != null && super.isEnabled();
      }
    });
		
		table.paint(getParent());

		ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Importieren..."),new UeberweisungImport(),null,false,"document-open.png");
		buttons.addButton(i18n.tr("Neue �berweisung"),new UeberweisungNew(),null,true,"text-x-generic.png");
		
		buttons.paint(getParent());
  }
}


/**********************************************************************
 * $Log$
 * Revision 1.14  2011-04-08 17:41:45  willuhn
 * @N Erster Druck-Support fuer Ueberweisungslisten
 *
 * Revision 1.13  2011-04-08 15:19:14  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 **********************************************************************/