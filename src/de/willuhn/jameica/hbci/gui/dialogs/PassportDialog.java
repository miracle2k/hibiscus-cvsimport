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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.AbstractInput;
import de.willuhn.jameica.gui.parts.SelectInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Passport;
import de.willuhn.jameica.hbci.rmi.PassportType;
import de.willuhn.util.I18N;

/**
 * Dialog f�r die Auswahl des Passports.
 * Es muss weder Text, noch Titel oder LabelText gesetzt werden.
 * Das ist alles schon drin.
 */
public class PassportDialog extends AbstractDialog {

	private AbstractInput auswahl;
	private Passport choosen;
	
	private I18N i18n;

  /**
   * ct.
   * @param position Position des Dialogs.
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#POSITION_CENTER
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#POSITION_MOUSE
   */
  public PassportDialog(int position) {
    super(position);
		i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
    setTitle(i18n.tr("Auswahl des Mediums"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception {

    // TODO: Schoener machen (ueber Factory).
    PassportType pt = choosen.getPassportType();
		String clazz = pt.getImplementor();
		return (Passport) Settings.getDatabase().createObject(Application.getClassLoader().load(clazz),choosen.getID());
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception {
		// Composite um alles drumrum.
		Composite comp = new Composite(parent,SWT.NONE);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		comp.setLayout(new GridLayout(3,false));
		
		// Text
		CLabel label = new CLabel(comp,SWT.WRAP);
		label.setText(i18n.tr("Bitte w�hlen Sie ein Sicherheitsmedium aus."));
		GridData grid = new GridData(GridData.FILL_HORIZONTAL);
		grid.horizontalSpan = 3;
		label.setLayoutData(grid);
		
		// Label vor Eingabefeld
		CLabel pLabel = new CLabel(comp,SWT.NONE);
		pLabel.setText(i18n.tr("verf�gbare Medien"));
		pLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

		// Liste der Passport-Typen holen
		Composite c = new Composite(comp,SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		c.setLayoutData(gd);
		c.setLayout(new GridLayout(2,false));
		DBIterator list = Settings.getDatabase().createList(Passport.class);
		auswahl = new SelectInput(list,null);
		auswahl.paint(c,SWT.DEFAULT);

		// Dummy-Label damit die Buttons buendig unter dem Eingabefeld stehen
		Label dummy = new Label(comp,SWT.NONE);
		dummy.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// OK-Button
		Button button = new Button(comp, SWT.FLAT);
		button.setText(i18n.tr("OK"));
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				choosen = (Passport) auswahl.getValue();
				close();
			}
		});

		// Abbrechen-Button
		Button cancel = new Button(comp,SWT.FLAT);
		cancel.setText(i18n.tr("Abbrechen"));
		cancel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		cancel.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				throw new RuntimeException("Dialog abgebrochen");
			}
		});

		addShellListener(new ShellListener() {
			public void shellClosed(ShellEvent e) {
				throw new RuntimeException("dialog cancelled via close button");
			}
			public void shellActivated(ShellEvent e) {}
			public void shellDeactivated(ShellEvent e) {}
			public void shellDeiconified(ShellEvent e) {}
			public void shellIconified(ShellEvent e) {}
		});
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.5  2004-03-19 01:44:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/03/11 08:55:42  willuhn
 * @N UmsatzDetails
 *
 * Revision 1.3  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.2  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.1  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 * Revision 1.2  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.1  2004/02/21 19:49:04  willuhn
 * @N PINDialog
 *
 **********************************************************************/