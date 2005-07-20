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
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.PassportRegistry;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.hbci.rmi.Adresse;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.hbci.server.hbci.tests.CanTermDelRestriction;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

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
   * @param date Datum, zu dem der Auftrag geloescht werden soll oder <code>null</code>
   * wenn zum naechstmoeglichen Zeitpunkt geloescht werden soll.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public HBCIDauerauftragDeleteJob(Dauerauftrag auftrag, Date date) throws RemoteException, ApplicationException
	{
		try
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

			setJobParam("orderid",this.dauerauftrag.getOrderID());

      setJobParam("src",Converter.HibiscusKonto2HBCIKonto(konto));

      String curr = konto.getWaehrung();
      if (curr == null || curr.length() == 0)
        curr = HBCIProperties.CURRENCY_DEFAULT_DE;

      setJobParam("btg",dauerauftrag.getBetrag(),curr);

      Adresse empfaenger = (Adresse) Settings.getDBService().createObject(Adresse.class,null);
      empfaenger.setBLZ(dauerauftrag.getGegenkontoBLZ());
      empfaenger.setKontonummer(dauerauftrag.getGegenkontoNummer());
      empfaenger.setName(dauerauftrag.getGegenkontoName());
      setJobParam("dst",Converter.HibiscusAdresse2HBCIKonto(empfaenger));
      setJobParam("name",empfaenger.getName());

      setJobParam("usage",dauerauftrag.getZweck());

      String zweck2 = dauerauftrag.getZweck2();
      if (zweck2 != null && zweck2.length() > 0)
        setJobParam("usage_2",zweck2);

      setJobParam("firstdate",dauerauftrag.getErsteZahlung());

      Date letzteZahlung = dauerauftrag.getLetzteZahlung();
      if (letzteZahlung != null)
        setJobParam("lastdate",letzteZahlung);

      Turnus turnus = dauerauftrag.getTurnus();
      setJobParam("timeunit",turnus.getZeiteinheit() == Turnus.ZEITEINHEIT_MONATLICH ? "M" : "W");
      setJobParam("turnus",turnus.getIntervall());
      setJobParam("execday",turnus.getTag());
      
			if (date != null)
			{
				// Jetzt noch die Tests fuer die Job-Restriktionen
        Passport passport = PassportRegistry.findByClass(this.konto.getPassportClass());
        // BUGZILLA #7 http://www.willuhn.de/bugzilla/show_bug.cgi?id=7
        passport.init(this.konto);

				Properties p = HBCIFactory.getInstance().getJobRestrictions(this,passport.getHandle());
				Enumeration keys = p.keys();
				while (keys.hasMoreElements())
				{
					String s = (String) keys.nextElement();
					Logger.debug("[hbci job restriction] name: " + s + ", value: " + p.getProperty(s));
				}

				Logger.info("target date for DauerDel: " + date.toString());
				new CanTermDelRestriction(p).test(); // Test nur, wenn Datum angegeben
				setJobParam("date",date);
			}

			// Den brauchen wir, damit das Loeschen funktioniert.
			HBCIFactory.getInstance().addJob(new HBCIDauerauftragListJob(this.konto));
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

		String empfName = i18n.tr("an") + " " + dauerauftrag.getGegenkontoName();

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
 * Revision 1.11  2005-07-20 22:40:56  web0
 * *** empty log message ***
 *
 * Revision 1.10  2005/05/19 23:31:07  web0
 * @B RMI over SSL support
 * @N added handbook
 *
 * Revision 1.9  2005/03/02 17:59:30  web0
 * @N some refactoring
 *
 * Revision 1.8  2004/11/18 23:46:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/11/17 19:02:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/11/14 19:21:37  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.4  2004/11/12 18:25:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/10/26 23:47:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/10/25 22:39:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 **********************************************************************/