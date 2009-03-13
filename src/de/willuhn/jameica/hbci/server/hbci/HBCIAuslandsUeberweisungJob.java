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
package de.willuhn.jameica.hbci.server.hbci;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Job fuer "Auslandsueberweisung".
 */
public class HBCIAuslandsUeberweisungJob extends AbstractHBCIJob
{

	private AuslandsUeberweisung ueberweisung = null;
	private Konto konto                       = null;
	private boolean markExecutedBefore        = false;

  /**
	 * ct.
   * @param ueberweisung die auszufuehrende Ueberweisung.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public HBCIAuslandsUeberweisungJob(AuslandsUeberweisung ueberweisung) throws ApplicationException, RemoteException
	{
    super();
		try
		{
			if (ueberweisung == null)
				throw new ApplicationException(i18n.tr("Bitte geben Sie einen Auftrag an"));
		
			if (ueberweisung.isNewObject())
				ueberweisung.store();
      
      if (ueberweisung.ausgefuehrt())
        throw new ApplicationException(i18n.tr("Auftrag wurde bereits ausgef�hrt"));

      this.ueberweisung = ueberweisung;
      this.konto = this.ueberweisung.getKonto();

      if (this.ueberweisung.getBetrag() > Settings.getUeberweisungLimit())
        throw new ApplicationException(i18n.tr("Auftragslimit �berschritten: {0} ", 
          HBCI.DECIMALFORMAT.format(Settings.getUeberweisungLimit()) + " " + this.konto.getWaehrung()));

			setJobParam("src",Converter.HibiscusKonto2HBCIKonto(konto));

      // BUGZILLA 29 http://www.willuhn.de/bugzilla/show_bug.cgi?id=29
      String curr = konto.getWaehrung();
      if (curr == null || curr.length() == 0)
        curr = HBCIProperties.CURRENCY_DEFAULT_DE;

			setJobParam("btg",       ueberweisung.getBetrag(),curr);
			setJobParam("dst.iban",  ueberweisung.getGegenkontoNummer());
      setJobParam("dst.name",  ueberweisung.getGegenkontoName());
      setJobParam("dst.kiname",ueberweisung.getGegenkontoInstitut());
			setJobParam("usage",     ueberweisung.getZweck());

      de.willuhn.jameica.system.Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
      markExecutedBefore = settings.getBoolean("transfer.markexecuted.before",false);
      if (markExecutedBefore)
        ueberweisung.setAusgefuehrt(true);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (ApplicationException e2)
		{
			throw e2;
		}
		catch (Throwable t)
		{
			Logger.error("error while executing job " + getIdentifier(),t);
			throw new ApplicationException(i18n.tr("Fehler beim Erstellen des Auftrags. Fehlermeldung: {0}",t.getMessage()),t);
		}
	}

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getIdentifier()
   */
  String getIdentifier()
  {
    return "UebForeign";
  }
  
  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getName()
   */
  public String getName() throws RemoteException
  {
    return i18n.tr("Auslands�berweisung an {0} (IBAN: {1})",new String[]{ueberweisung.getGegenkontoName(), ueberweisung.getGegenkontoNummer()});
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markExecuted()
   */
  void markExecuted() throws RemoteException, ApplicationException
  {
    // Wenn der Auftrag nicht vorher als ausgefuehrt markiert wurde, machen wir das jetzt
    if (!markExecutedBefore)
      ueberweisung.setAusgefuehrt(true);
    
    Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(ueberweisung));
    konto.addToProtokoll(i18n.tr("Auftrag ausgef�hrt an: {0}",ueberweisung.getGegenkontoNummer()),Protokoll.TYP_SUCCESS);
    Logger.info("foreign transfer submitted successfully");
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markFailed(java.lang.String)
   */
  String markFailed(String error) throws ApplicationException, RemoteException
  {
    // Wenn der Auftrag fehlerhaft war und schon als ausgefuehrt markiert wurde, machen
    // wir das jetzt wieder rurckgaengig
    if (markExecutedBefore)
      ueberweisung.setAusgefuehrt(false);
    
    String msg = i18n.tr("Fehler beim Ausf�hren des Auftrages an {0}: {1}",new String[]{ueberweisung.getGegenkontoName(),error});
    konto.addToProtokoll(msg,Protokoll.TYP_ERROR);
    return msg;
  }
}


/**********************************************************************
 * $Log$
 * Revision 1.1  2009-03-13 00:25:12  willuhn
 * @N Code fuer Auslandsueberweisungen fast fertig
 *
 **********************************************************************/