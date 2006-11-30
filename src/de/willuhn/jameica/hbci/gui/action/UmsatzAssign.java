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
package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.UmsatzTypAuswahlDialog;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ordnet ein oder meheren Umsaetzen eine Kategorie zu.
 */
/**
 */
public class UmsatzAssign implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Transfer</code>.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null)
      throw new ApplicationException(i18n.tr("Bitte w�hlen Sie einen oder mehrere Ums�tze aus"));

    if (!(context instanceof Umsatz) && !(context instanceof Umsatz[]))
      throw new ApplicationException(i18n.tr("Bitte w�hlen Sie einen oder mehrere Ums�tze aus"));

    Umsatz[] umsaetze = null;
    
    if (context instanceof Umsatz)
      umsaetze = new Umsatz[]{(Umsatz) context};
    else
      umsaetze = (Umsatz[]) context;

    if (umsaetze.length == 0)
      throw new ApplicationException(i18n.tr("Bitte w�hlen Sie einen oder mehrere Ums�tze aus"));
      
		try
    {
      UmsatzTypAuswahlDialog d = new UmsatzTypAuswahlDialog(UmsatzTypAuswahlDialog.POSITION_CENTER);
      UmsatzTyp ut = (UmsatzTyp) d.open();

      umsaetze[0].transactionBegin();
      for (int i=0;i<umsaetze.length;++i)
      {
        umsaetze[i].setUmsatzTyp(ut);
        umsaetze[i].store();
      }
      umsaetze[0].transactionCommit();
      
      if (ut == null)
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Zuordnung der Kategorie entfernt"), StatusBarMessage.TYPE_SUCCESS));
      else
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Umsatz-Kategorie {0} zugeordnet", ut.getName()), StatusBarMessage.TYPE_SUCCESS));
    }
    catch (OperationCanceledException oce)
    {
      rollback(umsaetze[0]);
      return;
    }
		catch (ApplicationException ae)
		{
      rollback(umsaetze[0]);
			throw ae;
		}
		catch (Exception e)
		{
      rollback(umsaetze[0]);
			Logger.error("error while assigning umsaetze",e);
			Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Zuordnen der Umsatz-Kategorie"), StatusBarMessage.TYPE_ERROR));
		}
  }
  
  private void rollback(DBObject o)
  {
    if (o == null)
      return;
    try
    {
      Logger.info("rollback transaction");
      o.transactionRollback();
    }
    catch (Exception e)
    {
      Logger.error("unable to rollback transaction - useless",e);
    }
  }
}


/**********************************************************************
 * $Log$
 * Revision 1.1  2006-11-30 23:48:40  willuhn
 * @N Erste Version der Umsatz-Kategorien drin
 *
 **********************************************************************/