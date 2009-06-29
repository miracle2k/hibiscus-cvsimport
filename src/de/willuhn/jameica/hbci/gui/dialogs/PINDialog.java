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

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.gui.dialogs.PasswordDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Dialog f�r die PIN-Eingabe.
 * Es muss weder Text, noch Titel oder LabelText gesetzt werden.
 * Das ist alles schon drin.
 */
public class PINDialog extends PasswordDialog {

	private I18N i18n;
	private HBCIPassport passport;

  /**
   * ct.
   * @param passport Passport, fuer den die PIN-Abfrage gemacht wird. Grund: Der
   * PIN-Dialog hat eine eingebaute Checksummen-Pruefung um zu checken, ob die
   * PIN richtig eingegeben wurde. Da diese Checksumme aber pro Passport gespeichert
   * wird, benoetigt der Dialoig eben jenen.
   */
  public PINDialog(HBCIPassport passport) {

    super(PINDialog.POSITION_CENTER);
    setSize(550,SWT.DEFAULT);
    this.passport = passport;

    // BUGZILLA 71 http://www.willuhn.de/bugzilla/show_bug.cgi?id=71
    String suffix = this.passport.getCustomerId();
  
    Konto konto = HBCIFactory.getInstance().getCurrentKonto();
    if (konto != null)
    {
      try
      {
        suffix += "." + konto.getKontonummer();
      }
      catch (RemoteException e)
      {
        Logger.error("unable to append account number to pin wallet entry",e);
      }
    }

		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    setLabelText(i18n.tr("Ihre PIN"));
    String s = null;
    try
    {
      s = konto.getBezeichnung();
      s += " [" + i18n.tr("Nr.") + " " + konto.getKontonummer();
      String name = HBCIUtils.getNameForBLZ(konto.getBLZ());
      if (name != null && name.length() > 0)
        s += " - " + name;
      s += "]";
    }
    catch (Exception e)
    {
      // ignore
    }
    if (s != null)
    {
      setTitle(i18n.tr("PIN-Eingabe. Konto: {0}",s));
      setText(i18n.tr("Bitte geben Sie Ihre PIN ein.\nKonto: {0}",s));
    }
    else
    {
      setTitle(i18n.tr("PIN-Eingabe"));
      setText(i18n.tr("Bitte geben Sie Ihre PIN ein."));
    }
  }

	/**
   * @see de.willuhn.jameica.gui.dialogs.PasswordDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    super.paint(parent);
    getShell().pack();
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.PasswordDialog#checkPassword(java.lang.String)
   */
  protected boolean checkPassword(String password)
	{
    // BUGZILLA 28 http://www.willuhn.de/bugzilla/show_bug.cgi?id=28
		if (password == null || password.length() < HBCIProperties.HBCI_PIN_MINLENGTH || password.length() > HBCIProperties.HBCI_PIN_MAXLENGTH)
		{
			String[] s = new String[] {
			  "" + HBCIProperties.HBCI_PIN_MINLENGTH,
			  "" + HBCIProperties.HBCI_PIN_MAXLENGTH
			};
			setErrorText(i18n.tr("L�nge der PIN ung�ltig ({0}-{1} Zeichen)",s) + " " + getRetryString());
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
 * Revision 1.20  2009-06-29 09:25:29  willuhn
 * @N BUGZILLA 738
 *
 * Revision 1.19  2009/03/31 11:01:40  willuhn
 * @R Speichern des PIN-Hashes komplett entfernt
 *
 * Revision 1.18  2009/03/30 22:54:15  willuhn
 * @C Checksummen-Speicherung geaendert:
 *  1) Es wird SHA1 statt MD5 verwendet
 *  2) Es wird die Checksumme der Checksumme der Checksumme erstellt
 *  3) ein zufaellig erzeugter Salt wird eingefuegt
 *  4) es werden nur noch die ersten 3 Zeichen der Checksumme gespeichert
 *
 * Revision 1.17  2008/02/27 16:12:57  willuhn
 * @N Passwort-Dialog fuer Schluesseldiskette mit mehr Informationen (Konto, Dateiname)
 *
 * Revision 1.16  2006/07/05 23:29:29  willuhn
 * @B bug 174
 *
 * Revision 1.15  2006/01/08 23:23:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2005/07/26 23:00:03  web0
 * @N Multithreading-Support fuer HBCI-Jobs
 *
 * Revision 1.13  2005/05/10 22:26:15  web0
 * @B bug 71
 *
 * Revision 1.12  2005/05/10 22:00:58  web0
 * @B bug 71 muss noch geklaert werden
 *
 * Revision 1.11  2005/03/25 23:08:44  web0
 * @B bug 28
 *
 * Revision 1.10  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.9  2005/02/01 17:15:37  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.6  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/06/30 20:58:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/03/30 22:07:50  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/03/03 22:26:41  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.2  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.1  2004/02/21 19:49:04  willuhn
 * @N PINDialog
 *
 **********************************************************************/