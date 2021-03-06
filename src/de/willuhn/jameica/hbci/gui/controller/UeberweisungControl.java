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

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.hbci.HBCIProperties;
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
    final Ueberweisung u = (Ueberweisung) getTransfer();
    
    List<Typ> list = new ArrayList<Typ>();
    list.add(new Typ(false,false));
    list.add(new Typ(true,false));
    list.add(new Typ(false,true));
    // TODO: Bei BZU- oder Spenden-Ueberweisung muss die Box deaktiviert werden und beide Flags duerfen nicht gesetzt sein
    this.typ = new SelectInput(list,new Typ(u.isTerminUeberweisung(),u.isUmbuchung()));
    this.typ.setName(i18n.tr("Auftragstyp"));
    this.typ.setAttribute("name");
    this.typ.setEnabled(!u.ausgefuehrt());
    this.typ.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        // Wir muessen die Entscheidung, ob es eine Termin-Ueberweisung ist,
        // sofort im Objekt speichern, denn die Information wird von
        // "getTermin()" gebraucht, um zu erkennen, ob der Auftrag faellig ist
        try
        {
          Typ t = (Typ) getTyp().getValue();
          u.setTerminUeberweisung(t.termin);

          // Kommentar vom Termin-Eingabefeld aktualisieren.
          getTermin().updateComment();
        }
        catch (Exception e)
        {
          Logger.error("unable to set flag",e);
        }
      }
    });
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

    String current = ((BaseUeberweisung)getTransfer()).getTextSchluessel();
    textschluessel = new SelectInput(TextSchluessel.get(TextSchluessel.SET_UEB),TextSchluessel.get(current));
    textschluessel.setName(i18n.tr("Textschl�ssel"));
    textschluessel.setEnabled(!((Terminable)getTransfer()).ausgefuehrt());

    ////////////////////////////////////////////////////////////////////////////
    // BZ� und Spenden-Ueberweisung

    // initiales Setzen der Einstellungen
    updateZweck(current);
    
    // Listener fuer die nachtraegliche Aenderung
    if (textschluessel.isEnabled())
    {
      textschluessel.addListener(new Listener() {
        public void handleEvent(Event event)
        {
          TextSchluessel s = (TextSchluessel) textschluessel.getValue();
          updateZweck(s != null ? s.getCode() : null);
        }
      });
    }
    //
    ////////////////////////////////////////////////////////////////////////////
    
    return textschluessel;
  }
  
  /**
   * Aktualisiert die Eingabefelder fuer den Verwendungszweck abhaengig vom Textschluessel.
   * @param code der Code.
   */
  private void updateZweck(String code)
  {
    try
    {
      TextInput zweck = getZweck();
      
      if (code != null && code.equals(TextSchluessel.TS_BZU))
      {
        zweck.setName(i18n.tr("BZ�-Pr�fziffer"));
        zweck.setMaxLength(HBCIProperties.HBCI_TRANSFER_BZU_LENGTH);
        zweck.setValidChars(HBCIProperties.HBCI_BZU_VALIDCHARS);
      }
      else if (code != null && code.equals(TextSchluessel.TS_SPENDE))
      {
        // TODO: Eingabefelder fuer die Spenden-Ueberweisung fehlen noch
      }
      else
      {
        zweck.setName(i18n.tr("Verwendungszweck"));
        zweck.setMaxLength(HBCIProperties.HBCI_TRANSFER_USAGE_MAXLENGTH);
        zweck.setValidChars(HBCIProperties.HBCI_DTAUS_VALIDCHARS);
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to apply changes",e);
    }
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
 * Revision 1.54  2011-05-20 16:22:31  willuhn
 * @N Termin-Eingabefeld in eigene Klasse ausgelagert (verhindert duplizierten Code) - bessere Kommentare
 *
 * Revision 1.53  2011-05-13 07:40:11  willuhn
 * *** empty log message ***
 *
 * Revision 1.52  2011-05-12 08:08:27  willuhn
 * @N BUGZILLA 591
 *
 * Revision 1.51  2011-05-11 16:23:57  willuhn
 * @N BUGZILLA 591
 *
 * Revision 1.50  2011-05-10 12:17:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.49  2011-05-10 11:41:30  willuhn
 * @N Text-Schluessel als Konstanten definiert - Teil aus dem Patch von Thomas vom 07.12.2010
 *
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