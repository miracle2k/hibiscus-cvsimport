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

package de.willuhn.jameica.hbci.export;

import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ClassFinder;

/**
 * Ueber diese Klasse koennen alle verfuegbaren Export-Formate abgerufen werden.
 */
public class ExportRegistry
{

  // Liste der Export-Filter
  private static Exporter[] exporters = null;

  /**
   * Initialisiert die Registry.
   */
  public static synchronized void init()
  {
    if (exporters != null)
      return; // wurde schonmal aufgerufen

    try
    {
      Logger.info("looking for installed export filters");
      ClassFinder finder = Application.getClassLoader().getClassFinder();
      Class[] list = finder.findImplementors(Exporter.class);
      if (list == null || list.length == 0)
        throw new ClassNotFoundException();

      // Initialisieren der Exporter
      exporters = new Exporter[list.length];

      for (int i=0;i<list.length;++i)
      {
        try
        {
          exporters[i] = (Exporter) list[i].newInstance();
        }
        catch (Exception e)
        {
          Logger.error("error while loading export filter " + list[i].getName(),e);
        }
      }

    }
    catch (ClassNotFoundException e)
    {
      Logger.warn("no export filters found");
      exporters = new Exporter[0];
    }
  }

  /**
   * Liefert eine Liste aller verfuegbaren Export-Formate.
   * @return Export-Filter.
   */
  public static Exporter[] getExporters()
  {
    return exporters;
  }
}


/**********************************************************************
 * $Log$
 * Revision 1.1  2005-06-02 21:48:44  web0
 * @N Exporter-Package
 * @N CSV-Exporter
 *
 **********************************************************************/