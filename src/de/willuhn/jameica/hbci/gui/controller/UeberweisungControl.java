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
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.TextSchluessel;
import de.willuhn.jameica.hbci.gui.action.UeberweisungNew;
import de.willuhn.jameica.hbci.gui.parts.UeberweisungList;
import de.willuhn.jameica.hbci.rmi.BaseUeberweisung;
import de.willuhn.jameica.hbci.rmi.HibiscusTransfer;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.logging.Logger;

/**
 * Controller fuer die Ueberweisungen.
 */
public class UeberweisungControl extends AbstractBaseUeberweisungControl
{

	private UeberweisungList table		 = null;
  private Ueberweisung transfer      = null;
  private SelectInput typ            = null;
  private SelectInput textschluessel = null;

  /**
   * ct.
   * @param view
   */
  public UeberweisungControl(AbstractView view)
  {
    super(view);
  }

	/**
	 * Ueberschrieben, damit wir bei Bedarf eine neue Ueberweisung erzeugen koennen.
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getTransfer()
   */
  public HibiscusTransfer getTransfer() throws RemoteException
	{
    if (transfer != null)
      return transfer;

    transfer = (Ueberweisung) getCurrentObject();
    if (transfer != null)
      return transfer;
      
    transfer = (Ueberweisung) Settings.getDBService().createObject(Ueberweisung.class,null);
		return transfer;
	}

  /**
   * Liefert eine Combobox zur Auswahl des Auftragstyps.
   * Zur Wahl stehen Ueberweisung, Termin-Ueberweisung und Umbuchung.
   * @return die Combobox.
   * @throws RemoteException
   */
  public SelectInput getTyp() throws RemoteException
  {
    if (this.typ != null)
      return this.typ;
    Ueberweisung u = (Ueberweisung) getTransfer();
    
    List<Typ> list = new ArrayList<Typ>();
    list.add(new Typ(false,false));
    list.add(new Typ(true,false));
    list.add(new Typ(false,true));
    this.typ = new SelectInput(list,new Typ(u.isTerminUeberweisung(),u.isUmbuchung()));
    this.typ.setName(i18n.tr("Auftragstyp"));
    this.typ.setAttribute("name");
    this.typ.setEnabled(!u.ausgefuehrt());
    return this.typ;
  }
  
  /**
	 * Liefert eine Tabelle mit allen vorhandenen Ueberweisungen.
	 * @return Tabelle.
	 * @throws RemoteException
	 */
	public UeberweisungList getUeberweisungListe() throws RemoteException
	{
		if (table != null)
			return table;

    table = new de.willuhn.jameica.hbci.gui.parts.UeberweisungList(new UeberweisungNew());
		return table;
	}
  
  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractBaseUeberweisungControl#getTextSchluessel()
   */
  public Input getTextSchluessel() throws RemoteException
  {
    if (textschluessel != null)
      return textschluessel;

    textschluessel = new SelectInput(TextSchluessel.get(new String[]{"51","53","54","59"}),TextSchluessel.get(((BaseUeberweisung)getTransfer()).getTextSchluessel()));
    textschluessel.setName(i18n.tr("Textschl�ssel"));
    textschluessel.setEnabled(!((Terminable)getTransfer()).ausgefuehrt());
    return textschluessel;
  }
  
  /**
   * Ueberschrieben, um das Flag fuer die Termin-Ueberweisung zu speichern.
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#handleStore()
   */
  public synchronized boolean handleStore()
  {
    try
    {
      Ueberweisung u = (Ueberweisung) getTransfer();
      
      Typ t = (Typ) getTyp().getValue();
      u.setTerminUeberweisung(t.termin);
      u.setUmbuchung(t.umb);
      return super.handleStore();
    }
    catch (RemoteException re)
    {
      Logger.error("error while storing ueberweisung",re);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern der �berweisung"));
      return false;
    }
  }
  
  /**
   * Hilfsklasse fuer den Auftragstyp.
   */
  public class Typ
  {
    private boolean termin = false;
    private boolean umb    = false;
    
    /**
     * ct.
     * @param termin true bei Termin-Ueberweisung.
     * @param umb true bei Umbuchung.
     */
    private Typ(boolean termin, boolean umb)
    {
      this.termin = termin;
      this.umb    = umb;
    }
    
    /**
     * Liefert den sprechenden Namen des Typs.
     * @return sprechender Name des Typs.
     */
    public String getName()
    {
      if (this.termin) return i18n.tr("Termin-�berweisung");
      if (this.umb)    return i18n.tr("Interne Umbuchung (�bertrag)");
      return           i18n.tr("�berweisung");
    }
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o)
    {
      if (o == null || !(o instanceof Typ))
        return false;
      Typ other = (Typ) o;
      return other.termin == this.termin &&
             other.umb == this.umb;
    }
  }
}


/**********************************************************************
 * $Log$
 * Revision 1.48  2011-04-08 17:41:45  willuhn
 * @N Erster Druck-Support fuer Ueberweisungslisten
 *
 * Revision 1.47  2010-08-17 11:32:10  willuhn
 * @C Code-Cleanup
 *
 * Revision 1.46  2010-06-07 12:43:41  willuhn
 * @N BUGZILLA 587
 *
 * Revision 1.45  2009/05/12 22:53:33  willuhn
 * @N BUGZILLA 189 - Ueberweisung als Umbuchung
 **********************************************************************/