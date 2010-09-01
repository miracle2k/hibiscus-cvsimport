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

package de.willuhn.jameica.hbci.util;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.TreeMap;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Umsatz;

/**
 * Hilfsklasse zum Finden eines Saldos zum angegebenen Zeitpunkt aus
 * einer vorgegebenen Liste von Umsaetzen.
 */
public class SaldoFinder
{
  private TreeMap<Date,Double> map = new TreeMap<Date,Double>();
  
  /**
   * ct.
   * @param umsaetze Liste der Umsaetze, in denen gesucht werden soll.
   * @throws RemoteException
   */
  public SaldoFinder(GenericIterator umsaetze) throws RemoteException
  {
    // Wir fuellen die Map
    while (umsaetze.hasNext())
    {
      Umsatz u = (Umsatz) umsaetze.next();
      // Vormerkbuchungen werden nicht beruecksichtigt, weil sie keinen Saldo haben
      if ((u.getFlags() & Umsatz.FLAG_NOTBOOKED) == Umsatz.FLAG_NOTBOOKED)
        continue;
      this.map.put(u.getValuta(),u.getSaldo());
    }
  }
  
  /**
   * Liefert den Saldo zum angegebenen Zeitpunkt.
   * @param date das Datum.
   * @return der Saldo zu diesem Zeitpunkt.
   */
  public Double get(Date date)
  {
    if (date == null)
      return 0.0d;
    Date key = HBCIProperties.startOfDay(date);
    
    // Checken, ob wir fuer genau diesen Tag einen Saldo haben
    Double d = this.map.get(key);
    if (d != null)
      return d;
    
    // Haben wir einen Saldo zu einem frueheren Zeitpunkt?
    Date lower = this.map.lowerKey(date); // JAVA 1.6
    if (lower != null)
      return this.map.get(lower);
    
    // Ne, wir haben auch keinen frueheren Saldo. Also war
    // er zu diesem Zeitpunkt noch 0.
    return 0.0d;
  }
}



/**********************************************************************
 * $Log$
 * Revision 1.3  2010-09-01 15:33:54  willuhn
 * @B Vormerkbuchungen in Saldo-Verlauf ignorieren, weil sie keinen Saldo haben
 *
 * Revision 1.2  2010-08-13 10:49:33  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2010-08-12 17:12:32  willuhn
 * @N Saldo-Chart komplett ueberarbeitet (Daten wurden vorher mehrmals geladen, Summen-Funktion, Anzeige mehrerer Konten, Durchschnitt ueber mehrere Konten, Bugfixing, echte "Homogenisierung" der Salden via SaldoFinder)
 *
 **********************************************************************/