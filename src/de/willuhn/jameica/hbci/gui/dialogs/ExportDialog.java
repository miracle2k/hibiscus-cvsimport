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
package de.willuhn.jameica.hbci.gui.dialogs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.internal.action.Program;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.io.Exporter;
import de.willuhn.jameica.hbci.io.IOFormat;
import de.willuhn.jameica.hbci.io.IORegistry;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Dialog, ueber den Daten exportiert werden koennen.
 */
public class ExportDialog extends AbstractDialog
{
  private final static int WINDOW_WIDTH = 420;

  private static DateFormat DATEFORMAT = new SimpleDateFormat("yyyyMMdd");

  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private Input exporterListe     = null;
  private CheckboxInput openFile  = null;
  private Object[] objects        = null;	
  private Class type              = null;

  private Settings  settings      = null;

  /**
   * ct.
   * @param objects Liste der zu exportierenden Objekte.
   * @param type die Art der zu exportierenden Objekte.
   */
  public ExportDialog(Object[] objects, Class type)
  {
    super(POSITION_CENTER);
		setTitle(i18n.tr("Daten-Export"));
    this.setSize(WINDOW_WIDTH,SWT.DEFAULT);

    this.objects = objects;
    this.type = type;

    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
		Container group = new SimpleContainer(parent);
		group.addText(i18n.tr("Bitte w�hlen Sie das gew�nschte Dateiformat aus f�r den Export aus"),true);

    Input formats = getExporterList();
		group.addLabelPair(i18n.tr("Verf�gbare Formate:"),formats);
    
    boolean exportEnabled = !(formats instanceof LabelInput);
    
    if (exportEnabled)
      group.addCheckbox(getOpenFile(),i18n.tr("Datei nach dem Export �ffnen"));

		ButtonArea buttons = new ButtonArea();
		Button button = new Button(i18n.tr("Export starten"),new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
				export();
			}
		},null,true,"ok.png");
    button.setEnabled(exportEnabled);
    buttons.addButton(button);
		buttons.addButton(i18n.tr("Abbrechen"), new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
				close();
			}
		},null,false,"process-stop.png");
		group.addButtonArea(buttons);
		
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
  }

  /**
   * Exportiert die Daten.
   * @throws ApplicationException
   */
  private void export() throws ApplicationException
  {
    Exp exp = null;

    try
    {
      exp = (Exp) getExporterList().getValue();
    }
    catch (Exception e)
    {
      Logger.error("error while saving export file",e);
      throw new ApplicationException(i18n.tr("Fehler beim Starten des Exports"),e);
    }

    if (exp == null || exp.exporter == null)
      throw new ApplicationException(i18n.tr("Bitte w�hlen Sie ein Export-Format aus"));

    settings.setAttribute("lastformat",exp.format.getName());

    FileDialog fd = new FileDialog(GUI.getShell(),SWT.SAVE);
    fd.setText(i18n.tr("Bitte geben Sie eine Datei ein, in die die Daten exportiert werden sollen."));
    fd.setOverwrite(true);
    String[] se = exp.format.getFileExtensions();
    String ext = se == null ? "" : se[0];
    ext = ext.replaceAll("\\*.",""); // "*." entfernen
    fd.setFileName(i18n.tr("hibiscus-export-{0}." + ext,DATEFORMAT.format(new Date())));

    String path = settings.getString("lastdir",System.getProperty("user.home"));
    if (path != null && path.length() > 0)
      fd.setFilterPath(path);

    final String s = fd.open();
    
    if (s == null || s.length() == 0)
    {
      close();
      return;
    }

    final File file = new File(s);
    
    // Wir merken uns noch das Verzeichnis vom letzten mal
    settings.setAttribute("lastdir",file.getParent());

    // Dialog schliessen
    final boolean open = ((Boolean)getOpenFile().getValue()).booleanValue();
    settings.setAttribute("open",open);
    close();

    final Exporter exporter = exp.exporter;
    final IOFormat format = exp.format;

    BackgroundTask t = new BackgroundTask() {
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        try
        {
          // Der Exporter schliesst den OutputStream selbst
          OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
          exporter.doExport(objects,format,os,monitor);
          monitor.setPercentComplete(100);
          monitor.setStatus(ProgressMonitor.STATUS_DONE);
          GUI.getStatusBar().setSuccessText(i18n.tr("Daten exportiert nach {0}",s));
          monitor.setStatusText(i18n.tr("Daten exportiert nach {0}",s));
          
          if (open)
          {
            GUI.getDisplay().asyncExec(new Runnable() {
              public void run()
              {
                try
                {
                  new Program().handleAction(file);
                }
                catch (ApplicationException ae)
                {
                  Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getLocalizedMessage(),StatusBarMessage.TYPE_ERROR));
                }
              }
            });
          }
        }
        catch (ApplicationException ae)
        {
          monitor.setStatus(ProgressMonitor.STATUS_ERROR);
          monitor.setStatusText(ae.getMessage());
          GUI.getStatusBar().setErrorText(ae.getMessage());
          throw ae;
        }
        catch (Exception e)
        {
          monitor.setStatus(ProgressMonitor.STATUS_ERROR);
          Logger.error("error while writing objects to " + s,e);
          ApplicationException ae = new ApplicationException(i18n.tr("Fehler beim Exportieren der Daten in {0}",s),e);
          monitor.setStatusText(ae.getMessage());
          GUI.getStatusBar().setErrorText(ae.getMessage());
          throw ae;
        }
      }

      public void interrupt() {}
      public boolean isInterrupted()
      {
        return false;
      }
    };

    Application.getController().start(t);
  }

  /**
   * Liefert eine Checkbox.
   * @return Checkbox.
   */
  private CheckboxInput getOpenFile()
  {
    if (this.openFile == null)
      this.openFile = new CheckboxInput(settings.getBoolean("open",true));
    return this.openFile;
  }
  
	/**
	 * Liefert eine Liste der verfuegbaren Exporter.
   * @return Liste der Exporter.
	 * @throws Exception
   */
  private Input getExporterList() throws Exception
	{
		if (exporterListe != null)
			return exporterListe;

    Exporter[] exporters = IORegistry.getExporters();

    int size          = 0;
    ArrayList l       = new ArrayList();
    String lastFormat = settings.getString("lastformat",null);
    Exp selected      = null;

    for (int i=0;i<exporters.length;++i)
		{
      Exporter exp = exporters[i];
      if (exp == null)
        continue;
      IOFormat[] formats = exp.getIOFormats(type);
      if (formats == null || formats.length == 0)
      {
        Logger.debug("exporter " + exp.getName() + " provides no export formats for " + type + " skipping");
        continue;
      }
      for (int j=0;j<formats.length;++j)
      {
        size++;
        Exp e = new Exp(exp,formats[j]);
        l.add(e);
        
        String lf = e.format.getName();
        if (lastFormat != null && lf != null && lf.equals(lastFormat))
          selected = e;
      }
		}

		if (size == 0)
		{
			exporterListe = new LabelInput(i18n.tr("Keine Export-Filter verf�gbar"));
			return exporterListe;
		}

    Collections.sort(l);
		Exp[] exp = (Exp[]) l.toArray(new Exp[size]);
		exporterListe = new SelectInput(PseudoIterator.fromArray(exp),selected);
		return exporterListe;
	}

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }

	/**
	 * Hilfsklasse zur Anzeige der Exporter.
   */
  private class Exp implements GenericObject, Comparable
	{
		private Exporter exporter   = null;
    private IOFormat format = null;
		
		private Exp(Exporter exporter, IOFormat format)
		{
			this.exporter = exporter;
      this.format = format;
		}

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String arg0) throws RemoteException
    {
      return this.format.getName();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[] {"name"};
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return this.exporter.getClass().getName() + "#" + this.format.getName();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "name";
    }

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject arg0) throws RemoteException
    {
    	if (arg0 == null)
	      return false;
	    return this.getID().equals(arg0.getID());
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o)
    {
      if (o == null || !(o instanceof Exp))
        return -1;
      try
      {
        return this.format.getName().compareTo(((Exp)o).format.getName());
      }
      catch (Exception e)
      {
        // Tss, dann halt nicht
      }
      return 0;
    }
	}
}


/**********************************************************************
 * $Log$
 * Revision 1.22  2011-05-03 16:44:23  willuhn
 * @C GUI cleanup
 *
 * Revision 1.21  2010-08-27 10:39:54  willuhn
 * @C Export-Dialog in gleicher Groesse und Position wie Import-Dialog
 *
 * Revision 1.20  2010/06/08 11:26:05  willuhn
 * @N SWT besitzt jetzt selbst eine Option im FileDialog, mit der geprueft werden kann, ob die Datei ueberschrieben werden soll oder nicht
 *
 * Revision 1.19  2010/03/16 00:44:17  willuhn
 * @N Komplettes Redesign des CSV-Imports.
 *   - Kann nun erheblich einfacher auch fuer andere Datentypen (z.Bsp.Ueberweisungen) verwendet werden
 *   - Fehlertoleranter
 *   - Mehrfachzuordnung von Spalten (z.Bsp. bei erweitertem Verwendungszweck) moeglich
 *   - modulare Deserialisierung der Werte
 *   - CSV-Exports von Hibiscus koennen nun 1:1 auch wieder importiert werden (Import-Preset identisch mit Export-Format)
 *   - Import-Preset wird nun im XML-Format nach ~/.jameica/hibiscus/csv serialisiert. Damit wird es kuenftig moeglich sein,
 *     CSV-Import-Profile vorzukonfigurieren und anschliessend zu exportieren, um sie mit anderen Usern teilen zu koennen
 *
 * Revision 1.18  2009/07/09 17:08:03  willuhn
 * @N BUGZILLA #740
 *
 * Revision 1.17  2008/07/11 12:28:02  willuhn
 * @N BUGZILLA 609
 *
 * Revision 1.16  2008/02/13 23:22:24  willuhn
 * @B http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?t=8175 (Nachtrag)
 *
 * Revision 1.15  2008/02/13 23:15:29  willuhn
 * @B http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?t=8175
 *
 * Revision 1.14  2007/04/23 18:40:44  jost
 * Javadoc Tippfehler
 *
 * Revision 1.13  2007/04/23 18:07:15  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.12  2007/03/21 15:37:46  willuhn
 * @N Vorschau der Umsaetze in Auswertung "Kontoauszug"
 *
 * Revision 1.11  2006/10/09 10:10:27  willuhn
 * @C s/dessen/abbrechen/
 *
 * Revision 1.10  2006/08/07 21:51:43  willuhn
 * @N Erste Version des DTAUS-Exporters
 *
 * Revision 1.9  2006/08/07 14:31:59  willuhn
 * @B misc bugfixing
 * @C Redesign des DTAUS-Imports fuer Sammeltransfers
 *
 * Revision 1.8  2006/07/03 23:04:32  willuhn
 * @N PDF-Reportwriter in IO-API gepresst, damit er auch an anderen Stellen (z.Bsp. in der Umsatzliste) mitverwendet werden kann.
 *
 * Revision 1.7  2006/06/08 22:29:47  willuhn
 * @N DTAUS-Import fuer Sammel-Lastschriften und Sammel-Ueberweisungen
 * @B Eine Reihe kleinerer Bugfixes in Sammeltransfers
 * @B Bug 197 besser geloest
 *
 * Revision 1.6  2006/01/25 09:22:05  willuhn
 * @B compile error
 *
 * Revision 1.5  2006/01/23 00:36:29  willuhn
 * @N Import, Export und Chipkartentest laufen jetzt als Background-Task
 *
 * Revision 1.4  2006/01/18 00:51:01  willuhn
 * @B bug 65
 *
 * Revision 1.3  2006/01/17 00:22:37  willuhn
 * @N erster Code fuer Swift MT940-Import
 *
 * Revision 1.2  2006/01/02 17:38:12  willuhn
 * @N moved Velocity to Jameica
 *
 * Revision 1.1  2005/07/04 12:41:39  web0
 * @B bug 90
 *
 * Revision 1.4  2005/06/30 23:52:42  web0
 * @N export via velocity
 *
 * Revision 1.3  2005/06/08 16:49:00  web0
 * @N new Import/Export-System
 *
 * Revision 1.2  2005/06/06 10:37:07  web0
 * *** empty log message ***
 *
 * Revision 1.1  2005/06/02 22:57:34  web0
 * @N Export von Konto-Umsaetzen
 *
 **********************************************************************/