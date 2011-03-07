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
import org.kapott.hbci.exceptions.JobNotSupportedException;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.PassportRegistry;
import de.willuhn.jameica.hbci.gui.DialogFactory;
import de.willuhn.jameica.hbci.messaging.HBCIFactoryMessage;
import de.willuhn.jameica.hbci.messaging.HBCIFactoryMessage.Status;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Diese Klasse ist fuer die Ausfuehrung der HBCI-Jobs zustaendig. <b>Hinweis:</b>:
 * Die Factory speichert grundsaetzlich keine Objekte in der Datenbank. Das ist
 * Sache des Aufrufers. Hier werden lediglich die HBCI-Jobs ausgefuehrt.
 */
public class HBCIFactory {


  private static boolean inProgress = false;
  
  
  private static I18N i18n;
  private static HBCIFactory factory;
  	private Vector jobs = new Vector();
    private Worker worker = null;
    private Listener listener = null;

  /**
   * ct.
   */
  private HBCIFactory() {
  	i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * Erzeugt eine neue Instanz der HBCIFactory oder liefert die existierende
   * zurueck.
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
    {
      Logger.write(Level.DEBUG,"hbci factory in progress - informative stacktrace",new Exception());
      throw new ApplicationException(i18n.tr("Es l�uft bereits eine andere HBCI-Abfrage."));
    }
  
  	jobs.add(job);
  }

  /**
   * Fuehrt alle Jobs aus, die bis dato geadded wurden.
   * 
   * @param konto Konto, ueber das die Jobs abgewickelt werden sollen.
   * @param l ein optionaler Listener, der ausgefuehrt werden soll, wenn die
   * HBCI-Factory fertig ist.
   * @throws ApplicationException Bei Benutzer-Fehlern (zB kein HBCI-Medium konfiguriert).
   * @throws OperationCanceledException Wenn der User den Vorgang abbricht.
   */
  public synchronized void executeJobs(final Konto konto, Listener l) throws
  	ApplicationException,
  	OperationCanceledException
  {
  
    if (konto == null)
      throw new ApplicationException(i18n.tr("Kein Konto ausgew�hlt"));
    
    try
    {
      if ((konto.getFlags() & Konto.FLAG_DISABLED) != 0)
        throw new ApplicationException(i18n.tr("Das Konto ist deaktiviert"));

      if ((konto.getFlags() & Konto.FLAG_OFFLINE) != 0)
        throw new ApplicationException(i18n.tr("Das Konto ist ein Offline-Konto"));
    }
    catch (RemoteException re)
    {
      Logger.error("unable to check flags",re);
      throw new ApplicationException(i18n.tr("Fehler beim Pr�fen des Kontos: {0}",re.getMessage()));
    }
  
    this.listener = l;
    this.worker = new Worker(konto);
    Application.getController().start(this.worker);
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
   * Liefert den Progress-Monitor, der Informationen ueber den aktuellen
   * HBCI-Verarbeitungszustand erhaelt.
   * @return Progress-Monitor.
   */
  public ProgressMonitor getProgressMonitor()
  {
    if (this.worker == null || this.worker.getMonitor() == null)
      return new ProgressMonitor() {
        public void setPercentComplete(int arg0) {}
        public void addPercentComplete(int arg0) {}
        public int getPercentComplete() {return 0;}
        public void setStatus(int arg0) {}
        public void setStatusText(String arg0) {}
        public void log(String arg0) {}
      };
    return this.worker.getMonitor();
  }
  
  /**
   * Liefert eine Liste aller bankspezifischen Restriktionen fuer den
   * angegebenen Geschaeftsvorfall auf diesem Passport. Sie werden intern
   * weiterverarbeitet, um zum Beispiel die Auswahlmoeglichkeiten in der
   * Benutzeroberflaeche auf die tatsaechlichen Moeglichkeiten der Bank zu
   * beschraenken.
   * @param konto das zugehoerige Konto.
   * @param job zu testender Job.
   * @return Liste der Restriktionen.
   * @throws Exception
   */
  public synchronized Properties getJobRestrictions(Konto konto, AbstractHBCIJob job) throws Exception
  {
    if (konto == null)
      throw new ApplicationException(i18n.tr("Kein Sicherheitsmedium ausgew�hlt"));

    if (job == null)
      throw new ApplicationException(i18n.tr("Kein Job ausgew�hlt"));

    if ((konto.getFlags() & Konto.FLAG_OFFLINE) != 0)
      return new Properties();

    Logger.info("checking job restrictions");
    PassportHandle ph = null;
    try {
      Passport passport = PassportRegistry.findByClass(konto.getPassportClass());
      passport.init(konto);
      ph = passport.getHandle();

      this.worker = new Worker(konto); // BUGZILLA 490 Nicht starten, nur erzeugen
      HBCIHandler handler = ph.open();
      HBCIJob j = handler.newJob(job.getIdentifier());
      return j.getJobRestrictions();
    }
    catch (Exception e)
    {
      // Checken, ob es eine JobNotSupportedException ist
      Throwable t = getCause(e,JobNotSupportedException.class);
      if (t != null)
      {
        Logger.error(t.getMessage(),t);
        throw new ApplicationException(i18n.tr("Dieser Gesch�ftsvorfall wird von Ihrer Bank leider nicht unterst�tzt."));
      }
      throw e;
    }
    finally
    {
      this.worker = null;
      try {
        if (ph != null)
          ph.close();
      }
      catch (Throwable t) {
        Logger.error("error while closing hbci handler",t);
      }
      Logger.info("job restrictions checked");
    }
  }

  /**
   * Schliesst den aktuellen Job. Muss von jeder Funktion in diese Factory
   * aufgerufen werden, wenn Sie mit ihrer Taetigkeit fertig ist (daher
   * sinnvollerweise im finally()) um die Factory fuer die naechsten Jobs
   * freizugeben.
   * @param status 
   */
  private synchronized void stop(final int status)
  {
    Logger.info("stopping hbci factory");
    inProgress = false;
    this.worker = null;
    this.jobs.clear();

    if (this.listener != null)
    {
      Logger.info("init listener");
      Runnable r = new Runnable()
      {
        public void run()
        {
          Event e = new Event();
          e.type = status;
          Logger.info("executing listener");
          listener.handleEvent(e);
        }
      };
      if (Application.inServerMode()) r.run();
      else GUI.getDisplay().asyncExec(r);
    }
    Application.getMessagingFactory().sendMessage(new HBCIFactoryMessage(Status.STOPPED));
    Logger.info("finished");
  }
	
  /**
   * Setzt die Factory auf den Status &quot;inProgress&quot; oder wirft eine
   * ApplicationException, wenn gerade ein anderer Job laeuft. Diese Funktion
   * muss von jeder Funktion der Factory ausgefuehrt werden, bevor sie mit ihrer
   * Taetigkeit beginnt. Somit ist sichergestellt, dass nie zwei Jobs
   * gleichzeitig laufen.
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
   * Liefert true, wenn der Benutzer die gerade laufende Aktion abgebrochen hat.
   * @return true, wenn der Benutzer die gerade laufende Aktion abgebrochen hat.
   * BUGZILLA 690
   */
  public boolean isCancelled()
  {
    return this.worker != null && this.worker.isInterrupted();
  }
  

  /**
   * Liefert das aktuell verwendete Konto. Es wird nur dann ein Konto geliefert,
   * wenn sich die HBCIFactory gerade in der Ausfuehrung von Jobs befindet
   * (executeJobs()). Ansonsten liefert die Funktion immer null.
   * @return das aktuelle Konto.
   */
  public Konto getCurrentKonto()
  {
    if (this.worker == null)
      return null;
    return this.worker.getKonto();
  }
  
  /**
   * Wir haben den Code zur Ausfuehrung in einen eigenen Thread verlagert damit
   * die GUI waehrenddessen nicht blockiert.
   */
  private class Worker implements BackgroundTask
  {
    private Konto konto             = null;

    private ProgressMonitor monitor = null;
    private Passport passport       = null;
    private PassportHandle handle   = null;
    private HBCIHandler handler     = null;

    private boolean error           = false;
    private boolean interrupted     = false;
    
    private Worker(Konto konto)
    {
      this.konto = konto;
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
     * @see de.willuhn.jameica.system.BackgroundTask#run(de.willuhn.util.ProgressMonitor)
     */
    public synchronized void run(final ProgressMonitor monitor) throws ApplicationException
    {
      this.monitor = monitor;
      int status = ProgressMonitor.STATUS_RUNNING;

      try
      {
        StringBuffer sb = new StringBuffer();
        sb.append(konto.getBezeichnung());
        String blz = HBCIUtils.getNameForBLZ(konto.getBLZ());
        if (blz != null && blz.length() > 0)
          sb.append(" [" + blz + "]");
        final String kn = sb.toString();
        
        if (interrupted) return;
        
        // //////////////////////////////////////////////////////////////////////
        // Jobs checken
        if (jobs.size() == 0)
        {
          Logger.warn("no hbci jobs defined");
          monitor.setStatusText(i18n.tr("{0}: Keine auszuf�hrenden HBCI-Auftr�ge angegeben",kn));
          return;
        }
        //
        // //////////////////////////////////////////////////////////////////////

        HBCIFactory.this.start();
        if (!Application.inServerMode())
          GUI.getStatusBar().startProgress();
        
        // //////////////////////////////////////////////////////////////////////
        // Passport erzeugen
        monitor.setStatusText(i18n.tr("{0}: Lade HBCI-Sicherheitsmedium",kn));
        monitor.addPercentComplete(2);
        
        Runnable r = new Runnable() {
          public void run()
          {
            try
            {
              passport = PassportRegistry.findByClass(konto.getPassportClass());
              // BUGZILLA #7 http://www.willuhn.de/bugzilla/show_bug.cgi?id=7
              monitor.setStatusText(i18n.tr("{0}: Initialisiere HBCI-Sicherheitsmedium",kn));

              if (passport == null)
                throw new ApplicationException(i18n.tr("Kein HBCI-Sicherheitsmedium f�r das Konto gefunden"));
              passport.init(konto);
            }
            catch (ApplicationException ae)
            {
              monitor.setStatusText(ae.getMessage());
              error = true;
            }
            catch (Exception e)
            {
              Throwable t = getCause(e);
              Logger.error("unable to init passport",e);
              monitor.setStatusText(i18n.tr("{0}: Fehler beim Initialisieren des Sicherheitsmediums",kn));
              monitor.log(t.getMessage());
              error = true;
            }
          }
        };
        
        if (Application.inServerMode()) r.run();
        else GUI.getDisplay().syncExec(r);
        
        if (error) return;
        if (interrupted) return;

        if (passport == null)
        {
          Logger.error("no passport available");
          monitor.setStatusText(i18n.tr("{0}: Kein Sicherheitsmedium angegeben",kn));
          error = true;
          return;
        }
        //
        // //////////////////////////////////////////////////////////////////////
        

        // //////////////////////////////////////////////////////////////////////
        // PassportHandle erzeugen
        monitor.setStatusText(i18n.tr("{0}: Erzeuge HBCI-Handle",kn));
        monitor.addPercentComplete(2);

        r = new Runnable() {
          public void run()
          {
            try
            {
              handle = passport.getHandle();
            }
            catch (RemoteException e1)
            {
              Logger.error("unable to create HBCI handle",e1);
              monitor.setStatusText(i18n.tr("{0}: HBCI-Medium kann nicht initialisiert werden",kn));
              error = true;
            }
          }
        };
        
        if (Application.inServerMode()) r.run();
        else GUI.getDisplay().syncExec(r);
        
        if (error) return;
        if (interrupted) return;

        if (handle == null)
        {
          Logger.error("unable to create HBCI handle");
          monitor.setStatusText(i18n.tr("{0}: HBCI-Medium kann nicht initialisiert werden",kn));
          error = true;
          return;
        }
        //
        // //////////////////////////////////////////////////////////////////////
        

        // //////////////////////////////////////////////////////////////////////
        // HBCI-Verbindung aufbauen
        monitor.setStatusText(i18n.tr("{0}: �ffne HBCI-Verbindung",kn));
        monitor.addPercentComplete(2);

        r = new Runnable() {
          public void run()
          {
            try
            {
              handler = handle.open();
            }
            catch (OperationCanceledException oce)
            {
              Logger.info("operation cancelled");
              monitor.setStatusText(i18n.tr("Vorgang abgebrochen"));
              error = true;
            }
            catch (ApplicationException ae)
            {
              monitor.setStatusText(ae.getMessage());
              error = true;
            }
            catch (Exception e)
            {
              Throwable t = getCause(e);
              Logger.error("unable to open handle",e);
              monitor.setStatusText(i18n.tr("{0}: Fehler beim �ffnen der HBCI-Verbindung",kn));
              monitor.log(t.getMessage());
              error = true;
            }
          }
        };
        if (Application.inServerMode()) r.run();
        else GUI.getDisplay().syncExec(r);

        if (error) return;
        if (interrupted) return;
        //
        // //////////////////////////////////////////////////////////////////////

        // //////////////////////////////////////////////////////////////////////
        // Jobs erzeugen
        Logger.info("processing jobs");

        for (int i=0;i<jobs.size();++i)
        {
          if (interrupted) return;
          final AbstractHBCIJob job = (AbstractHBCIJob) jobs.get(i);
          
          monitor.setStatusText(i18n.tr("{0}: Aktiviere HBCI-Job: \"{1}\"",new String[]{kn,job.getName()}));
          monitor.addPercentComplete(2);

          Logger.info("adding job " + job.getIdentifier() + " to queue");
          
          // TODO: In der folgenden Zeile kann es zum Beispiel einem Fehler kommen, wenn der Geschaeftsvorfall nicht von der Bank unterstuetzt wird.
          // In dem Fall faellt das Programm direkt in den letzten catch/finally-Block. Das job.handleResult() wird nicht ausgefuehrt.
          // Falls "transfer.markexecuted.before" auf true gesetzt ist, fuehrt das dazu, dass die bis hierher erzeugten Auftraege
          // als ausgefuehrt markiert bleiben. Sie sollten aber eigentlich wieder als <nicht ausgefuehrt> markiert werden. Es
          // waere vermutlich sinnvoller, diese for-Schleife hier in den folgenden try/catch-Block "Jobs ausfuehren" zu verschieben.
          // Muss aber mal noch getestet werden.
          HBCIJob j = handler.newJob(job.getIdentifier());
          dumpJob(j);
          job.setJob(j);
          j.addToQueue();
          if (job.isExclusive())
          {
            Logger.info("job will be executed in seperate hbci message");
            handler.newMsg();
          }
        }
        //
        // //////////////////////////////////////////////////////////////////////

        
        if (interrupted) return;
        
        // BUGZILLA 327
        try
        {
          // //////////////////////////////////////////////////////////////////////
          // Jobs ausfuehren
          Logger.info("executing jobs");
          monitor.setStatusText(i18n.tr("{0}: F�hre HBCI-Jobs aus",kn));
          monitor.addPercentComplete(4);
          handler.execute();
          monitor.setStatusText(i18n.tr("{0}: HBCI-Jobs ausgef�hrt",kn));
          monitor.addPercentComplete(4);
          //
          // //////////////////////////////////////////////////////////////////////
        }
        finally
        {
          String name = null;

          // //////////////////////////////////////////////////////////////////////
          // Job-Ergebnisse auswerten
          for (int i=0;i<jobs.size();++i)
          {
            try
            {
              final AbstractHBCIJob job = (AbstractHBCIJob) jobs.get(i);
              name = job.getName();
              monitor.setStatusText(i18n.tr("{0}: Werte Ergebnis von HBCI-Job \"{1}\" aus",new String[]{kn,name}));
              monitor.addPercentComplete(2);
              Logger.info("executing check for job " + job.getIdentifier());
              job.handleResult();
            }
            catch (ApplicationException ae)
            {
              if (!interrupted)
              {
                monitor.setStatusText(ae.getMessage());
                error = true;
              }
            }
            catch (Throwable t)
            {
              if (!interrupted)
              {
                monitor.setStatusText(i18n.tr("Fehler beim Auswerten des HBCI-Auftrages {0}", name));
                Logger.error("error while processing job result",t);
                monitor.log(t.getMessage());
                error = true;
              }
            }
          }
          //
          // //////////////////////////////////////////////////////////////////////
        }
      }
      catch (OperationCanceledException e3)
      {
        monitor.setStatusText(i18n.tr("HBCI-�bertragung abgebrochen"));
        monitor.setStatus(ProgressMonitor.STATUS_CANCEL);
      }
      catch (ApplicationException ae)
      {
        monitor.setStatusText(ae.getMessage());
        error = true;
      }
      catch (Throwable t)
      {
        Throwable t2 = getCause(t);
        Logger.error("error while executing hbci jobs",t);
        monitor.setStatusText(i18n.tr("Fehler beim Ausf�hren der HBCI-Auftr�ge {0}", t.toString()));
        monitor.log(t2.getMessage());
        error = true;
      }
      finally
      {
        try
        {
          monitor.setStatusText(i18n.tr("Beende HBCI-�bertragung"));
          monitor.addPercentComplete(2);
          jobs.clear(); // Jobqueue leer machen.
          try {
            if (handle != null)
              handle.close();
          }
          catch (Throwable t) {/* useless */}

          String msg = null;

          if (!interrupted && !error)
          {
            status = ProgressMonitor.STATUS_DONE;
            msg = "HBCI-�bertragung erfolgreich beendet";
          }
          if (interrupted)
          {
            status = ProgressMonitor.STATUS_CANCEL;
            msg = "HBCI-�bertragung abgebrochen";
          }
          if (error)
          {
            status = ProgressMonitor.STATUS_ERROR;
            msg = "HBCI-�bertragung mit Fehlern beendet";
            DialogFactory.clearPINCache();
          }
          monitor.setStatus(status);
          monitor.setStatusText(i18n.tr(msg));
          monitor.setPercentComplete(100);
        }
        finally
        {
          if (!Application.inServerMode())
            GUI.getStatusBar().stopProgress();
          HBCIFactory.this.stop(status);
        }
      }
    }

    /**
     * @see de.willuhn.jameica.system.BackgroundTask#interrupt()
     */
    public void interrupt()
    {
      monitor.setStatusText(i18n.tr("Breche HBCI-�bertragung ab"));
      Logger.warn("mark hbci session as interrupted");
      this.interrupted = true;
    }

    /**
     * @see de.willuhn.jameica.system.BackgroundTask#isInterrupted()
     */
    public boolean isInterrupted()
    {
      return this.interrupted;
    }
  }
  
  /**
   * Laeuft den Stack der Exceptions bis zur urspruenglichen hoch und liefert sie zurueck.
   * HBCI4Java verpackt Exceptions oft tief ineinander. Sie werden gefangen, in eine
   * neue gepackt und wieder geworfen. Um nun die eigentliche Fehlermeldung zu kriegen,
   * suchen wir hier nach der ersten. 
   * BUGZILLA 249
   * @param t die Exception.
   * @return die urspruengliche.
   */
  public static Throwable getCause(Throwable t)
  {
    return getCause(t,null);
  }
  
  /**
   * Laeuft den Stack der Exceptions bis zur urspruenglichen hoch und liefert sie zurueck.
   * HBCI4Java verpackt Exceptions oft tief ineinander. Sie werden gefangen, in eine
   * neue gepackt und wieder geworfen. Um nun die eigentliche Fehlermeldung zu kriegen,
   * suchen wir hier nach der ersten. 
   * BUGZILLA 249
   * @param t die Exception.
   * @param c optionale Angabe der gesuchten Exception.
   * Wird sie nicht angegeben, liefert die Funktion die erste geworfene Exception
   * im Stacktrace. Wird sie angegeben, liefert die Funktion die erste gefundene
   * Exception dieser Klasse - insofern sie gefunden wird. Wird sie nicht gefunden,
   * liefert die Funktion NULL.
   * @return die urspruengliche.
   */
  public static Throwable getCause(Throwable t, Class<? extends Throwable> c)
  {
    Throwable cause = t;
    
    for (int i=0;i<20;++i) // maximal 20 Schritte nach oben
    {
      if (c != null && c.equals(cause.getClass()))
        return cause;
      
      Throwable current = cause.getCause();

      if (current == null)
        break; // Ende, hier kommt nichts mehr
      
      if (current == cause) // Wir wiederholen uns
        break;
      
      cause = current;
    }
    
    // Wenn eine gesuchte Exception angegeben wurde, haben wir sie hier nicht gefunden
    return c != null ? null : cause;
  }

}


/*******************************************************************************
 * $Log$
 * Revision 1.66  2011-03-07 10:33:53  willuhn
 * @N BUGZILLA 999
 *
 * Revision 1.65  2010-12-27 22:47:52  willuhn
 * @N BUGZILLA 964
 *
 * Revision 1.64  2010/06/17 17:20:58  willuhn
 * @N Exception-Handling beim Laden der Schluesseldatei ueberarbeitet - OperationCancelledException wird nun sauber behandelt - auch wenn sie in HBCI_Exceptions gekapselt ist
 *
 * Revision 1.63  2010/04/22 12:42:03  willuhn
 * @N Erste Version des Supports fuer Offline-Konten
 *
 * Revision 1.62  2009/12/29 17:06:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.61  2009/09/15 00:32:24  willuhn
 * @N BUGZILLA 745 - Keine deaktivierten Konten zulassen
 *
 * Revision 1.60  2009/01/16 22:50:00  willuhn
 * @N bugzilla token
 *
 * Revision 1.59  2009/01/16 22:44:21  willuhn
 * @B Wenn eine HBCI-Session vom User abgebrochen wurde, liefert das JobResult#isOK() u.U. trotzdem true, was dazu fuehrt, dass eine Ueberweisung versehentlich als ausgefuehrt markiert wurde. Neue Funktion "markCancelled()" eingefuehrt.
 *
 * Revision 1.58  2007/12/21 17:37:29  willuhn
 * @N Update auf HBCI4Java 2.5.6
 *
 * Revision 1.57  2007/12/06 23:53:56  willuhn
 * @B Bug 490
 *
 * Revision 1.56  2007/12/05 22:45:59  willuhn
 * @N Bug 513 Debug-Ausgaben eingebaut
 *
 * Revision 1.55  2007/12/05 22:42:57  willuhn
 * *** empty log message ***
 *
 * Revision 1.54  2007/12/04 11:24:38  willuhn
 * @B Bug 509
 *
 * Revision 1.53  2007/12/03 13:17:54  willuhn
 * @N Debugging-Infos
 *
 * Revision 1.52  2007/05/20 23:45:10  willuhn
 * @N HBCI-Jobausfuehrung Servertauglich gemacht
 *
 * Revision 1.51  2007/05/16 13:59:53  willuhn
 * @N Bug 227 HBCI-Synchronisierung auch im Fehlerfall fortsetzen
 * @C Synchronizer ueberarbeitet
 * @B HBCIFactory hat globalen Status auch bei Abbruch auf Error gesetzt
 *
 * Revision 1.50  2007/03/14 12:01:33  willuhn
 * @N made getCause public
 *
 * Revision 1.49  2007/02/21 12:10:36  willuhn
 * Bug 349
 *
 * Revision 1.48  2007/02/21 10:20:08  willuhn
 * @N Log-Ausgabe, wenn HBCI-Session abgebrochen wurde
 *
 * Revision 1.47  2007/02/21 10:02:27  willuhn
 * @C Code zum Ausfuehren exklusiver Jobs redesigned
 ******************************************************************************/