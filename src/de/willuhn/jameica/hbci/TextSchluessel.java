/**********************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 * $Locker$
 * $State$
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci;

import java.util.ArrayList;

import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Bean fuer die Textschluessel.
 */
public class TextSchluessel
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private final static ArrayList list = new ArrayList();
  static
  {
    list.add(new TextSchluessel("04",i18n.tr("Abbuchungsverfahren")));
    list.add(new TextSchluessel("05",i18n.tr("Einzugserm�chtigung")));
    list.add(new TextSchluessel("52",i18n.tr("Dauerauftrag")));
    list.add(new TextSchluessel("51",i18n.tr("�berweisung")));
    list.add(new TextSchluessel("53",i18n.tr("�berweisung Lohn/Gehalt/Rente")));
    list.add(new TextSchluessel("54",i18n.tr("Verm�genswirksame Leistungen")));
    list.add(new TextSchluessel("59",i18n.tr("R�ck�berweisung")));
  }

  private String code = null;
  private String name = null;
  
  /**
   * Liefert eine Liste der Textschluessel-Objekte mit den genannten Codes.
   * @param codes Liste der Codes oder <code>null</code>, wenn alle zurueckgeliefert werden sollen.
   * @return Liste der Textschluessel mit diesen Codes.
   * Die Textschluessel werden in der gleichen Reihenfolge zurueckgeliefert, in der die Codes uebergeben wurden.
   */
  public static TextSchluessel[] get(String[] codes)
  {
    if (codes == null || codes.length == 0)
      return (TextSchluessel[]) list.toArray(new TextSchluessel[list.size()]);

    ArrayList l = new ArrayList();
    for (int i=0;i<codes.length;++i)
    {
      for (int k=0;k<list.size();++k)
      {
        TextSchluessel ts = (TextSchluessel) list.get(k);
        String code = ts.getCode();
        if (code.equals(codes[i]))
        {
          l.add(ts);
          continue;
        }
      }
    }
    
    return (TextSchluessel[]) l.toArray(new TextSchluessel[l.size()]);
  }
  
  /**
   * Liefert einen einzelnen Textschluessel.
   * @param code Code des Textschluessels.
   * @return der Textschluessel oder <code>null</code>, wenn er nicht existiert.
   */
  public static TextSchluessel get(String code)
  {
    if (code == null || code.length() == 0)
      return null;

    for (int i=0;i<list.size();++i)
    {
      TextSchluessel ts = (TextSchluessel) list.get(i);
      if (ts.getCode().equals(code))
        return ts;
    }
    return null;
  }
  
  /**
   * ct
   * @param code Nummer des Textschl�ssel.
   * @param name Bezeichnung.
   */
  private TextSchluessel(String code, String name)
  {
    this.code = code;
    this.name = name;
  }
  
  /**
   * Liefert den Textschluessel.
   * @return der Textschluessel.
   */
  public String getCode()
  {
    return this.code;
  }
  
  /**
   * Liefert den Namen des Textschluessels.
   * @return der Name des Textschluessels.
   */
  public String getName()
  {
    return this.name;
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return i18n.tr("[{0}] {1}", new String[]{this.code,this.name});
  }
  
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object other)
  {
    if (this == other)
      return true;
    if (other == null || !(other instanceof TextSchluessel))
      return false;
    return this.code.equals(((TextSchluessel)other).getCode());
  }
}


/*********************************************************************
 * $Log$
 * Revision 1.3  2010-09-24 12:22:04  willuhn
 * @N Thomas' Patch fuer Textschluessel in Dauerauftraegen
 *
 * Revision 1.2  2010/06/07 12:43:41  willuhn
 * @N BUGZILLA 587
 *
 * Revision 1.1  2008/08/01 11:05:14  willuhn
 * @N BUGZILLA 587
 *
 **********************************************************************/