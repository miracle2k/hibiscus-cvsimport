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

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Repraesentiert eine Zeile in den Umsaetzen.
 */
public class UmsatzImpl extends AbstractDBObject implements Umsatz {

	I18N i18n;

  /**
   * @throws RemoteException
   */
  public UmsatzImpl() throws RemoteException {
    super();
    i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName() {
    return "umsatz";
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#getPrimaryField()
   */
  public String getPrimaryField() throws RemoteException {
    return "zweck";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#deleteCheck()
   */
  protected void deleteCheck() throws ApplicationException {
		// Die koennen eigentlich getrost geloescht werden.
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException {
		// Die Umsaetze werden nicht von Hand eingegeben sondern
		// gelangen via HBCI zu uns. Nichtsdestotrotz duerfen
		// wir nur die speichern, die vollstaendig sind.
		try {

			if (getBetrag() == 0.0)
				throw new ApplicationException(i18n.tr("Betrag fehlt."));

			if (getDatum() == null)
				throw new ApplicationException(i18n.tr("Datum fehlt."));

			if (getKonto() == null)
				throw new ApplicationException(i18n.tr("Umsatz muss einem Konto zugewiesen sein."));

			if (getValuta() == null)
				throw new ApplicationException(i18n.tr("Valuta fehlt."));

			if (getZweck() == null || getZweck().length() == 0)
				throw new ApplicationException(i18n.tr("Verwendungszweck fehlt."));
		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while insertcheck in umsatz",e);
			throw new ApplicationException(i18n.tr("Fehler beim Speichern des Umsatzes"));
		}
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException {
		insertCheck();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getForeignObject(java.lang.String)
   */
  protected Class getForeignObject(String field) throws RemoteException {
		if ("konto_id".equals(field))
			return Konto.class;
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getKonto()
   */
  public Konto getKonto() throws RemoteException {
    return (Konto) getField("konto_id");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getEmpfaengerName()
   */
  public String getEmpfaengerName() throws RemoteException {
    return (String) getField("empfaenger_name");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getEmpfaengerKonto()
   */
  public String getEmpfaengerKonto() throws RemoteException
  {
    return (String) getField("empfaenger_konto");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getEmpfaengerBLZ()
   */
  public String getEmpfaengerBLZ() throws RemoteException
  {
    return (String) getField("empfaenger_blz");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getBetrag()
   */
  public double getBetrag() throws RemoteException {
		Double d = (Double) getField("betrag");
		if (d == null)
			return 0;
		return d.doubleValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getDatum()
   */
  public Date getDatum() throws RemoteException {
		return (Date) getField("datum");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getValuta()
   */
  public Date getValuta() throws RemoteException {
		return (Date) getField("datum");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getZweck()
   */
  public String getZweck() throws RemoteException {
		return (String) getField("zweck");
  }

	/**
	 * @see de.willuhn.jameica.hbci.rmi.Umsatz#getZweck2()
	 */
	public String getZweck2() throws RemoteException {
		return (String) getField("zweck2");
	}

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setEmpfaengerName(java.lang.String)
   */
  public void setEmpfaengerName(String name) throws RemoteException {
		setField("empfaenger_name",name);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setEmpfaengerKonto(java.lang.String)
   */
  public void setEmpfaengerKonto(String konto) throws RemoteException {
    setField("empfaenger_konto",konto);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setEmpfaengerBLZ(java.lang.String)
   */
  public void setEmpfaengerBLZ(String blz) throws RemoteException {
    setField("empfaenger_blz",blz);
  }
  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setBetrag(double)
   */
  public void setBetrag(double d) throws RemoteException {
		setField("betrag",new Double(d));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setZweck(java.lang.String)
   */
  public void setZweck(String zweck) throws RemoteException {
		setField("zweck",zweck);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setZweck2(java.lang.String)
   */
  public void setZweck2(String zweck2) throws RemoteException {
		setField("zweck2",zweck2);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setDatum(java.util.Date)
   */
  public void setDatum(Date d) throws RemoteException {
		setField("datum",d);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setValuta(java.util.Date)
   */
  public void setValuta(Date d) throws RemoteException {
		setField("valuta",d);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setKonto(de.willuhn.jameica.hbci.rmi.Konto)
   */
  public void setKonto(Konto k) throws RemoteException {
		if (k == null)
			return;
		setField("konto_id",new Integer(k.getID()));
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.3  2004-03-06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.2  2004/03/05 08:38:47  willuhn
 * @N umsaetze works now
 *
 * Revision 1.1  2004/03/05 00:04:10  willuhn
 * @N added code for umsatzlist
 *
 **********************************************************************/