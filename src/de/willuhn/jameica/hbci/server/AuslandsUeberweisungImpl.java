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

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.Duplicatable;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Eine Auslands-Ueberweisung.
 */
public class AuslandsUeberweisungImpl extends AbstractBaseUeberweisungImpl
  implements AuslandsUeberweisung
{
  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @throws RemoteException
   */
  public AuslandsUeberweisungImpl() throws RemoteException {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName() {
    return "aueberweisung";
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Duplicatable#duplicate()
   */
  public Duplicatable duplicate() throws RemoteException {
    AuslandsUeberweisung u = (AuslandsUeberweisung) getService().createObject(AuslandsUeberweisung.class,null);
    u.setBetrag(getBetrag());
    u.setGegenkontoNummer(getGegenkontoNummer());
    u.setGegenkontoName(getGegenkontoName());
    u.setKonto(getKonto());
    u.setZweck(getZweck());
    return u;
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
        throw new ApplicationException(i18n.tr("Bitte geben Sie die IBAN des Gegenkontos ein"));
      
      if (getGegenkontoInstitut() == null || getGegenkontoInstitut().length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie den vollst�ndigen Namen der Zielbank ein"));

      HBCIProperties.checkChars(getGegenkontoNummer(), HBCIProperties.HBCI_KTO_VALIDCHARS);
      HBCIProperties.checkLength(getGegenkontoNummer(), HBCIProperties.HBCI_IBAN_MAXLENGTH);

      double betrag = getBetrag();
      if (betrag == 0.0 || Double.isNaN(betrag))
        throw new ApplicationException(i18n.tr("Bitte geben Sie einen g�ltigen Betrag ein."));

      if (getGegenkontoName() == null || getGegenkontoName().length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie den Namen des Kontoinhabers des Gegenkontos ein"));

      HBCIProperties.checkLength(getGegenkontoName(), HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH);
      HBCIProperties.checkChars(getGegenkontoName(), HBCIProperties.HBCI_DTAUS_VALIDCHARS);

      if (!HBCIProperties.checkIBANCRC(getGegenkontoNummer()))
        throw new ApplicationException(i18n.tr("Ung�ltige IBAN. Bitte pr�fen Sie Ihre Eingaben."));
        
      HBCIProperties.checkLength(getZweck(), HBCIProperties.HBCI_FOREIGNTRANSFER_USAGE_MAXLENGTH);
      HBCIProperties.checkChars(getZweck(), HBCIProperties.HBCI_DTAUS_VALIDCHARS);
    }
    catch (RemoteException e)
    {
      Logger.error("error while checking foreign ueberweisung",e);
      throw new ApplicationException(i18n.tr("Fehler beim Pr�fen der Auslands�berweisung."));
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.server.AbstractBaseUeberweisungImpl#getTextSchluessel()
   */
  @Override
  public String getTextSchluessel() throws RemoteException
  {
    throw new RemoteException("textschluessel not allowed for foreign transfer");
  }

  /**
   * @see de.willuhn.jameica.hbci.server.AbstractBaseUeberweisungImpl#setTextSchluessel(java.lang.String)
   */
  @Override
  public void setTextSchluessel(String schluessel) throws RemoteException
  {
    throw new RemoteException("textschluessel not allowed for foreign transfer");
  }

  /**
   * @see de.willuhn.jameica.hbci.server.AbstractHibiscusTransferImpl#getGegenkontoBLZ()
   */
  @Override
  public String getGegenkontoBLZ() throws RemoteException
  {
    throw new RemoteException("blz not allowed for foreign transfer");
  }

  /**
   * @see de.willuhn.jameica.hbci.server.AbstractHibiscusTransferImpl#getWeitereVerwendungszwecke()
   */
  @Override
  public String[] getWeitereVerwendungszwecke() throws RemoteException
  {
    throw new RemoteException("extended usages not allowed for foreign transfer");
  }

  /**
   * @see de.willuhn.jameica.hbci.server.AbstractHibiscusTransferImpl#getZweck2()
   */
  @Override
  public String getZweck2() throws RemoteException
  {
    throw new RemoteException("second usage not allowed for foreign transfer");
  }

  /**
   * @see de.willuhn.jameica.hbci.server.AbstractHibiscusTransferImpl#setGegenkontoBLZ(java.lang.String)
   */
  @Override
  public void setGegenkontoBLZ(String blz) throws RemoteException
  {
    throw new RemoteException("blz not allowed for foreign transfer");
  }

  /**
   * @see de.willuhn.jameica.hbci.server.AbstractHibiscusTransferImpl#setWeitereVerwendungszwecke(java.lang.String[])
   */
  @Override
  public void setWeitereVerwendungszwecke(String[] list) throws RemoteException
  {
    throw new RemoteException("extended usages not allowed for foreign transfer");
  }

  /**
   * @see de.willuhn.jameica.hbci.server.AbstractHibiscusTransferImpl#setZweck2(java.lang.String)
   */
  @Override
  public void setZweck2(String zweck2) throws RemoteException
  {
    throw new RemoteException("second usage not allowed for foreign transfer");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getTransferTyp()
   */
  public int getTransferTyp() throws RemoteException
  {
    return Transfer.TYP_AUSLANDSUEBERWEISUNG;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung#getGegenkontoInstitut()
   */
  public String getGegenkontoInstitut() throws RemoteException
  {
    return (String) getAttribute("empfaenger_bank");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung#setGegenkontoInstitut(java.lang.String)
   */
  public void setGegenkontoInstitut(String name) throws RemoteException
  {
    setAttribute("empfaenger_bank",name);
  }
}


/**********************************************************************
 * $Log$
 * Revision 1.1  2009-02-17 00:00:02  willuhn
 * @N BUGZILLA 159 - Erster Code fuer Auslands-Ueberweisungen
 *
 **********************************************************************/