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
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Klasse fuer Ueberweisungen und Lastschriften.
 */
public abstract class AbstractBaseUeberweisungImpl extends AbstractHibiscusTransferImpl
  implements Terminable
{

  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @throws RemoteException
   */
  public AbstractBaseUeberweisungImpl() throws RemoteException {
    super();
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException {
    return "zweck";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException {
		try {
			if (!whileStore && ausgefuehrt())
				throw new ApplicationException(i18n.tr("Auftrag wurde bereits ausgef�hrt und kann daher nicht mehr ge�ndert werden."));
		}
		catch (RemoteException e)
		{
			Logger.error("error while checking ueberweisung",e);
			throw new ApplicationException(i18n.tr("Fehler beim Pr�fen des Auftrags."));
		}
		super.updateCheck();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insert()
   */
  public void insert() throws RemoteException, ApplicationException
  {
    if (getTermin() == null)
      setTermin(new Date());
    if (getAttribute("ausgefuehrt") == null) // Status noch nicht definiert
      setAttribute("ausgefuehrt",new Integer(0));
    super.insert();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Terminable#getTermin()
   */
  public Date getTermin() throws RemoteException {
    return (Date) getAttribute("termin");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Terminable#ausgefuehrt()
   */
  public boolean ausgefuehrt() throws RemoteException {
		Integer i = (Integer) getAttribute("ausgefuehrt");
		if (i == null)
			return false;
		return i.intValue() == 1;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Terminable#setTermin(java.util.Date)
   */
  public void setTermin(Date termin) throws RemoteException {
		setAttribute("termin",termin);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Terminable#ueberfaellig()
   */
  public boolean ueberfaellig() throws RemoteException {
    if (ausgefuehrt())
    	return false;
    Date termin = getTermin();
    if (termin == null)
    	return false;
    return (termin.before(new Date()));
  }

  // Kleines Hilfsboolean damit uns der Status-Wechsel
  // beim Speichern nicht um die Ohren fliegt.
  private boolean whileStore = false;

  /**
   * @see de.willuhn.jameica.hbci.rmi.Terminable#setAusgefuehrt()
   */
  public void setAusgefuehrt() throws RemoteException, ApplicationException
  {
    try
    {
      whileStore = true;
      setAttribute("ausgefuehrt",new Integer(1));
      store();
    }
    finally
    {
      whileStore = false;
    }
  }
}


/**********************************************************************
 * $Log$
 * Revision 1.11  2008-04-27 22:22:56  willuhn
 * @C I18N-Referenzen statisch
 *
 * Revision 1.10  2007/04/23 18:07:15  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.9  2006/12/01 00:02:34  willuhn
 * @C made unserializable members transient
 *
 * Revision 1.8  2006/06/08 22:29:47  willuhn
 * @N DTAUS-Import fuer Sammel-Lastschriften und Sammel-Ueberweisungen
 * @B Eine Reihe kleinerer Bugfixes in Sammeltransfers
 * @B Bug 197 besser geloest
 *
 * Revision 1.7  2006/02/20 17:33:08  willuhn
 * @B bug 197
 *
 * Revision 1.6  2005/06/23 21:13:03  web0
 * @B bug 84
 *
 * Revision 1.5  2005/06/23 17:36:33  web0
 * @B bug 84
 *
 * Revision 1.4  2005/05/30 22:55:27  web0
 * *** empty log message ***
 *
 * Revision 1.3  2005/03/02 17:59:30  web0
 * @N some refactoring
 *
 * Revision 1.2  2005/02/19 16:49:32  willuhn
 * @B bugs 3,8,10
 *
 * Revision 1.1  2005/02/04 18:27:54  willuhn
 * @C Refactoring zwischen Lastschrift und Ueberweisung
 *
 **********************************************************************/