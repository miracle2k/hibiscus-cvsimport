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
package de.willuhn.jameica.hbci.passports.ddv.server;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.passports.ddv.rmi.Reader;

/**
 * Implementierung fuer die Default-Einstellungen des
 * "Kaan Standard Plus USB" von Kobil.
 */
public class KaanStandardPlusReader extends AbstractKaanReader implements Reader
{

  /**
   * @throws RemoteException
   */
  public KaanStandardPlusReader() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getName()
   */
  public String getName() throws RemoteException
  {
    return "Kaan Standard Plus USB (Kobil)";
  }
}


/**********************************************************************
 * $Log$
 * Revision 1.1  2010-06-17 11:45:48  willuhn
 * @C kompletten Code aus "hbci_passport_ddv" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 *
 * Revision 1.7  2008/09/15 21:53:44  willuhn
 * @N Kaan TriB@nk + 64Bit-Support
 *
 * Revision 1.6  2008/07/29 08:27:43  willuhn
 * @N Kaan TriB@nk
 * @C Pfadtrenner via File.separator
 *
 * Revision 1.5  2006/08/03 22:13:49  willuhn
 * @N OmniKey 4000 Preset
 *
 * Revision 1.4  2005/08/08 15:07:35  willuhn
 * @N added jnilib for mac os
 * @N os autodetection for mac os
 *
 * Revision 1.3  2005/04/11 17:22:00  web0
 * @C backslashes for windows
 *
 * Revision 1.2  2005/01/15 18:06:35  willuhn
 * @C path to correct CT API driver for windows
 *
 * Revision 1.1  2004/09/16 22:35:39  willuhn
 * @N Kaan Standard Plus
 *
 * Revision 1.2  2004/07/27 23:51:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/27 22:56:18  willuhn
 * @N Reader presets
 *
 **********************************************************************/