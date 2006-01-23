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

import java.util.Enumeration;
import java.util.Hashtable;

import de.willuhn.jameica.system.Settings;

/**
 * Ein Container, der die Feldzuordnungen eines CSV-Imports speichert.
 */
public class CSVMapping
{

  private Settings settings = new Settings(CSVMapping.class); 
  
  private Class type         = null;
  private Hashtable names    = null;
  
  private Hashtable mapping  = null;
  
  /**
   * @param type
   * @param names
   */
  public CSVMapping(Class type, Hashtable names)
  {
    this.type  = type;
    this.names = names;
    read();
  }
  
  /**
   * Liefert eine Kopie der Hashtable mit den Namen.
   * @return Hashtable mit den Namen.
   */
  public Hashtable getNames()
  {
    return (Hashtable) this.names.clone();
  }

  /**
   * Liest das Mapping aus der Config-Datei.
   */
  private synchronized void read()
  {
    mapping = new Hashtable();
    Enumeration e = names.keys();
    while (e.hasMoreElements())
    {
      String key = (String) e.nextElement();
      int index = settings.getInt(type.getName() + "." + key,-1);
      if (index == -1)
        continue;
      mapping.put(new Integer(index),key);
    }
  }
  
  /**
   * Speichert ein einzelnes Mapping.
   * @param index Spaltennummer.
   * @param key zugeordnetes Attribut.
   */
  public void set(int index, String key)
  {
    mapping.put(new Integer(index),key);
    store();
  }
  
  /**
   * Liefert die Zuordnung zu diesem Index oder null, wenn keiner definiert ist.
   * @param index Spaltennummer.
   * @return zugeordnetes Attribut.
   */
  public String get(int index)
  {
    return (String) mapping.get(new Integer(index));
  }

  /**
   * Entfernt die Zuordnung zu dieser Spalte.
   * @param index zu entfernende Zuordnung.
   */
  public void remove(int index)
  {
    mapping.remove(new Integer(index));
    store();
  }
  
  /**
   * Liefert true, wenn bei dem Mapping die erste Zeile uebersprungen werden soll.
   * @return true, wenn die erste Zeile uebersprungen werden soll.
   */
  public boolean skipFirstLine()
  {
    return this.settings.getBoolean(type.getName() + ".skipfirst",false);
  }
  
  /**
   * Legt fest, ob die erste Zeile uebersprungen werden soll.
   * @param b true, wenn die erste Zeile uebersprungen werden soll.
   */
  public void setSkipFirstLine(boolean b)
  {
    this.settings.setAttribute(type.getName() + ".skipfirst",b);
  }
  
  /**
   * Speichert das CSV-Mapping.
   */
  private synchronized void store()
  {
    Enumeration e = mapping.keys();
    while (e.hasMoreElements())
    {
      Integer index = (Integer) e.nextElement();
      String key = (String) mapping.get(index);
      settings.setAttribute(type.getName() + "." + key,index.intValue());
    }
  }
}


/*********************************************************************
 * $Log$
 * Revision 1.3  2006-01-23 23:07:23  willuhn
 * @N csv import stuff
 *
 * Revision 1.2  2006/01/23 18:16:51  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2006/01/23 18:13:19  willuhn
 * @N first code for csv import
 *
 **********************************************************************/