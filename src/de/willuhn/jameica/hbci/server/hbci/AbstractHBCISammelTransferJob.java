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
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
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

	private I18N i18n = null;
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

			this.transfer = transfer;
			this.konto = transfer.getKonto();

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
		String statusText = getStatusText();

		String empfName = i18n.tr("an Konto") + " " + transfer.getKonto().getBezeichnung();

		if (!getJobResult().isOK())
		{

			String msg = i18n.tr("Fehler beim Ausf�hren des Sammel-Auftrages") + " " + empfName;


			String error = (statusText != null) ?
										i18n.tr("Fehlermeldung der Bank") + ": " + statusText :
										i18n.tr("Unbekannter Fehler");

			konto.addToProtokoll(msg + " ("+error+")",Protokoll.TYP_ERROR);
			throw new ApplicationException(msg + " ("+error+")");
		}


		// Wir markieren die Ueberweisung als "ausgefuehrt"
		transfer.setAusgefuehrt();
    konto.addToProtokoll(i18n.tr("Sammel-Auftrag ausgef�hrt") + " " + empfName,Protokoll.TYP_SUCCESS);
		Logger.info("sammellastschrift submitted successfully");
  }
}


/**********************************************************************
 * $Log$
 * Revision 1.2  2005-11-02 17:33:31  willuhn
 * @B fataler Bug in Sammellastschrift/Sammelueberweisung
 *
 * Revision 1.1  2005/09/30 00:08:51  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 **********************************************************************/