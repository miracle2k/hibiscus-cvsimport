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
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Job fuer "Dauerauftrag loeschen".
 */
public class HBCIDauerauftragDeleteJob extends AbstractHBCIJob
{

	private I18N i18n 								= null;
	private Dauerauftrag dauerauftrag = null;
	private Konto konto 							= null;

  /**
	 * ct.
   * @param auftrag Dauerauftrag, der geloescht werden soll
   * @throws RemoteException
   * @throws ApplicationException
   */
  public HBCIDauerauftragDeleteJob(Dauerauftrag auftrag) throws RemoteException, ApplicationException
	{
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (auftrag == null)
			throw new ApplicationException(i18n.tr("Bitte w�hlen Sie einen Dauerauftrag aus"));

		if (!auftrag.isActive())
			throw new ApplicationException(i18n.tr("Dauerauftrag liegt nicht bei Bank vor und muss daher nicht online gel�scht werden"));

		if (auftrag.isNewObject())
			auftrag.store();

		this.dauerauftrag = auftrag;
		this.konto        = auftrag.getKonto();

		setJobParam("orderid",auftrag.getOrderID());
		// TODO: Beim Loeschen eines Dauerauftrags das Zieldatum konfigurierbar machen
	}

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getIdentifier()
   */
  String getIdentifier() {
    return "DauerDel";
  }


	/**
	 * Prueft, ob das Loeschen bei der Bank erfolgreich war und loescht den
	 * Dauerauftrag anschliessend in der Datenbank.
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#handleResult()
   */
	void handleResult() throws ApplicationException, RemoteException
	{
		String statusText = getStatusText();

		String empfName = i18n.tr("an") + " " + dauerauftrag.getEmpfaengerName();

		if (!getJobResult().isOK())
		{

			String msg = i18n.tr("Fehler beim L�schen des Dauerauftrages") + " " + empfName;


			String error = (statusText != null) ?
										i18n.tr("Fehlermeldung der Bank") + ": " + statusText :
										i18n.tr("Unbekannter Fehler");

			konto.addToProtokoll(msg + " ("+error+")",Protokoll.TYP_ERROR);
			throw new ApplicationException(msg + " ("+error+")");
		}

		konto.addToProtokoll(i18n.tr("Dauerauftrag gel�scht") + " " + empfName,Protokoll.TYP_SUCCESS);

		dauerauftrag.delete();

		Logger.info("dauerauftrag deleted successfully");
	}

}


/**********************************************************************
 * $Log$
 * Revision 1.3  2004-10-26 23:47:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/10/25 22:39:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 **********************************************************************/