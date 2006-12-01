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

import java.beans.XMLEncoder;
import java.io.OutputStream;
import java.rmi.RemoteException;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Exportiert Daten im XML-Format.
 * Macht eigentlich nichts anderes, als die Objekte mit Java-Mitteln nach XML zu serialisieren.
 */
public class XMLExporter implements Exporter
{
  private I18N i18n = null;
  
  /**
   * ct. 
   */
  public XMLExporter()
  {
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.jameica.hbci.io.Exporter#doExport(de.willuhn.datasource.GenericObject[], de.willuhn.jameica.hbci.io.IOFormat, java.io.OutputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doExport(GenericObject[] objects, IOFormat format,OutputStream os, final ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    try
    {
      double factor = 1;
      if (monitor != null)
      {
        factor = ((double)(100 - monitor.getPercentComplete())) / objects.length;
        monitor.setStatusText(i18n.tr("Exportiere Daten"));
      }

      XMLEncoder e = new XMLEncoder(os);
      for (int i=0;i<objects.length;++i)
      {
        if (monitor != null)  monitor.setPercentComplete((int)((i) * factor));
        Object name = objects[i].getAttribute(objects[i].getPrimaryAttribute());
        if (name != null && monitor != null)
          monitor.log(i18n.tr("Speichere Datensatz {0}",name.toString()));
        e.writeObject(objects[i]);
      }
    }
    finally
    {
      if (monitor != null)
      {
        monitor.setStatusText(i18n.tr("Schliesse Export-Datei"));
      }
      try
      {
        os.close();
      }
      catch (Exception e) {/*useless*/}
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getIOFormats(java.lang.Class)
   */
  public IOFormat[] getIOFormats(Class objectType)
  {
    return null;
//    return new IOFormat[]{new IOFormat() {
//    
//      public String getName()
//      {
//        return i18n.tr("XML-Format");
//      }
//    
//      /**
//       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
//       */
//      public String[] getFileExtensions()
//      {
//        return new String[]{"xml"};
//      }
//    
//    }};
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  public String getName()
  {
    return i18n.tr("XML-Format");
  }

}


/*********************************************************************
 * $Log$
 * Revision 1.1  2006-12-01 01:28:16  willuhn
 * @N Experimenteller Import-Export-Code
 *
 **********************************************************************/