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
import java.util.Date;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.controller.AbstractControl;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.parts.Input;
import de.willuhn.jameica.gui.parts.LabelInput;
import de.willuhn.jameica.gui.parts.SelectInput;
import de.willuhn.jameica.gui.parts.Table;
import de.willuhn.jameica.gui.parts.TextInput;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.views.KontoListe;
import de.willuhn.jameica.hbci.gui.views.KontoNeu;
import de.willuhn.jameica.hbci.gui.views.UmsatzListe;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Passport;
import de.willuhn.jameica.hbci.rmi.PassportDDV;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller der fuer den Dialog "Bankverbindungen" zustaendig ist.
 */
public class KontoControl extends AbstractControl {

	// Fachobjekte
	private Konto konto 			 		= null;
	private Passport passport  		= null;
	
	// Eingabe-Felder
	private Input kontonummer  		= null;
	private Input blz          		= null;
	private Input name				 		= null;
	private Input passportAuswahl = null;
  private Input waehrung     		= null;
  private Input kundennummer 		= null;
  
  private Input saldo				 		= null;
  private Input saldoDatum   		= null;

	private boolean stored = false;

	private I18N i18n;
  /**
   * ct.
   * @param view
   */
  public KontoControl(AbstractView view) {
    super(view);
		i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
  }

	/**
	 * Liefert die aktuelle Bankverbindung.
   * @return Bankverbindung.
   * @throws RemoteException
   */
  public Konto getKonto() throws RemoteException
	{
		if (konto != null)
			return konto;
		
		konto = (Konto) getCurrentObject();
		if (konto != null)
			return konto;
		
		// Kein Konto verfuegbar - wir bauen ein neues.
		konto = (Konto) Settings.getDatabase().createObject(Konto.class,null);
		return konto;
	}

	/**
	 * Liefert den Passport des Kontos.
   * @return Passport.
   * @throws RemoteException
   */
  public Passport getPassport() throws RemoteException
	{
		if (passport != null)
			return passport;
			
		// TODO:
		return (Passport) Settings.getDatabase().createObject(PassportDDV.class,getPassportAuswahl().getValue());
	}

	/**
	 * Liefert das Eingabe-Feld fuer die Kontonummer.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getKontonummer() throws RemoteException
	{
		if (kontonummer != null)
			return kontonummer;
		kontonummer = new TextInput(getKonto().getKontonummer());
		return kontonummer;
	}

	/**
	 * Liefert das Eingabe-Feld fuer die Bankleitzahl.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getBlz() throws RemoteException
	{
		if (blz != null)
			return blz;
		blz = new TextInput(getKonto().getBLZ());
		blz.setComment("");
		blz.addListener(new BLZListener());
		return blz;
	}

	/**
	 * Liefert den Namen des Konto-Inhabers.
   * @return Name des Konto-Inhabers.
   * @throws RemoteException
   */
  public Input getName() throws RemoteException
	{
		if (name != null)
			return name;
		name = new TextInput(getKonto().getName());
		return name;
	}

	/**
	 * Liefert das Eingabefeld fuer die Kundennummer.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getKundennummer() throws RemoteException
	{
		if (kundennummer != null)
			return kundennummer;
		kundennummer = new TextInput(getKonto().getKundennummer());
		return kundennummer;
	}

  /**
   * Liefert die Waehrungsbezeichnung.
   * @return Waehrungsbezeichnung.
   * @throws RemoteException
   */
  public Input getWaehrung() throws RemoteException
  {
    if (waehrung != null)
      return waehrung;
    waehrung = new TextInput(getKonto().getWaehrung());
    return waehrung;
  }

	/**
	 * Lifert das Auswahl-Feld fuer das Sicherheitsmedium.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getPassportAuswahl() throws RemoteException
	{
		if (passportAuswahl != null)
			return passportAuswahl;

		passportAuswahl = new SelectInput(Settings.getDatabase().createList(Passport.class),null);
		return passportAuswahl;
	}

	/**
	 * Liefert ein Feld zur Anzeige des Saldos.
   * @return Anzeige-Feld.
   * @throws RemoteException
   */
  public Input getSaldo() throws RemoteException
	{
		if (saldo != null)
			return saldo;
			
		double s = getKonto().getSaldo();
		saldo = new LabelInput(
			s == 0.0 && getKonto().getSaldoDatum() == null ?
				"" :
				HBCI.DECIMALFORMAT.format(s) + " " + getKonto().getWaehrung());
		return saldo;
	}

	/**
	 * Liefert ein Feld zur Anzeige des Datums des Saldos.
   * @return Anzeige-Feld.
   * @throws RemoteException
   */
  public Input getSaldoDatum() throws RemoteException
	{
		if (saldoDatum != null)
			return saldoDatum;

		Date d = getKonto().getSaldoDatum();
		saldoDatum = new LabelInput(d == null ? "" : HBCI.LONGDATEFORMAT.format(d));
		return saldoDatum;
	}

  /**
	 * Liefert eine Tabelle mit allen vorhandenen Bankverbindungen.
   * @return Tabelle mit Bankverbindungen.
   * @throws RemoteException
   */
  public Table getKontoListe() throws RemoteException
	{
		DBIterator list = Settings.getDatabase().createList(Konto.class);

		Table table = new Table(list,this);
		table.addColumn(i18n.tr("Kontonummer"),"kontonummer");
		table.addColumn(i18n.tr("Bankleitzahl"),"blz");
		table.addColumn(i18n.tr("Kontoinhaber"),"name");
		table.addColumn(i18n.tr("Kundennummer"),"kundennummer");
		table.addColumn(i18n.tr("Sicherheitsmedium"),"passport_id");
		return table;
	}

	/**
   * Initialisiert den Dialog und loest die EventHandler aus.
   */
  public void init()
	{
		new BLZListener().handleEvent(null);
	}

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleDelete()
   */
  public void handleDelete() {
		try {

			YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
			d.setTitle(i18n.tr("Bankverbindung l�schen"));
			d.setText(i18n.tr("Wollen Sie diese Bankverbindung wirklich l�schen?"));

			try {
				Boolean choice = (Boolean) d.open();
				if (!choice.booleanValue())
					return;
			}
			catch (Exception e)
			{
				Application.getLog().error(e.getLocalizedMessage(),e);
				return;
			}

			// ok, wir loeschen das Objekt
			getKonto().delete();
			GUI.setActionText(i18n.tr("Bankverbindung gel�scht."));
		}
		catch (RemoteException e)
		{
			GUI.setActionText(i18n.tr("Fehler beim L�schen der Bankverbindung."));
			Application.getLog().error("unable to delete konto",e);
		}
		catch (ApplicationException ae)
		{
			GUI.setActionText(ae.getLocalizedMessage());
		}
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCancel()
   */
  public void handleCancel() {
		GUI.startView(KontoListe.class.getName(),null);
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleStore()
   */
  public void handleStore() {
		try {

			//////////////////////////////////////////////////////////////////////////
			// Passport checken
      
			Passport p = (Passport) Settings.getDatabase().createObject(Passport.class,
																																  getPassportAuswahl().getValue());

			if (p.isNewObject())
			{
				GUI.setActionText(i18n.tr("Bitte w�hlen Sie ein Sicherheitsmedium aus."));
				return;
			}
			getKonto().setPassport(p);
			//
			//////////////////////////////////////////////////////////////////////////

			getKonto().setKontonummer(getKontonummer().getValue());
			getKonto().setBLZ(getBlz().getValue());
			getKonto().setName(getName().getValue());
      getKonto().setWaehrung(getWaehrung().getValue());
      getKonto().setKundennummer(getKundennummer().getValue());
      
			// und jetzt speichern wir.
			getKonto().store();
			GUI.setActionText(i18n.tr("Bankverbindung gespeichert."));
			stored = true;
		}
		catch (ApplicationException e1)
		{
			GUI.setActionText(e1.getLocalizedMessage());
		}
		catch (RemoteException e)
		{
			Application.getLog().error("unable to store konto",e);
			GUI.setActionText("Fehler beim Speichern der Bankverbindung.");
		}

  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCreate()
   */
  public void handleCreate() {
		GUI.startView(KontoNeu.class.getName(),null);
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleOpen(java.lang.Object)
   */
  public void handleOpen(Object o) {
		GUI.startView(KontoNeu.class.getName(),o);
  }

	/**
   * Liest alle ueber das Sicherheitsmedium verfuegbaren Konten
   * aus und speichert sie (insofern Konten mit identischer kto-Nummer/BLZ nicht schon existieren).
   */
  public void handleReadFromPassport()
	{
		GUI.setActionText(i18n.tr("Chipkarte wird ausgelesen..."));

		GUI.startSync(new Runnable() {
      public void run() {
				try {
					DBIterator existing = Settings.getDatabase().createList(Konto.class);
					Konto check = null;
					Konto newKonto = null;
					Konto[] konten = getPassport().getKonten();

					for (int i=0;i<konten.length;++i)
					{
						// Wir checken, ob's das Konto schon gibt
						boolean found = false;
						while (existing.hasNext())
						{
							check = (Konto) existing.next();
							if (check.getBLZ().equals(konten[i].getBLZ()) &&
								check.getKontonummer().equals(konten[i].getKontonummer()))
							{
								found = true;
								break;
							}
							
						}
						existing.begin();
						if (!found)
						{
							// Konto neu anlegen
							try {
								newKonto = (Konto) Settings.getDatabase().createObject(Konto.class,null);
								newKonto.setBLZ(konten[i].getBLZ());
								newKonto.setKontonummer(konten[i].getKontonummer());
								newKonto.setKundennummer(konten[i].getKundennummer());
								newKonto.setName(konten[i].getName());
								newKonto.setWaehrung(konten[i].getWaehrung());
								newKonto.setPassport(getPassport()); // wir speichern den ausgewaehlten Passport.
								newKonto.store();
							}
							catch (Exception e)
							{
								// Wenn ein Konto fehlschlaegt, soll nicht gleich der ganze Vorgang abbrechen
								Application.getLog().error("error while storing konto",e);
								GUI.setActionText(i18n.tr("Fehler beim Anlegen des Kontos") + " " + konten[i].getKontonummer());
							}
						}
						
					}
					GUI.startView(KontoListe.class.getName(),null); // Page reload
					GUI.setActionText(i18n.tr("Konten erfolgreich ausgelesen"));
				}
				catch (RemoteException e)
				{
					Application.getLog().error("error while reading data from passport",e);
					GUI.setActionText(i18n.tr("Fehler beim Lesen der Konto-Daten"));
				}
      }
    });
	}

	/**
   * Aktualisiert den angezeigten Saldo.
   */
  public void handleRefreshSaldo()
	{

		GUI.startProgress();

		GUI.startSync(new Runnable() {
      public void run() {
      	try {
      		GUI.setActionText(i18n.tr("Saldo des Kontos wird ermittelt..."));
					getKonto().refreshSaldo();
					getSaldo().setValue(HBCI.DECIMALFORMAT.format(getKonto().getSaldo()) + " " + getKonto().getWaehrung());
					getSaldoDatum().setValue(HBCI.LONGDATEFORMAT.format(getKonto().getSaldoDatum()));
					GUI.setActionText(i18n.tr("Saldo des Kontos erfolgreich �bertragen..."));
      	}
      	catch (RemoteException e)
      	{
					Application.getLog().error("error while reading saldo",e);
					GUI.setActionText(e.getLocalizedMessage());
      	}
      	catch (ApplicationException e2)
      	{
      		GUI.setActionText(e2.getLocalizedMessage());
      	}
      }
    });

		GUI.stopProgress();
	}

	/**
   * Laedt die Seite mit den Umsaetzen dieses Kontos.
   */
  public void handleShowUmsaetze()
	{
		try {
			Konto konto = getKonto();
			if (konto == null || konto.isNewObject())
			{
				GUI.setActionText(i18n.tr("Bitte speichern Sie zuerst das Konto."));
				return;
			}
			GUI.startView(UmsatzListe.class.getName(),getKonto());
		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while starting umsatz list",e);
			GUI.setActionText(i18n.tr("Fehler beim Laden der Kontoausz�ge"));
		}
	}

	/**
	 * Sucht das Geldinstitut zur eingegebenen BLZ und zeigt es als Kommentar
	 * hinter dem BLZ-Feld an.
   */
  private class BLZListener implements Listener
	{

    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event) {

			try {
				String name = HBCIUtils.getNameForBLZ(getBlz().getValue());
				getBlz().setComment(name);
			}
			catch (RemoteException e)
			{
				Application.getLog().error("error while updating blz comment",e);
			}
    }
	}
}


/**********************************************************************
 * $Log$
 * Revision 1.13  2004-03-05 00:04:10  willuhn
 * @N added code for umsatzlist
 *
 * Revision 1.12  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.11  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 * Revision 1.10  2004/02/24 22:47:05  willuhn
 * @N GUI refactoring
 *
 * Revision 1.9  2004/02/23 20:30:47  willuhn
 * @C refactoring in AbstractDialog
 *
 * Revision 1.8  2004/02/22 20:04:54  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.7  2004/02/20 01:36:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/02/20 01:25:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/02/17 01:07:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.3  2004/02/12 23:46:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/11 15:40:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 **********************************************************************/