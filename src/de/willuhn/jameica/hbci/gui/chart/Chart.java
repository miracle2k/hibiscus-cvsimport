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

import de.willuhn.jameica.gui.Part;

/**
 * Basis-Interface fuer ein Chart.
 */
public interface Chart extends Part
{
  /**
   * Speichert den Titel des Charts.
   * @param title Titel.
   */
  public void setTitle(String title);
  
  /**
   * Liefert den Titel des Charts.
   * @return Titel.
   */
  public String getTitle();
  
  /**
   * Fuegt dem Chart eine Datenreihe hinzu,
   * @param data
   */
  public void addData(ChartData data);
  
  /**
   * Entfernt eine Datenreihe aus dem Chart.
   * @param data
   */
  public void removeData(ChartData data);

  /**
   * Zeichnet das Chart neu.
   * @throws RemoteException
   */
  public void redraw() throws RemoteException;
}


/*********************************************************************
 * $Log$
 * Revision 1.3  2006-03-09 18:24:05  willuhn
 * @N Auswahl der Tage in Umsatz-Chart
 *
 * Revision 1.2  2005/12/20 00:03:27  willuhn
 * @N Test-Code fuer Tortendiagramm-Auswertungen
 *
 * Revision 1.1  2005/12/12 15:46:55  willuhn
 * @N Hibiscus verwendet jetzt Birt zum Erzeugen der Charts
 *
 **********************************************************************/