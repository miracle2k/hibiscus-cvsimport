/*****************************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 * $Locker$
 * $State$
 *
****************************************************************************/
package de.willuhn.jameica.hbci.passports.pintan.rmi;

import java.rmi.RemoteException;
import java.util.Date;

import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.hbci.passport.Configuration;
import de.willuhn.jameica.hbci.rmi.Konto;

/**
 * Interface fuer eine einzelne PIN/TAN-Konfiguration fuer eine
 * spezifische Bank.
 * @author willuhn
 */
public interface PinTanConfig extends GenericObject, Configuration
{

  /**
   * Liefert die BLZ fuer die diese Config zustaendig ist.
   * @return BLZ.
   * @throws RemoteException
   */
  public String getBLZ() throws RemoteException;

  /**
   * Liefert eine optionale Liste von hart verdrahteten Konten.
   * Das ist sinnvoll, wenn der User mehrere Konten bei der gleichen
   * Bank mit unterschiedlichen PIN/TAN-Konfigurationen hat. Dann wuerde bei jeder
   * Bank-Abfrage ein Dialog zur Auswahl der Config kommen, weils
   * Hibiscus allein anhand BLZ/Kundenkennung nicht mehr unterscheiden kann.
   * @return Liste der optionalen Konten oder <code>null</code>
   * BUGZILLA 173
   * BUGZILLA 314
   * @throws RemoteException
   */
  public Konto[] getKonten() throws RemoteException;

  /**
   * Speichert eine optionale Liste von festzugeordneten Konten.
   * BUGZILLA 173
   * BUGZILLA 314
   * @param k Liste der Konten.
   * @throws RemoteException
   */
  public void setKonten(Konto[] k) throws RemoteException;

  /**
   * Liefert die HTTPs-URL, ueber die die Bank erreichbar ist.
   * @return URL
   * @throws RemoteException
   */
  public String getURL() throws RemoteException;

  /**
   * Speichert die HTTPs-URL, ueber die die Bank erreichbar ist.
   * Wichtig: Das Protokoll ("https://") wird nicht mit abgespeichert.
   * @param url URL
   * @throws RemoteException
   */
  public void setURL(String url) throws RemoteException;

  /**
   * Liefert den TCP-Port des Servers.
   * Default: "443".
   * @return Port des Servers.
   * @throws RemoteException
   */
  public int getPort() throws RemoteException;

  /**
   * Definiert den TCP-Port.
   * @param port
   * @throws RemoteException
   */
  public void setPort(int port) throws RemoteException;

  /**
   * Liefert den Filter-Typ.
   * Default: "Base64".
   * @return der Filter-Typ.
   * @throws RemoteException
   */
  public String getFilterType() throws RemoteException;

  /**
   * Legt den Filter-Typ fest.
   * @param type
   * @throws RemoteException
   */
  public void setFilterType(String type) throws RemoteException;

  /**
   * Liefert die HBCI-Version.
   * @return HBCI-Version.
   * @throws RemoteException
   */
  public String getHBCIVersion() throws RemoteException;

  /**
   * Speichert die zu verwendende HBCI-Version.
   * @param version HBCI-Version.
   * @throws RemoteException
   */
  public void setHBCIVersion(String version) throws RemoteException;
  
  /**
   * Liefert die Kundenkennung.
   * @return Kundenkennung.
   * @throws RemoteException
   */
  public String getCustomerId() throws RemoteException;

  /**
   * Speichert die Kundenkennung.
   * @param customer
   * @throws RemoteException
   */
  public void setCustomerId(String customer) throws RemoteException;

  /**
   * Liefert die Benutzerkennung.
   * @return Benutzerkennung.
   * @throws RemoteException
   */
  public String getUserId() throws RemoteException;
  
  /**
   * Speichert die Benutzerkennung.
   * @param user
   * @throws RemoteException
   */
  public void setUserId(String user) throws RemoteException;
  
  /**
   * Dateiname der HBCI4Java-Config.
   * @return HBCI4Java-Config.
   * @throws RemoteException
   */
  public String getFilename() throws RemoteException;
  
  /**
   * Liefert den Passport.
   * @return Passport.
   * @throws RemoteException
   */
  public HBCIPassport getPassport() throws RemoteException;
  
  /**
   * Optionale Angabe einer Bezeichnung fuer die Konfig.
   * @return Bezeichnung.
   * @throws RemoteException
   */
  public String getBezeichnung() throws RemoteException;
  
  /**
   * Speichert eine optionale Bezeichnung fuer die Konfig.
   * @param bezeichnung Bezeichnung.
   * @throws RemoteException
   */
  public void setBezeichnung(String bezeichnung) throws RemoteException;
  
  /**
   * Prueft, ob die verbrauchten TANs gespeichert werden sollen.
   * @return true, wenn die TANs gespeichert werden.
   * @throws RemoteException
   * BUGZILLA 62
   */
  public boolean getSaveUsedTan() throws RemoteException;
  
  /**
   * Legt fest, ob die verbrauchten TANs gespeichert werden sollen.
   * @param save true, wenn die TANs gespeichert werden sollen.
   * @throws RemoteException
   * BUGZILLA 62
   */
  public void setSaveUsedTan(boolean save) throws RemoteException;
  
  /**
   * Liefert einen ggf gespeicherten Sicherheitsmechanismus.
   * @return ID des Sicherheitsmechanismus.
   * @throws RemoteException
   */
  public String getSecMech() throws RemoteException;

  /**
   * Speichert einen Sicherheitsmechanismus.
   * @param s der Sicherheitsmechanismus.
   * @throws RemoteException
   */
  public void setSecMech(String s) throws RemoteException;

  /**
   * Prueft, ob die TAN waehrend der Eingabe angezeigt werden soll.
   * @return true, wenn die TANs angezeigt werden sollen.
   * @throws RemoteException
   */
  public boolean getShowTan() throws RemoteException;

  /**
   * Legt fest, ob die TANs bei der Eingabe angezeigt werden sollen.
   * @param show true, wenn sie angezeigt werden sollen.
   * @throws RemoteException
   */
  public void setShowTan(boolean show) throws RemoteException;
  
  /**
   * Speichert eine verbrauchte TAN.
   * @param tan die verbrauchte TAN.
   * @throws RemoteException
   */
  public void saveUsedTan(String tan) throws RemoteException;

  /**
   * Prueft, ob die TAN schon verbraucht wurde und liefert das Datum des Verbrauchs zurueck.
   * @param tan die zu testende TAN.
   * @return null, wenn die TAN noch nicht benutzt wurde, sonst das Datum des Verbrauchs.
   * @throws RemoteException
   */
  public Date getTanUsed(String tan) throws RemoteException;
  
  /**
   * Loescht die Liste der verbrauchten TANs.
   * @throws RemoteException
   */
  public void clearUsedTans() throws RemoteException;
  
  /**
   * Liefert eine Liste der verbrauchten TANs.
   * @return Liste der verbrauchten TANs.
   * @throws RemoteException
   */
  public String[] getUsedTans() throws RemoteException;
  
  /**
   * Liefert die Liste der zuletzt eingegebenen TAN-Medien-Bezeichnungen.
   * @return Liste der zuletzt eingegebenen TAN-Medien-Bezeichnungen.
   * @throws RemoteException
   */
  public String[] getTanMedias() throws RemoteException;
  
  /**
   * Speichert die Liste der zuletzt eingegebenen TAN-Medien-Bezeichnungen.
   * @param names die Liste der zuletzt eingegebenen TAN-Medien-Bezeichnungen.
   * @throws RemoteException
   */
  public void setTanMedias(String[] names) throws RemoteException;
  
  /**
   * Fuegt ein neues TAN-Medium zur Liste der bekannten hinzu.
   * @param name die neue TAN-Medien-Bezeichnung.
   * @throws RemoteException
   */
  public void addTanMedia(String name) throws RemoteException;

  /**
   * Liefert das zuletzt verwendete TAN-Medium.
   * @return das zuletzt verwendete TAN-Medium.
   * @throws RemoteException
   */
  public String getTanMedia() throws RemoteException;
  
  /**
   * Speichert das zuletzt verwendete TAN-Medium.
   * @param name das zuletzt verwendete TAN-Medium.
   * @throws RemoteException
   */
  public void setTanMedia(String name) throws RemoteException;
}

/*****************************************************************************
 * $Log$
 * Revision 1.3  2011-05-09 09:35:15  willuhn
 * @N BUGZILLA 827
 *
 * Revision 1.2  2011-04-29 09:17:35  willuhn
 * @N Neues Standard-Interface "Configuration" fuer eine gemeinsame API ueber alle Arten von HBCI-Konfigurationen
 * @R Passports sind keine UnicastRemote-Objekte mehr
 *
 * Revision 1.1  2010-06-17 11:38:16  willuhn
 * @C kompletten Code aus "hbci_passport_pintan" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
*****************************************************************************/