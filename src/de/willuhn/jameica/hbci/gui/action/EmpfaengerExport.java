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

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.ExportDialog;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, ueber die Adressen exportieren werden koennen.
 * Als Parameter kann eine einzelnes Address-Objekt oder ein Array uebergeben werden.
 */
public class EmpfaengerExport implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Address</code> oder <code>Address[]</code>.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null)
			throw new ApplicationException(i18n.tr("Bitte w�hlen Sie mindestens eine Adresse aus"));

    // Der Check hier ist bewusst mit "isAssignable" weil das Array bei der
    // Existenz von externen Adressbuechern ggf. nicht Typsicher ist und
    // ein "(context instanceof Address[])" fehlschlagen wuerde
		if (!(context instanceof Address) && !(context.getClass().isAssignableFrom(Address[].class)))
			throw new ApplicationException(i18n.tr("Bitte w�hlen Sie einen oder mehrere Adressen aus"));

    Object[] u = null;
		try {

			if (context instanceof Address)
			{
				u = new Address[1];
        u[0] = (Address) context;
			}
      else
      {
        u = (Object[])context;
      }

      ExportDialog d = new ExportDialog(u, Address.class);
      d.open();
		}
		catch (ApplicationException ae)
		{
			throw ae;
		}
		catch (Exception e)
		{
			Logger.error("error while exporting addresses",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Exportieren der Adressen"));
		}
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.2  2007-04-23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.1  2006/10/05 16:42:28  willuhn
 * @N CSV-Import/Export fuer Adressen
 *
 **********************************************************************/