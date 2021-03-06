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

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit Ueberweisungen.
 */
public class UeberweisungList extends AbstractTransferList
{

  /**
   * @param action
   * @throws RemoteException
   */
  public UeberweisungList(Action action) throws RemoteException
  {
    super(action);
    setContextMenu(new de.willuhn.jameica.hbci.gui.menus.UeberweisungList());
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.parts.AbstractTransferList#getList(java.util.Date, java.util.Date, java.lang.String)
   */
  protected DBIterator getList(Date from, Date to, String text) throws RemoteException
  {
    DBIterator list = super.getList(from, to, text);
    if (text != null && text.length() > 0)
    {
      String s = "%" + text.toLowerCase() + "%";
      list.addFilter("(LOWER(empfaenger_konto) like ? or LOWER(empfaenger_name) like ? or LOWER(zweck) like ? or LOWER(zweck2) like ? or LOWER(zweck3) like ?)", new Object[]{s,s,s,s,s});
    }
    return list;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.parts.AbstractTransferList#getObjectType()
   */
  protected Class getObjectType()
  {
    return Ueberweisung.class;
  }
}


/**********************************************************************
 * $Log$
 * Revision 1.11  2010-08-16 11:13:52  willuhn
 * @N In den Auftragslisten kann jetzt auch nach einem Text gesucht werden
 *
 * Revision 1.10  2007/04/24 16:55:00  willuhn
 * @N Aktualisierte Daten nur bei geaendertem Datum laden
 *
 * Revision 1.9  2007/04/19 18:12:21  willuhn
 * @N MySQL-Support (GUI zum Konfigurieren fehlt noch)
 *
 * Revision 1.8  2006/10/17 00:04:31  willuhn
 * @N new Formatters in Transfer-Listen
 * @N merged UeberweisungList + LastschriftList into AbstractTransferList
 *
 * Revision 1.7  2006/06/08 22:29:47  willuhn
 * @N DTAUS-Import fuer Sammel-Lastschriften und Sammel-Ueberweisungen
 * @B Eine Reihe kleinerer Bugfixes in Sammeltransfers
 * @B Bug 197 besser geloest
 *
 * Revision 1.6  2006/06/06 22:41:26  willuhn
 * @N Generische Loesch-Action fuer DBObjects (DBObjectDelete)
 * @N Live-Aktualisierung der Tabelle mit den importierten Ueberweisungen
 * @B Korrekte Berechnung des Fortschrittsbalken bei Import
 *
 * Revision 1.5  2006/05/11 16:53:09  willuhn
 * @B bug 233
 *
 * Revision 1.4  2006/03/30 21:00:11  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2005/06/27 15:35:27  web0
 * @B bug 84
 *
 * Revision 1.2  2005/06/23 21:13:03  web0
 * @B bug 84
 *
 * Revision 1.1  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 **********************************************************************/