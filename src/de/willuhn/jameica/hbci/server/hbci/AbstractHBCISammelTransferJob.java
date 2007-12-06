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

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Abstrakter Basis-Job fuer Sammel-Transfers.
 */
public abstract class AbstractHBCISammelTransferJob extends AbstractHBCIJob
{

	I18N i18n = null;
	private SammelTransfer transfer = null;
	private Konto konto = null;

  /**
	 * ct.
   * Achtung. Der Job-Parameter "data" fehlt noch und muss in den
   * abgeleiteten Klassen gesetzt werden.
   * @param transfer der auszufuehrende Sammel-Transfer.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public AbstractHBCISammelTransferJob(SammelTransfer transfer) throws ApplicationException, RemoteException
	{
		try
		{
			i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

			if (transfer == null)
				throw new ApplicationException(i18n.tr("Bitte geben Sie einen Sammel-Auftrag an"));
		
			if (transfer.isNewObject())
				transfer.store();

      if (transfer.ausgefuehrt())
        throw new ApplicationException(i18n.tr("Sammel-Auftrag wurde bereits ausgef�hrt"));

			this.transfer = transfer;
			this.konto = transfer.getKonto();

      DBIterator buchungen = this.transfer.getBuchungen();
      while (buchungen.hasNext())
      {
        SammelTransferBuchung b = (SammelTransferBuchung) buchungen.next();
        if (b.getBetrag() > Settings.getUeberweisungLimit())
          throw new ApplicationException(i18n.tr("Auftragslimit �berschritten: {0} ", 
            HBCI.DECIMALFORMAT.format(Settings.getUeberweisungLimit()) + " " + this.konto.getWaehrung()));
      }
      
      
			setJobParam("my",Converter.HibiscusKonto2HBCIKonto(konto));
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
   * Prueft, ob der Sammel-Auftrag erfolgreich war und markiert diese im Erfolgsfall als "ausgefuehrt".
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#handleResult()
   */
  void handleResult() throws ApplicationException, RemoteException
  {
		String empfName = transfer.getBezeichnung();

		if (getJobResult().isOK())
		{
      // Wir markieren die Ueberweisung als "ausgefuehrt"
      transfer.setAusgefuehrt();
      konto.addToProtokoll(i18n.tr("Sammel-Auftrag {0} ausgef�hrt",empfName),Protokoll.TYP_SUCCESS);
      Logger.info("sammellastschrift submitted successfully");
      return;
    }

    String msg = i18n.tr("Fehler beim Ausf�hren des Sammel-Auftrages {0}",empfName);
    String error = getStatusText();
    konto.addToProtokoll(msg + ": " + error,Protokoll.TYP_ERROR);
    throw new ApplicationException(msg + ": " + error);
  }
  
  /**
   * Liefert den Sammel-Transfer.
   * @return der Sammel-Transfer.
   */
  SammelTransfer getSammelTransfer()
  {
    return this.transfer;
  }
}


/**********************************************************************
 * $Log$
 * Revision 1.7  2007-12-06 14:25:32  willuhn
 * @B Bug 494
 *
 * Revision 1.6  2006/06/26 13:25:20  willuhn
 * @N Franks eBay-Parser
 *
 * Revision 1.5  2006/04/25 16:39:07  willuhn
 * @N Konstruktoren von HBCI-Jobs werfen nun eine ApplicationException, wenn der Auftrag bereits ausgefuehrt wurde
 *
 * Revision 1.4  2006/03/15 18:01:30  willuhn
 * @N AbstractHBCIJob#getName
 *
 * Revision 1.3  2006/03/15 17:28:41  willuhn
 * @C Refactoring der Anzeige der HBCI-Fehlermeldungen
 *
 * Revision 1.2  2005/11/02 17:33:31  willuhn
 * @B fataler Bug in Sammellastschrift/Sammelueberweisung
 *
 * Revision 1.1  2005/09/30 00:08:51  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 **********************************************************************/