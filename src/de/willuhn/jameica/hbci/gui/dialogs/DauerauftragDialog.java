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
package de.willuhn.jameica.hbci.gui.dialogs;

import java.util.Date;

import org.eclipse.swt.widgets.Composite;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.server.TurnusHelper;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Oeffnet einen Dialog und zeigt den uebergebenen Dauerauftrag an.
 * Wird verwendet, wenn ein Dauerauftrag ausgefuehrt werden
 * soll - dann wird vorher diese Sicherheitsabfrage eingeblendet, die
 * nochmal die Details des Dauerauftrags anzeigt. Erst wenn der User
 * hier OK klickt, wird der Daueruftrag ausgefuehrt.
 */
public class DauerauftragDialog extends AbstractDialog {

	private I18N i18n;
	private Dauerauftrag auftrag;
	private Boolean choosen = Boolean.FALSE;

  /**
   * ct.
   * @param d anzuzeigender Dauerauftrag.
   * @param position
   */
  public DauerauftragDialog(Dauerauftrag d, int position) {
    super(position);

		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    this.auftrag = d;
    this.setTitle(i18n.tr("Sicher?"));
    
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception {
    return choosen;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception {

		LabelGroup group = new LabelGroup(parent,i18n.tr("Details des Dauerauftrages"));
			
		if (auftrag.isActive())
			group.addText(i18n.tr("Sind Sie sicher, da� Sie diese �nderungen jetzt zur Bank senden wollen?") + "\n",true);
		else
			group.addText(i18n.tr("Sind Sie sicher, da� Sie diesen Dauerauftrag jetzt ausf�hren wollen?") + "\n",true);
		
		Input kto = new LabelInput(auftrag.getKonto().getKontonummer());
		kto.setComment(auftrag.getKonto().getBezeichnung());
		group.addLabelPair(i18n.tr("Eigenes Konto"),kto);

		group.addSeparator();

		Input empfName = new LabelInput(auftrag.getGegenkontoName());
		group.addLabelPair(i18n.tr("Name des Empf�nger"),empfName);

		Input empfKto = new LabelInput(auftrag.getGegenkontoNummer());
		empfKto.setComment(auftrag.getGegenkontoBLZ() + "/" + HBCIUtils.getNameForBLZ(auftrag.getGegenkontoBLZ()));
		group.addLabelPair(i18n.tr("Konto des Empf�ngers"),empfKto);

		group.addSeparator();

    Input betrag = new LabelInput(HBCI.DECIMALFORMAT.format(auftrag.getBetrag()) + " " + auftrag.getKonto().getWaehrung());
    group.addLabelPair(i18n.tr("Betrag"),betrag);

    group.addSeparator();

    Date e = auftrag.getErsteZahlung();
    String se = i18n.tr("Zum n�chstm�glichen Termin");
    if (e != null) se = HBCI.DATEFORMAT.format(e);
    Input ersteZahlung = new LabelInput(se);
    group.addLabelPair(i18n.tr("Erste Zahlung"),ersteZahlung);

    Date l = auftrag.getLetzteZahlung();
    String sl = i18n.tr("keine End-Datum vorgegeben");
    if (l != null) sl = HBCI.DATEFORMAT.format(l);
    Input letzteZahlung = new LabelInput(sl);
    group.addLabelPair(i18n.tr("Letzte Zahlung"),letzteZahlung);

    Input turnus = new LabelInput(TurnusHelper.createBezeichnung(auftrag.getTurnus()));
    group.addLabelPair(i18n.tr("Zahlungsturnus"),turnus);

    // BUGZILLA 32 http://www.willuhn.de/bugzilla/show_bug.cgi?id=32
    LabelGroup zweck = new LabelGroup(parent,i18n.tr("Verwendungszweck"));
    zweck.addText(auftrag.getZweck(),true);
    String z2 = auftrag.getZweck2();
    if (z2 != null && z2.length() > 0)
    {
      zweck.addSeparator();
      zweck.addText(z2,true);
    }


    ButtonArea b = new ButtonArea(parent,2);
		b.addButton(i18n.tr("Jetzt ausf�hren"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
				choosen = Boolean.TRUE;
				close();
      }
    });
		b.addButton(i18n.tr("Abbrechen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
				choosen = Boolean.FALSE;
				close();
      }
    });
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.5  2005-04-05 22:49:02  web0
 * @B bug 32
 *
 * Revision 1.4  2005/03/02 17:59:31  web0
 * @N some refactoring
 *
 * Revision 1.3  2005/03/01 00:38:27  web0
 * *** empty log message ***
 *
 * Revision 1.2  2004/10/25 23:12:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 **********************************************************************/