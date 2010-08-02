/**********************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server.hbci.rewriter;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.willuhn.jameica.hbci.rmi.Umsatz;

/**
 * Implementierung des Rewriters fuer die Deutsche Bank.
 * BUGZILLA 887
 */
public class DeutscheBankUmsatzRewriter implements UmsatzRewriter
{
  /**
   * @see de.willuhn.jameica.hbci.server.hbci.rewriter.UmsatzRewriter#getBlzList()
   */
  public List<String> getBlzList()
  {
    List<String> list = new ArrayList<String>();
    list.add("50070024");
    return list;
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.rewriter.UmsatzRewriter#rewrite(de.willuhn.jameica.hbci.rmi.Umsatz)
   */
  public void rewrite(Umsatz u) throws Exception
  {
    String name = u.getGegenkontoName();
    if (name != null && name.length() > 0)
      return; // Steht schon was drin

    String kto  = u.getGegenkontoNummer();
    if (kto != null && kto.length() > 0)
      return; // Steht schon was drin
    
    String blz  = u.getGegenkontoBLZ();
    if (blz != null && blz.length() > 0)
      return; // Steht schon was drin
    
    String s1   = u.getZweck();
    String s2   = u.getZweck2();
    String[] s3 = u.getWeitereVerwendungszwecke();
    
    List<String> lines = new ArrayList<String>();
    if (s1 != null && s1.length() > 0) lines.add(s1);
    if (s2 != null && s2.length() > 0) lines.add(s2);
    if (s3 != null && s3.length > 0) lines.addAll(Arrays.asList(s3));
    
    if (lines.size() == 0)
      return; // Kein Verwendungszweck da
    
    for (int i=0;i<lines.size();++i)
    {
      if (applyGegenkonto(u,lines.get(i)))
      {
        // Gegenkonto wurde gefunden. Dann ignorieren wir die erste Zeile
        // und uebernehmen die Zeile 1 als Gegenkonto-Inhaber und die
        // Zeilen 2 - (i-1) als Verwendungszweck

        // Alte Verwendungszwecke erstmal loeschen
        u.setZweck(null);
        u.setZweck2(null);
        u.setWeitereVerwendungszwecke(null);
        
        if (lines.size() < 2)
          return;

        // Gegenkonto-Inhaber
        u.setGegenkontoName(lines.get(1).trim());
        
        if (i >= 2)
        {
          List<String> list = new ArrayList<String>(lines.subList(2,i));
          if (list.size() == 0) return; // haben wir noch was uebrig?

          // 1. Verwendungszweck
          u.setZweck(list.remove(0).trim());
          if (list.size() == 0) return; // haben wir noch was uebrig?

          // 2. Verwendungszweck
          u.setZweck2(list.remove(0).trim());
          if (list.size() == 0) return; // haben wir noch was uebrig?

          // 3. weitere Verwendungszwecke
          u.setWeitereVerwendungszwecke(list.toArray(new String[list.size()]));
        }
      }
    }
  }
  
  /**
   * Versucht, aus der uebergebenen Verwendungszweck-Zeile Gegenkonto/BLZ zu parsen
   * und dem Umsatz zuzuordnen.
   * @param u der Umsatz
   * @param s der zu parsende Text.
   * @throws RemoteException
   */
  private boolean applyGegenkonto(Umsatz u, String s) throws RemoteException
  {
    if (s == null || s.length() == 0 || u == null)
      return false;
    
    String gk = s.trim();
    if (!gk.matches("^KTO([ ]{1,7})[0-9]{3,10} BLZ [0-9]{8}"))
      return false;
    
    gk = gk.replaceAll("[a-zA-Z]","").trim();
    String[] sl = gk.split(" {1,7}");
    if (sl.length != 2)
      return false;
    u.setGegenkontoNummer(sl[0].trim());
    u.setGegenkontoBLZ(sl[1].trim());
    return true;
  }
}



/**********************************************************************
 * $Log$
 * Revision 1.1  2010-08-02 09:02:23  willuhn
 * @N BUGZILLA 887
 *
 **********************************************************************/