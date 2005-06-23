/*****************************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 * $Locker$
 * $State$
 *
****************************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.SammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung des Containers fuer Sammellastschrift-Buchungen.
 * @author willuhn
 */
public class SammelLastschriftImpl extends AbstractDBObject
  implements SammelLastschrift
{

  private I18N i18n;

  /**
   * ct.
   * @throws java.rmi.RemoteException
   */
  public SammelLastschriftImpl() throws RemoteException
  {
    super();
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName()
  {
    return "slastschrift";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException
  {
    return "bezeichnung";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException
  {
    try {
      if (getKonto() == null)
        throw new ApplicationException(i18n.tr("Bitte w�hlen Sie ein Konto aus."));
      if (getKonto().isNewObject())
        throw new ApplicationException(i18n.tr("Bitte speichern Sie zun�chst das Konto"));

      if (getBezeichnung() == null || getBezeichnung().length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie eine Bezeichnung ein."));
    }
    catch (RemoteException e)
    {
      Logger.error("error while checking sammellastschrift",e);
      throw new ApplicationException(i18n.tr("Fehler beim Pr�fen der Sammel-Lastschrift."));
    }
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException
  {
    try {
      if (!whileStore && ausgefuehrt())
        throw new ApplicationException(i18n.tr("Auftrag wurde bereits ausgef�hrt und kann daher nicht mehr ge�ndert werden."));
    }
    catch (RemoteException e)
    {
      Logger.error("error while checking sammellastschrift",e);
      throw new ApplicationException(i18n.tr("Fehler beim Pr�fen des Auftrags."));
    }
    insertCheck();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insert()
   */
  public void insert() throws RemoteException, ApplicationException
  {
    if (getTermin() == null)
      setTermin(new Date());
    if (getAttribute("ausgefuehrt") == null) // Status noch nicht definiert
      setAttribute("ausgefuehrt",new Integer(0));
    super.insert();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getForeignObject(java.lang.String)
   */
  protected Class getForeignObject(String arg0) throws RemoteException
  {
    if ("konto_id".equals(arg0))
      return Konto.class;
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelLastschrift#getBuchungen()
   */
  public DBIterator getBuchungen() throws RemoteException
  {
    DBIterator list = this.getService().createList(SammelLastBuchung.class);
    list.addFilter("slastschrift_id = " + this.getID());
    return list;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelLastschrift#getKonto()
   */
  public Konto getKonto() throws RemoteException
  {
    return (Konto) getAttribute("konto_id");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelLastschrift#setKonto(de.willuhn.jameica.hbci.rmi.Konto)
   */
  public void setKonto(Konto konto) throws RemoteException
  {
    setAttribute("konto_id", konto);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Terminable#getTermin()
   */
  public Date getTermin() throws RemoteException
  {
    return (Date) getAttribute("termin");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Terminable#ausgefuehrt()
   */
  public boolean ausgefuehrt() throws RemoteException
  {
    Integer i = (Integer) getAttribute("ausgefuehrt");
    if (i == null)
      return false;
    return i.intValue() == 1;
  }

  // Kleines Hilfsboolean damit uns der Status-Wechsel
  // beim Speichern nicht um die Ohren fliegt.
  private boolean whileStore = false;


  /**
   * @see de.willuhn.jameica.hbci.rmi.Terminable#setAusgefuehrt()
   */
  public void setAusgefuehrt() throws RemoteException, ApplicationException
  {
    try
    {
      whileStore = true;
      setAttribute("ausgefuehrt",new Integer(1));
      store();
    }
    finally
    {
      whileStore = false;
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Terminable#setTermin(java.util.Date)
   */
  public void setTermin(Date termin) throws RemoteException
  {
    setAttribute("termin",termin);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Terminable#ueberfaellig()
   */
  public boolean ueberfaellig() throws RemoteException
  {
    if (ausgefuehrt())
      return false;
    Date termin = getTermin();
    if (termin == null)
      return false;
    return (termin.before(new Date()));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelLastschrift#getBezeichnung()
   */
  public String getBezeichnung() throws RemoteException
  {
    return (String) getAttribute("bezeichnung");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelLastschrift#setBezeichnung(java.lang.String)
   */
  public void setBezeichnung(String bezeichnung) throws RemoteException
  {
    setAttribute("bezeichnung", bezeichnung);
  }

  /**
   * @see de.willuhn.datasource.rmi.Changeable#delete()
   */
  public void delete() throws RemoteException, ApplicationException
  {
    // Wir muessen auch alle Buchungen mitloeschen
    // da Constraints dorthin existieren.
    try {
      this.transactionBegin();

      int count = 0;
      // dann die Dauerauftraege
      DBIterator list = getBuchungen();
      SammelLastBuchung b = null;
      while (list.hasNext())
      {
        b = (SammelLastBuchung) list.next();
        b.delete();
        count++;
      }

      // Jetzt koennen wir uns selbst loeschen
      super.delete();

      // und noch in's Protokoll schreiben.
      Konto k = this.getKonto();
      if (k == null)
        return;
      k.addToProtokoll(i18n.tr(
        "Sammellastschrift [Bezeichnung: {0}] gel�scht. Enthaltene Buchungen: {1}",
        new String[] {
          getBezeichnung(),
          count+"",
        }), Protokoll.TYP_SUCCESS);

      this.transactionCommit();
    }
    catch (RemoteException e)
    {
      this.transactionRollback();
      throw e;
    }
    catch (ApplicationException e2)
    {
      this.transactionRollback();
      throw e2;
    }
  }

  /**
   * @see de.willuhn.datasource.rmi.Changeable#store()
   */
  public void store() throws RemoteException, ApplicationException
  {
    super.store();
    Konto k = this.getKonto();
    k.addToProtokoll(i18n.tr("Sammellastschrift [Bezeichnung: {0}] gespeichert",getBezeichnung()),
      Protokoll.TYP_SUCCESS);
  }

  /**
   * Ueberschrieben, um ein Pseudo-Attribut "buchungen" zu erzeugen, welches
   * eine String-Repraesentation der enthaltenen Buchungen enthaelt.
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  public Object getAttribute(String arg0) throws RemoteException
  {
  	if ("buchungen".equals(arg0))
  	{
			try
			{
				StringBuffer sb = new StringBuffer();
				DBIterator di = getBuchungen();
				while (di.hasNext())
				{
					SammelLastBuchung b = (SammelLastBuchung) di.next();
					String[] params = new String[]
					{
						b.getZweck(),
						b.getGegenkontoName(),
						HBCI.DECIMALFORMAT.format(b.getBetrag()),
						getKonto().getWaehrung()
					};
					sb.append(i18n.tr("[{0}] {1}, Betrag {2} {3}",params));
					if (di.hasNext())
						sb.append("\n");
				}
				return sb.toString();
			}
			catch (RemoteException e)
			{
				Logger.error("error while reading buchungen",e);
				return i18n.tr("Buchungen nicht lesbar");
			}
  	}
    return super.getAttribute(arg0);
  }
}

/*****************************************************************************
 * $Log$
 * Revision 1.7  2005-06-23 21:13:03  web0
 * @B bug 84
 *
 * Revision 1.6  2005/06/23 17:36:33  web0
 * @B bug 84
 *
 * Revision 1.5  2005/05/30 22:55:27  web0
 * *** empty log message ***
 *
 * Revision 1.4  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.3  2005/03/02 17:59:30  web0
 * @N some refactoring
 *
 * Revision 1.2  2005/03/01 18:51:04  web0
 * @N Dialoge fuer Sammel-Lastschriften
 *
 * Revision 1.1  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
*****************************************************************************/