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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.manager.HBCIHandler;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Diese Klasse ist fuer die Ausfuehrung der HBCI-Jobs zustaendig.
 * <b>Hinweis:</b>: Die Factory speichert grundsaetzlich keine Objekte
 * in der Datenbank. Das ist Sache des Aufrufers. Hier werden lediglich
 * die HBCI-Jobs ausgefuehrt.
 */
public class HBCIFactory {


	private static boolean inProgress = false;
	private static boolean cancelled  = false;

	private Object mutex = new Object();

	private static I18N i18n;
	private static HBCIFactory factory;
  	private ArrayList jobs = new ArrayList();
		private ArrayList exclusiveJobs = new ArrayList();

  /**
   * ct.
   */
  private HBCIFactory() {
  	i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

	/**
	 * Erzeugt eine neue Instanz der HBCIFactory oder liefert die
	 * existierende zurueck.
   * @return Instanz der Job-Factory.
   */
  public static synchronized HBCIFactory getInstance()
	{
		if (factory != null)
			return factory;

		factory = new HBCIFactory();
		return factory;			
	}

	/**
	 * Fuegt einen weiteren Job zur Queue hinzu.
   * @param job auszufuehrender Job.
   * @throws ApplicationException
   */
  public synchronized void addJob(AbstractHBCIJob job) throws ApplicationException
	{
		if (inProgress)
			throw new ApplicationException(i18n.tr("Es l�uft bereits eine andere HBCI-Abfrage."));

		synchronized(mutex)
		{
			jobs.add(job);
		}
	}

	/**
	 * Fuegt einen weiteren Job zur Queue hinzu.
	 * Dieser Job wird jedoch separat ausgefuehrt. Jobs, die ueber <code>addJob</code>
	 * hinzugefuegt wurden, werden en bloc vom HBCI-System ausgefuehrt. Jobs, die
	 * ueber diese Methode hier hinzugefuegt werden, werden alle einzeln ausgefuehrt.
	 * @param job auszufuehrender Job.
	 * @throws ApplicationException
	 */
	public synchronized void addExclusiveJob(AbstractHBCIJob job) throws ApplicationException
	{
		if (inProgress)
			throw new ApplicationException(i18n.tr("Es l�uft bereits eine andere HBCI-Abfrage."));

		synchronized(mutex)
		{
			exclusiveJobs.add(job);
		}
	}

  /**
	 * Fuehrt alle Jobs aus, die bis dato geadded wurden.
	 * @param handle der Passport, ueber den die Jobs ausgefuehrt werden sollen.
	 * @throws ApplicationException Bei Benutzer-Fehlern (zB kein HBCI-Medium konfiguriert).
	 * @throws RemoteException Fehler beim Zugriff auf Fachobjekte.
   * @throws OperationCanceledException Wenn der User den Vorgang abbricht.
	 */
	public synchronized void executeJobs(PassportHandle handle) throws
		ApplicationException,
		RemoteException,
		OperationCanceledException
	{

		if (handle == null)
			throw new ApplicationException(i18n.tr("Kein HBCI-Medium ausgew�hlt"));

		synchronized(mutex)
		{

			if (jobs.size() == 0 && exclusiveJobs.size() == 0)
			{
				Logger.warn("no jobs defined");
				return;
			}

			start();

			try {


				HBCIHandler handler = handle.open();

				Logger.info("processing exclusive jobs");
				for (int i=0;i<exclusiveJobs.size();++i)
				{
					final AbstractHBCIJob job = (AbstractHBCIJob) exclusiveJobs.get(i);
					
					Logger.info("executing exclusive job " + job.getIdentifier());
					HBCIJob j = handler.newJob(job.getIdentifier());
					dumpJob(j);
					job.setJob(j);
					handler.addJob(j);
					handler.execute();
					if (cancelled)
					{
						cancelled = false;
						throw new OperationCanceledException();
					}
					Logger.info("executing check for job " + job.getIdentifier());
					job.handleResult();
				}


				Logger.info("processing batch jobs");

				for (int i=0;i<jobs.size();++i)
				{
					final AbstractHBCIJob job = (AbstractHBCIJob) jobs.get(i);
					
					Logger.info("adding job " + job.getIdentifier() + " to queue");
					HBCIJob j = handler.newJob(job.getIdentifier());
					dumpJob(j);
					job.setJob(j);
					handler.addJob(j);
				}

				Logger.info("executing jobs");
				handler.execute();
				if (cancelled)
				{
					cancelled = false;
					throw new OperationCanceledException();
				}
				for (int i=0;i<jobs.size();++i)
				{
					AbstractHBCIJob job = (AbstractHBCIJob) jobs.get(i);
					Logger.info("executing check for job " + job.getIdentifier());
					job.handleResult();
				}
			}
			catch (RemoteException e)
			{
				throw e;
			}
			catch (ApplicationException e2)
			{
				throw e2;
			}
			catch (OperationCanceledException e3)
			{
				throw e3;
			}
			catch (Throwable t)
			{
				Logger.error("error while executing jobs",t);
				throw new ApplicationException(i18n.tr("Fehler beim Ausf�hren der Auftr�ge. Fehlermeldung: {0}",t.getMessage()),t);
			}
			finally
			{
				stop();
				jobs = new ArrayList(); // Jobqueue leer machen.
				exclusiveJobs = new ArrayList(); // Jobqueue leer machen.
				try {
					handle.close();
				}
				catch (Throwable t) {/* useless*/}
			}
		}
	}
	
	/**
	 * Gibt Informationen ueber den Job im Log aus.
   * @param job Job.
   */
  private void dumpJob(HBCIJob job)
	{
		Logger.debug("Job restrictions for " + job.getName());
		Properties p = job.getJobRestrictions();
		Enumeration en = p.keys();
		while (en.hasMoreElements())
		{
			String key = (String) en.nextElement();
			Logger.debug("  " + key + ": " + p.getProperty(key));
		}
	}
	
	/**
	 * Liefert eine Liste aller bankspezifischen Restriktionen fuer den angegebenen
	 * Geschaeftsvorfall auf diesem Passport.
	 * Sie werden intern weiterverarbeitet, um zum Beispiel die Auswahlmoeglichkeiten
	 * in der Benutzeroberflaeche auf die tatsaechlichen Moeglichkeiten der Bank zu beschraenken.
   * @param job zu testender Job.
	 * @param handle der Passport, ueber den der Job getestet werden soll.
   * @return Liste der Restriktionen.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public synchronized Properties getJobRestrictions(AbstractHBCIJob job, PassportHandle h)
		throws ApplicationException, RemoteException
	{
		if (job == null)
			throw new ApplicationException(i18n.tr("Kein Job ausgew�hlt"));

		if (h == null)
			throw new ApplicationException(i18n.tr("Kein Sicherheitsmedium ausgew�hlt"));

		start();

		try {
		
			HBCIHandler handler = h.open();
			HBCIJob j = handler.newJob(job.getIdentifier());
			return j.getJobRestrictions();
		}
		finally
		{
			stop();
			try {
				h.close();
			}
			catch (Throwable t) {/* useless*/}
		}
	}

	/**
	 * Schliesst den aktuellen Job.
	 * Muss von jeder Funktion in diese Factory aufgerufen werden, wenn Sie mit
	 * ihrer Taetigkeit fertig ist (daher sinnvollerweise im finally()) um
	 * die Factory fuer die naechsten Jobs freizugeben.
   */
  private synchronized void stop()
	{
		inProgress = false;
	}
	
	/**
	 * Setzt die Factory auf den Status &quot;inProgress&quot; oder wirft
	 * eine ApplicationException, wenn gerade ein anderer Job laeuft.
	 * Diese Funktion muss von jeder Funktion der Factory ausgefuehrt werden,
	 * bevor sie mit ihrer Taetigkeit beginnt. Somit ist sichergestellt,
	 * dass nie zwei Jobs gleichzeitig laufen.
   * @throws ApplicationException
   */
  private synchronized void start() throws ApplicationException
	{
		if (inProgress)
			throw new ApplicationException(i18n.tr("Es l�uft bereits eine andere HBCI-Abfrage."));

		inProgress = true;
		
	}
	
	/**
   * Teilt der HBCIFactory mit, dass die gerade laufende Aktion vom Benutzer
   * abgebrochen wurde. Wird aus dem HBCICallBack heraus aufgerufen.
   */
  public synchronized void markCancelled()
	{
		if (!inProgress)
			return; // hier gibts gar nichts abzubrechen ;)
		cancelled = true;
	}

}


/**********************************************************************
 * $Log$
 * Revision 1.23  2005-02-28 23:59:57  web0
 * @B http://www.willuhn.de/bugzilla/show_bug.cgi?id=15
 *
 * Revision 1.22  2005/02/01 17:15:37  willuhn
 * *** empty log message ***
 *
 * Revision 1.21  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.20  2004/11/12 18:25:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.19  2004/11/04 22:30:33  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2004/11/02 18:48:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/10/29 00:32:32  willuhn
 * @N HBCI job restrictions
 *
 * Revision 1.16  2004/10/26 23:47:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2004/10/25 22:39:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.13  2004/10/24 17:19:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/10/19 23:33:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 * Revision 1.10  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.9  2004/07/21 23:54:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/06/30 20:58:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/06/10 20:56:33  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.6  2004/05/05 22:14:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/05/04 23:07:23  willuhn
 * @C refactored Passport stuff
 *
 * Revision 1.4  2004/04/27 22:23:56  willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
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
 * Revision 1.1  2004/04/14 23:53:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/04/05 23:28:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/04/04 18:30:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/03/11 08:55:42  willuhn
 * @N UmsatzDetails
 *
 * Revision 1.7  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.6  2004/03/05 00:19:23  willuhn
 * @D javadoc fixes
 * @C Converter moved into server package
 *
 * Revision 1.5  2004/03/05 00:04:10  willuhn
 * @N added code for umsatzlist
 *
 * Revision 1.4  2004/02/21 19:49:04  willuhn
 * @N PINDialog
 *
 * Revision 1.3  2004/02/20 01:25:25  willuhn
 * *** empty log message ***
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