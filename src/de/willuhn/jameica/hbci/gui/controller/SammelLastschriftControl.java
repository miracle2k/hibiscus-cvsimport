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
import java.util.Date;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.CalendarDialog;
import de.willuhn.jameica.gui.dialogs.ListDialog;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.SammelLastBuchungNew;
import de.willuhn.jameica.hbci.gui.action.SammelLastschriftNew;
import de.willuhn.jameica.hbci.gui.menus.SammelLastBuchungList;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung des Controllers fuer den Dialog "Liste der Sammellastschriften".
 * @author willuhn
 */
public class SammelLastschriftControl extends AbstractControl
{

  private SammelLastschrift lastschrift 	= null;

  private I18N i18n                     	= null;

  private TablePart table               	= null;
  private DialogInput kontoAuswahl				= null;
  private Input name                    	= null;
  private DialogInput termin            	= null;
  private Input comment                 	= null;

  /**
   * ct.
   * @param view
   */
  public SammelLastschriftControl(AbstractView view)
  {
    super(view);
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * Liefert die aktuelle Sammel-Lastschrift oder erstellt eine neue.
   * @return Sammel-Lastschrift.
   * @throws RemoteException
   */
  public SammelLastschrift getLastschrift() throws RemoteException
  {
    if (lastschrift != null)
      return lastschrift;

    if (getCurrentObject() != null)
    {
      lastschrift = (SammelLastschrift) getCurrentObject();
      return lastschrift;
    }

    lastschrift = (SammelLastschrift) Settings.getDBService().createObject(SammelLastschrift.class,null);
    return lastschrift;
  }

  /**
   * Liefert eine Tabelle mit den existierenden Sammellastschriften.
   * @return Liste der Sammellastschriften.
   * @throws RemoteException
   */
  public TablePart getListe() throws RemoteException
  {
    if (table != null)
      return table;

    table = new de.willuhn.jameica.hbci.gui.parts.SammelLastschriftList(new SammelLastschriftNew());
    return table;
  }

  /**
   * Liefert eine Liste mit den in dieser Sammel-Lastschrift enthaltenen Buchungen.
   * @return Liste der Buchungen.
   * @throws RemoteException
   */
  public TablePart getBuchungen() throws RemoteException
  {
    DBIterator list = getLastschrift().getBuchungen();

    TablePart buchungen = new TablePart(list,new SammelLastBuchungNew());
    buchungen.setFormatter(new TableFormatter() {
      public void format(TableItem item) {
        try {
          if (getLastschrift().ausgefuehrt())
          {
            item.setForeground(Color.COMMENT.getSWTColor());
          }
        }
        catch (RemoteException e) { /*ignore */}
      }
    });
    buchungen.addColumn(i18n.tr("Verwendungszweck"),"zweck");
    buchungen.addColumn(i18n.tr("Kontoinhaber"),"gegenkonto_name");
    buchungen.addColumn(i18n.tr("Kontonummer"),"gegenkonto_nr");
    buchungen.addColumn(i18n.tr("Bankleitzahl"),"gegenkonto_blz");
    Konto k = getLastschrift().getKonto();
    String curr = k != null ? k.getWaehrung() : "";
    buchungen.addColumn(i18n.tr("Betrag"),"betrag",new CurrencyFormatter(curr,HBCI.DECIMALFORMAT));
    buchungen.setContextMenu(new SammelLastBuchungList(getLastschrift()));
    return buchungen;
  }

  /**
   * Liefert ein Auswahlfeld fuer das Konto.
   * @return Auswahl-Feld.
   * @throws RemoteException
   */
  public DialogInput getKontoAuswahl() throws RemoteException
  {
    if (kontoAuswahl != null)
      return kontoAuswahl;

		ListDialog d = new ListDialog(Settings.getDBService().createList(Konto.class),ListDialog.POSITION_MOUSE);
		d.addColumn(i18n.tr("Bezeichnung"),"bezeichnung");
		d.addColumn(i18n.tr("Kontonummer"),"kontonummer");
		d.addColumn(i18n.tr("BLZ"),"blz");
		d.setTitle(i18n.tr("Auswahl des Kontos"));
		d.addCloseListener(new Listener()
    {
      public void handleEvent(Event event)
      {
				if (event == null || event.data == null)
					return;
				Konto konto = (Konto) event.data;

				try {
					String b = konto.getBezeichnung();
					getKontoAuswahl().setText(konto.getKontonummer());
					getKontoAuswahl().setComment(b == null ? "" : b);
				}
				catch (RemoteException er)
				{
					Logger.error("error while updating currency",er);
					GUI.getStatusBar().setErrorText(i18n.tr("Fehler bei der Auswahl des Kontos"));
				}
      }
    });

		Konto k = getLastschrift().getKonto();
		kontoAuswahl = new DialogInput(k == null ? "" : k.getKontonummer(),d);
		kontoAuswahl.setComment(k == null ? "" : k.getBezeichnung());
		kontoAuswahl.disableClientControl();
		kontoAuswahl.setValue(k);

		return kontoAuswahl;
  }

  /**
   * Liefert das Eingabe-Feld fuer den Termin.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public DialogInput getTermin() throws RemoteException
  {
    final Terminable bu = (Terminable) getLastschrift();

    if (termin != null)
      return termin;
    CalendarDialog cd = new CalendarDialog(CalendarDialog.POSITION_MOUSE);
    cd.setTitle(i18n.tr("Termin"));
    cd.addCloseListener(new Listener() {
      public void handleEvent(Event event) {
        if (event == null || event.data == null)
          return;
        Date choosen = (Date) event.data;
        termin.setText(HBCI.DATEFORMAT.format(choosen));

        try {
          // Wenn das neue Datum spaeter als das aktuelle ist,
          // nehmen wir den Kommentar weg
          if (bu.ueberfaellig() && choosen.after(new Date()));
            getComment().setValue("");
          if (choosen.before(new Date()))
            getComment().setValue(i18n.tr("Der Auftrag ist �berf�llig."));
        }
        catch (RemoteException e) {/*ignore*/}
      }
    });

    Date d = bu.getTermin();
    if (d == null)
      d = new Date();
    cd.setDate(d);
    termin = new DialogInput(HBCI.DATEFORMAT.format(d),cd);
    termin.disableClientControl();
    termin.setValue(d);

    if (bu.ausgefuehrt())
      termin.disable();

    return termin;
  }

  /**
   * Liefert ein Kommentar-Feld zu dieser Ueberweisung.
   * @return Kommentarfeld.
   * @throws RemoteException
   */
  public Input getComment() throws RemoteException
  {
    if (comment != null)
      return comment;
    comment = new LabelInput("");
    Terminable bu = (Terminable) getLastschrift();
    if (bu.ausgefuehrt())
    {
      comment.setValue(i18n.tr("Der Auftrag wurde bereits ausgef�hrt"));
    }
    else if (bu.ueberfaellig())
    {
      comment.setValue(i18n.tr("Der Auftrag ist �berf�llig"));
    }
    return comment;
  }

  /**
   * Liefert ein Eingabe-Feld fuer den Namen der Sammellastschrift.
   * @return Name der Sammel-Lastschrift.
   * @throws RemoteException
   */
  public Input getName() throws RemoteException
  {
    if (name != null)
      return name;
    name = new TextInput(getLastschrift().getBezeichnung());
    if (getLastschrift().ausgefuehrt())
      name.disable();
    return name;
  }
  
  /**
   * Speichert die Sammellastschrift.
   */
  public synchronized void handleStore()
  {
    try {
      getLastschrift().setKonto((Konto)getKontoAuswahl().getValue());
      getLastschrift().setBezeichnung((String)getName().getValue());
      getLastschrift().setTermin((Date)getTermin().getValue());
      getLastschrift().store();
      GUI.getStatusBar().setSuccessText(i18n.tr("Sammel-Lastschrift gespeichert"));
    }
    catch (ApplicationException e2)
    {
      GUI.getView().setErrorText(e2.getMessage());
    }
    catch (RemoteException e)
    {
      Logger.error("error while storing sammellastschrift",e);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern der Sammel-Lastschrift"));
    }
  }
}

/*****************************************************************************
 * $Log$
 * Revision 1.6  2005-05-02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 * Revision 1.5  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.4  2005/03/02 00:22:05  web0
 * @N first code for "Sammellastschrift"
 *
 * Revision 1.3  2005/03/01 18:51:04  web0
 * @N Dialoge fuer Sammel-Lastschriften
 *
 * Revision 1.2  2005/02/28 18:40:49  web0
 * @N first code for "Sammellastschrift"
 *
 * Revision 1.1  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
*****************************************************************************/