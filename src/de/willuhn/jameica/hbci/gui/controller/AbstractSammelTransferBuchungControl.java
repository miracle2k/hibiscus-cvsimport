/*****************************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 * $Locker$
 * $State$
 *
****************************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.ListDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Adresse;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Abstrakter Controller fuer die Dialoge "Buchung einer Sammellastschrift/-ueberweisung bearbeiten".
 * @author willuhn
 */
public abstract class AbstractSammelTransferBuchungControl extends AbstractControl
{

	// Fach-Objekte
	private Adresse gegenKonto							= null;
	
	// Eingabe-Felder
	private Input betrag										= null;
	private TextInput zweck									= null;
	private TextInput zweck2								= null;

	private DialogInput gkNummer 						= null;
	private Input gkName 										= null;
	private Input gkBLZ	 										= null;

	private CheckboxInput storeAddress		 	= null;

	private I18N i18n                       = null;

  /**
   * ct.
   * @param view
   */
  public AbstractSammelTransferBuchungControl(AbstractView view)
  {
    super(view);
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

	/**
	 * Liefert die aktuelle Buchung.
   * @return die Buchung.
   */
  public abstract SammelTransferBuchung getBuchung();

	/**
	 * Liefert das Eingabe-Feld fuer das Gegenkonto.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public DialogInput getGegenKonto() throws RemoteException
	{
		if (gkNummer != null)
			return gkNummer;

		ListDialog d = new ListDialog(Settings.getDBService().createList(Adresse.class),ListDialog.POSITION_MOUSE);
		d.addColumn(i18n.tr("Name"),"name");
		d.addColumn(i18n.tr("Kontonummer"),"kontonummer");
		d.addColumn(i18n.tr("BLZ"),"blz");
		d.setTitle(i18n.tr("Auswahl des Gegenkontos"));
		d.addCloseListener(new GegenkontoListener());

		gkNummer = new DialogInput(getBuchung().getGegenkontoNummer(),d);

		return gkNummer;
	}

	/**
	 * Liefert das Eingabe-Feld fuer die BLZ.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getGegenkontoBLZ() throws RemoteException
	{
		if (gkBLZ != null)
			return gkBLZ;
		gkBLZ = new TextInput(getBuchung().getGegenkontoBLZ(),HBCIProperties.HBCI_BLZ_LENGTH);

		gkBLZ.setComment("");
		gkBLZ.addListener(new BLZListener());
		return gkBLZ;
	}

	/**
	 * Liefert das Eingabe-Feld fuer den Namen des Kontoinhabers des Gegenkontos.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getGegenkontoName() throws RemoteException
	{
		if (gkName != null)
			return gkName;
		gkName = new TextInput(getBuchung().getGegenkontoName(),HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH);
		return gkName;
	}

	/**
	 * Liefert das Eingabe-Feld fuer den Verwendungszweck.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getZweck() throws RemoteException
	{
		if (zweck != null)
			return zweck;
		zweck = new TextInput(getBuchung().getZweck(),HBCIProperties.HBCI_TRANSFER_USAGE_MAXLENGTH);
		zweck.setValidChars(HBCIProperties.HBCI_DTAUS_VALIDCHARS);
		return zweck;
	}

	/**
	 * Liefert das Eingabe-Feld fuer den "weiteren" Verwendungszweck.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getZweck2() throws RemoteException
	{
		if (zweck2 != null)
			return zweck2;
		zweck2 = new TextInput(getBuchung().getZweck2(),HBCIProperties.HBCI_TRANSFER_USAGE_MAXLENGTH);
		zweck2.setValidChars(HBCIProperties.HBCI_DTAUS_VALIDCHARS);
		return zweck2;
	}


	/**
	 * Liefert das Eingabe-Feld fuer den Betrag.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getBetrag() throws RemoteException
	{
		if (betrag != null)
			return betrag;
		betrag = new DecimalInput(getBuchung().getBetrag(),HBCI.DECIMALFORMAT);

		// wir loesen den KontoListener aus, um die Waehrung sofort anzuzeigen
		
		try
		{
			String curr = getBuchung().getSammelTransfer().getKonto().getWaehrung();
			if (curr != null)
				betrag.setComment(curr);
		}
		catch (Exception e)
		{
			Logger.error("error while reading currency name",e);
		}
		return betrag;
	}

	/**
	 * Liefert eine CheckBox ueber die ausgewaehlt werden kann,
	 * ob die Adresse (Gegenkonto) mitgespeichert werden soll.
	 * @return CheckBox.
	 * @throws RemoteException
	 */
	public CheckboxInput getStoreAddress() throws RemoteException
	{
		if (storeAddress != null)
			return storeAddress;

		// Nur bei neuen Buchungen aktivieren
		storeAddress = new CheckboxInput(getBuchung().isNewObject());

		return storeAddress;
	}

  /**
	 * Speichert den Geld-Transfer.
   * @param next legt fest, ob nach dem Speichern gleich zur naechsten Buchung gesprungen werden soll.
	 */
	public abstract void handleStore(boolean next);

	/**
	 * Listener, der den Namen des Geldinstitutes bei BLZ-Auswahl dranschreibt.
	 */
	private class BLZListener implements Listener
	{
		/**
		 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
		 */
		public void handleEvent(Event event) {
			String name = HBCIUtils.getNameForBLZ((String)gkBLZ.getValue());
			gkBLZ.setComment(name);
		}
	}

	/**
	 * Listener, der bei Auswahl des Gegenkontos die restlichen Daten vervollstaendigt.
	 */
	private class GegenkontoListener implements Listener
	{

		/**
		 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
		 */
		public void handleEvent(Event event) {
			if (event == null)
				return;
			gegenKonto = (Adresse) event.data;
			if (gegenKonto == null)
				return;
			try {
				getGegenKonto().setText(gegenKonto.getKontonummer());
				getGegenkontoBLZ().setValue(gegenKonto.getBLZ());
				getGegenkontoName().setValue(gegenKonto.getName());
				// Wenn die Adresse aus dem Adressbuch kommt, deaktivieren wir die Checkbox
				getStoreAddress().setValue(Boolean.FALSE);
				
				// und jetzt noch das Geld-Institut dranpappen
				new BLZListener().handleEvent(null);
			}
			catch (RemoteException er)
			{
				Logger.error("error while choosing address",er);
				GUI.getStatusBar().setErrorText(i18n.tr("Fehler bei der Auswahl der Adresse"));
			}
		}
	}

}

/*****************************************************************************
 * $Log$
 * Revision 1.1  2005-09-30 00:08:51  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
*****************************************************************************/