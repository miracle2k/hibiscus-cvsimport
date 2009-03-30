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
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.dialogs.PasswordDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.security.Wallet;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.security.Checksum;
import de.willuhn.util.Base64;
import de.willuhn.util.I18N;

/**
 * Dialog f�r die PIN-Eingabe.
 * Es muss weder Text, noch Titel oder LabelText gesetzt werden.
 * Das ist alles schon drin.
 */
public class PINDialog extends PasswordDialog {

	private I18N i18n;
	private HBCIPassport passport;

  private String walletKey       = null;
  
  private CheckboxInput checkPin = null;
  private Label checkPinText     = null;
  private String checkPinComment = null;

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

    this.walletKey = "hbci.passport.pinchecksum." + suffix;

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
      setText(i18n.tr("Bitte geben Sie Ihre PIN ein. Konto: {0}",s));
    }
    else
    {
      setTitle(i18n.tr("PIN-Eingabe"));
      setText(i18n.tr("Bitte geben Sie Ihre PIN ein."));
    }
    
    this.checkPinComment = i18n.tr("Hinweis: Falls sich Ihre PIN ge�ndert hat, dann l�schen Sie bitte die\n" +
                                   "Check-Summe unter \"Plugins->Hibiscus->Einstellungen->Gespeicherte Pr�fsummen l�schen\"");
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.PasswordDialog#extend(de.willuhn.jameica.gui.util.Container)
   */
  protected void extend(Container container) throws Exception
  {
    // BUGZILLA 174
    this.checkPin = new CheckboxInput(Settings.getCheckPin());
    container.addCheckbox(checkPin,i18n.tr("PIN-Eingabe via Check-Summe pr�fen"));

    Part p = new Part() {
      public void paint(Composite parent) throws RemoteException
      {
        checkPinText = new Label(parent,SWT.WRAP);
        checkPinText.setBackground(Color.BACKGROUND.getSWTColor());
        if (Settings.getCheckPin())
          checkPinText.setText(checkPinComment);
        else
          checkPinText.setText("\n\n\n");
        checkPinText.setForeground(Color.COMMENT.getSWTColor());
        checkPinText.setLayoutData(new GridData(GridData.FILL_BOTH));
        // Workaround fuer Windows, weil dort mehrzeilige
        // Labels nicht korrekt umgebrochen werden.
        checkPinText.addControlListener(new ControlAdapter() {
          public void controlResized(ControlEvent e)
          {
            checkPinText.setSize(checkPinText.computeSize(checkPinText.getSize().x,SWT.DEFAULT));
          }
        });
      }
    };
    container.addPart(p);
  }
  
	/**
   * @see de.willuhn.jameica.gui.dialogs.PasswordDialog#checkPassword(java.lang.String)
   */
  protected boolean checkPassword(String password)
	{
    // Aktuellen Wert der Checkbox holen
    if (this.checkPin != null)
    {
      Boolean b = (Boolean)this.checkPin.getValue();
      
      // Wir uebernehmen den Wert in die Einstellungen
      Settings.setCheckPin(b.booleanValue());
    }

    // BUGZILLA 174
    // Erstmal Text resetten
    if (Settings.getCheckPin() && this.checkPinText != null && !this.checkPinText.isDisposed())
    {
      this.checkPinText.setText("\n\n\n");
    }

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

		// Checksummen-Pruefung nicht aktiv. Also raus.
		if (!Settings.getCheckPin())
			return true;

		try
		{
			// PIN-Ueberpruefung aktiv. Also checken wir die Pruef-Summe
			Wallet w = Settings.getWallet();
			String checkSum = (String) w.get(this.walletKey);
			
			//////////////////////////////////////////////////////////////////////////
			// Migration: Auf neue Speicherung der Checksumme umstellen
			String migration = (String) w.get(this.walletKey + ".migration");
			if (migration == null || migration.length() == 0)
			{
			  Logger.info("migrating pin checksum");
			  checkSum = null; // forciert das Neuanlegen des Hashes
			  w.set(this.walletKey + ".migration",(new Date()).toString());
			}
			//
      //////////////////////////////////////////////////////////////////////////

			if (checkSum == null || checkSum.length() == 0)
			{
				// Das ist die erste Eingabe. Wir speichern eine Checksumme
			  // der PIN
				try {
					w.set(this.walletKey,getChecksum(password));
				}
				catch (NoSuchAlgorithmException e)
				{
					Logger.error("hash algorithm not found",e);
					GUI.getStatusBar().setErrorText(i18n.tr("Pr�fsumme konnte nicht ermittelt werden. Option wurde deaktiviert."));
					Settings.setCheckPin(false);
					w.set(this.walletKey,null);
				}
				return true;
			}

      // Check-Summe existiert, dann vergleichen wir die Eingabe
			String n = null;
			try {
				n = getChecksum(password);
			}
			catch (NoSuchAlgorithmException e)
			{
				Logger.error("hash algorithm not found",e);
				GUI.getStatusBar().setErrorText(i18n.tr("Pr�fsumme konnte nicht verglichen werden. Option wurde deaktiviert."));
				Settings.setCheckPin(false);
				w.set(this.walletKey,null);
				return true;
			}
			if (n != null && checkSum != null && n.length() > 0 && checkSum.length() > 0 && n.equals(checkSum))
			{
				// Eingabe korrekt
				return true;
			}

      setErrorText(i18n.tr("PIN falsch.") + " " + getRetryString());

      // BUGZILLA 174
      if (this.checkPinText != null && !this.checkPinText.isDisposed())
      {
        this.checkPinText.setText(checkPinComment);
        this.checkPinText.redraw();
      }

      return false;

    }
		catch (Exception e)
		{
			Logger.error("error while checking pin",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Pr�fsumme konnte nicht verglichen werden. Option wurde deaktiviert."));
			Settings.setCheckPin(false);
			return true;
		}
	}
  
  /**
   * Erzeugt die Checksumme fuer das Passwort.
   * @param pw das Passwort.
   * @return die Checksumme.
   * @throws Exception
   */
  private String getChecksum(String pw) throws Exception
  {
    Wallet w = Settings.getWallet();
    String salt = (String) w.get("salt");
    if (salt == null || salt.length() == 0)
    {
      byte[] b = new byte[2];
      SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
      random.nextBytes(b);
      salt = Base64.encode(b);
      w.set("salt",salt);
    }
    
    byte[] src = (pw + salt).getBytes();
    String a = Checksum.SHA1;
    String dst = Base64.encode(Checksum.checksum(Checksum.checksum(Checksum.checksum(src,a),a),a));
    return dst.substring(0,3); // Wir verwenden nur einen Teil der Checksumme
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
 * Revision 1.18  2009-03-30 22:54:15  willuhn
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