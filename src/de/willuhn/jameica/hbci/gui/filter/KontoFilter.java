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

package de.willuhn.jameica.hbci.gui.filter;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Konto;

/**
 * Mit diesem Filter koennen einzelne Konten bei der Suche
 * ausgefiltert werden. Das wird z.Bsp. genutzt, um bei
 * Auslandsueberweisungen nur jene Konten anzuzeigen, die
 * eine IBAN besitzen.
 */
public interface KontoFilter extends Filter<Konto>
{
  /**
   * @see de.willuhn.jameica.hbci.gui.filter.Filter#accept(java.lang.Object)
   */
  public boolean accept(Konto konto) throws RemoteException;
  
  /**
   * Filter, der alle Konten zulaesst.
   */
  public final static KontoFilter ALL = new KontoFilter()
  {
    /**
     * @see de.willuhn.jameica.hbci.gui.filter.KontoFilter#accept(de.willuhn.jameica.hbci.rmi.Konto)
     */
    public boolean accept(Konto konto) throws RemoteException
    {
      return true;
    }
  };
  
  /**
   * Filter, der nur aktive Konten zulaesst.
   */
  public final static KontoFilter ACTIVE = new KontoFilter()
  {
    /**
     * @see de.willuhn.jameica.hbci.gui.filter.KontoFilter#accept(de.willuhn.jameica.hbci.rmi.Konto)
     */
    public boolean accept(Konto konto) throws RemoteException
    {
      if (konto == null)
        return false;
      return (konto.getFlags() & Konto.FLAG_DISABLED) != Konto.FLAG_DISABLED;
    }
  };

  /**
   * Filter, der nur aktive Konten zulaesst, die eine IBAN haben.
   */
  public final static KontoFilter FOREIGN = new KontoFilter()
  {
    /**
     * @see de.willuhn.jameica.hbci.gui.filter.AddressFilter#accept(de.willuhn.jameica.hbci.rmi.Address)
     */
    public boolean accept(Konto konto) throws RemoteException
    {
      if (konto == null)
        return false;
      
      String iban = konto.getIban();

      return ((konto.getFlags() & Konto.FLAG_DISABLED) != Konto.FLAG_DISABLED) &&
             (iban != null && iban.length() <= HBCIProperties.HBCI_IBAN_MAXLENGTH);
    }
  }; 
}


/**********************************************************************
 * $Log$
 * Revision 1.1  2009-10-20 23:12:58  willuhn
 * @N Support fuer SEPA-Ueberweisungen
 * @N Konten um IBAN und BIC erweitert
 *
 * Revision 1.1  2009/03/13 00:25:12  willuhn
 * @N Code fuer Auslandsueberweisungen fast fertig
 *
 **********************************************************************/
