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
package de.willuhn.jameica.hbci.gui.views;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.parts.PanelButtonPrint;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.DBObjectDelete;
import de.willuhn.jameica.hbci.gui.action.UeberweisungDuplicate;
import de.willuhn.jameica.hbci.gui.action.UeberweisungExecute;
import de.willuhn.jameica.hbci.gui.controller.UeberweisungControl;
import de.willuhn.jameica.hbci.io.print.PrintSupportUeberweisung;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bearbeitung der Ueberweisungen.
 */
public class UeberweisungNew extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();


  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

		final UeberweisungControl control = new UeberweisungControl(this);
    final Ueberweisung transfer = (Ueberweisung) control.getTransfer();

		GUI.getView().setTitle(i18n.tr("�berweisung bearbeiten"));
		GUI.getView().addPanelButton(new PanelButtonPrint(new PrintSupportUeberweisung(transfer)));

    Container container = new SimpleContainer(getParent());
    container.addHeadline(i18n.tr("Konto"));
    container.addInput(control.getKontoAuswahl());

    container.addHeadline(i18n.tr("Empf�nger"));
    container.addInput(control.getEmpfaengerName());
    container.addInput(control.getEmpfaengerKonto());    
    container.addInput(control.getEmpfaengerBlz());    
    container.addCheckbox(control.getStoreEmpfaenger(),i18n.tr("In Adressbuch �bernehmen"));

    container.addHeadline(i18n.tr("Details"));
    container.addInput(control.getZweck());
    container.addInput(control.getZweck2());
    container.addInput(control.getBetrag());
    container.addInput(control.getTextSchluessel());
    container.addInput(control.getTyp());
    container.addInput(control.getTermin());

		ButtonArea buttonArea = new ButtonArea();
		buttonArea.addButton(i18n.tr("L�schen"),new DBObjectDelete(),transfer,false,"user-trash-full.png");
    buttonArea.addButton(i18n.tr("Duplizieren..."), new UeberweisungDuplicate(),transfer,false,"edit-copy.png");

    Button execute = new Button(i18n.tr("Jetzt ausf�hren..."), new Action() {
      public void handleAction(Object context) throws ApplicationException {
				if (control.handleStore()) // BUGZILLA 661
  				new UeberweisungExecute().handleAction(transfer);
      }
    },null,false,"emblem-important.png");
    execute.setEnabled(!transfer.ausgefuehrt());
    
    Button store = new Button(i18n.tr("Speichern"), new Action() {
      public void handleAction(Object context) throws ApplicationException {
      	control.handleStore();
      }
    },null,!transfer.ausgefuehrt(),"document-save.png");
    store.setEnabled(!transfer.ausgefuehrt());
    
    buttonArea.addButton(execute);
    buttonArea.addButton(store);
    
    buttonArea.paint(getParent());
  }
}


/**********************************************************************
 * $Log$
 * Revision 1.27  2011-04-08 15:19:14  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 * Revision 1.26  2011-04-08 13:38:44  willuhn
 * @N Druck-Support fuer Einzel-Ueberweisungen. Weitere werden folgen.
 *
 * Revision 1.25  2010-08-17 11:41:45  willuhn
 * @N Duplizieren-Button auch in der Detail-Ansicht
 *
 * Revision 1.24  2010-08-17 11:32:11  willuhn
 * @C Code-Cleanup
 *
 * Revision 1.23  2009-05-12 22:53:33  willuhn
 * @N BUGZILLA 189 - Ueberweisung als Umbuchung
 *
 * Revision 1.22  2009/05/06 23:11:23  willuhn
 * @N Mehr Icons auf Buttons
 *
 * Revision 1.21  2009/02/24 23:51:01  willuhn
 * @N Auswahl der Empfaenger/Zahlungspflichtigen jetzt ueber Auto-Suggest-Felder
 *
 * Revision 1.20  2009/01/20 10:51:46  willuhn
 * @N Mehr Icons - fuer Buttons
 **********************************************************************/