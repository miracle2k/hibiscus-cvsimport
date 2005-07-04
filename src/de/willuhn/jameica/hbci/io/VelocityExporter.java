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

package de.willuhn.jameica.hbci.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.eclipse.swt.graphics.Image;

import de.willuhn.datasource.GenericObject;
import de.willuhn.io.FileFinder;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.plugin.PluginResources;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung eines Exporters, welcher das Velocity-Framework nutzt.
 */
public class VelocityExporter implements Exporter
{

  private File templateDir        = null;

  private HashMap formats         = new HashMap();

  private I18N i18n = null;
  
  static
  {
    try
    {
      // Velocity initialisieren
      Logger.info("init velocity template engine");
      Velocity.setProperty(Velocity.RESOURCE_LOADER,"hibiscus");
      Velocity.setProperty("hibiscus.resource.loader.description","Hibiscus Velocity Loader");
      Velocity.setProperty("hibiscus.resource.loader.class",VelocityLoader.class.getName());

      Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, new VelocityLogger());
      Velocity.init();
    }
    catch (Throwable t)
    {
      Logger.error("velocity init failed",t);
    }
  }

  /**
   * ct.
   */
  public VelocityExporter()
  {
    super();
    Logger.info("init velocity export engine");
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    PluginResources res = Application.getPluginLoader().getPlugin(HBCI.class).getResources();
    this.templateDir = new File(res.getPath() + File.separator + "lib","velocity");
    Logger.info("velocity template dir: " + this.templateDir.getAbsolutePath());
  }

  /**
   * @see de.willuhn.jameica.hbci.io.Exporter#export(de.willuhn.jameica.hbci.io.ExportFormat, java.lang.Class, de.willuhn.datasource.GenericObject[], java.io.OutputStream)
   */
  public void export(ExportFormat format, Class objectType, GenericObject[] objects, OutputStream os) throws RemoteException, ApplicationException
  {
    if (os == null)
      throw new ApplicationException(i18n.tr("Kein Ausgabe-Ziel f�r die Datei angegeben"));

    if (format == null)
      throw new ApplicationException(i18n.tr("Kein Ausgabe-Format angegeben"));

    if (objects == null || objects.length == 0)
      throw new ApplicationException(i18n.tr("Keine zu exportierenden Ums�tze angegeben"));

    Logger.debug("preparing velocity context");
    VelocityContext context = new VelocityContext();

    context.put("datum",        new Date());
    context.put("dateformat",   HBCI.DATEFORMAT);
    context.put("decimalformat",HBCI.DECIMALFORMAT);
    context.put("objects",      objects);

    BufferedWriter writer = null;
    try
    {
      writer = new BufferedWriter(new OutputStreamWriter(os));

      Template template = Velocity.getTemplate(objectType.getName() + "." + format.getFileExtension() + ".vm");
      template.merge(context,writer);
    }
    catch (Exception e)
    {
      Logger.error("error while writing into export file",e);
      throw new ApplicationException(i18n.tr("Fehler beim Schreiben in die Export-Datei"));
    }
    finally
    {
      if (writer != null)
      {
        try
        {
          writer.close();
        }
        catch (Exception e)
        {
          // useless
        }
      }
    }
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  public String getName()
  {
    return i18n.tr("Velocity-Export");
  }

  /**
   * @see de.willuhn.jameica.hbci.io.Exporter#getExportFormats(java.lang.Class)
   */
  public ExportFormat[] getExportFormats(Class type)
  {
    if (type == null)
      return new ExportFormat[0];
    
    ExportFormat[] loaded = (ExportFormat[]) this.formats.get(type);
    if (loaded != null)
      return loaded;

    Logger.info("looking velocity templates for object type " + type.getClass());
    FileFinder finder = new FileFinder(this.templateDir);
    String cn = type.getName().replaceAll("\\.","\\\\.");
    finder.matches(cn + ".*?\\.vm$");

    File[] found = finder.findRecursive();
    
    ArrayList l = new ArrayList();
    for (int i=0;i<found.length;++i)
    {
      final File ef = found[i];
      if ("VM_global_library.vm".equalsIgnoreCase(ef.getName()))
        continue;
      Logger.info("  found template: " + ef.getAbsolutePath());
      String name = ef.getName();
      name = name.replaceAll(cn + "\\.",""); // Klassenname und Punkt dahinter entfernen
      int dot = name.indexOf(".");
      if (dot == -1)
        continue;
      final String ext = name.substring(0,dot);
      if (ext == null || ext.length() == 0)
        continue;

      final ExportFormat exf = new ExportFormat() {

        /**
         * @see de.willuhn.jameica.hbci.io.ExportFormat#getName()
         */
        public String getName()
        {
          return ext.toUpperCase() + "-" + i18n.tr("Format");
        }

        /**
         * @see de.willuhn.jameica.hbci.io.ExportFormat#getFileExtension()
         */
        public String getFileExtension()
        {
          return ext;
        }

        /**
         * @see de.willuhn.jameica.hbci.io.ExportFormat#getScreenshot()
         */
        public Image getScreenshot()
        {
          return null;
        }
      };
      l.add(exf);
    }
    loaded = (ExportFormat[]) l.toArray(new ExportFormat[l.size()]);
    this.formats.put(type,loaded);
    return loaded;
    
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.2  2005-07-04 12:41:39  web0
 * @B bug 90
 *
 * Revision 1.1  2005/06/30 23:52:42  web0
 * @N export via velocity
 *
 * Revision 1.2  2005/06/15 16:10:48  web0
 * @B javadoc fixes
 *
 * Revision 1.1  2005/06/08 16:48:54  web0
 * @N new Import/Export-System
 *
 * Revision 1.2  2005/06/06 10:37:01  web0
 * *** empty log message ***
 *
 * Revision 1.1  2005/06/02 21:48:44  web0
 * @N Exporter-Package
 * @N CSV-Exporter
 *
 **********************************************************************/