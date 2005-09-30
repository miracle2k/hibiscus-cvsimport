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

import org.kapott.hbci.GV_Result.GVRDauerList;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Saldo;
import org.kapott.hbci.structures.Value;
import org.kapott.hbci.swift.DTAUS;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Adresse;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.util.ApplicationException;

/**
 * Hilfeklasse, welche Objekte aus HBCI4Java in unsere Datenstrukturen konvertiert
 * und umgekehrt.
 */
public class Converter {


	/**
	 * Konvertiert einen einzelnen Umsatz von HBCI4Java nach Hibiscus.
	 * <br>
	 * <b>WICHTIG:</b><br>
	 * <ul>
	 * 	<li>
	 * 		Da das <code>UmsLine</code> zwar die Kundennummer
	 *    enthaelt, nicht aber das konkrete Konto, auf das sich der Umsatz
	 * 		bezieht, wird das Feld leer gelassen. Es ist daher Sache des Aufrufers,
	 * 		noch die Funktion <code>umsatz.setKonto(Konto)</code> aufzurufen, damit
	 * 		das Objekt in der Datenbank gespeichert werden kann.
	 *  </li>
	 *  <li>
	 * 		Eine Buchung enthaelt typischerweise einen Empfaenger ;)
	 *    Bei Haben-Buchungen ist man selbst dieser. Von daher bleibt
	 *    der Empfaenger bei eben jenen leer. Bei Soll-Buchungen wird die
	 *    Bankverbindung des Gegenkontos ermittelt, damit ein <code>Empfaenger</code>
	 *    erzeugt und dieser im Umsatz-Objekt gesetzt. Es wird jedoch nocht nicht
	 *    in der Datenbank gespeichert. Vorm Speichern des Umsatzes muss also
	 *    noch ein <code>umsatz.getEmpfaenger().store()</code> gemacht werden.<br>
	 *    Hinweis: Laut JavaDoc von HBCI4Java ist das Gegenkonto optional. Es
	 *    kann also auch bei Soll-Buchungen fehlen.
	 *  </li>
	 * </ul>
	 * @param u der zu convertierende Umsatz.
   * @return das neu erzeugte Umsatz-Objekt.
	 * @throws RemoteException
   */
  public static Umsatz HBCIUmsatz2HibiscusUmsatz(GVRKUms.UmsLine u) throws RemoteException
	{
		Umsatz umsatz = (Umsatz) Settings.getDBService().createObject(Umsatz.class,null);

		umsatz.setArt(u.text);
		umsatz.setCustomerRef(u.customerref);
		umsatz.setPrimanota(u.primanota);

      //BUGZILLA 67 http://www.willuhn.de/bugzilla/show_bug.cgi?id=67
      Saldo s = u.saldo;
      if (s != null)
      {
        try {
          if (s.cd.endsWith("C"))
            umsatz.setSaldo(s.value.value); // Haben-Saldo
          else
          umsatz.setSaldo(-s.value.value);  // Soll-Saldo
        }
        catch (NullPointerException e)
        {
          // Falls u.saldo null liefert
          /* ignore */
        }
      }

		// C(redit) = HABEN
		// D(ebit)  = SOLL
		if (u.cd.endsWith("C"))
			umsatz.setBetrag(u.value.value); // Haben-Buchung
		else
		{
			umsatz.setBetrag(-u.value.value); // Soll-Buchung
		}

		umsatz.setDatum(u.bdate);
		umsatz.setValuta(u.valuta);
		if (u.usage.length == 0)
		{
			// Baeh, eine Buchung ohne Text. Dann schreiben wir selbst 'nen Dummy rein
			umsatz.setZweck("-");
		}
		else {
			umsatz.setZweck(u.usage[0]);
		}

		if (u.usage.length > 1)
		{
			String merged = "";
			for (int i=1;i<u.usage.length;++i)
			{
				merged += (u.usage[i] + " - ");
			}
			umsatz.setZweck2(merged);
		}

		// und jetzt noch der Empfaenger (wenn er existiert)
		if (u.other != null) 
		{
		  umsatz.setEmpfaenger(HBCIKonto2HibiscusAdresse(u.other));
		}
		return umsatz;
	}

  /**
	 * Konvertiert eine Zeile aus der Liste der abgerufenen Dauerauftraege.
   * @param d der Dauerauftrag aus HBCI4Java.
   * @return Unser Dauerauftrag.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public static Dauerauftrag HBCIDauer2HibiscusDauerauftrag(GVRDauerList.Dauer d)
  	throws RemoteException, ApplicationException
	{
		DauerauftragImpl auftrag = (DauerauftragImpl) Settings.getDBService().createObject(Dauerauftrag.class,null);
		auftrag.setErsteZahlung(d.firstdate);
		auftrag.setLetzteZahlung(d.lastdate);
    
		// TODO: Das ist nicht eindeutig. Da der Converter schaut, ob er ein solches
    // Konto schon hat und bei Bedarf das existierende verwendet. Es kann aber
    // sein, dass ein User ein und das selbe Konto mit verschiedenen Sicherheitsmedien
    // bedient. In diesem Fall wird der Dauerauftrag evtl. beim falschen Konto
    // einsortiert.
    auftrag.setKonto(HBCIKonto2HibiscusKonto(d.my));

    auftrag.setBetrag(d.value.value);
		auftrag.setOrderID(d.orderid);

		// Jetzt noch der Empfaenger
		auftrag.setGegenkonto(HBCIKonto2HibiscusAdresse(d.other));

		// Verwendungszweck
		if (d.usage.length == 0)
		{
			auftrag.setZweck("-");
		}
		else {
			auftrag.setZweck(d.usage[0]);
		}

		// Wir haben nur zwei Felder fuer den Zweck. Wenn also mehr als
		// 2 vorhanden sind (kann das ueberhaupt sein?), muessen wir die
		// restlichen leider ignorieren um nicht ueber die 27-Zeichen Maximum
		// pro Zweck zu kommen.
		if (d.usage.length > 1)
			auftrag.setZweck2(d.usage[1]);

		auftrag.setTurnus(TurnusHelper.createByDauerAuftrag(d));
		return auftrag;
	}

	/**
	 * Konvertiert ein Hibiscus-Konto in ein HBCI4Java Konto.
   * @param konto unser Konto.
   * @return das HBCI4Java Konto.
   * @throws RemoteException
   */
  public static Konto HibiscusKonto2HBCIKonto(de.willuhn.jameica.hbci.rmi.Konto konto) throws RemoteException
	{
		org.kapott.hbci.structures.Konto k =
			new org.kapott.hbci.structures.Konto(konto.getBLZ(),konto.getKontonummer());
		k.country = "DE";
		k.curr = konto.getWaehrung();
		k.customerid = konto.getKundennummer();
		k.name = konto.getName();
		return k;  	
	}

	/**
	 * Konvertiert ein HBCI4Java-Konto in ein Hibiscus Konto.
	 * Existiert ein Konto mit dieser Kontonummer und BLZ bereits in Hibiscus,
	 * wird jenes stattdessen zurueckgeliefert.
	 * @param konto das HBCI4Java Konto.
	 * @param passportClass optionale Angabe einer Passport-Klasse. Ist er angegeben wird, nur dann ein existierendes Konto
   * verwendet, wenn neben Kontonummer und BLZ auch die Klasse des Passportuebereinstimmt.
	 * @return unser Konto.
	 * @throws RemoteException
	 */
	public static de.willuhn.jameica.hbci.rmi.Konto HBCIKonto2HibiscusKonto(Konto konto, Class passportClass) throws RemoteException
	{
		DBIterator list = Settings.getDBService().createList(de.willuhn.jameica.hbci.rmi.Konto.class);
		list.addFilter("kontonummer = '" + konto.number + "'");
		list.addFilter("blz = '" + konto.blz + "'");
    if (passportClass != null)
      list.addFilter("passport_class = '" + passportClass.getName() + "'");

    if (list.hasNext())
			return (de.willuhn.jameica.hbci.rmi.Konto) list.next(); // Konto gibts schon

		// Ne, wir erstellen ein neues
		de.willuhn.jameica.hbci.rmi.Konto k =
			(de.willuhn.jameica.hbci.rmi.Konto) Settings.getDBService().createObject(de.willuhn.jameica.hbci.rmi.Konto.class,null);
		k.setBLZ(konto.blz);
		k.setKontonummer(konto.number);
		k.setKundennummer(konto.customerid);
		k.setName(konto.name);
		k.setBezeichnung(konto.type);
		k.setWaehrung(konto.curr);
		return k;  	
	}

  /**
   * Konvertiert ein HBCI4Java-Konto in ein Hibiscus Konto.
   * Existiert ein Konto mit dieser Kontonummer und BLZ bereits in Hibiscus,
   * wird jenes stattdessen zurueckgeliefert.
   * @param konto das HBCI4Java Konto.
   * @return unser Konto.
   * @throws RemoteException
   */
  public static de.willuhn.jameica.hbci.rmi.Konto HBCIKonto2HibiscusKonto(Konto konto) throws RemoteException
  {
    return HBCIKonto2HibiscusKonto(konto,null);
  }

  /**
	 * Konvertiert einen Hibiscus-Adresse in ein HBCI4Java Konto.
	 * @param adresse unsere Adresse.
	 * @return das HBCI4Java Konto.
	 * @throws RemoteException
	 */
	public static Konto HibiscusAdresse2HBCIKonto(Adresse adresse) throws RemoteException
	{
		org.kapott.hbci.structures.Konto k =
			new org.kapott.hbci.structures.Konto("DE",adresse.getBLZ(),adresse.getKontonummer());
		k.name = adresse.getName();
		return k;
	}

	/**
	 * Konvertiert ein HBCI4Java Konto in eine Hibiscus-Adresse.
	 * @param konto das HBCI-Konto.
	 * @return unsere Adresse.
	 * @throws RemoteException
	 */
	public static Adresse HBCIKonto2HibiscusAdresse(Konto konto) throws RemoteException
	{
		Adresse e = (Adresse) Settings.getDBService().createObject(Adresse.class,null);
		e.setBLZ(konto.blz);
		e.setKontonummer(konto.number);
		String name = konto.name;
		if (konto.name2 != null)
			name += (" " + konto.name2);
		e.setName(name);
		return e;  	
	}

	/**
	 * Konvertiert einen Sammel-Auftrag in DTAUS-Format.
   * @param s Sammel-Auftrag.
   * @return DTAUS-Repraesentation.
   * @throws RemoteException
   */
  public static DTAUS HibiscusSammelTransfer2DTAUS(SammelTransfer s) throws RemoteException
	{
		DTAUS dtaus = new DTAUS(HibiscusKonto2HBCIKonto(s.getKonto()),DTAUS.TYPE_DEBIT);
		DBIterator buchungen = s.getBuchungen();
		SammelTransferBuchung b = null;
		while (buchungen.hasNext())
		{
			b = (SammelTransferBuchung) buchungen.next();
			final DTAUS.Transaction tr = dtaus.new Transaction();
			tr.otherAccount = HibiscusAdresse2HBCIKonto(b.getGegenkonto());
			tr.value = new Value(b.getBetrag());
			tr.addUsage(b.getZweck());
			String z2 = b.getZweck2();
			if (z2 != null && z2.length() > 0)
				tr.addUsage(z2);
			dtaus.addEntry(tr);
		}
		return dtaus;
	}
  
}


/**********************************************************************
 * $Log$
 * Revision 1.26  2005-09-30 00:08:50  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 * Revision 1.25  2005/08/01 23:27:42  web0
 * *** empty log message ***
 *
 * Revision 1.24  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 * Revision 1.23  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.22  2005/03/06 18:04:17  web0
 * @B Converter hat beim Konvertieren eines HBCI4Java-Kontos in eine Adresse ggf. eine lokal vorhandene geliefert
 *
 * Revision 1.21  2005/03/06 14:04:26  web0
 * @N SammelLastschrift seems to work now
 *
 * Revision 1.20  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.19  2005/03/02 17:59:30  web0
 * @N some refactoring
 *
 * Revision 1.18  2005/02/27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 * Revision 1.17  2004/10/24 17:19:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/10/23 17:34:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 * Revision 1.14  2004/10/17 16:28:46  willuhn
 * @N Die ersten Dauerauftraege abgerufen ;)
 *
 * Revision 1.13  2004/07/23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.12  2004/07/20 21:48:00  willuhn
 * @N ContextMenus
 *
 * Revision 1.11  2004/07/14 23:48:31  willuhn
 * @N mehr Code fuer Dauerauftraege
 *
 * Revision 1.10  2004/07/04 17:07:58  willuhn
 * @B Umsaetze wurden teilweise nicht als bereits vorhanden erkannt und wurden somit doppelt angezeigt
 *
 * Revision 1.9  2004/06/10 20:56:33  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.8  2004/05/05 22:14:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/04/27 23:50:15  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/04/27 22:23:56  willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
 *
 * Revision 1.5  2004/04/25 17:41:05  willuhn
 * @D javadoc
 *
 * Revision 1.4  2004/04/22 23:46:50  willuhn
 * @N UeberweisungJob
 *
 * Revision 1.3  2004/04/19 22:05:51  willuhn
 * @C HBCIJobs refactored
 *
 * Revision 1.2  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.1  2004/03/05 00:19:23  willuhn
 * @D javadoc fixes
 * @C Converter moved into server package
 *
 * Revision 1.1  2004/03/05 00:04:10  willuhn
 * @N added code for umsatzlist
 *
 **********************************************************************/