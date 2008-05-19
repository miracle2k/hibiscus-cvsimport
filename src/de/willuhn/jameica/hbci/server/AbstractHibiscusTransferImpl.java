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
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.HibiscusTransfer;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Implementierung von Geld-Transfers zwischen Konten.
 */
public abstract class AbstractHibiscusTransferImpl extends AbstractDBObject implements HibiscusTransfer
{

  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * ct.
   * @throws RemoteException
   */
  public AbstractHibiscusTransferImpl() throws RemoteException {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException {
  	try {
			if (getKonto() == null)
				throw new ApplicationException(i18n.tr("Bitte w�hlen Sie ein Konto aus."));
			if (getKonto().isNewObject())
				throw new ApplicationException(i18n.tr("Bitte speichern Sie zun�chst das Konto"));

			if (getGegenkontoNummer() == null || getGegenkontoNummer().length() == 0)
				throw new ApplicationException(i18n.tr("Bitte geben Sie die Kontonummer des Gegenkontos ein"));
			
			if (getGegenkontoBLZ() == null || getGegenkontoBLZ().length() == 0)
				throw new ApplicationException(i18n.tr("Bitte geben Sie die BLZ des Gegenkontos ein"));

      // BUGZILLA 280
      HBCIProperties.checkChars(getGegenkontoNummer(), HBCIProperties.HBCI_KTO_VALIDCHARS);
      HBCIProperties.checkChars(getGegenkontoBLZ(), HBCIProperties.HBCI_BLZ_VALIDCHARS);
      HBCIProperties.checkLength(getGegenkontoNummer(), HBCIProperties.HBCI_KTO_MAXLENGTH_HARD);

      if (getBetrag() == 0.0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie einen g�ltigen Betrag ein."));

      if (getGegenkontoName() == null || getGegenkontoName().length() == 0)
				throw new ApplicationException(i18n.tr("Bitte geben Sie den Namen des Kontoinhabers des Gegenkontos ein"));

      int blzLen = getGegenkontoBLZ().length();
      if (blzLen != HBCIProperties.HBCI_BLZ_LENGTH)
        throw new ApplicationException(i18n.tr("Ung�ltige BLZ \"{0}\". Muss {1} Stellen lang sein.", new String[]{getGegenkontoBLZ(),""+HBCIProperties.HBCI_BLZ_LENGTH}));
      
      HBCIProperties.checkLength(getGegenkontoName(), HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH);

      // BUGZILLA 163
      HBCIProperties.checkChars(getGegenkontoName(), HBCIProperties.HBCI_DTAUS_VALIDCHARS);

      if (!HBCIProperties.checkAccountCRC(getGegenkontoBLZ(),getGegenkontoNummer()))
				throw new ApplicationException(i18n.tr("Ung�ltige BLZ/Kontonummer. Bitte pr�fen Sie Ihre Eingaben."));
				
			if (getZweck() == null || "".equals(getZweck()))
				throw new ApplicationException(i18n.tr("Bitte geben Sie einen Verwendungszweck ein"));

      HBCIProperties.checkLength(getZweck(), HBCIProperties.HBCI_TRANSFER_USAGE_MAXLENGTH);
      HBCIProperties.checkLength(getZweck2(), HBCIProperties.HBCI_TRANSFER_USAGE_MAXLENGTH);

			HBCIProperties.checkChars(getZweck(), HBCIProperties.HBCI_DTAUS_VALIDCHARS);
      HBCIProperties.checkChars(getZweck2(), HBCIProperties.HBCI_DTAUS_VALIDCHARS);
  	}
  	catch (RemoteException e)
  	{
  		Logger.error("error while checking ueberweisung",e);
  		throw new ApplicationException(i18n.tr("Fehler beim Pr�fen der �berweisung."));
  	}
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException {
		insertCheck();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getForeignObject(java.lang.String)
   */
  protected Class getForeignObject(String field) throws RemoteException {
		if ("konto_id".equals(field))
			return Konto.class;
    return super.getForeignObject(field);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#getKonto()
   */
  public Konto getKonto() throws RemoteException {
    return (Konto) getAttribute("konto_id");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getBetrag()
   */
  public double getBetrag() throws RemoteException {
		Double d = (Double) getAttribute("betrag");
		if (d == null)
			return 0;
		return d.doubleValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getZweck()
   */
  public String getZweck() throws RemoteException {
    return (String) getAttribute("zweck");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getZweck2()
   */
  public String getZweck2() throws RemoteException {
		return (String) getAttribute("zweck2");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setKonto(de.willuhn.jameica.hbci.rmi.Konto)
   */
  public void setKonto(Konto konto) throws RemoteException {
		setAttribute("konto_id",konto);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setBetrag(double)
   */
  public void setBetrag(double betrag) throws RemoteException {
		setAttribute("betrag", new Double(betrag));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setZweck(java.lang.String)
   */
  public void setZweck(String zweck) throws RemoteException {
		setAttribute("zweck",zweck);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setZweck2(java.lang.String)
   */
  public void setZweck2(String zweck2) throws RemoteException {
		setAttribute("zweck2",zweck2);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getGegenkontoNummer()
   */
  public String getGegenkontoNummer() throws RemoteException {
    return (String) getAttribute("empfaenger_konto");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getGegenkontoBLZ()
   */
  public String getGegenkontoBLZ() throws RemoteException {
		return (String) getAttribute("empfaenger_blz");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getGegenkontoName()
   */
  public String getGegenkontoName() throws RemoteException {
		return (String) getAttribute("empfaenger_name");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setGegenkontoNummer(java.lang.String)
   */
  public void setGegenkontoNummer(String konto) throws RemoteException {
		setAttribute("empfaenger_konto",konto);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setGegenkontoBLZ(java.lang.String)
   */
  public void setGegenkontoBLZ(String blz) throws RemoteException {
		setAttribute("empfaenger_blz",blz);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setGegenkontoName(java.lang.String)
   */
  public void setGegenkontoName(String name) throws RemoteException {
		setAttribute("empfaenger_name",name);
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#delete()
   */
  public void delete() throws RemoteException, ApplicationException
  {
		Konto k = this.getKonto();
		super.delete();
		if (k == null)
			return;
    String[] params = new String[]
    {
      getGegenkontoName(),
      getGegenkontoNummer(),
      getGegenkontoBLZ(),
      k.getWaehrung(),
      HBCI.DECIMALFORMAT.format(getBetrag())
    };
    k.addToProtokoll(i18n.tr("Auftrag [Gegenkonto: {0}, Kto. {1}, BLZ {2}] {3} {4} gel�scht",params),Protokoll.TYP_SUCCESS);
  }

	/**
	 * @see de.willuhn.datasource.rmi.Changeable#store()
	 */
	public void store() throws RemoteException, ApplicationException
	{
		super.store();
		Konto k = this.getKonto();
    String[] params = new String[]
    {
      getGegenkontoName(),
      getGegenkontoNummer(),
      getGegenkontoBLZ(),
      k.getWaehrung(),
      HBCI.DECIMALFORMAT.format(getBetrag())
    };
    k.addToProtokoll(i18n.tr("Auftrag [Gegenkonto: {0}, Kto. {1}, BLZ {2}] {3} {4} gespeichert",params),Protokoll.TYP_SUCCESS);
	}

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setGegenkonto(de.willuhn.jameica.hbci.rmi.Address)
   */
  public void setGegenkonto(Address e) throws RemoteException
  {
  	if (e == null)
  		return;
    setGegenkontoBLZ(e.getBLZ());
  	setGegenkontoNummer(e.getKontonummer());
  	setGegenkontoName(e.getName());
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getWeitereVerwendungszwecke()
   */
  public GenericIterator getWeitereVerwendungszwecke() throws RemoteException
  {
    return VerwendungszweckUtil.get(this);
  }
}


/**********************************************************************
 * $Log$
 * Revision 1.6  2008-05-19 22:35:53  willuhn
 * @N Maximale Laenge von Kontonummern konfigurierbar (Soft- und Hardlimit)
 * @N Laengenpruefungen der Kontonummer in Dialogen und Fachobjekten
 *
 * Revision 1.5  2008/04/27 22:22:56  willuhn
 * @C I18N-Referenzen statisch
 *
 * Revision 1.4  2008/02/15 17:39:10  willuhn
 * @N BUGZILLA 188 Basis-API fuer weitere Zeilen Verwendungszweck. GUI fehlt noch
 * @N DB-Update 0005. Speichern des Textschluessels bei Sammelauftragsbuchungen in der Datenbank
 *
 * Revision 1.3  2007/10/18 10:24:49  willuhn
 * @B Foreign-Objects in AbstractDBObject auch dann korrekt behandeln, wenn sie noch nicht gespeichert wurden
 * @C Beim Abrufen der Dauerauftraege nicht mehr nach Konten suchen sondern hart dem Konto zuweisen, ueber das sie abgerufen wurden
 *
 * Revision 1.2  2007/06/04 16:38:33  willuhn
 * @N Pruefung der BLZ-Laenge
 *
 * Revision 1.1  2007/04/23 18:07:15  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.28  2006/12/01 00:02:34  willuhn
 * @C made unserializable members transient
 *
 * Revision 1.27  2006/10/06 16:00:42  willuhn
 * @B Bug 280
 *
 * Revision 1.26  2006/06/26 13:25:20  willuhn
 * @N Franks eBay-Parser
 *
 * Revision 1.25  2006/06/08 22:29:47  willuhn
 * @N DTAUS-Import fuer Sammel-Lastschriften und Sammel-Ueberweisungen
 * @B Eine Reihe kleinerer Bugfixes in Sammeltransfers
 * @B Bug 197 besser geloest
 *
 * Revision 1.24  2006/05/11 10:57:35  willuhn
 * @C merged Bug 232 into HEAD
 *
 * Revision 1.23.2.1  2006/05/11 10:44:43  willuhn
 * @B bug 232
 *
 * Revision 1.23  2006/02/20 17:33:08  willuhn
 * @B bug 197
 *
 * Revision 1.22  2006/02/06 16:03:50  willuhn
 * @B bug 163
 *
 * Revision 1.21  2005/05/19 23:31:07  web0
 * @B RMI over SSL support
 * @N added handbook
 *
 * Revision 1.20  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.19  2005/03/02 17:59:30  web0
 * @N some refactoring
 *
 * Revision 1.18  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
 * Revision 1.17  2005/02/27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 * Revision 1.16  2005/01/19 00:33:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2005/01/19 00:16:05  willuhn
 * @N Lastschriften
 *
 * Revision 1.14  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/11/02 18:48:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/11/01 23:10:19  willuhn
 * @N Pruefung auf gueltige Zeichen in Verwendungszweck
 *
 * Revision 1.11  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 * Revision 1.10  2004/10/17 16:28:46  willuhn
 * @N Die ersten Dauerauftraege abgerufen ;)
 *
 * Revision 1.9  2004/10/15 20:09:43  willuhn
 * @B Laengen-Pruefung bei Empfaengername
 *
 * Revision 1.8  2004/08/18 23:13:51  willuhn
 * @D Javadoc
 *
 * Revision 1.7  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.6  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/07/20 22:53:03  willuhn
 * @C Refactoring
 *
 * Revision 1.4  2004/07/20 21:48:00  willuhn
 * @N ContextMenus
 *
 * Revision 1.3  2004/07/14 23:48:31  willuhn
 * @N mehr Code fuer Dauerauftraege
 *
 * Revision 1.2  2004/07/13 22:20:37  willuhn
 * @N Code fuer DauerAuftraege
 * @C paar Funktionsnamen umbenannt
 *
 * Revision 1.1  2004/07/11 16:14:29  willuhn
 * @N erster Code fuer Dauerauftraege
 *
 * Revision 1.14  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 * Revision 1.13  2004/06/30 20:58:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/06/17 00:14:10  willuhn
 * @N GenericObject, GenericIterator
 *
 * Revision 1.11  2004/05/26 23:23:10  willuhn
 * @N neue Sicherheitsabfrage vor Ueberweisung
 * @C Check des Ueberweisungslimit
 * @N Timeout fuer Messages in Statusbars
 *
 * Revision 1.10  2004/05/23 15:33:10  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/04/24 19:04:51  willuhn
 * @N Ueberweisung.execute works!! ;)
 *
 * Revision 1.8  2004/04/22 23:46:50  willuhn
 * @N UeberweisungJob
 *
 * Revision 1.7  2004/04/19 22:05:51  willuhn
 * @C HBCIJobs refactored
 *
 * Revision 1.6  2004/04/14 23:53:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/04/05 23:28:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.3  2004/03/05 00:04:10  willuhn
 * @N added code for umsatzlist
 *
 * Revision 1.2  2004/02/17 01:01:38  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/