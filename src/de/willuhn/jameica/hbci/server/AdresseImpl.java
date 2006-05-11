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

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Adresse;
import de.willuhn.jameica.hbci.rmi.SammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisungBuchung;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 */
public class AdresseImpl extends AbstractDBObject implements Adresse {

  private I18N i18n = null;
  /**
   * @throws RemoteException
   */
  public AdresseImpl() throws RemoteException {
    super();
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName() {
    return "empfaenger";
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException {
    return "name";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException {
		try {

			if (getName() == null || getName().length() == 0)
				throw new ApplicationException(i18n.tr("Bitte geben Sie einen Namen ein."));

      HBCIProperties.checkLength(getName(), HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH);

			if (getBLZ() == null || getBLZ().length() == 0)
				throw new ApplicationException(i18n.tr("Bitte geben Sie eine BLZ ein."));

			if (getKontonummer() == null || getKontonummer().length() == 0)
				throw new ApplicationException(i18n.tr("Bitte geben Sie eine Kontonummer ein."));

			if (!HBCIProperties.checkAccountCRC(getBLZ(),getKontonummer()))
				throw new ApplicationException(i18n.tr("Ung�ltige BLZ/Kontonummer. Bitte pr�fen Sie Ihre Eingaben."));

		}
		catch (RemoteException e)
		{
			Logger.error("error while checking empfaenger",e);
			throw new ApplicationException(i18n.tr("Fehler bei der Pr�fung des Empf�ngers"));
		}
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException {
		insertCheck();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Adresse#getKontonummer()
   */
  public String getKontonummer() throws RemoteException {
    return (String) getAttribute("kontonummer");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Adresse#getBLZ()
   */
  public String getBLZ() throws RemoteException {
		return (String) getAttribute("blz");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Adresse#getName()
   */
  public String getName() throws RemoteException {
		return (String) getAttribute("name");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Adresse#setKontonummer(java.lang.String)
   */
  public void setKontonummer(String kontonummer) throws RemoteException {
  	setAttribute("kontonummer",kontonummer);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Adresse#setBLZ(java.lang.String)
   */
  public void setBLZ(String blz) throws RemoteException {
  	setAttribute("blz",blz);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Adresse#setName(java.lang.String)
   */
  public void setName(String name) throws RemoteException {
  	setAttribute("name",name);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Adresse#getKommentar()
   */
  public String getKommentar() throws RemoteException
  {
    return (String) getAttribute("kommentar");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Adresse#setKommentar(java.lang.String)
   */
  public void setKommentar(String kommentar) throws RemoteException
  {
    setAttribute("kommentar",kommentar);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Adresse#getUmsaetze()
   */
  public DBIterator getUmsaetze() throws RemoteException
  {
    DBIterator umsaetze = getService().createList(Umsatz.class);
    umsaetze.addFilter("empfaenger_konto = '" + getKontonummer() + "'");
    umsaetze.addFilter("empfaenger_blz = '" + getBLZ() + "'");
    umsaetze.setOrder(" ORDER BY TONUMBER(valuta) DESC");
    return umsaetze;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Adresse#getSammellastBuchungen()
   */
  public DBIterator getSammellastBuchungen() throws RemoteException
  {
    DBIterator buchungen = getService().createList(SammelLastBuchung.class);
    buchungen.addFilter("gegenkonto_nr = '" + getKontonummer() + "'");
    buchungen.addFilter("gegenkonto_blz = '" + getBLZ() + "'");
    buchungen.setOrder(" ORDER BY id DESC");
    return buchungen;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Adresse#getSammelUeberweisungBuchungen()
   */
  public DBIterator getSammelUeberweisungBuchungen() throws RemoteException
  {
    DBIterator buchungen = getService().createList(SammelUeberweisungBuchung.class);
    buchungen.addFilter("gegenkonto_nr = '" + getKontonummer() + "'");
    buchungen.addFilter("gegenkonto_blz = '" + getBLZ() + "'");
    buchungen.setOrder(" ORDER BY id DESC");
    return buchungen;
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.10  2006-05-11 10:57:35  willuhn
 * @C merged Bug 232 into HEAD
 *
 * Revision 1.9.4.1  2006/05/11 10:44:43  willuhn
 * @B bug 232
 *
 * Revision 1.9  2005/10/03 16:17:57  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2005/08/22 12:23:18  willuhn
 * @N bug 107
 *
 * Revision 1.7  2005/08/16 21:33:13  willuhn
 * @N Kommentar-Feld in Adressen
 * @N Neuer Adress-Auswahl-Dialog
 * @B Checkbox "in Adressbuch speichern" in Ueberweisungen
 *
 * Revision 1.6  2005/05/30 22:55:27  web0
 * *** empty log message ***
 *
 * Revision 1.5  2005/05/19 23:31:07  web0
 * @B RMI over SSL support
 * @N added handbook
 *
 * Revision 1.4  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.3  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.2  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
 * Revision 1.1  2005/02/27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 * Revision 1.11  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/11/02 18:48:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/10/15 20:09:43  willuhn
 * @B Laengen-Pruefung bei Empfaengername
 *
 * Revision 1.8  2004/08/18 23:13:51  willuhn
 * @D Javadoc
 *
 * Revision 1.7  2004/07/23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.6  2004/07/13 22:20:37  willuhn
 * @N Code fuer DauerAuftraege
 * @C paar Funktionsnamen umbenannt
 *
 * Revision 1.5  2004/06/30 20:58:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/06/17 00:14:10  willuhn
 * @N GenericObject, GenericIterator
 *
 * Revision 1.3  2004/04/05 23:28:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/22 20:04:54  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.1  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/