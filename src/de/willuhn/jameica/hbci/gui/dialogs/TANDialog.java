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
package de.willuhn.jameica.hbci.gui.dialogs;

import de.willuhn.jameica.gui.dialogs.PasswordDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Dialog f�r die TAN-Eingabe.
 * Es muss weder Text, noch Titel oder LabelText gesetzt werden.
 * Das ist alles schon drin.
 */
public class TANDialog extends PasswordDialog {

	private I18N i18n;
  /**
   * ct.
   */
  public TANDialog() {
    super(TANDialog.POSITION_CENTER);
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    // Deaktivierung der Anzeige von Sternen im TAN-Dialog.
    setShowPassword(Settings.getShowTan());

    setTitle(i18n.tr("TAN-Eingabe"));
    setLabelText(i18n.tr("Ihre TAN"));
    setText(i18n.tr("Bitte geben Sie eine TAN-Nummer ein."));
  }

	/**
   * @see de.willuhn.jameica.gui.dialogs.PasswordDialog#checkPassword(java.lang.String)
   */
  protected boolean checkPassword(String password)
	{
		if (password == null || password.length() == 0)
		{
			setErrorText(i18n.tr("Fehler: Bitte geben Sie eine TAN ein.") + " " + getRetryString());
			return false;
		}
		return true;
	}

	/**
	 * Liefert einen locale String mit der Anzahl der Restversuche.
	 * z.Bsp.: "Noch 2 Versuche.".
   * @return String mit den Restversuchen.
   */
  private String getRetryString()
	{
		String retries = getRemainingRetries() > 1 ? i18n.tr("Versuche") : i18n.tr("Versuch");
		return (i18n.tr("Noch") + " " + getRemainingRetries() + " " + retries + ".");
	}
}


/**********************************************************************
 * $Log$
 * Revision 1.6  2005-06-06 09:54:39  web0
 * *** empty log message ***
 *
 * Revision 1.5  2005/06/02 22:57:34  web0
 * @N Export von Konto-Umsaetzen
 *
 * Revision 1.4  2005/02/02 16:15:52  willuhn
 * @N Neue Dialoge fuer RDH
 *
 * Revision 1.3  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.2  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/05/05 21:27:13  willuhn
 * @N added TAN-Dialog
 *
 **********************************************************************/