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
import java.util.Properties;
import java.util.Vector;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.manager.HBCIHandler;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProgressMonitor;
import de.willuhn.jameica.hbci.PassportRegistry;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Diese Klasse ist fuer die Ausfuehrung der HBCI-Jobs zustaendig.
 * <b>Hinweis:</b>: Die Factory speichert grundsaetzlich keine Objekte
 * in der Datenbank. Das ist Sache des Aufrufers. Hier werden lediglich
 * die HBCI-Jobs ausgefuehrt.
 */
public class HBCIFactory {


	private static boolean inProgress = false;


	private static I18N i18n;
	private static HBCIFactory factory;
  	private Vector jobs = new Vector();
		private Vector exclusiveJobs = new Vector();
    private Worker worker = null;

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

		jobs.add(job);
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

		exclusiveJobs.add(job);
	}

  /**
	 * Fuehrt alle Jobs aus, die bis dato geadded wurden.
	 * @param konto Konto, ueber das die Jobs abgewickelt werden sollen.
   * @param l ein optionaler Listener, der ausgefuehrt werden soll, wenn die HBCI-Factory fertig ist.
	 * @throws ApplicationException Bei Benutzer-Fehlern (zB kein HBCI-Medium konfiguriert).
   * @throws OperationCanceledException Wenn der User den Vorgang abbricht.
	 */
	public synchronized void executeJobs(final Konto konto, Listener l) throws
		ApplicationException,
		OperationCanceledException
	{

    if (konto == null)
      throw new ApplicationException(i18n.tr("Kein Konto ausgew�hlt"));

    this.worker = new Worker(konto,l);
    this.worker.start();
	}
	
  /**
   * Prueft, ob gerade HBCI-Auftraege verarbeitet werden.
   * @return true, wenn gerade Auftraege verarbeitet werden.
   */
  public boolean inProgress()
  {
    return inProgress;
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
   * Liefert den Progress-Monitor, der Informationen ueber den aktuellen HBCI-Verarbeitungszustand erhaelt.
   * @return Progress-Monitor.
   */
  public ProgressMonitor getProgressMonitor()
  {
    return this.worker.getMonitor();
  }
  
  /**
	 * Liefert eine Liste aller bankspezifischen Restriktionen fuer den angegebenen
	 * Geschaeftsvorfall auf diesem Passport.
	 * Sie werden intern weiterverarbeitet, um zum Beispiel die Auswahlmoeglichkeiten
	 * in der Benutzeroberflaeche auf die tatsaechlichen Moeglichkeiten der Bank zu beschraenken.
   * @param job zu testender Job.
	 * @param h der Passport, ueber den der Job getestet werden soll.
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

    if (this.worker != null)
		  this.worker.interrupt();
	}

  /**
   * Liefert das aktuell verwendete Konto.
   * Es wird nur dann ein Konto geliefert, wenn sich die HBCIFactory gerade
   * in der Ausfuehrung von Jobs befindet (executeJobs()). Ansonsten liefert
   * die Funktion immer null.
   * @return das aktuelle Konto.
   */
  public Konto getCurrentKonto()
  {
    return this.worker.getKonto();
  }
  
  

  /**
   * Wir haben den Code zur Ausfuehrung in einen eigenen Thread verlagert
   * damit die GUI waehrenddessen nicht blockiert.
   */
  private class Worker extends Thread
  {
    private Konto konto             = null;
    private Listener listener       = null;

    private ProgressMonitor monitor = null;

    private Passport passport       = null;
    private PassportHandle handle   = null;
    private HBCIHandler handler     = null;

    private boolean error           = false;
    
    private Worker(Konto konto, Listener l)
    {
      this.konto = konto;
      this.listener = l;
      this.monitor = new HBCIProgressMonitor();
    }

    private Konto getKonto()
    {
      return this.konto;
    }

    private ProgressMonitor getMonitor()
    {
      return this.monitor;
    }
    /**
     * @see java.lang.Runnable#run()
     */
    public synchronized void run()
    {
      int status = ProgressMonitor.STATUS_RUNNING;

      try
      {
        HBCIFactory.this.start();
        GUI.getStatusBar().startProgress();
        
        ////////////////////////////////////////////////////////////////////////
        // Passport erzeugen
        getMonitor().setStatusText(i18n.tr("Lade HBCI-Sicherheitsmedium"));
        getMonitor().addPercentComplete(2);
        
        GUI.getDisplay().syncExec(new Runnable() {
          public void run()
          {
            try
            {
              passport = PassportRegistry.findByClass(konto.getPassportClass());
              // BUGZILLA #7 http://www.willuhn.de/bugzilla/show_bug.cgi?id=7
              getMonitor().setStatusText(i18n.tr("Initialisiere HBCI-Sicherheitsmedium"));

              passport.init(konto);
            }
            catch (ApplicationException ae)
            {
              getMonitor().setStatusText(ae.getMessage());
              error = true;
            }
            catch (Exception e)
            {
              Logger.error("unable to init passport",e);
              getMonitor().setStatusText(i18n.tr("Fehler beim Initialisieren des Sicherheitsmediums"));
              error = true;
            }
          }
        });
        if (error) return;

        if (passport == null)
        {
          Logger.error("no passport available");
          getMonitor().setStatusText(i18n.tr("Kein Sicherheitsmedium angegeben"));
          error = true;
          return;
        }
        //
        ////////////////////////////////////////////////////////////////////////
        

        ////////////////////////////////////////////////////////////////////////
        // PassportHandle erzeugen
        getMonitor().setStatusText(i18n.tr("Erzeuge HBCI-Handle"));
        getMonitor().addPercentComplete(2);

        GUI.getDisplay().syncExec(new Runnable() {
          public void run()
          {
            try
            {
              handle = passport.getHandle();
            }
            catch (RemoteException e1)
            {
              Logger.error("unable to create HBCI handle",e1);
              getMonitor().setStatusText(i18n.tr("HBCI-Medium kann nicht initialisiert werden"));
              error = true;
            }
          }
        });
        if (error) return;

        if (handle == null)
        {
          Logger.error("unable to create HBCI handle");
          getMonitor().setStatusText(i18n.tr("HBCI-Medium kann nicht initialisiert werden"));
          error = true;
          return;
        }
        //
        ////////////////////////////////////////////////////////////////////////
        

        ////////////////////////////////////////////////////////////////////////
        // Jobs checken
        if (jobs.size() == 0 && exclusiveJobs.size() == 0)
        {
          Logger.warn("no hbci jobs defined");
          getMonitor().setStatusText(i18n.tr("Keine auszuf�hrenden HBCI-Auftr�ge angegeben"));
          error = true;
          return;
        }
        //
        ////////////////////////////////////////////////////////////////////////

        
        ////////////////////////////////////////////////////////////////////////
        // HBCI-Verbindung aufbauen
        getMonitor().setStatusText(i18n.tr("�ffne HBCI-Verbindung"));
        getMonitor().addPercentComplete(2);

        GUI.getDisplay().syncExec(new Runnable() {
          public void run()
          {
            try
            {
              handler = handle.open();
            }
            catch (ApplicationException ae)
            {
              getMonitor().setStatusText(ae.getMessage());
              error = true;
            }
            catch (Exception e)
            {
              Logger.error("unable to open handle",e);
              getMonitor().setStatusText(i18n.tr("Fehler beim �ffnen der HBCI-Verbindung"));
              error = true;
            }
          }
        });
        if (error) return;
        //
        ////////////////////////////////////////////////////////////////////////

        
        ////////////////////////////////////////////////////////////////////////
        // Exclusive Jobs erzeugen
        Logger.info("processing exclusive jobs");
        for (int i=0;i<exclusiveJobs.size();++i)
        {
          final AbstractHBCIJob job = (AbstractHBCIJob) exclusiveJobs.get(i);
          
          getMonitor().setStatusText(i18n.tr("Aktiviere HBCI-Job: \"{0}\"",job.getIdentifier()));
          getMonitor().addPercentComplete(2);

          Logger.info("executing exclusive job " + job.getIdentifier());
          HBCIJob j = handler.newJob(job.getIdentifier());
          dumpJob(j);
          job.setJob(j);
          handler.addJob(j);
          handler.newMsg();
        }
        //
        ////////////////////////////////////////////////////////////////////////

        
        ////////////////////////////////////////////////////////////////////////
        // Regulaere Jobs erzeugen
        Logger.info("processing batch jobs");

        for (int i=0;i<jobs.size();++i)
        {
          final AbstractHBCIJob job = (AbstractHBCIJob) jobs.get(i);
          
          getMonitor().setStatusText(i18n.tr("Aktiviere HBCI-Job: \"{0}\"",job.getIdentifier()));
          getMonitor().addPercentComplete(2);

          Logger.info("adding job " + job.getIdentifier() + " to queue");
          HBCIJob j = handler.newJob(job.getIdentifier());
          dumpJob(j);
          job.setJob(j);
          handler.addJob(j);
        }
        //
        ////////////////////////////////////////////////////////////////////////

        
        ////////////////////////////////////////////////////////////////////////
        // Jobs ausfuehren
        Logger.info("executing jobs");
        getMonitor().setStatusText(i18n.tr("F�hre HBCI-Jobs aus"));
        getMonitor().addPercentComplete(4);
        handler.execute();
        getMonitor().setStatusText(i18n.tr("HBCI-Jobs ausgef�hrt"));
        getMonitor().addPercentComplete(4);
        //
        ////////////////////////////////////////////////////////////////////////


        ////////////////////////////////////////////////////////////////////////
        // Job-Ergebnisse auswerten
        for (int i=0;i<exclusiveJobs.size();++i)
        {
          final AbstractHBCIJob job = (AbstractHBCIJob) exclusiveJobs.get(i);
          getMonitor().setStatusText(i18n.tr("Werte Ergebnis von HBCI-Job \"{0}\" aus",job.getIdentifier()));
          getMonitor().addPercentComplete(2);
          Logger.info("executing check for exclusive job " + job.getIdentifier());
          job.handleResult();
        }

        for (int i=0;i<jobs.size();++i)
        {
          final AbstractHBCIJob job = (AbstractHBCIJob) jobs.get(i);
          getMonitor().setStatusText(i18n.tr("Werte Ergebnis von HBCI-Job \"{0}\" aus",job.getIdentifier()));
          getMonitor().addPercentComplete(2);
          Logger.info("executing check for job " + job.getIdentifier());
          job.handleResult();
        }
        //
        ////////////////////////////////////////////////////////////////////////
        
      }
      catch (OperationCanceledException e3)
      {
        getMonitor().setStatusText(i18n.tr("HBCI-�bertragung abgebrochen"));
        getMonitor().setStatus(ProgressMonitor.STATUS_CANCEL);
      }
      catch (ApplicationException ae)
      {
        getMonitor().setStatusText(ae.getMessage());
        error = true;
      }
      catch (Throwable t)
      {
        Logger.error("error while executing hbci jobs",t);
        getMonitor().setStatusText(i18n.tr("Fehler beim Ausf�hren der HBCI-Auftr�ge {0}", t.toString()));
        error = true;
      }
      finally
      {
        try
        {
          getMonitor().setStatusText(i18n.tr("Beende HBCI-�bertragung"));
          getMonitor().addPercentComplete(2);
          jobs = new Vector(); // Jobqueue leer machen.
          exclusiveJobs = new Vector(); // Jobqueue leer machen.
          try {
            if (handle != null)
              handle.close();
          }
          catch (Throwable t) {/* useless*/}

          String msg = null;

          if (error)
          {
            status = ProgressMonitor.STATUS_ERROR;
            msg = "HBCI-�bertragung mit Fehlern beendet";
          }
          else if (isInterrupted())
          {
            status = ProgressMonitor.STATUS_CANCEL;
            msg = "HBCI-�bertragung abgebrochen";
          }
          else
          {
            status = ProgressMonitor.STATUS_DONE;
            msg = "HBCI-�bertragung erfolgreich beendet";
          }
          getMonitor().setStatus(status);
          getMonitor().setStatusText(i18n.tr(msg));
          getMonitor().setPercentComplete(100);
        }
        finally
        {
          HBCIFactory.this.stop();
          GUI.getStatusBar().stopProgress();
          if (this.listener != null)
          {
            Event e = new Event();
            e.type = status;
            this.listener.handleEvent(e);
          }
        }
      }
    }
  }
}


/**********************************************************************
 * $Log$
 * Revision 1.33  2005-07-26 23:57:18  web0
 * @N Restliche HBCI-Jobs umgestellt
 *
 * Revision 1.32  2005/07/26 23:00:03  web0
 * @N Multithreading-Support fuer HBCI-Jobs
 *
 * Revision 1.31  2005/06/21 20:11:10  web0
 * @C cvs merge
 *
 * Revision 1.30  2005/06/15 16:10:48  web0
 * @B javadoc fixes
 *
 * Revision 1.29  2005/05/19 23:31:07  web0
 * @B RMI over SSL support
 * @N added handbook
 *
 * Revision 1.28  2005/05/10 22:26:15  web0
 * @B bug 71
 *
 * Revision 1.27  2005/05/06 14:05:04  web0
 * *** empty log message ***
 *
 * Revision 1.26  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.25  2005/03/06 16:33:57  web0
 * @B huu, job results of exclusive jobs were not executed
 *
 * Revision 1.24  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.23  2005/02/28 23:59:57  web0
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