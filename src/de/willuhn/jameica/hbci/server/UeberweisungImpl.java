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

import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Empfaenger;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.hbci.server.hbci.*;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Eine Ueberweisung.
 */
public class UeberweisungImpl
  extends AbstractDBObject
  implements Ueberweisung {

	private boolean inExecute = false;
	
	private I18N i18n;

  /**
   * @throws RemoteException
   */
  public UeberweisungImpl() throws RemoteException {
    super();
    i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName() {
    return "ueberweisung";
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
		try {
			if (ausgefuehrt())
				throw new ApplicationException(i18n.tr("Bereits ausgef�hrte �berweisungen k�nnen nicht gel�scht werden."));
		}
		catch (RemoteException e)
		{
			throw new ApplicationException(i18n.tr("Fehler beim L�schen der �berweisung."));
		}
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException {
  	try {
			if (getBetrag() == 0.0)
				throw new ApplicationException("Bitte geben Sie einen g�ltigen Betrag ein.");

			if (getKonto() == null)
				throw new ApplicationException("Bitte w�hlen Sie ein Konto aus.");
			if (getKonto().isNewObject())
				throw new ApplicationException("Bitte speichern Sie zun�chst das Konto");

			if (getEmpfaengerKonto() == null || "".equals(getEmpfaengerKonto()))
				throw new ApplicationException("Bitte geben Sie die Kontonummer des Empf�ngers ein");
			
			if (getEmpfaengerBlz() == null || "".equals(getEmpfaengerBlz()))
				throw new ApplicationException("Bitte geben Sie die BLZ des Empf�ngers ein");

			if (!HBCIUtils.checkAccountCRC(getEmpfaengerBlz(),getEmpfaengerKonto()))
				throw new ApplicationException("Ung�ltige BLZ/Kontonummer. Bitte pr�fen Sie Ihre Eingaben.");
				
			if (getZweck() == null || "".equals(getZweck()))
				throw new ApplicationException("Bitte geben Sie einen Verwendungszweck ein");

			if (getZweck().length() > 35)
				throw new ApplicationException("Bitten geben Sie als Verwendungszweck maximal 35 Zeichen an");
				
			if (getZweck2() != null && getZweck2().length() > 35)
				throw new ApplicationException("Bitten geben Sie als weiteren Verwendungszweck maximal 35 Zeichen an");

			if (getTermin() == null)
				throw new ApplicationException("Bitte geben Sie einen Termin an, zu dem die �berweisung ausgef�hrt werden soll.");
  	}
  	catch (RemoteException e)
  	{
  		Application.getLog().error("error while checking ueberweisung",e);
  		throw new ApplicationException("Fehler beim Pr�fen der �berweisung.");
  	}
			
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException {
		try {
			if (ausgefuehrt() && !inExecute)
				throw new ApplicationException("Die �berweisung wurde bereits ausgef�hrt und kann daher nicht mehr ge�ndert werden.");
		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while checking ueberweisung",e);
			throw new ApplicationException("Fehler beim Pr�fen der �berweisung.");
		}
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
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#getKonto()
   */
  public Konto getKonto() throws RemoteException {
    return (Konto) getField("konto_id");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#getBetrag()
   */
  public double getBetrag() throws RemoteException {
		Double d = (Double) getField("betrag");
		if (d == null)
			return 0;
		return d.doubleValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#getZweck()
   */
  public String getZweck() throws RemoteException {
    return (String) getField("zweck");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#getZweck2()
   */
  public String getZweck2() throws RemoteException {
		return (String) getField("zweck2");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#getTermin()
   */
  public Date getTermin() throws RemoteException {
    return (Date) getField("termin");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#ausgefuehrt()
   */
  public boolean ausgefuehrt() throws RemoteException {
		Integer i = (Integer) getField("ausgefuehrt");
		if (i == null)
			return false;
		return i.intValue() == 1;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#setKonto(de.willuhn.jameica.hbci.rmi.Konto)
   */
  public void setKonto(Konto konto) throws RemoteException {
		if (konto == null) return;
		setField("konto_id",new Integer(konto.getID()));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#setBetrag(double)
   */
  public void setBetrag(double betrag) throws RemoteException {
		setField("betrag", new Double(betrag));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#setZweck(java.lang.String)
   */
  public void setZweck(String zweck) throws RemoteException {
		setField("zweck",zweck);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#setZweck2(java.lang.String)
   */
  public void setZweck2(String zweck2) throws RemoteException {
		setField("zweck2",zweck2);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#setTermin(java.util.Date)
   */
  public void setTermin(Date termin) throws RemoteException {
		setField("termin",termin);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#execute()
   */
  public synchronized void execute() throws ApplicationException, RemoteException {

		if (isNewObject())
			store();
	
		try {

			HBCIFactory factory = HBCIFactory.getInstance();
			HBCIUeberweisungJob job = new HBCIUeberweisungJob(getKonto());

			Empfaenger empfaenger = (Empfaenger) Settings.getDatabase().createObject(Empfaenger.class,null);
			empfaenger.setBLZ(getEmpfaengerBlz());
			empfaenger.setKontonummer(getEmpfaengerKonto());
			empfaenger.setName(getEmpfaengerName());
			
			job.setEmpfaenger(empfaenger);

			job.setBetrag(getBetrag());
			job.setZweck(getZweck());
			job.setZweck2(getZweck2());
			
			factory.addJob(job);
			factory.executeJobs(getKonto().getPassport().getHandle());

			// Wenn der Job nicht erfolgreich war, fliegt hier eine ApplikationException
			// mit der Fehlermeldung der Bank.
			job.check();

			// wenn alles erfolgreich verlief, koennen wir die Ueberweisung auf
			// Status "ausgefuehrt" setzen.
			inExecute = true; // ist noetig, weil uns sonst das updateCheck() um die Ohren fliegt
			setField("ausgefuehrt",new Integer(1));
			store();
		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while executing ueberweisung",e);
			throw new ApplicationException(i18n.tr("Fehler beim Ausfuehren der �berweisung"));
		}
		finally {
			inExecute = false;
		}
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#store()
   */
  public void store() throws RemoteException, ApplicationException {
		if (isNewObject())
		{
			if (getTermin() == null) setTermin(new Date());
			setField("ausgefuehrt",new Integer(0));
		}
    super.store();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#getEmpfaengerKonto()
   */
  public String getEmpfaengerKonto() throws RemoteException {
    return (String) getField("empfaenger_konto");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#getEmpfaengerBlz()
   */
  public String getEmpfaengerBlz() throws RemoteException {
		return (String) getField("empfaenger_blz");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#getEmpfaengerName()
   */
  public String getEmpfaengerName() throws RemoteException {
		return (String) getField("empfaenger_name");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#setEmpfaengerKonto(java.lang.String)
   */
  public void setEmpfaengerKonto(String konto) throws RemoteException {
		setField("empfaenger_konto",konto);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#setEmpfaengerBlz(java.lang.String)
   */
  public void setEmpfaengerBlz(String blz) throws RemoteException {
		setField("empfaenger_blz",blz);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#setEmpfaengerName(java.lang.String)
   */
  public void setEmpfaengerName(String name) throws RemoteException {
		setField("empfaenger_name",name);
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.8  2004-04-22 23:46:50  willuhn
 * @N UeberweisungJob
 *
 * Revision 1.7  2004/04/19 22:05:51  willuhn
 * @C HBCIJobs refactored
 *
 * Revision 1.6  2004/04/14 23:53:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/04/05 23:28:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.3  2004/03/05 00:04:10  willuhn
 * @N added code for umsatzlist
 *
 * Revision 1.2  2004/02/17 01:01:38  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/