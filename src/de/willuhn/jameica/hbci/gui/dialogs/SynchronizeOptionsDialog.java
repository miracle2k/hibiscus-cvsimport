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

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ein Dialog, ueber den die Synchronisierungs-Optionen fuer ein Konto eingestellt werden koennen.
 */
public class SynchronizeOptionsDialog extends AbstractDialog
{
  private I18N i18n   = null;
  private Konto konto = null;

  /**
   * ct.
   * @param konto das Konto.
   * @param position
   */
  public SynchronizeOptionsDialog(Konto konto, int position)
  {
    super(position);
    this.konto = konto;
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    this.setTitle(i18n.tr("Synchronisierungsoptionen"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent,i18n.tr("Optionen"));
    
    final String id = konto.getID();
    final Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
    final CheckboxInput umsatz = new CheckboxInput(settings.getBoolean("sync.konto." + id + ".umsatz",true));
    final CheckboxInput ueb    = new CheckboxInput(settings.getBoolean("sync.konto." + id + ".ueb",true));
    final CheckboxInput last   = new CheckboxInput(settings.getBoolean("sync.konto." + id + ".last",true));
    final CheckboxInput dauer  = new CheckboxInput(settings.getBoolean("sync.konto." + id + ".dauer",true));

    group.addText(i18n.tr("Bitte w�hlen Sie aus, welche Gesch�ftsvorf�lle bei\nder Synchronisierung des Kontos ausgef�hrt werden sollen."),false);
    group.addCheckbox(umsatz,i18n.tr("Saldo und Ums�tze abrufen"));
    group.addCheckbox(ueb   ,i18n.tr("�berf�llige �berweisungen absenden"));
    group.addCheckbox(last  ,i18n.tr("�berf�llige Lastschriften einziehen"));
    group.addCheckbox(dauer ,i18n.tr("Dauerauftr�ge synchronisieren"));
    
    ButtonArea buttons = new ButtonArea(parent,2);
    buttons.addButton(i18n.tr("�bernehmen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        settings.setAttribute("sync.konto." + id + ".umsatz", ((Boolean)umsatz.getValue()).booleanValue());
        settings.setAttribute("sync.konto." + id + ".ueb",    ((Boolean)ueb.getValue()).booleanValue());
        settings.setAttribute("sync.konto." + id + ".last",   ((Boolean)last.getValue()).booleanValue());
        settings.setAttribute("sync.konto." + id + ".dauer",  ((Boolean)dauer.getValue()).booleanValue());
        close();
      }
    });
    buttons.addButton(i18n.tr("Abbrechen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    });
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }

}


/*********************************************************************
 * $Log$
 * Revision 1.1  2006-03-21 00:43:14  willuhn
 * @B bug 209
 *
 **********************************************************************/