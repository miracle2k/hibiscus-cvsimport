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
package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.ExportDialog;
import de.willuhn.jameica.hbci.io.UmsatzTree;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action f�r die Ausgabe eine Umsatz-Kategorie-Liste
 */
public class UmsatzTypTreeExport implements Action
{
  /**
   * Erwartet ein Objekt vom Typ <code>GenericIterator</code>
   */
  public void handleAction(Object context) throws ApplicationException
  {
    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    if (context == null)
      throw new ApplicationException(i18n.tr("Bitte w�hlen Sie die zu exportierenden Ums�tze aus"));

    if (!(context instanceof UmsatzTree))
      throw new ApplicationException(i18n.tr("Bitte w�hlen Sie die zu exportierenden Ums�tze aus"));

    try
    {
      ExportDialog d = new ExportDialog(new UmsatzTree[]{(UmsatzTree)context}, UmsatzTree.class);
      d.open();
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("error while writing report", e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler bei der Erstellung der Liste"), StatusBarMessage.TYPE_ERROR));
    }
  }

}

/*******************************************************************************
 * $Log$
 * Revision 1.1  2007-05-02 11:18:04  willuhn
 * @C PDF-Export von Umsatz-Trees in IO-API gepresst ;)
 *
 * Revision 1.1  2007/04/29 10:18:46  jost
 * Neu: PDF-Ausgabe der Umsätze nach Kategorien
 *
 ******************************************************************************/
