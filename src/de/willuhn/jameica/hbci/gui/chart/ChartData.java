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

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.formatter.Formatter;

/**
 * Basis-Interface, welches die zu zeichnenden Datenreihen enthaelt.
 */
public interface ChartData
{
  /**
   * Liefert die zu zeichnende Datenreihe.
   * @return Datenreihe.
   * @throws RemoteException
   */
  public GenericIterator getData() throws RemoteException;
  
  /**
   * Liefert das Label der Datenreihe.
   * @return Label der Datenreihe.
   * @throws RemoteException
   */
  public String getLabel() throws RemoteException;
  
  /**
   * Liefert den Namen des Attributs, welches fuer die Werte
   * verwendet werden soll. Das Attribut muss numerisch sein.
   * @return Name des Werte-Attributs.
   * @throws RemoteException
   */
  public String getDataAttribute() throws RemoteException;
  
  /**
   * Liefert den Namen des Attributs fuer die Beschriftung.
   * @return Name des Attributs fuer die Beschriftung.
   * @throws RemoteException
   */
  public String getLabelAttribute() throws RemoteException;
  
  /**
   * Liefert einen optionalen Formatter fuer das Beschriftungs-Attribut.
   * @return optionaler Formatter.
   * @throws RemoteException
   */
  public Formatter getLabelFormatter() throws RemoteException;
}


/*********************************************************************
 * $Log$
 * Revision 1.1  2005-12-12 15:46:55  willuhn
 * @N Hibiscus verwendet jetzt Birt zum Erzeugen der Charts
 *
 **********************************************************************/