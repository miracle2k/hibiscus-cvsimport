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
package de.willuhn.jameica.hbci;

import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * enthaelt HBCI-Parameter.
 */
public class HBCIProperties
{

	private static Settings settings = new Settings(HBCIProperties.class);
  private static I18N i18n = null;
  
  static
  {
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

	/**
	 * Liste der erlaubten Zeichen (z.Bsp. fuer den Verwendungszweck.).
	 */
	public final static String HBCI_DTAUS_VALIDCHARS =
		settings.getString("hbci.dtaus.validchars",
											 "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ,.&-+*%/$�������"
		); 

  /**
   * Maximale Text-Laenge einer Verwendungszweck-Zeile.
   */
  public final static int HBCI_TRANSFER_USAGE_MAXLENGTH =
    settings.getInt("hbci.transfer.usage.maxlength",27);

	/**
	 * Maximale Text-Laenge fuer Namen.
	 */
	public final static int HBCI_TRANSFER_NAME_MAXLENGTH =
		settings.getInt("hbci.transfer.name.maxlength",27);

  /**
   * Prueft die uebergebenen Strings auf Vorhandensein nicht erlaubter Zeichen.
   * @param chars zu testende Zeichen.
   * @throws ApplicationException
   */
  public final static void checkChars(String chars) throws ApplicationException
  {
    if (chars == null || chars.length() == 0)
      return;
    char[] c = chars.toCharArray();
    for (int i=0;i<c.length;++i)
    {
      if (HBCIProperties.HBCI_DTAUS_VALIDCHARS.indexOf(c[i]) == -1)
        throw new ApplicationException(i18n.tr("Das Zeichen \"{0}\" darf nicht verwendet werden",""+c[i])); 
    }
  }


	// disabled
	private HBCIProperties()
	{
	}

}


/**********************************************************************
 * $Log$
 * Revision 1.4  2005-03-05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.3  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
 * Revision 1.2  2004/11/02 18:48:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/11/01 23:10:19  willuhn
 * @N Pruefung auf gueltige Zeichen in Verwendungszweck
 *
 **********************************************************************/