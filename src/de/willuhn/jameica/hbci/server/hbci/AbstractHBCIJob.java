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
package de.willuhn.jameica.hbci.server.hbci;

import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.kapott.hbci.GV_Result.HBCIJobResult;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.hbci.HBCIJob;

/**
 * Basis-Klasse fuer die HBCI-Jobs.
 */
public abstract class AbstractHBCIJob implements HBCIJob {

	private org.kapott.hbci.GV.HBCIJob job = null;
	private Konto konto = null;

	private Hashtable params 			= new Hashtable(); 
	private Hashtable kontoParams = new Hashtable(); 

	/**
	 * ct.
   * @param konto
   */
  public AbstractHBCIJob(Konto konto)
	{
		super();
		this.konto = konto;
	}

  /**
   * @see de.willuhn.jameica.hbci.rmi.hbci.HBCIJob#getIdentifier()
   */
  public abstract String getIdentifier();

  /**
   * @see de.willuhn.jameica.hbci.rmi.hbci.HBCIJob#setJob(org.kapott.hbci.GV.HBCIJob)
   */
  public final void setJob(org.kapott.hbci.GV.HBCIJob job) throws RemoteException
  {
  	this.job = job;
  	Enumeration e = params.keys();
  	while (e.hasMoreElements())
  	{
  		String name = (String) e.nextElement();
  		String value = (String) params.get(name);
  		job.setParam(name,value);
  	}

		Enumeration e2 = kontoParams.keys();
		while (e2.hasMoreElements())
		{
			String name = (String) e2.nextElement();
			org.kapott.hbci.structures.Konto konto = (org.kapott.hbci.structures.Konto) kontoParams.get(name);
			job.setParam(name,konto);
		}

  }

	/**
	 * @see de.willuhn.jameica.hbci.rmi.hbci.HBCIJob#getKonto()
	 */
	public final Konto getKonto() {
		return konto;
	}

  /**
   * Liefert das Job-Resultat.
   * @return Job-Resultat.
   */
  final HBCIJobResult getJobResult()
	{
		return job.getJobResult();
	}
	
	/**
	 * Liefert den Status-Text.
   * @return
   */
  final String getStatusText()
	{
		return getJobResult().getJobStatus().getRetVals()[0].text;
	}

	/**
	 * Ueber diese Funktion muessen die konkreten Implementierungen
	 * alle zusaetzlichen Job-Parameter setzen.
   * @param name Name des Parameters.
   * @param value Wert des Parameters.
   */
  final void setJobParam(String name, String value)
	{
		if (name == null || value == null)
		{
			Application.getLog().warn("job parameter invalid. name: " + name + ", value: " + value);
			return;
		}
		params.put(name,value);
	}

	/**
	 * Ueber diese Funktion muessen die konkreten Implementierungen
	 * alle zusaetzlichen Job-Parameter setzen.
	 * @param name Name des Parameters.
	 * @param konto das Konto.
	 */
	final void setJobParam(String name, org.kapott.hbci.structures.Konto konto)
	{
		if (name == null || konto == null)
		{
			Application.getLog().warn("job parameter invalid. name: " + name + ", konto: " + konto);
			return;
		}
		kontoParams.put(name,konto);
	}

	/**
	 * Fuegt zum Konto einen Protokoll-Eintrag mit dem genannten Kommentar hinzu.
   * @param kommentar zu speichernder Kommentar.
   * @param protokollTyp Protokoll-Typ (siehe Konstanten in <code>Protokoll</code>).
   */
  final void addToProtokoll(String kommentar, int protokollTyp)
	{
		if (kommentar == null || kommentar.length() == 0)
			return;

		try {
			Protokoll entry = (Protokoll) Settings.getDatabase().createObject(Protokoll.class,null);
			entry.setKonto(this.getKonto());
			entry.setKommentar(kommentar);
			entry.setTyp(protokollTyp);
			entry.store();
		}
		catch (Exception e)
		{
			// Es macht keinen Sinn, hier die Exception nach oben zu reichen.
			// Was sollte in diesem Fall sinnvolles gemacht werden? Den gesamten
			// HBCI-Job abbrechen? Nene, dann lieber auf den Log-Eintrag verzichten
			// und nur ins Application-Log schreiben. ;)
			Application.getLog().error("error while writing protocol",e);
		}
		
	}
}


/**********************************************************************
 * $Log$
 * Revision 1.4  2004-05-25 23:23:18  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 * Revision 1.3  2004/04/24 19:04:51  willuhn
 * @N Ueberweisung.execute works!! ;)
 *
 * Revision 1.2  2004/04/22 23:46:50  willuhn
 * @N UeberweisungJob
 *
 * Revision 1.1  2004/04/19 22:05:51  willuhn
 * @C HBCIJobs refactored
 *
 **********************************************************************/