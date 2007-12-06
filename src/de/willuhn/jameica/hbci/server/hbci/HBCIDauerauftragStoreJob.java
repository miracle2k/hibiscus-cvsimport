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

import org.kapott.hbci.GV_Result.GVRDauerEdit;
import org.kapott.hbci.GV_Result.GVRDauerNew;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.hbci.server.hbci.tests.PreTimeRestriction;
import de.willuhn.jameica.hbci.server.hbci.tests.TurnusRestriction;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Job fuer "Dauerauftrag bei der Bank speichern".
 * Der Job erkennt selbst, ob der Dauerauftrag bei der Bank neu angelegt werden
 * muss oder ob bereits einer vorliegt, der nur geaendert werden muss.
 */
public class HBCIDauerauftragStoreJob extends AbstractHBCIJob {

	private I18N i18n 								= null;
	private Dauerauftrag dauerauftrag = null;
	private Konto konto 							= null;

	private boolean active = false;

  /**
	 * ct.
   * @param auftrag Dauerauftrag, der bei der Bank gespeichert werden soll
   * @throws ApplicationException
   * @throws RemoteException
   */
  public HBCIDauerauftragStoreJob(Dauerauftrag auftrag) throws ApplicationException, RemoteException
	{
		try
		{
			i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

			if (auftrag == null)
				throw new ApplicationException(i18n.tr("Bitte w�hlen Sie einen Dauerauftrag aus"));

			if (auftrag.isNewObject())
				auftrag.store();

			this.dauerauftrag = auftrag;
			this.konto        = auftrag.getKonto();
			this.active				= auftrag.isActive();

			if (!active)
      {
			  // Limit pruefen wir nur bei neuen Dauerauftraegen
        if (this.dauerauftrag.getBetrag() > Settings.getUeberweisungLimit())
          throw new ApplicationException(i18n.tr("Auftragslimit �berschritten: {0} ", 
            HBCI.DECIMALFORMAT.format(Settings.getUeberweisungLimit()) + " " + this.konto.getWaehrung()));
      }

      if (active)
				setJobParam("orderid",auftrag.getOrderID());

			setJobParam("src",Converter.HibiscusKonto2HBCIKonto(konto));

      // BUGZILLA 29 http://www.willuhn.de/bugzilla/show_bug.cgi?id=29
      String curr = konto.getWaehrung();
      if (curr == null || curr.length() == 0)
        curr = HBCIProperties.CURRENCY_DEFAULT_DE;

			setJobParam("btg",dauerauftrag.getBetrag(),curr);

			HibiscusAddress empfaenger = (HibiscusAddress) Settings.getDBService().createObject(HibiscusAddress.class,null);
			empfaenger.setBLZ(dauerauftrag.getGegenkontoBLZ());
			empfaenger.setKontonummer(dauerauftrag.getGegenkontoNummer());
			empfaenger.setName(dauerauftrag.getGegenkontoName());
			setJobParam("dst",Converter.Address2HBCIKonto(empfaenger));

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


			// Jetzt noch die Tests fuer die Job-Restriktionen
			Properties p = HBCIFactory.getInstance().getJobRestrictions(this.konto,this);
			Enumeration keys = p.keys();
			while (keys.hasMoreElements())
			{
				String s = (String) keys.nextElement();
				Logger.info("[hbci job restriction] name: " + s + ", value: " + p.getProperty(s));
			}
			new TurnusRestriction(turnus,p).test();
			if (!active) // nur pruefen bei neuen Dauerauftraegen
				new PreTimeRestriction(dauerauftrag.getErsteZahlung(),p).test();
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
		if (active)
			return "DauerEdit";
  	return "DauerNew";
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getName()
   */
  public String getName() throws RemoteException
  {
    String empfName = dauerauftrag.getGegenkontoName();
    if (active)
      return i18n.tr("Dauerauftrag an {0} aktualisieren",empfName);
    return i18n.tr("Dauerauftrag an {0}",empfName);
  }

  /**
	 * Prueft, ob das Senden des Dauerauftrags erfolgreich war und speichert im
	 * Erfolgsfall die Order-ID.
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#handleResult()
	 */
	void handleResult() throws ApplicationException, RemoteException
	{
		String empfName = dauerauftrag.getGegenkontoName();

		if (getJobResult().isOK())
		{
      if (dauerauftrag.isActive())
        konto.addToProtokoll(i18n.tr("Dauerauftrag aktualisiert an {0}",empfName),Protokoll.TYP_SUCCESS);
      else
        konto.addToProtokoll(i18n.tr("Dauerauftrag ausgef�hrt an {0} ",empfName),Protokoll.TYP_SUCCESS);

      // jetzt muessen wir noch die Order-ID speichern, wenn er neu eingereicht wurde
      String orderID = null;
      if (!active)
      {
        GVRDauerNew result = (GVRDauerNew) this.getJobResult();
        orderID = result.getOrderId();
      }
      else
      {
        GVRDauerEdit result = (GVRDauerEdit) this.getJobResult();
        orderID = result.getOrderId();
      }
      // Der Auftrag war neu, dann muessen wir noch die Order-ID speichern
      dauerauftrag.setOrderID(orderID);
      dauerauftrag.store();

      Logger.info("dauerauftrag submitted successfully");
      return;
		}
    String msg = i18n.tr("Fehler beim Ausf�hren des Dauerauftrages an {0}",empfName);
    String error = getStatusText();
    konto.addToProtokoll(msg + ": " + error,Protokoll.TYP_ERROR);
    throw new ApplicationException(msg + ": " + error);
	}

}


/**********************************************************************
 * $Log$
 * Revision 1.21  2007-12-06 23:53:56  willuhn
 * @B Bug 490
 *
 * Revision 1.20  2007/12/06 14:25:32  willuhn
 * @B Bug 494
 *
 * Revision 1.19  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.18  2006/06/26 13:25:20  willuhn
 * @N Franks eBay-Parser
 *
 * Revision 1.17  2006/06/19 11:52:15  willuhn
 * @N Update auf hbci4java 2.5.0rc9
 *
 * Revision 1.16  2006/03/17 00:51:25  willuhn
 * @N bug 209 Neues Synchronisierungs-Subsystem
 *
 * Revision 1.15  2006/03/15 18:01:30  willuhn
 * @N AbstractHBCIJob#getName
 *
 * Revision 1.14  2006/03/15 17:28:41  willuhn
 * @C Refactoring der Anzeige der HBCI-Fehlermeldungen
 *
 * Revision 1.13  2005/11/14 13:38:43  willuhn
 * @N Termin-Ueberweisungen
 *
 * Revision 1.12  2005/05/19 23:31:07  web0
 * @B RMI over SSL support
 * @N added handbook
 *
 * Revision 1.11  2005/03/30 23:26:28  web0
 * @B bug 29
 * @B bug 30
 *
 * Revision 1.10  2005/03/02 17:59:30  web0
 * @N some refactoring
 *
 * Revision 1.9  2005/02/28 23:59:57  web0
 * @B http://www.willuhn.de/bugzilla/show_bug.cgi?id=15
 *
 * Revision 1.8  2005/02/27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 * Revision 1.7  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.6  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/10/29 00:33:00  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/10/29 00:32:32  willuhn
 * @N HBCI job restrictions
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