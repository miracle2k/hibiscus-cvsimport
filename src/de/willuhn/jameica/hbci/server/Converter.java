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
import java.util.ArrayList;

import org.kapott.hbci.GV_Result.GVRDauerList;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Saldo;
import org.kapott.hbci.structures.Value;
import org.kapott.hbci.swift.DTAUS;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
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

    double kurs = 1.95583;

    //BUGZILLA 67 http://www.willuhn.de/bugzilla/show_bug.cgi?id=67
    Saldo s = u.saldo;
    if (s != null)
    {
      Value v = s.value;
      if (v != null)
      {
        // BUGZILLA 318
        double saldo = v.getDoubleValue();
        String curr  = v.getCurr();
        if (curr != null && "DEM".equals(curr))
          saldo /= kurs;
        umsatz.setSaldo(saldo);
      }
    }
    
    Value v = u.value;
    double betrag = v.getDoubleValue();
    String curr = v.getCurr();
    
    // BUGZILLA 318
    if (curr != null && "DEM".equals(curr))
      betrag /= kurs;

    umsatz.setBetrag(betrag);
		umsatz.setDatum(u.bdate);
		umsatz.setValuta(u.valuta);

    // BUGZILLA 146
    // Aus einer Mail von Stefan Palme
    //    Es geht noch besser. Wenn in "umsline.gvcode" nicht der Wert "999"
    //    drinsteht, sind die Variablen "text", "primanota", "usage", "other"
    //    und "addkey" irgendwie sinnvoll gef�llt.  Steht in "gvcode" der Wert
    //    "999" drin, dann sind diese Variablen alle null, und der ungeparste 
    //    Inhalt des Feldes :86: steht komplett in "additional".
    

    // Selberparsen kann ich wohl vergessen, wenn 999 drin steht. Wenn selbst
    // Stefan das nicht macht, lass ich lieber gleich die Finger davon ;)
    if (u.usage == null || u.usage.length == 0)
		{
      String usage = u.additional;
      if (usage == null || usage.length() == 0)
      {
        // Wir haben ueberhaupt nichts.
        umsatz.setZweck("-");
      }
      else
      {
        // Java's Regex-Implementierung ist sowas von daemlich.
        // String.split() macht nur Rotz, wenn man mit Quantifierern
        // arbeitet. Also ersetzten wir erst mal alles gegen nen
        // eigenen String und verwenden den dann zum Splitten.
        usage = usage.replaceAll("(.{27})","$1--##--##");
        String[] lines = usage.split("--##--##");
        
        if (lines.length >= 1) umsatz.setZweck(lines[0]);
        if (lines.length >= 2) umsatz.setZweck2(lines[1]);
        if (lines.length >= 3)
        {
          // Wenn noch mehr da ist, pappen wir den Rest zusammen in
          // den Kommentar
          StringBuffer sb = new StringBuffer();
          for (int i=2;i<lines.length;++i)
          {
            sb.append(lines[i]);
          }
          umsatz.setKommentar(sb.toString());
        }
      }
		}
		else {
      // erste Zeile in den ersten Verwendungszweck
			umsatz.setZweck(u.usage[0]);
      
      // Noch eine Zeile?
      // Die kommt in den zweiten Verwendungszweck
      if (u.usage.length > 1)
        umsatz.setZweck2(u.usage[1]);

      // Erweiterte Verwendungszwecke?
      if (u.usage.length > 2)
      {
        ArrayList lines = new ArrayList();
        for (int i=2;i<u.usage.length;++i)
          lines.add(u.usage[i]);
        umsatz.setWeitereVerwendungszwecke((String[])lines.toArray(new String[lines.size()]));
      }
		}
    
		// und jetzt noch der Empfaenger (wenn er existiert)
		if (u.other != null) 
		{
		  umsatz.setGegenkonto(HBCIKonto2Address(u.other));
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

    auftrag.setBetrag(d.value.getDoubleValue());
		auftrag.setOrderID(d.orderid);

		// Jetzt noch der Empfaenger
		auftrag.setGegenkonto(HBCIKonto2Address(d.other));

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
    {
      // BUGZILLA 517
      String usage2 = d.usage[1];
      if (usage2 != null)
        usage2 = usage2.trim();
      if (usage2 != null && usage2.length() > 0)
        auftrag.setZweck2(usage2);
    }

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
    k.type = konto.getBezeichnung(); // BUGZILLA 338
		k.name = konto.getName();
    k.subnumber = konto.getUnterkonto(); // BUGZILLA 355
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
		list.addFilter("kontonummer = ?", new Object[]{konto.number});
		list.addFilter("blz = ?",         new Object[]{konto.blz});
    if (passportClass != null)
      list.addFilter("passport_class = ?", new Object[]{passportClass.getName()});

    // BUGZILLA 355
    if (konto.subnumber != null && konto.subnumber.length() > 0)
      list.addFilter("unterkonto = ?",new Object[]{konto.subnumber});
    
    // BUGZILLA 338: Wenn das Konto eine Bezeichnung hat, muss sie uebereinstimmen
    if (konto.type != null && konto.type.length() > 0)
      list.addFilter("bezeichnung = ?", new Object[]{konto.type});

    if (list.hasNext())
			return (de.willuhn.jameica.hbci.rmi.Konto) list.next(); // Konto gibts schon

		// Ne, wir erstellen ein neues
		de.willuhn.jameica.hbci.rmi.Konto k =
			(de.willuhn.jameica.hbci.rmi.Konto) Settings.getDBService().createObject(de.willuhn.jameica.hbci.rmi.Konto.class,null);
		k.setBLZ(konto.blz);
		k.setKontonummer(konto.number);
    k.setUnterkonto(konto.subnumber); // BUGZILLA 355
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
	public static Konto Address2HBCIKonto(Address adresse) throws RemoteException
	{
		Konto k = new Konto("DE",adresse.getBlz(),adresse.getKontonummer());
		k.name = adresse.getName();
		return k;
	}

	/**
	 * Konvertiert ein HBCI4Java Konto in eine Hibiscus-Adresse.
	 * @param konto das HBCI-Konto.
	 * @return unsere Adresse.
	 * @throws RemoteException
	 */
	public static Address HBCIKonto2Address(Konto konto) throws RemoteException
	{
		HibiscusAddress e = (HibiscusAddress) Settings.getDBService().createObject(HibiscusAddress.class,null);
		e.setBlz(konto.blz);
		e.setKontonummer(konto.number);
		String name = konto.name;
		if (konto.name2 != null && konto.name2.length() > 0)
			name += (" " + konto.name2);
		e.setName(name);
		return e;  	
	}
	
  /**
   * Konvertiert eine Sammel-Ueberweisung in DTAUS-Format.
   * @param su Sammel-Ueberweisung.
   * @return DTAUS-Repraesentation.
   * @throws RemoteException
   */
  public static DTAUS HibiscusSammelUeberweisung2DTAUS(SammelUeberweisung su) throws RemoteException
  {
    // TYPE_CREDIT = Sammel�berweisung
    // TYPE_DEBIT = Sammellastschrift
    return HibiscusSammelTransfer2DTAUS(su, DTAUS.TYPE_CREDIT);
  }

  /**
   * Konvertiert eine Sammel-Lastschrift in DTAUS-Format.
   * @param sl Sammel-Lastschrift.
   * @return DTAUS-Repraesentation.
   * @throws RemoteException
   */
  public static DTAUS HibiscusSammelLastschrift2DTAUS(SammelLastschrift sl) throws RemoteException
  {
    // TYPE_CREDIT = Sammel�berweisung
    // TYPE_DEBIT = Sammellastschrift
    return HibiscusSammelTransfer2DTAUS(sl, DTAUS.TYPE_DEBIT);
  }

  /**
   * Hilfsfunktion. Ist private, damit niemand aus Versehen den falschen Type angibt.
   * @param s Sammel-Transfer.
   * @param type Art des Transfers.
   * @see DTAUS#TYPE_CREDIT
   * @see DTAUS#TYPE_DEBIT
   * @return DTAUS-Repraesentation.
   * @throws RemoteException
   */
  private static DTAUS HibiscusSammelTransfer2DTAUS(SammelTransfer s, int type) throws RemoteException
	{

    DTAUS dtaus = new DTAUS(HibiscusKonto2HBCIKonto(s.getKonto()),type);
		DBIterator buchungen = s.getBuchungen();
		SammelTransferBuchung b = null;
		while (buchungen.hasNext())
		{
			b = (SammelTransferBuchung) buchungen.next();
			final DTAUS.Transaction tr = dtaus.new Transaction();
      
      Konto other = new Konto("DE",b.getGegenkontoBLZ(),b.getGegenkontoNummer());
      other.name = b.getGegenkontoName();

      tr.otherAccount = other;
			tr.value = new Value(String.valueOf(b.getBetrag()));
			tr.addUsage(b.getZweck());
      
      String key = b.getTextSchluessel();
      if (key != null && key.length() > 0)
        tr.key = key; // Nur setzen, wenn in der Buchung definiert. Gibt sonst in DTAUS#toString eine NPE
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
 * Revision 1.48  2008-11-26 00:39:36  willuhn
 * @N Erste Version erweiterter Verwendungszwecke. Muss dringend noch getestet werden.
 *
 * Revision 1.47  2008/11/25 01:03:12  willuhn
 * *** empty log message ***
 *
 * Revision 1.46  2008/11/24 00:12:07  willuhn
 * @R Spezial-Umsatzparser entfernt - wird kuenftig direkt in HBCI4Java gemacht
 *
 * Revision 1.45  2008/11/17 23:30:00  willuhn
 * @C Aufrufe der depeicated BLZ-Funktionen angepasst
 **********************************************************************/