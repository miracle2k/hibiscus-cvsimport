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

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import de.willuhn.jameica.hbci.rmi.HibiscusTransfer;
import de.willuhn.jameica.hbci.rmi.Transfer;




/**
 * Hilfsklasse zum Mergen und Splitten der erweiterten Verwendungszwecke.
 */
public class VerwendungszweckUtil
{
  /**
   * Splittet die Verwendungszweck-Zeilen am Zeilenumbruch.
   * @param lines die Zeilen.
   * @return Ein Array mit den Zeilen.
   * Niemals NULL sondern hoechstens ein leeres Array.
   */
  public static String[] split(String lines)
  {
    if (lines == null || lines.length() == 0)
      return new String[0];
    return lines.split("\n");
  }
  
  /**
   * Zerlegt einen langen Verwendungszweck in 27 Zeichen lange Haeppchen.
   * @param line die zu parsende Zeile.
   * @return die 27 Zeichen langen Schnippsel.
   */
  public static String[] parse(String line)
  {
    if (line == null || line.length() == 0)
      return new String[0];

    // Java's Regex-Implementierung ist sowas von daemlich.
    // String.split() macht nur Rotz, wenn man mit Quantifierern
    // arbeitet. Also ersetzten wir erst mal alles gegen nen
    // eigenen String und verwenden den dann zum Splitten.
    String s = line.replaceAll("(.{27})","$1--##--##");
    return s.split("--##--##");
  }
  
  /**
   * Verteilt die angegebenen Verwendungszweck-Zeilen auf zweck, zweck2 und zweck3.
   * @param t der Auftrag, in dem die Verwendungszweck-Zeilen gespeichert werden sollen.
   * @param lines die zu uebernehmenden Zeilen.
   * @throws RemoteException
   */
  public static void apply(HibiscusTransfer t, String[] lines) throws RemoteException
  {
    if (t == null || lines == null || lines.length == 0)
      return;
    
    List<String> l = clean(lines);
    if (l.size() > 0) t.setZweck(l.remove(0));  // Zeile 1
    if (l.size() > 0) t.setZweck2(l.remove(0)); // Zeile 2
    if (l.size() > 0) t.setWeitereVerwendungszwecke(l.toArray(new String[l.size()])); // Zeile 3 - x
  }
  
  /**
   * Merget die Verwendungszweck-Zeilen zu einem String zusammen.
   * Die Zeilen sind mit Zeilenumbruch versehen.
   * @param lines die Zeilen.
   * @return die gemergten Zeilen. Wird NULL oder ein leeres
   * Array uebergeben, liefert die Funktion NULL.
   */
  public static String merge(String... lines)
  {
    if (lines == null || lines.length == 0)
      return null;
    
    List<String> cleaned = clean(lines);
    StringBuffer sb = new StringBuffer();
    for (String line:cleaned)
    {
      sb.append(line);
      sb.append("\n");
    }

    String result = sb.toString();
    return result.length() == 0 ? null : result;
  }
  
  /**
   * Liefert eine bereinigte Liste der Verwendungszweck-Zeilen des Auftrages.
   * @param t der Auftrag.
   * @return bereinigte Liste der Verwendungszweck-Zeilen des Auftrages.
   * @throws RemoteException
   */
  public static String[] toArray(Transfer t) throws RemoteException
  {
    List<String> lines = new ArrayList<String>();
    lines.add(t.getZweck());
    lines.add(t.getZweck2());
    String[] wvz = t.getWeitereVerwendungszwecke();
    if (wvz != null && wvz.length > 0)
    {
      for (String s:wvz)
        lines.add(s);
    }
    
    String[] list = lines.toArray(new String[lines.size()]);
    List<String> result = clean(list);
    return result.toArray(new String[result.size()]);
  }

  /**
   * Merget die Verwendungszweck-Zeilen des Auftrages zu einer Zeile zusammen.
   * Als Trennzeichen fuer die Zeilen wird " " (ein Leerzeichen) verwendet.
   * @param t der Auftrag.
   * @return der String mit einer Zeile, die alle Verwendungszwecke enthaelt.
   * @throws RemoteException
   */
  public static String toString(Transfer t) throws RemoteException
  {
    return toString(t," ");
  }
  
  /**
   * Merget die Verwendungszweck-Zeilen des Auftrages zu einer Zeile zusammen.
   * @param t der Auftrag.
   * @param sep das zu verwendende Trennzeichen fuer die Zeilen. Wenn es null ist, wird " "
   * (ein Leerzeichen) verwendet.
   * @return der String mit einer Zeile, die alle Verwendungszwecke enthaelt.
   * @throws RemoteException
   */
  public static String toString(Transfer t, String sep) throws RemoteException
  {
    if (sep == null)
      sep = " ";
    StringBuffer sb = new StringBuffer();
    
    String[] lines = toArray(t);
    for (int i=0;i<lines.length;++i)
    {
      sb.append(lines[i]);
      
      // Trennzeichen bei der letzten Zeile weglassen
      if (i+1 < lines.length)
        sb.append(sep);
    }

    String result = sb.toString();
    
    // Wenn als Trennzeichen "\n" angegeben ist, kann es
    // bei den weiteren Verwendungszwecken drin bleiben
    if (sep.equals("\n"))
      return result;
    
    // Andernfalls ersetzen wir es gegen das angegebene Zeichen
    return result.replace("\n",sep);
  }
  
  /**
   * Bereinigt die Verwendungszweck-Zeilen.
   * Hierbei werden leere Zeilen oder NULL-Elemente entfernt.
   * Ausserdem werden alle Zeilen getrimt.
   * @param lines die zu bereinigenden Zeilen.
   * @return die bereinigten Zeilen.
   */
  private static List<String> clean(String... lines)
  {
    List<String> result = new ArrayList<String>();
    if (lines == null || lines.length == 0)
      return result;
    
    for (String line:lines)
    {
      if (line == null)
        continue;
      line = line.trim();
      if (line.length() > 0)
        result.add(line);
    }
    
    return result;
  }
}


/*********************************************************************
 * $Log$
 * Revision 1.9  2011-06-07 10:07:50  willuhn
 * @C Verwendungszweck-Handling vereinheitlicht/vereinfacht - geht jetzt fast ueberall ueber VerwendungszweckUtil
 *
 * Revision 1.8  2011-05-11 09:12:07  willuhn
 * @C Merge-Funktionen fuer den Verwendungszweck ueberarbeitet
 **********************************************************************/