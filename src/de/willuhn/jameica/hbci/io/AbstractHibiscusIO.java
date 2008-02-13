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

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Basisklasse fuer Import/Export im Hibiscus-Format.
 */
public abstract class AbstractHibiscusIO implements IO
{
  I18N i18n = null;
  
  /**
   * ct. 
   */
  public AbstractHibiscusIO()
  {
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getIOFormats(java.lang.Class)
   */
  public IOFormat[] getIOFormats(Class objectType)
  {
    return new IOFormat[0];
//    return new IOFormat[]{new IOFormat() {
//    
//      public String getName()
//      {
//        return i18n.tr("Hibiscus-Format (experimentell!)");
//      }
//    
//      /**
//       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
//       */
//      public String[] getFileExtensions()
//      {
//        return new String[]{"hib"};
//      }
//    
//    }};
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  public String getName()
  {
    return i18n.tr("Hibiscus-Format (experimentell!)");
  }

}


/*********************************************************************
 * $Log$
 * Revision 1.2  2008-02-13 23:44:27  willuhn
 * @R Hibiscus-Eigenformat (binaer-serialisierte Objekte) bei Export und Import abgeklemmt
 * @N Import und Export von Umsatz-Kategorien im XML-Format
 * @B Verzaehler bei XML-Import
 *
 * Revision 1.1  2006/12/01 01:28:16  willuhn
 * @N Experimenteller Import-Export-Code
 *
 **********************************************************************/