/**********************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 * $Locker$
 * $State$
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.TabFolder;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Headline;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Back;
import de.willuhn.jameica.hbci.gui.controller.KontoauszugControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Zeigt Kontoausz�ge an und gibt gibt sie in eine PDF-Datei aus.
 */
public class KontoauszugList extends AbstractView
{
  private I18N i18n = null;

  /**
   * ct.
   */
  public KontoauszugList()
  {
    super();
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    GUI.getView().setTitle(i18n.tr("Kontoausz�ge"));

    final KontoauszugControl control = new KontoauszugControl(this);

    /////////////////////////////////////////////////////////////////
    // Tab-Container
    TabFolder folder = new TabFolder(getParent(), SWT.NONE);
    folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    folder.setBackground(Color.BACKGROUND.getSWTColor());

    TabGroup zeitraum = new TabGroup(folder,i18n.tr("Konto/Zeitraum"));
    zeitraum.addLabelPair(i18n.tr("Konto"), control.getKontoAuswahl());
    zeitraum.addLabelPair(i18n.tr("Start-Datum"), control.getStart());
    zeitraum.addLabelPair(i18n.tr("End-Datum"), control.getEnd());
    
    TabGroup gegenkonto = new TabGroup(folder,i18n.tr("Gegenkonto"));
    gegenkonto.addLabelPair(i18n.tr("Kontonummer enth�lt"),           control.getGegenkontoNummer());    
    gegenkonto.addLabelPair(i18n.tr("BLZ enth�lt"),                   control.getGegenkontoBLZ());    
    gegenkonto.addLabelPair(i18n.tr("Name des Kontoinhabers enth�lt"),control.getGegenkontoName());

    new Headline(getParent(),i18n.tr("Gefundene Ums�tze"));
    control.getUmsatzList().paint(getParent());

    ButtonArea buttons = new ButtonArea(getParent(), 2);
    buttons.addButton(i18n.tr("Zur�ck"),new Back());
    buttons.addButton(i18n.tr("Kontoauszug exportieren..."), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handlePrint();
      }
    },null,true);
  }

}

/*******************************************************************************
 * $Log$
 * Revision 1.7  2007-04-26 15:02:19  willuhn
 * @N Zusaetzliche Suche nach Gegenkonto
 *
 * Revision 1.6  2007/03/21 16:56:56  willuhn
 * @N Online-Hilfe aktualisiert
 * @N Bug 337 (Stichtag in Sparquote)
 * @C Refactoring in Sparquote
 *
 * Revision 1.5  2007/03/21 15:37:46  willuhn
 * @N Vorschau der Umsaetze in Auswertung "Kontoauszug"
 *
 * Revision 1.4  2006/07/03 23:04:32  willuhn
 * @N PDF-Reportwriter in IO-API gepresst, damit er auch an anderen Stellen (z.Bsp. in der Umsatzliste) mitverwendet werden kann.
 *
 * Revision 1.3  2006/06/19 16:20:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2006/05/15 20:14:51  jost
 * Ausgabe -> PDF-Ausgabe
 * Revision 1.1 2006/05/14 19:53:09 jost
 * Prerelease Kontoauszug-Report Revision 1.4 2006/01/18 00:51:00
 ******************************************************************************/
