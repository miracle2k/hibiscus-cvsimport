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
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.controller.AbstractControl;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.views.EmpfaengerNeu;
import de.willuhn.jameica.hbci.rmi.Empfaenger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller fuer die Empfaenger-Adressen.
 */
public class EmpfaengerControl extends AbstractControl {

	// Fach-Objekte
	private Empfaenger empfaenger = null;
	// Eingabe-Felder
	private AbstractInput kontonummer = null;
	private AbstractInput blz					= null;
	private AbstractInput name				= null;

	private I18N i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
  /**
   * @param view
   */
  public EmpfaengerControl(AbstractView view) {
    super(view);
  }

	/**
	 * Liefert den Empfaenger.
	 * Existiert er nicht, wird ein neuer erzeugt.
   * @return der Empfaenger.
   * @throws RemoteException
   */
  public Empfaenger getEmpfaenger() throws RemoteException
	{
		if (empfaenger != null)
			return empfaenger;
		
		empfaenger = (Empfaenger) getCurrentObject();
		if (empfaenger != null)
			return empfaenger;

		empfaenger = (Empfaenger) Settings.getDatabase().createObject(Empfaenger.class,null);
		return empfaenger;
	}

	/**
	 * Liefert eine Tabelle mit allen vorhandenen Empfaengern.
   * @return Tabelle.
   * @throws RemoteException
   */
  public TablePart getEmpfaengerListe() throws RemoteException
	{
		DBIterator list = Settings.getDatabase().createList(Empfaenger.class);

		TablePart table = new TablePart(list,this);
		table.addColumn(i18n.tr("Kontonummer"),"kontonummer");
		table.addColumn(i18n.tr("Bankleitzahl"),"blz");
		table.addColumn(i18n.tr("Name"),"name");
		return table;
	}

	/**
	 * Liefert das Eingabe-Feld fuer die Kontonummer.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public AbstractInput getKontonummer() throws RemoteException
	{
		if (kontonummer != null)
			return kontonummer;
		kontonummer = new TextInput(getEmpfaenger().getKontonummer());
		return kontonummer;
	}

	/**
	 * Liefert das Eingabe-Feld fuer die BLZ.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public AbstractInput getBlz() throws RemoteException
	{
		if (blz != null)
			return blz;
		blz = new TextInput(getEmpfaenger().getBLZ());
		blz.setComment("");
		blz.addListener(new BLZListener());
		return blz;
	}

	/**
	 * Liefert das Eingabe-Feld fuer den Namen.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public AbstractInput getName() throws RemoteException
	{
		if (name != null)
			return name;
		name = new TextInput(getEmpfaenger().getName());
		return name;
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
  public synchronized void handleDelete() {
		try {

			if (getEmpfaenger() == null || getEmpfaenger().isNewObject())
				return;

			YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
			d.setTitle(i18n.tr("Empf�ngeradresse l�schen"));
			d.setText(i18n.tr("Wollen Sie diese Empf�ngeradresse wirklich l�schen?"));

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
			getEmpfaenger().delete();
			GUI.getStatusBar().setSuccessText(i18n.tr("Empf�ngeradresse gel�scht."));
			GUI.startPreviousView();
		}
		catch (RemoteException e)
		{
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim L�schen der Empf�ngeradresse."));
			Application.getLog().error("unable to delete empfaenger");
		}
		catch (ApplicationException ae)
		{
			GUI.getView().setErrorText(ae.getLocalizedMessage());
		}

  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCancel()
   */
  public void handleCancel() {
		// GUI.startView(EmpfaengerListe.class.getName(),null);
		GUI.startPreviousView();
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleStore()
   */
  public synchronized void handleStore() {
  	try {
  		getEmpfaenger().setKontonummer((String)getKontonummer().getValue());
  		getEmpfaenger().setBLZ((String)getBlz().getValue());
  		getEmpfaenger().setName((String)getName().getValue());
  		getEmpfaenger().store();
  		GUI.getStatusBar().setSuccessText(i18n.tr("Empf�ngeradresse gespeichert"));
  	}
  	catch (RemoteException e)
  	{
  		Application.getLog().error("error while storing empfaenger",e);
  		GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern der Adresse"));
  	}
  	catch (ApplicationException e2)
  	{
  		GUI.getView().setErrorText(e2.getLocalizedMessage());
  	}
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCreate()
   */
  public void handleCreate() {
		GUI.startView(EmpfaengerNeu.class.getName(),null);
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleOpen(java.lang.Object)
   */
  public void handleOpen(Object o) {
		GUI.startView(EmpfaengerNeu.class.getName(),o);
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
				String name = HBCIUtils.getNameForBLZ((String)getBlz().getValue());
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
 * Revision 1.12  2004-06-08 22:28:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/06/03 00:23:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/05/26 23:23:10  willuhn
 * @N neue Sicherheitsabfrage vor Ueberweisung
 * @C Check des Ueberweisungslimit
 * @N Timeout fuer Messages in Statusbars
 *
 * Revision 1.9  2004/04/13 23:14:23  willuhn
 * @N datadir
 *
 * Revision 1.8  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.7  2004/03/30 22:07:50  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/03/11 08:55:42  willuhn
 * @N UmsatzDetails
 *
 * Revision 1.5  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.4  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.3  2004/02/24 22:47:04  willuhn
 * @N GUI refactoring
 *
 * Revision 1.2  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.1  2004/02/17 01:09:45  willuhn
 * *** empty log message ***
 *
 **********************************************************************/