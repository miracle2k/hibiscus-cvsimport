/**********************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 * $Locker$
 * $State$
 *
 * Copyright (c) by  bbv AG
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.views;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.Headline;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Back;
import de.willuhn.jameica.hbci.gui.controller.SynchronizeControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * @author willuhn
 */
public class Synchronize extends AbstractView
{

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    GUI.getView().setTitle(i18n.tr("Konten synchronisieren"));
    
    final SynchronizeControl control = new SynchronizeControl(this);
    
    new Headline(getParent(),i18n.tr("Folgende Konten werden synchronisiert"));
    control.getKontoList().paint(getParent());

    Container c = new LabelGroup(getParent(),i18n.tr("Optionen"));
    
    c.addCheckbox(control.getSyncUeb(),i18n.tr("Offene f�llige �berweisungen senden"));
    c.addCheckbox(control.getSyncLast(),i18n.tr("Offene f�llige Lastschriften senden"));
    c.addCheckbox(control.getSyncDauer(),i18n.tr("Dauerauftr�ge synchronisieren"));
    
    ButtonArea b = new ButtonArea(getParent(),2);
    b.addButton(i18n.tr("Zur�ck"), new Back());
    b.addButton(i18n.tr("Synchronisierung starten"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleStart();
      }
    },null,true);
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException
  {
  }

}


/*********************************************************************
 * $Log$
 * Revision 1.2  2005-08-01 16:10:41  web0
 * @N synchronize
 *
 * Revision 1.1  2005/07/29 16:48:13  web0
 * @N Synchronize
 *
 *********************************************************************/