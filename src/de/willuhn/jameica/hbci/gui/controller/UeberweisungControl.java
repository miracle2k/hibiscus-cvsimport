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

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.controller.AbstractControl;
import de.willuhn.jameica.gui.dialogs.ListDialog;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.SearchInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.views.UeberweisungNeu;
import de.willuhn.jameica.hbci.rmi.Empfaenger;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller fuer die Ueberweisungen.
 */
public class UeberweisungControl extends AbstractControl {

	// Fach-Objekte
	private Ueberweisung ueberweisung = null;
	private Empfaenger empfaenger 		= null;
	private Konto konto								= null;
	
	// Eingabe-Felder
	private AbstractInput kontoAuswahl				= null;
	private AbstractInput betrag							= null;
	private AbstractInput zweck								= null;
	private AbstractInput zweck2							= null;
	private AbstractInput termin							= null;

	private AbstractInput empfName 						= null;
	private AbstractInput empfkto 						= null;
	private AbstractInput empfblz 						= null;
	
	private CheckboxInput storeEmpfaenger = null;

	private I18N i18n;

	private boolean stored								= false;

  /**
   * ct.
   * @param view
   */
  public UeberweisungControl(AbstractView view) {
    super(view);
		i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
  }

	/**
	 * Liefert die Ueberweisung oder erzeugt bei Bedarf eine neue.
   * @return die Ueberweisung.
   * @throws RemoteException
   */
  public Ueberweisung getUeberweisung() throws RemoteException
	{
		if (ueberweisung != null)
			return ueberweisung;
		
		ueberweisung = (Ueberweisung) getCurrentObject();
		if (ueberweisung != null)
			return ueberweisung;
		
		ueberweisung = (Ueberweisung) Settings.getDatabase().createObject(Ueberweisung.class,null);
		return ueberweisung;
	}

	/**
	 * Liefert das Konto der Ueberweisung.
   * @return das Konto.
   * @throws RemoteException
   */
  private Konto getKonto() throws RemoteException
	{
		if (konto != null)
			return konto;

		konto = getUeberweisung().getKonto();
		if (konto != null)
			return konto;

		konto = (Konto) Settings.getDatabase().createObject(Konto.class,null);
		return konto;
	}

	/**
	 * Liefert eine Tabelle mit allen vorhandenen Ueberweisungen.
	 * @return Tabelle.
	 * @throws RemoteException
	 */
	public TablePart getUeberweisungListe() throws RemoteException
	{
		DBIterator list = Settings.getDatabase().createList(Ueberweisung.class);

		TablePart table = new TablePart(list,this,new TableFormatter() {
      public void format(TableItem item) {
      	Ueberweisung u = (Ueberweisung) item.getData();
      	if (u == null)
      		return;
      	try {
					if (u.ausgefuehrt())
					{
						item.setBackground(Settings.getBuchungHabenBackground());
						item.setForeground(Settings.getBuchungHabenForeground());
					}
					else {
						item.setBackground(Settings.getBuchungSollBackground());
						item.setForeground(Settings.getBuchungSollForeground());
					}
      	}
      	catch (RemoteException e)
      	{
      		Application.getLog().error("unable to format table item",e);
      	}
      }
    });
		table.addColumn(i18n.tr("Konto"),"konto_id");
		table.addColumn(i18n.tr("Kto. des Empf�ngers"),"empfaenger_konto");
		table.addColumn(i18n.tr("BLZ des Empf�ngers"),"empfaenger_blz");
		table.addColumn(i18n.tr("Name des Empf�ngers"),"empfaenger_name");
		table.addColumn(i18n.tr("Betrag"),"betrag", new CurrencyFormatter("",HBCI.DECIMALFORMAT));
		table.addColumn(i18n.tr("Termin"),"termin", new DateFormatter(HBCI.LONGDATEFORMAT));
		table.addColumn(i18n.tr("Status"),"ausgefuehrt",new Formatter() {
      public String format(Object o) {
				try {
					int i = ((Integer) o).intValue();
					return i == 1 ? i18n.tr("ausgef�hrt") : i18n.tr("offen");
				}
				catch (Exception e) {}
				return ""+o;
      }
    });
		return table;
	}

	/**
	 * Liefert ein Auswahlfeld fuer das Konto.
   * @return Auswahl-Feld.
   * @throws RemoteException
   */
  public AbstractInput getKontoAuswahl() throws RemoteException
	{
		if (kontoAuswahl != null)
			return kontoAuswahl;

		kontoAuswahl = new SelectInput(getKonto());
		kontoAuswahl.addListener(new KontoListener());
		String b = getKonto().getBezeichnung();
		kontoAuswahl.setComment(b == null ? "" : b);
		return kontoAuswahl;
	}

	/**
	 * Liefert das Eingabe-Feld fuer den Empfaenger.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public AbstractInput getEmpfaengerKonto() throws RemoteException
	{
		if (empfkto != null)
			return empfkto;

		ListDialog d = new ListDialog(Settings.getDatabase().createList(Empfaenger.class),ListDialog.POSITION_MOUSE);
		d.addColumn(i18n.tr("Name"),"name");
		d.addColumn(i18n.tr("Kontonummer"),"kontonummer");
		d.addColumn(i18n.tr("BLZ"),"blz");
		d.setTitle(i18n.tr("Auswahl des Empf�ngers"));
		d.addListener(new EmpfaengerListener());

		empfkto = new SearchInput(getUeberweisung().getEmpfaengerKonto(),d);
		return empfkto;
	}

	/**
	 * Liefert das Eingabe-Feld fuer die BLZ.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public AbstractInput getEmpfaengerBlz() throws RemoteException
	{
		if (empfblz != null)
			return empfblz;
		empfblz = new TextInput(getUeberweisung().getEmpfaengerBlz());
		return empfblz;
	}

	/**
	 * Liefert das Eingabe-Feld fuer den Empfaenger-Namen.
   * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public AbstractInput getEmpfaengerName() throws RemoteException
	{
		if (empfName != null)
			return empfName;
		empfName = new TextInput(getUeberweisung().getEmpfaengerName());
		return empfName;
	}

	/**
	 * Liefert das Eingabe-Feld fuer den Verwendungszweck.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public AbstractInput getZweck() throws RemoteException
	{
		if (zweck != null)
			return zweck;
		zweck = new TextInput(getUeberweisung().getZweck());
		return zweck;
	}

	/**
	 * Liefert das Eingabe-Feld fuer den "weiteren" Verwendungszweck.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public AbstractInput getZweck2() throws RemoteException
	{
		if (zweck2 != null)
			return zweck2;
		zweck2 = new TextInput(getUeberweisung().getZweck2());
		return zweck2;
	}

	/**
	 * Liefert das Eingabe-Feld fuer den Betrag.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public AbstractInput getBetrag() throws RemoteException
	{
		if (betrag != null)
			return betrag;
		betrag = new DecimalInput(getUeberweisung().getBetrag(),HBCI.DECIMALFORMAT);
		new KontoListener().handleEvent(null);
		return betrag;
	}

	/**
	 * Liefert das Eingabe-Feld fuer den Termin.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public AbstractInput getTermin() throws RemoteException
	{
		if (termin != null)
			return termin;
			// TODO
//		termin = new DateInput();
		return termin;
	}

	/**
	 * Liefert eine CheckBox ueber die ausgewaehlt werden kann,
	 * ob der Empfaenger mitgespeichert werden soll.
   * @return CheckBox.
   * @throws RemoteException
   */
  public CheckboxInput getStoreEmpfaenger() throws RemoteException
	{
		if (storeEmpfaenger != null)
			return storeEmpfaenger;

		// Nur bei neuen Ueberweisungen aktivieren
		storeEmpfaenger = new CheckboxInput(getUeberweisung().isNewObject());
		return storeEmpfaenger;
	}

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleDelete()
   */
  public void handleDelete() {
		try {
			YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
			d.setTitle(i18n.tr("Sicher?"));
			d.setText(i18n.tr("Wollen Sie die �berweisung wirklich l�schen?"));
			if (!((Boolean) d.open()).booleanValue())
				return;
			getUeberweisung().delete();
			GUI.getStatusBar().setSuccessText(i18n.tr("�berweisung gel�scht."));
		}
		catch (Exception e)
		{
			Application.getLog().error("error while deleting ueberweisung",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim L�schen der �berweisung."));
		}
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCancel()
   */
  public void handleCancel() {
		// GUI.startView(UeberweisungListe.class.getName(),null);
		GUI.startPreviousView();
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleStore()
   */
  public void handleStore()
  {
		stored = false;
  	try {

			getUeberweisung().transactionBegin();

  		getUeberweisung().setBetrag(((Double)getBetrag().getValue()).doubleValue());
  		getUeberweisung().setKonto((Konto)getKontoAuswahl().getValue());
  		getUeberweisung().setZweck((String)getZweck().getValue());
			getUeberweisung().setZweck2((String)getZweck2().getValue());

			String kto  = ((SearchInput) getEmpfaengerKonto()).getText();
			String blz  = (String)getEmpfaengerBlz().getValue();
			String name = (String)getEmpfaengerName().getValue();

			getUeberweisung().setEmpfaengerKonto(kto);
			getUeberweisung().setEmpfaengerBlz(blz);
			getUeberweisung().setEmpfaengerName(name);
			getUeberweisung().store();

			Boolean store = (Boolean) getStoreEmpfaenger().getValue();
			if (store.booleanValue())
			{

				// wir checken erstmal, ob wir den schon haben.
				DBIterator list = Settings.getDatabase().createList(Empfaenger.class);
				list.addFilter("kontonummer = '" + kto + "'");
				list.addFilter("blz = '" + blz + "'");
				if (list.hasNext())
				{
					YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
					d.setTitle(i18n.tr("Empf�nger existiert"));
					d.setText(i18n.tr("Ein Empf�nger mit dieser Kontonummer und BLZ existiert bereits. " +
							"M�chten Sie den Empf�nger dennoch zum Adressbuch hinzuf�gen?"));
					if (!((Boolean) d.open()).booleanValue()) return;
				}
				Empfaenger e = (Empfaenger) Settings.getDatabase().createObject(Empfaenger.class,null);
				e.setBLZ(blz);
				e.setKontonummer(kto);
				e.setName(name);
				e.store();
				GUI.getStatusBar().setSuccessText(i18n.tr("�berweisung und Adresse gespeichert"));
			}
			else {
				GUI.getStatusBar().setSuccessText(i18n.tr("�berweisung gespeichert"));
			}
			getUeberweisung().transactionCommit();
			stored = true;
  	}
  	catch (ApplicationException e)
  	{
			try {
				getUeberweisung().transactionRollback();
			}
			catch (RemoteException re)
			{
				Application.getLog().error("rollback failed",re);
			}
  		GUI.getView().setErrorText(i18n.tr(e.getMessage()));
  	}
  	catch (Exception e2)
  	{
			try {
				getUeberweisung().transactionRollback();
			}
			catch (RemoteException re)
			{
				Application.getLog().error("rollback failed",re);
			}
  		Application.getLog().error("error while storing ueberweisung",e2);
  		GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern der �berweisung"));
  	}
  }

	/**
   * Speichert die Ueberweisung und fuehrt sie sofort aus.
   */
  public void handleExecute()
	{
		handleStore();

		if (!stored)
			return;

		GUI.getStatusBar().startProgress();
		GUI.getStatusBar().setSuccessText(i18n.tr("F�hre �berweisung aus..."));

		GUI.startSync(new Runnable() {
			public void run() {
				try {
					getUeberweisung().execute();
					GUI.getStatusBar().setSuccessText(i18n.tr("...�berweisung erfolgreich ausgef�hrt."));
				}
				catch (ApplicationException e)
				{
					GUI.getView().setErrorText(i18n.tr(e.getMessage()));
				}
				catch (RemoteException e)
				{
					Application.getLog().error("error while executing ueberweisung",e);
					GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausf�hren der �berweisung."));
				}
			}
		});
		GUI.getStatusBar().stopProgress();
	}

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCreate()
   */
  public void handleCreate() {
		GUI.startView(UeberweisungNeu.class.getName(),null);
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleOpen(java.lang.Object)
   */
  public void handleOpen(Object o) {
		GUI.startView(UeberweisungNeu.class.getName(),o);
  }

	
	/**
	 * Listener, der die Auswahl des Kontos ueberwacht und die Waehrungsbezeichnung
	 * hinter dem Betrag abhaengig vom ausgewaehlten Konto anpasst.
   */
  private class KontoListener implements Listener
	{
		/**
		 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
		 */
		public void handleEvent(Event event) {
			try {
				konto = (Konto) getKontoAuswahl().getValue();
				String b = getKonto().getBezeichnung();
				kontoAuswahl.setComment(b == null ? "" : b);
				betrag.setComment(getKonto().getWaehrung());
			}
			catch (RemoteException er)
			{
				Application.getLog().error("error while updating currency",er);
				GUI.getStatusBar().setErrorText(i18n.tr("Fehler bei Ermittlung der W�hrung"));
			}
		}
	}

	/**
	 * Listener, der bei Auswahl des Empfaengers die restlichen Daten vervollstaendigt.
   */
  private class EmpfaengerListener implements Listener
	{

    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event) {
			empfaenger = (Empfaenger) event.data;
			try {
				empfkto.setValue(empfaenger.getKontonummer());
				empfblz.setValue(empfaenger.getBLZ());
				empfName.setValue(empfaenger.getName());
				// Wenn der Empfaenger aus dem Adressbuch kommt, deaktivieren wir die Checkbox
				storeEmpfaenger.setValue(Boolean.FALSE);
			}
			catch (RemoteException er)
			{
				Application.getLog().error("error while choosing empfaenger",er);
				GUI.getStatusBar().setErrorText(i18n.tr("Fehler bei der Auswahl des Empf�ngers"));
    	}
    }
	}
}


/**********************************************************************
 * $Log$
 * Revision 1.11  2004-04-13 23:14:22  willuhn
 * @N datadir
 *
 * Revision 1.10  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.9  2004/04/05 23:28:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/03/30 22:07:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/03/11 08:55:42  willuhn
 * @N UmsatzDetails
 *
 * Revision 1.6  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.5  2004/03/04 00:35:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/03/04 00:26:24  willuhn
 * @N Ueberweisung
 *
 * Revision 1.3  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.2  2004/02/24 22:47:04  willuhn
 * @N GUI refactoring
 *
 * Revision 1.1  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/