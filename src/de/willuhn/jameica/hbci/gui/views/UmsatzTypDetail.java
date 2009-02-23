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
import de.willuhn.jameica.gui.internal.buttons.Back;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.DBObjectDelete;
import de.willuhn.jameica.hbci.gui.controller.UmsatzTypControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Detail-Ansicht einer Umsatz-Kategorie.
 */
public class UmsatzTypDetail extends AbstractView
{
  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();


    final UmsatzTypControl control = new UmsatzTypControl(this);

		GUI.getView().setTitle(i18n.tr("Umsatz-Kategorie"));
		
    LabelGroup group = new LabelGroup(getParent(),i18n.tr("Eigenschaften"));
    group.addLabelPair(i18n.tr("Bezeichnung"), control.getName());
//    group.addLabelPair(i18n.tr("�bergeordnete Kategorie"), control.getParent());
    group.addLabelPair(i18n.tr("Nummer"), control.getNummer());
    group.addLabelPair(i18n.tr("Suchbegriff"), control.getPattern());
    group.addCheckbox(control.getRegex(),i18n.tr("Suchbegriff ist ein regul�rer Ausdruck"));
    group.addSeparator();
    group.addLabelPair(i18n.tr("Art des Umsatzes"), control.getArt());

    
    ButtonArea buttons = new ButtonArea(getParent(),3);
    buttons.addButton(new Back(false));
    buttons.addButton(i18n.tr("L�schen"),   new DBObjectDelete(),control.getCurrentObject());
    buttons.addButton(i18n.tr("Speichern"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleStore();
      }
    },null,true);
  }
}


/**********************************************************************
 * $Log$
 * Revision 1.5  2009-02-23 23:44:50  willuhn
 * @N Etwas Code fuer Support fuer Unter-/Ober-Kategorien
 *
 * Revision 1.4  2009/01/20 10:51:45  willuhn
 * @N Mehr Icons - fuer Buttons
 *
 * Revision 1.3  2007/03/10 07:18:14  jost
 * Neu: Nummer für die Sortierung der Umsatz-Kategorien
 *
 * Revision 1.2  2006/11/23 23:24:17  willuhn
 * @N Umsatz-Kategorien: DB-Update, Edit
 *
 * Revision 1.1  2006/11/23 17:25:37  willuhn
 * @N Umsatz-Kategorien - in PROGRESS!
 *
 **********************************************************************/