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
package de.willuhn.jameica.hbci.gui.menus;

import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.ExtensionRegistry;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.DBObjectDelete;
import de.willuhn.jameica.hbci.gui.action.UmsatzTypNew;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Liefert ein vorgefertigtes Kontext-Menu, welches an Listen von Umsatz-Kategorien
 * angehaengt werden kann.
 */
public class UmsatzTypList extends ContextMenu implements Extendable
{

	private I18N i18n;

  /**
	 * Erzeugt ein Kontext-Menu fuer eine Liste von Umsaetzen.
	 */
	public UmsatzTypList()
	{
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		addItem(new OpenItem());
    addItem(new CheckedContextMenuItem(i18n.tr("L�schen..."), new DBObjectDelete()));
    // Wir geben das Context-Menu jetzt noch zur Erweiterung frei.
    ExtensionRegistry.extend(this);

	}

  /**
   * Ueberschrieben, um zu pruefen, ob ein Array oder ein einzelnes Element markiert ist.
   */
  private class OpenItem extends CheckedContextMenuItem
  {
    private OpenItem()
    {
      super(i18n.tr("�ffnen"),new UmsatzTypNew());
    }

    /**
     * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof UmsatzTyp[])
        return false;
      return super.isEnabledFor(o);
    }
  }

  /**
   * @see de.willuhn.jameica.gui.extension.Extendable#getExtendableID()
   */
  public String getExtendableID()
  {
    return this.getClass().getName();
  }
}


/**********************************************************************
 * $Log$
 * Revision 1.1  2006-11-23 17:25:38  willuhn
 * @N Umsatz-Kategorien - in PROGRESS!
 *
 **********************************************************************/