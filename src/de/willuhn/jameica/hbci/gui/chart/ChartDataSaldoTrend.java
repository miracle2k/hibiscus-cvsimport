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

package de.willuhn.jameica.hbci.gui.chart;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Implementierung eines Datensatzes fuer die Darstellung des Saldo-Durchschnitts.
 */
public class ChartDataSaldoTrend extends ChartDataSaldoVerlauf
{
  /**
   * ct.
   * @param k
   */
  public ChartDataSaldoTrend(Konto k)
  {
    super(k);
  }

  /**
   * ct.
   * @param k
   * @param days
   */
  public ChartDataSaldoTrend(Konto k, int days)
  {
    super(k, days);
  }

  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getData()
   */
  public GenericIterator getData() throws RemoteException
  {
    // Wir holen uns die konkreten Werte von der Super-Klasse und bauen
    // daraus neue mit Mittelwerten.
    List<Umsatz> umsaetze = PseudoIterator.asList(super.getData());
    
    List<Item> items = new ArrayList<Item>();
    for (int i=0;i<umsaetze.size();++i)
    {
      items.add(createAverage(umsaetze,i));
    }
    
    return PseudoIterator.fromArray(items.toArray(new Item[items.size()]));
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartDataSaldoVerlauf#getLabel()
   */
  public String getLabel() throws RemoteException
  {
    return i18n.tr("Trend");
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartDataSaldoVerlauf#getCurve()
   */
  public boolean getCurve()
  {
    return true;
  }

  /**
   * Liefert ein Item, dessen Saldo dem Durchschnitt der x Werte links
   * und rechts daneben entspricht.
   * @param list Liste der Umsaetze.
   * @param pos Position.
   * @return der Durchschnitt.
   */
  private Item createAverage(List<Umsatz> list, int pos)
  {
    Item item = new Item();

    int found = 0;
    Date first = null;
    Date last = null;
    for (int i=-15;i<=15;++i)
    {
      try
      {
        Umsatz current = list.get(pos + i);
        found++;
        
        if (first == null)
          first = current.getDatum();
        
        item.saldo += current.getSaldo();
        last = current.getDatum();
      }
      catch (Exception e)
      {
        // Ignore
      }
    }
    item.saldo /= found;
    
    // Beim Datum nehmen wir das erste und letzte
    // und suchen uns genau den Zeitpunkt dazwischen
    long lf = first.getTime();
    long ll = last.getTime();
    
    long middle = lf + ((ll - lf) / 2);
    item.datum = new Date(middle);

    return item;
  }

  
  /**
   * Hilfsklasse, die einen einzelnen Mittelwert haelt.
   */
  private class Item implements GenericObject
  {
    private double saldo;
    private Date datum = null;
    
    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject o) throws RemoteException
    {
      if (o == null || !(o instanceof Item))
        return false;
      return this.getID().equals(o.getID());
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) throws RemoteException
    {
      if ("saldo".equals(name))
        return this.saldo;
      if ("datum".equals(name))
        return this.datum;
      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[]{"saldo","datum"};
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return this.datum.toString() + this.saldo;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "saldo";
    }
  }
}


/*********************************************************************
 * $Log$
 * Revision 1.1  2009-08-27 13:37:28  willuhn
 * @N Der grafische Saldo-Verlauf zeigt nun zusaetzlich  eine Trendkurve an
 *
 **********************************************************************/