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

import org.kapott.hbci.GV_Result.GVRSaldoReq;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Job fuer "Salden-Abfrage".
 */
public class HBCISaldoJob extends AbstractHBCIJob {

	private I18N i18n = null;

	public HBCISaldoJob(Konto konto)
	{
		super(konto);
		i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
	}

  /**
   * @see de.willuhn.jameica.hbci.rmi.hbci.HBCIJob#getIdentifier()
   */
  public String getIdentifier() {
    return "SaldoReq";
  }

	/**
	 * Liefert den Saldo.
   * @return Saldo.
   */
  public double getSaldo() throws ApplicationException
	{
		GVRSaldoReq result = (GVRSaldoReq) getJobResult();

		String statusText = getStatusText();
		if (!result.isOK())
		{
			throw new ApplicationException(
				statusText != null ?
					i18n.tr("Fehlermeldung der Bank") + ": " + statusText :
					i18n.tr("Fehler bei der Ermittlung des Saldos"));
		}
		Application.getLog().debug("job result is ok, returning saldo");
		return result.getEntries()[0].ready.value.value;
	}
}


/**********************************************************************
 * $Log$
 * Revision 1.1  2004-04-19 22:05:51  willuhn
 * @C HBCIJobs refactored
 *
 **********************************************************************/