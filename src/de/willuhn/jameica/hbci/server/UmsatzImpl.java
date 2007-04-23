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
import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.zip.CRC32;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Repraesentiert eine Zeile in den Umsaetzen.
 */
public class UmsatzImpl extends AbstractDBObject implements Umsatz
{

	private transient I18N i18n;
  
  /**
   * Cache fuer die Umsatz-Kategorien.
   */
  public final static Hashtable UMSATZTYP_CACHE = new Hashtable();

  /**
   * @throws RemoteException
   */
  public UmsatzImpl() throws RemoteException {
    super();
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName() {
    return "umsatz";
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException {
    return "zweck";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException {
		// Die Umsaetze werden nicht von Hand eingegeben sondern
		// gelangen via HBCI zu uns. Nichtsdestotrotz duerfen
		// wir nur die speichern, die vollstaendig sind.
		try {

			if (getBetrag() == 0.0)
				throw new ApplicationException(i18n.tr("Betrag fehlt."));

			if (getDatum() == null)
				throw new ApplicationException(i18n.tr("Datum fehlt."));

			if (getKonto() == null)
				throw new ApplicationException(i18n.tr("Umsatz muss einem Konto zugewiesen sein."));

			if (getValuta() == null)
				throw new ApplicationException(i18n.tr("Valuta fehlt."));
		}
		catch (RemoteException e)
		{
			Logger.error("error while insertcheck in umsatz",e);
			throw new ApplicationException(i18n.tr("Fehler beim Speichern des Umsatzes"));
		}
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException {
		insertCheck();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getForeignObject(java.lang.String)
   */
  protected Class getForeignObject(String field) throws RemoteException {
		if ("konto_id".equals(field))
			return Konto.class;
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#getKonto()
   */
  public Konto getKonto() throws RemoteException {
    return (Konto) getAttribute("konto_id");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getGegenkontoName()
   */
  public String getGegenkontoName() throws RemoteException {
    return (String) getAttribute("empfaenger_name");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getGegenkontoNummer()
   */
  public String getGegenkontoNummer() throws RemoteException
  {
    return (String) getAttribute("empfaenger_konto");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getGegenkontoBLZ()
   */
  public String getGegenkontoBLZ() throws RemoteException
  {
    return (String) getAttribute("empfaenger_blz");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getBetrag()
   */
  public double getBetrag() throws RemoteException {
		Double d = (Double) getAttribute("betrag");
		if (d == null)
			return 0;
		return d.doubleValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getDatum()
   */
  public Date getDatum() throws RemoteException {
		return (Date) getAttribute("datum");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getValuta()
   */
  public Date getValuta() throws RemoteException {
		return (Date) getAttribute("valuta");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getZweck()
   */
  public String getZweck() throws RemoteException {
		return (String) getAttribute("zweck");
  }

	/**
	 * @see de.willuhn.jameica.hbci.rmi.Transfer#getZweck2()
	 */
	public String getZweck2() throws RemoteException {
		return (String) getAttribute("zweck2");
	}

	/**
	 * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setGegenkonto(de.willuhn.jameica.hbci.rmi.Address)
	 */
	public void setGegenkonto(Address empf) throws RemoteException
	{
		setGegenkontoBLZ(empf.getBLZ());
		setGegenkontoNummer(empf.getKontonummer());
		setGegenkontoName(empf.getName());
	}

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setGegenkontoName(java.lang.String)
   */
  public void setGegenkontoName(String name) throws RemoteException {
		setAttribute("empfaenger_name",name);
  }

	/**
	 * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setGegenkontoNummer(java.lang.String)
	 */
	public void setGegenkontoNummer(String konto) throws RemoteException {
    setAttribute("empfaenger_konto",konto);
  }
  
	/**
	 * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setGegenkontoBLZ(java.lang.String)
	 */
	public void setGegenkontoBLZ(String blz) throws RemoteException {
    setAttribute("empfaenger_blz",blz);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setBetrag(double)
   */
  public void setBetrag(double d) throws RemoteException {
		setAttribute("betrag",new Double(d));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setZweck(java.lang.String)
   */
  public void setZweck(String zweck) throws RemoteException {
		setAttribute("zweck",zweck);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setZweck2(java.lang.String)
   */
  public void setZweck2(String zweck2) throws RemoteException {
		setAttribute("zweck2",zweck2);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setDatum(java.util.Date)
   */
  public void setDatum(Date d) throws RemoteException {
		setAttribute("datum",d);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setValuta(java.util.Date)
   */
  public void setValuta(Date d) throws RemoteException {
		setAttribute("valuta",d);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setKonto(de.willuhn.jameica.hbci.rmi.Konto)
   */
  public void setKonto(Konto k) throws RemoteException {
		setAttribute("konto_id",k);
  }

  /**
   * Wir ueberschreiben die Funktion hier, weil beim Abrufen der
   * Umsaetze nur diejenigen gespeichert werden sollen, welche noch
   * nicht in der Datenbank existieren.
   * Da ein Umsatz von der Bank scheinbar keinen Identifier mitbringt,
   * muessen wir selbst einen fachlichen Vergleich durchfuehren.
   * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
   */
  public boolean equals(GenericObject o) throws RemoteException {
		if (o == null)
			return false;
		try {
			Umsatz other = (Umsatz) o;
			return other.getChecksum() == getChecksum();
		}
		catch (ClassCastException e)
		{
			return false;
		}
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getSaldo()
   */
  public double getSaldo() throws RemoteException {
		Double d = (Double) getAttribute("saldo");
		if (d == null)
			return 0;
		return d.doubleValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getPrimanota()
   */
  public String getPrimanota() throws RemoteException {
		return (String) getAttribute("primanota");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getArt()
   */
  public String getArt() throws RemoteException {
		return (String) getAttribute("art");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getCustomerRef()
   */
  public String getCustomerRef() throws RemoteException {
		return (String) getAttribute("customerref");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setSaldo(double)
   */
  public void setSaldo(double s) throws RemoteException {
		setAttribute("saldo",new Double(s));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setPrimanota(java.lang.String)
   */
  public void setPrimanota(String primanota) throws RemoteException {
		setAttribute("primanota",primanota);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setArt(java.lang.String)
   */
  public void setArt(String art) throws RemoteException {
		setAttribute("art",art);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setCustomerRef(java.lang.String)
   */
  public void setCustomerRef(String ref) throws RemoteException {
		setAttribute("customerref",ref);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Checksum#getChecksum()
   */
  public long getChecksum() throws RemoteException {

    Number n = (Number) this.getAttribute("checksum");
    if (n != null && n.longValue() != 0)
      return n.longValue();

    // BUGZILLA 184
    Date datum   = getDatum();
    Date valuta  = getValuta();
    String s = (""+getArt()).toUpperCase() +
		           getBetrag() +
		           getKonto().getChecksum() +
		           getCustomerRef() +
		           getGegenkontoBLZ() +
		           getGegenkontoNummer() +
		           (""+getGegenkontoName()).toUpperCase() +
		           getPrimanota() +
		           getSaldo() +
		           (""+getZweck()).toUpperCase() +
		           (""+getZweck2()).toUpperCase() +
		           (datum == null ? "" : HBCI.DATEFORMAT.format(datum)) +
							 (valuta == null ? "" : HBCI.DATEFORMAT.format(valuta));
		CRC32 crc = new CRC32();
		crc.update(s.getBytes());
    return crc.getValue();
  }

  /**
   * Ueberschrieben, um ein synthetisches Attribute "mergedzweck" zu erzeugen.
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  public Object getAttribute(String arg0) throws RemoteException
  {
    if ("umsatztyp".equals(arg0))
      return getUmsatzTyp();
    
    // Fuer Kategoriebaum
    if ("name".equals(arg0))
      return getGegenkontoName();
      
    if ("id-int".equals(arg0))
    {
      try
      {
        return new Integer(getID());
      }
      catch (Exception e)
      {
        Logger.error("unable to parse id: " + getID());
        return getID();
      }
    }
    if ("mergedzweck".equals(arg0))
      return getZweck() + (getZweck2() != null ? getZweck2() : "");

    // BUGZILLA 86 http://www.willuhn.de/bugzilla/show_bug.cgi?id=86
    if ("empfaenger".equals(arg0))
    {
      String name = getGegenkontoName();
      if (name != null)
        return name;

      String kto = getGegenkontoNummer();
      String blz = getGegenkontoBLZ();
      if (kto == null || blz == null)
        return null;

      return i18n.tr("Kto. {0}, BLZ {1}", new String[]{kto,blz});
    }

    return super.getAttribute(arg0);
  }

  /**
   * @see de.willuhn.datasource.rmi.Changeable#delete()
   */
  public void delete() throws RemoteException, ApplicationException
  {
    // BUGZILLA #70 http://www.willuhn.de/bugzilla/show_bug.cgi?id=70
    Konto k = getKonto();
    String[] fields = new String[]
    {
      getGegenkontoName(),
      getGegenkontoNummer(),
      getGegenkontoBLZ(),
      HBCI.DATEFORMAT.format(getValuta()),
      getZweck(),
      k.getWaehrung() + " " + HBCI.DECIMALFORMAT.format(getBetrag())
    };
    String msg = i18n.tr("Umsatz [Gegenkonto: {0}, Kto. {1} BLZ {2}], Valuta {3}, Zweck: {4}] {5} gel�scht",fields);

    super.delete();
    k.addToProtokoll(msg,Protokoll.TYP_SUCCESS);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getKommentar()
   */
  public String getKommentar() throws RemoteException
  {
    return (String) getAttribute("kommentar");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setKommentar(java.lang.String)
   */
  public void setKommentar(String kommentar) throws RemoteException
  {
    setAttribute("kommentar",kommentar);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#hasChangedByUser()
   */
  public boolean hasChangedByUser() throws RemoteException
  {
    Number n = (Number) this.getAttribute("checksum");
    return (n != null && n.longValue() != 0);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setChangedByUser()
   */
  public void setChangedByUser() throws RemoteException
  {
    if (hasChangedByUser())
      return; // wurde schon markiert
    setAttribute("checksum",new Long(getChecksum()));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setGenericAttribute(java.lang.String, java.lang.String)
   */
  public void setGenericAttribute(String name, String value) throws RemoteException, ApplicationException
  {
    if (name == null)
      return;
    
    if (value == null)
    {
      super.setAttribute(name,value);
      return;
    }

    try
    {
      if ("betrag".equals(name))
      {
        setBetrag(HBCI.DECIMALFORMAT.parse(value).doubleValue());
        return;
      }
      if ("saldo".equals(name))
      {
        setSaldo(HBCI.DECIMALFORMAT.parse(value).doubleValue());
        return;
      }
    }
    catch (ParseException e)
    {
      throw new ApplicationException(i18n.tr("Betrag \"{0}\" besitzt nicht das Format 000,00",value));
    }
    
    try
    {
      if ("datum".equals(name))
      {
        setDatum(HBCI.DATEFORMAT.parse(value));
        return;
      }
      if ("valuta".equals(name))
      {
        setValuta(HBCI.DATEFORMAT.parse(value));
        return;
      }
    }
    catch (ParseException e)
    {
      throw new ApplicationException(i18n.tr("Datum \"{0}\" besitzt nicht das Format TT.MM.JJJJ",value));
    }
    
    super.setAttribute(name,value);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getUmsatzTyp()
   */
  public UmsatzTyp getUmsatzTyp() throws RemoteException
  {
    if (UMSATZTYP_CACHE.size() == 0)
    {
      // Wir initialisieren den Cache
      DBIterator list = getService().createList(UmsatzTyp.class);
      while (list.hasNext())
      {
        UmsatzTyp t = (UmsatzTyp) list.next();
        UMSATZTYP_CACHE.put(t.getID(),t);
      }
    }
    
    // ID von fest verdrahteten Kategorien
    Integer i = (Integer) super.getAttribute("umsatztyp_id");

    if (i == null)
    {
      // Nicht zugeordnet, dann schauen wir mal, ob's eine dynamische Zuordnung gibt
      Enumeration typen = UMSATZTYP_CACHE.elements();
      while (typen.hasMoreElements())
      {
        UmsatzTyp ut = (UmsatzTyp) typen.nextElement();
        if (ut.matches(this))
          return ut;
      }
      // keine dynamische Umsatzkategorie gefunden. Dann raus hier
      return null;
    }
   
    // Wir haben eine ID und sie ist fest verdrahtet
    String id = i.toString();
    
    UmsatzTyp ut = (UmsatzTyp) UMSATZTYP_CACHE.get(id);
    if (ut == null)
    {
      // Hu? Nicht im Cache? Dann ist sie waehrend der aktuellen Sitzung dazugekommen
      // und wir laden sie noch in den Cache.
      try
      {
        ut = (UmsatzTyp) getService().createObject(UmsatzTyp.class,id);
        UMSATZTYP_CACHE.put(id,ut);
      }
      catch (ObjectNotFoundException one)
      {
        // inzwischen schon wieder geloescht worden. Ignorieren wir
      }
    }
    return ut;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setUmsatzTyp(de.willuhn.jameica.hbci.rmi.UmsatzTyp)
   */
  public void setUmsatzTyp(UmsatzTyp ut) throws RemoteException
  {
    setAttribute("umsatztyp_id",ut == null ? null : new Integer(ut.getID()));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#isAssigned()
   */
  public boolean isAssigned() throws RemoteException
  {
    return super.getAttribute("umsatztyp_id") != null;
  }
}


/**********************************************************************
 * $Log$
 * Revision 1.45  2007-04-23 18:07:15  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.44  2007/03/21 18:47:36  willuhn
 * @N Neue Spalte in Kategorie-Tree
 * @N Sortierung des Kontoauszuges wie in Tabelle angezeigt
 * @C Code cleanup
 *
 * Revision 1.43  2007/03/08 18:56:39  willuhn
 * @N Mehrere Spalten in Kategorie-Baum
 *
 * Revision 1.42  2007/02/19 15:01:54  willuhn
 * @N Auch dynamische Umsatzkategorien in Umsatzliste zeigen
 *
 * Revision 1.41  2006/12/29 14:28:47  willuhn
 * @B Bug 345
 * @B jede Menge Bugfixes bei SQL-Statements mit Valuta
 *
 * Revision 1.40  2006/12/01 00:02:34  willuhn
 * @C made unserializable members transient
 *
 * Revision 1.39  2006/11/30 23:48:40  willuhn
 * @N Erste Version der Umsatz-Kategorien drin
 *
 * Revision 1.38  2006/11/23 17:25:38  willuhn
 * @N Umsatz-Kategorien - in PROGRESS!
 *
 * Revision 1.37  2006/10/18 17:28:32  willuhn
 * @B Bug 299
 *
 * Revision 1.36  2006/10/10 22:06:59  willuhn
 * @C s/48/84/
 *
 * Revision 1.35  2006/10/10 22:05:32  willuhn
 * @B Bug 148
 *
 * Revision 1.34  2006/10/07 19:50:08  willuhn
 * @D javadoc
 *
 * Revision 1.33  2006/08/21 23:15:01  willuhn
 * @N Bug 184 (CSV-Import)
 *
 * Revision 1.32  2006/02/06 23:03:23  willuhn
 * @B Sortierung der Spalte "#"
 *
 * Revision 1.31  2005/12/29 01:22:11  willuhn
 * @R UmsatzZuordnung entfernt
 * @B Debugging am Pie-Chart
 *
 * Revision 1.30  2005/12/13 00:06:31  willuhn
 * @N UmsatzTyp erweitert
 *
 * Revision 1.29  2005/12/05 20:16:15  willuhn
 * @N Umsatz-Filter Refactoring
 *
 * Revision 1.28  2005/11/14 23:47:20  willuhn
 * @N added first code for umsatz categories
 *
 * Revision 1.27  2005/06/30 21:48:56  web0
 * @B bug 75
 *
 * Revision 1.26  2005/06/27 14:37:14  web0
 * @B bug 75
 *
 * Revision 1.25  2005/06/23 17:36:33  web0
 * @B bug 84
 *
 * Revision 1.24  2005/06/13 23:11:01  web0
 * *** empty log message ***
 *
 * Revision 1.23  2005/06/07 22:41:09  web0
 * @B bug 70
 *
 * Revision 1.22  2005/05/30 22:55:27  web0
 * *** empty log message ***
 *
 * Revision 1.21  2005/05/30 14:25:48  web0
 * *** empty log message ***
 *
 * Revision 1.20  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.19  2005/02/27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 * Revision 1.18  2005/02/19 16:49:32  willuhn
 * @B bugs 3,8,10
 *
 * Revision 1.17  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/10/23 17:34:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2004/10/17 16:28:46  willuhn
 * @N Die ersten Dauerauftraege abgerufen ;)
 *
 * Revision 1.14  2004/08/18 23:13:51  willuhn
 * @D Javadoc
 *
 * Revision 1.13  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.12  2004/07/23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.11  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/07/13 22:20:37  willuhn
 * @N Code fuer DauerAuftraege
 * @C paar Funktionsnamen umbenannt
 *
 * Revision 1.9  2004/07/04 17:07:59  willuhn
 * @B Umsaetze wurden teilweise nicht als bereits vorhanden erkannt und wurden somit doppelt angezeigt
 *
 * Revision 1.8  2004/06/30 20:58:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/06/17 00:14:10  willuhn
 * @N GenericObject, GenericIterator
 *
 * Revision 1.6  2004/05/25 23:23:17  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 * Revision 1.5  2004/04/27 22:23:56  willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
 *
 * Revision 1.4  2004/04/05 23:28:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.2  2004/03/05 08:38:47  willuhn
 * @N umsaetze works now
 *
 * Revision 1.1  2004/03/05 00:04:10  willuhn
 * @N added code for umsatzlist
 *
 **********************************************************************/