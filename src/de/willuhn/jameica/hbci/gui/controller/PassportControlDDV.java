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

import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.controller.AbstractControl;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.views.PassportDetails;
import de.willuhn.jameica.hbci.rmi.PassportDDV;
import de.willuhn.jameica.hbci.rmi.hbci.PassportHandle;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller, der die Eingaben zur Konfiguration des Passports handelt.
 */
public class PassportControlDDV extends AbstractControl {

	// Fachobjekte
	private PassportDDV passport = null;

	// Eingabe-Felder
	private AbstractInput name 			 = null; 
	private AbstractInput type			 = null;
	private AbstractInput model			 = null;
	private AbstractInput port 			 = null;
	private AbstractInput ctNumber	 = null;
	private AbstractInput entryIndex = null;

	private CheckboxInput useBio 			= null;
	private CheckboxInput useSoftPin 	= null; 
	
	private I18N i18n;
  /**
   * ct.
   * @param view
   */
  public PassportControlDDV(AbstractView view) {
    super(view);
		i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
  }

	/**
	 * Liefert den Passport.
   * @return Passport.
   * @throws RemoteException
   */
  public PassportDDV getPassport() throws RemoteException
	{
		if (passport != null)
			return passport;
		
		passport = (PassportDDV) getCurrentObject();

		if (passport == null)
		{
			GUI.getStatusBar().setErrorText(i18n.tr("Ausgew�hltes Sicherheitsmedium wurde nicht gefunden"));
			throw new RemoteException("passport not found");
		}
		return passport;
	}

	/**
	 * Liefert eine Auswahl der unterstuetzten Chipkartenleser.
   * @return Auswahl-Feld.
   * @throws RemoteException
   */
  public AbstractInput getModel() throws RemoteException
	{
		if (model != null)
			return model;
		// TODO: unterstuetzte Modelle
		return null;
	}

	/**
	 * Liefert das Eingabe-Feld fuer den Port.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public AbstractInput getPort() throws RemoteException
	{
		if (port != null)
			return port;

		port = new SelectInput(PassportDDV.PORTS,""+PassportDDV.PORTS[getPassport().getPort()]);
		port.setComment(i18n.tr("meist COM1 oder COM2"));
		return port;
	}		

	/**
	 * Liefert das Eingabe-Feld fuer die Nummer des Lesers.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public AbstractInput getCTNumber() throws RemoteException
	{
		if (ctNumber != null)
			return ctNumber;

		ctNumber = new TextInput(""+getPassport().getCTNumber());
		ctNumber.setComment(i18n.tr("meist 0"));
		return ctNumber;
	}

	/**
	 * Liefert das Eingabe-Feld fuer den Namen des Lesers.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public AbstractInput getName() throws RemoteException
	{
		if (name != null)
			return name;

		name = new TextInput(getPassport().getName());
		return name;
	}

	/**
	 * Liefert das Eingabe-Feld fuer den Typ des Passports.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public AbstractInput getType() throws RemoteException
	{
		if (type != null)
			return type;

		type = new LabelInput(getPassport().getPassportType().getName());
		return type;
	}

	/**
	 * Liefert das Eingabe-Feld fuer die Index-Nummer des HBCI-Zugangs.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public AbstractInput getEntryIndex() throws RemoteException
	{
		if (entryIndex != null)
			return entryIndex;

		entryIndex = new TextInput(""+getPassport().getEntryIndex());
		entryIndex.setComment(i18n.tr("meist 1"));
		return entryIndex;
	}

	/**
	 * Liefert die Checkbox fuer die Auswahl biometrischer Verfahren.
   * @return Checkbox.
   * @throws RemoteException
   */
  public CheckboxInput getBio() throws RemoteException
	{
		if (useBio != null)
			return useBio;

		useBio = new CheckboxInput(getPassport().useBIO());
		return useBio;
	}

	/**
	 * Liefert die Checkbox fuer die Auswahl der Tastatur als PIN-Eingabe.
	 * @return Checkbox.
	 * @throws RemoteException
	 */
	public CheckboxInput getSoftPin() throws RemoteException
	{
		if (useSoftPin != null)
			return useSoftPin;

		useSoftPin = new CheckboxInput(getPassport().useSoftPin());
		return useSoftPin;
	}


  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleDelete()
   */
  public void handleDelete() {
		
  	try
  	{
			YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
			d.setTitle(i18n.tr("Medium l�schen?"));
			d.setText(i18n.tr("Sind Sie sicher, da� Sie das Sicherheitsmedium l�schen m�chten?"));
			Boolean b = (Boolean) d.open();
			if (!b.booleanValue())
				return;

  		getPassport().delete();
			GUI.getStatusBar().setSuccessText(i18n.tr("Medium gel�scht"));
  	}
  	catch (ApplicationException e2)
  	{
			GUI.getView().setErrorText(i18n.tr(e2.getMessage()));
  	}
		catch (Exception e)
		{
			Application.getLog().error("error while deleting passport",e);
		}
  }
  
  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCancel()
   */
  public void handleCancel() {
		// GUI.startView(Settings.class.getName(),null);
		GUI.startPreviousView();
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleStore()
   */
  public void handleStore() {

  	try {
			try {
				getPassport().setCTNumber(Integer.parseInt((String)getCTNumber().getValue()));
			}
			catch (NumberFormatException e)
			{
				GUI.getView().setErrorText(i18n.tr("Bitte geben Sie im Feld \"Nummer des Chipkartenlesers\" eine g�ltige Zahl ein."));
				return;
			}

			try {
				getPassport().setEntryIndex(Integer.parseInt((String)getEntryIndex().getValue()));
			}
			catch (NumberFormatException e)
			{
				GUI.getView().setErrorText(i18n.tr("Bitte geben Sie im Feld \"Index des HBCI-Zugangs\" eine g�ltige Zahl ein."));
				return;
			}

			for (int i=0;i<PassportDDV.PORTS.length;++i)
			{
				if (PassportDDV.PORTS[i].equals(getPort().getValue()))
					getPassport().setPort(i);
			}

			getPassport().setBIO(((Boolean)getBio().getValue()).booleanValue());
			getPassport().setSoftPin(((Boolean)getSoftPin().getValue()).booleanValue());
			getPassport().setName((String)getName().getValue());
			
			getPassport().store();

			GUI.getStatusBar().setSuccessText(i18n.tr("Einstellungen gespeichert"));
  	}
  	catch (ApplicationException e)
  	{
			GUI.getView().setErrorText(i18n.tr(e.getMessage()));
  	}
  	catch (RemoteException e)
  	{
  		Application.getLog().error("error while storing params",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern der Einstellungen"));
  	}
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCreate()
   */
  public void handleCreate() {
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleOpen(java.lang.Object)
   */
  public void handleOpen(Object o) {
  	GUI.startView(PassportDetails.class.getName(),o);
  }

	/**
   * Testet die Einstellungen.
   */
  public void handleTest()
	{

		// Speichern, damit sicher ist, dass wir vernuenftige Daten fuer den
		// Test haben und die auch gespeichert sind
		handleStore();

		GUI.getStatusBar().setSuccessText(i18n.tr("Teste Chipkartenleser..."));

		GUI.startSync(new Runnable() {
      public void run() {
				try {
					PassportHandle handle = getPassport().getHandle();
					handle.open();
					handle.close(); // nein, nicht im finally, denn wenn das Oeffnen
																 // fehlschlaegt, ist nichts zum Schliessen da ;)
					GUI.getStatusBar().setSuccessText(i18n.tr("Chipkartenleser erfolgreich getestet."));
				}
				catch (RemoteException e)
				{
					GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Testen des Chipkartenlesers."));
					Application.getLog().debug("error while testing chipcard reader: " + e.getLocalizedMessage());
				}
      }
    });
	}
}


/**********************************************************************
 * $Log$
 * Revision 1.16  2004-04-25 18:17:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2004/04/19 22:05:52  willuhn
 * @C HBCIJobs refactored
 *
 * Revision 1.14  2004/04/13 23:14:23  willuhn
 * @N datadir
 *
 * Revision 1.13  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.12  2004/03/30 22:07:50  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/03/11 08:55:42  willuhn
 * @N UmsatzDetails
 *
 * Revision 1.10  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.9  2004/03/04 00:26:24  willuhn
 * @N Ueberweisung
 *
 * Revision 1.8  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.7  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 * Revision 1.6  2004/02/24 22:47:05  willuhn
 * @N GUI refactoring
 *
 * Revision 1.5  2004/02/23 20:30:47  willuhn
 * @C refactoring in AbstractDialog
 *
 * Revision 1.4  2004/02/20 01:36:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/02/13 00:41:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/12 23:46:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/12 00:38:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 **********************************************************************/