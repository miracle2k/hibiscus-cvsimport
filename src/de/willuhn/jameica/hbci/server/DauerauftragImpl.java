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
import java.util.zip.CRC32;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Implementierung eines Dauer-Auftrags.
 */
public class DauerauftragImpl extends AbstractTransferImpl implements Dauerauftrag
{

	private I18N i18n;
	
  /**
   * ct.
   * @throws RemoteException
   */
  public DauerauftragImpl() throws RemoteException
  {
    super();
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName()
  {
    return "dauerauftrag";
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException
  {
    return "zweck";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#deleteCheck()
   */
  protected void deleteCheck() throws ApplicationException
  {
  	// koennen getrost geloescht werden - sind ja eh nur Spiegeldaten
  	// die wir jederzeit wieder von der Bank holen koennen
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Dauerauftrag#getErsteZahlung()
   */
  public Date getErsteZahlung() throws RemoteException
  {
    return (Date) getAttribute("erste_zahlung");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Dauerauftrag#getLetzteZahlung()
   */
  public Date getLetzteZahlung() throws RemoteException
  {
		return (Date) getAttribute("letzte_zahlung");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Dauerauftrag#getTurnus()
   */
  public Turnus getTurnus() throws RemoteException
  {
    return (Turnus) getAttribute("turnus_id");
  }

	/**
   * @see de.willuhn.jameica.hbci.rmi.Dauerauftrag#isActive()
   */
	public boolean isActive() throws RemoteException
	{
		Integer i = (Integer) getAttribute("aktiv");
		if (i == null)
			return false;
		return i.intValue() == 1;
	}

  /**
   * @see de.willuhn.jameica.hbci.rmi.Dauerauftrag#setErsteZahlung(java.util.Date)
   */
  public void setErsteZahlung(Date datum) throws RemoteException
  {
  	setAttribute("erste_zahlung",datum);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Dauerauftrag#setLetzteZahlung(java.util.Date)
   */
  public void setLetzteZahlung(Date datum) throws RemoteException
  {
		setAttribute("letzte_zahlung",datum);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Dauerauftrag#setTurnus(de.willuhn.jameica.hbci.rmi.Turnus)
   */
  public void setTurnus(Turnus turnus) throws RemoteException
  {
  	setAttribute("turnus_id",turnus);
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getForeignObject(java.lang.String)
   */
  protected Class getForeignObject(String field) throws RemoteException
  {
  	if ("turnus_id".equals(field))
  		return Turnus.class;
    return super.getForeignObject(field);
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException
  {
		try {
			if (getTurnus() == null || getTurnus().getID() == null)
				throw new ApplicationException(i18n.tr("Bitte w�hlen Sie einen Zahlungsturnus aus"));
			if (getErsteZahlung() == null)
				throw new ApplicationException(i18n.tr("Bitte geben Sie ein Datum f�r die erste Zahlung an"));

			// Jetzt muessen wir noch checken, ob sich das Datum nicht in der Vergangenheit
			// befindet. Hierzu koennen wir aber nicht das aktuelle Datum als Vergleich nehmen
			// da das bereits einige Sekunden _nach_ dem Datum der ersten Zahlung liegt.
			// Daher lassen wir 1 Tag Toleranz zu.
// TODO Macht Probleme beim Abrufen
//			Date today = new Date(System.currentTimeMillis() - (1000l * 60 * 60 * 24));
//			if (getErsteZahlung().before(today))
//				throw new ApplicationException(i18n.tr("Bitte w�hlen Sie f�r die erste Zahlung ein Datum in der Zukunft"));
		
			// Und jetzt noch checken, dass sich das Datum der letzten Zahlung
			// hinter der ersten Zahlung befindet
			if (getLetzteZahlung() != null && !getLetzteZahlung().after(getErsteZahlung()))
				throw new ApplicationException(i18n.tr("Bei Angabe eines Datum f�r die letzte Zahlung muss dieses nach der ersten Zahlung liegen"));
		}
		catch (RemoteException e)
		{
			Logger.error("error while insert check in DauerAuftrag",e);
			throw new ApplicationException(i18n.tr("Fehler bei der Pr�fung des Dauerauftrags"));
		}
    super.insertCheck();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException
  {
    super.updateCheck();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Checksum#getChecksum()
   */
  public long getChecksum() throws RemoteException
  {
		String ersteZahlung  = getErsteZahlung() == null ? "" : HBCI.DATEFORMAT.format(getErsteZahlung());
		String letzteZahlung = getLetzteZahlung() == null ? "" : HBCI.DATEFORMAT.format(getLetzteZahlung());
		String s = getTurnus().getChecksum() +
							 getBetrag() +
							 getEmpfaengerBLZ() +
							 getEmpfaengerKonto() +
							 getEmpfaengerName() +
							 getKonto().getChecksum() +
							 getZweck() +
							 getZweck2() +
							 ersteZahlung +
							 letzteZahlung;
		CRC32 crc = new CRC32();
		crc.update(s.getBytes());
		return crc.getValue();
  }

  /**
   * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
   */
  public boolean equals(GenericObject o) throws RemoteException
  {
		if (o == null)
			return false;
		try {
			Dauerauftrag other = (Dauerauftrag) o;
			return other.getChecksum() == getChecksum();
		}
		catch (ClassCastException e)
		{
			return false;
		}
  }
  
  /**
   * Markiert den Dauerauftrag als "aktiv".
   */
  protected void activate() throws RemoteException
  {
  	// TODO Das gefaellt mir noch nicht.
  	setAttribute("aktiv",new Integer(1));
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.7  2004-10-17 16:28:46  willuhn
 * @N Die ersten Dauerauftraege abgerufen ;)
 *
 * Revision 1.6  2004/08/18 23:13:51  willuhn
 * @D Javadoc
 *
 * Revision 1.5  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.4  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/07/15 23:39:22  willuhn
 * @N TurnusImpl
 *
 * Revision 1.2  2004/07/13 22:20:37  willuhn
 * @N Code fuer DauerAuftraege
 * @C paar Funktionsnamen umbenannt
 *
 * Revision 1.1  2004/07/11 16:14:29  willuhn
 * @N erster Code fuer Dauerauftraege
 *
 **********************************************************************/