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

import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaSizeName;

import org.eclipse.swt.widgets.Composite;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.passport.INILetter;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog, der den neu erzeugten Schluessel anzeigt und den Benutzer
 * auffordert, den Ini-Brief an seine Bank zu senden.
 */
public class NewKeysDialog extends AbstractDialog
{

	private final static DocFlavor DOCFLAVOR = DocFlavor.STRING.TEXT_PLAIN;
	private final static PrintRequestAttributeSet PRINTPROPS = new HashPrintRequestAttributeSet();

	private HBCIPassport passport;
	private INILetter iniletter;
	private I18N i18n;
	
	private Input printerList = null;

	static
	{
		PRINTPROPS.add(MediaSizeName.ISO_A4);
	}

  /**
   * @param p
   */
  public NewKeysDialog(HBCIPassport p)
  {
    super(NewKeysDialog.POSITION_CENTER);
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
		setTitle(i18n.tr("Ini-Brief erzeugen"));


		this.passport = p;
		iniletter = new INILetter(passport,INILetter.TYPE_USER);

  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
		LabelGroup group = new LabelGroup(parent,i18n.tr("Ini-Brief"));
		group.addText(i18n.tr(
      "Bitte drucken Sie den Ini-Brief aus und senden Ihn an Ihre Bank.\n" +      "Nach der Freischaltung durch Ihr Geldinstitut kann dieser Schl�ssel\n" +      "verwendet werden."),true);

		group.addLabelPair(i18n.tr("Schl�ssel-Hashwert"),new LabelInput(HBCIUtils.data2hex(iniletter.getKeyHash())));
		group.addLabelPair(i18n.tr("Drucker-Auswahl:"),getPrinterList());

		ButtonArea buttons = new ButtonArea(parent,2);
		buttons.addButton(i18n.tr("Drucken"),new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
				print();
			}
		},null,true);
		buttons.addButton(i18n.tr("Schliessen"), new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
				close();
			}
		});
  }

	/**
	 * Druckt den Ini-Brief aus.
   * @throws ApplicationException
   */
  private void print() throws ApplicationException
	{
		try
		{
			Printer p = (Printer) getPrinterList().getValue();
			if (p == null)
				throw new ApplicationException(i18n.tr("Kein Drucker gefunden."));

			PrintService service = p.service;

			DocPrintJob pj = service.createPrintJob();

			StringBuffer text = new StringBuffer();
			text.append(i18n.tr("INI-Brief") + "\n");
			text.append("---------------------------------------------------------------------------\n");
			text.append(i18n.tr("Schl�ssel-Hashwert: ") + HBCIUtils.data2hex(iniletter.getKeyHash()) + "\n");
			text.append(i18n.tr("Schl�sselnummer   : ") + passport.getMyPublicSigKey().num + "\n");
			text.append(i18n.tr("Schl�sselversion  : ") + passport.getMyPublicSigKey().version + "\n");
			text.append("---------------------------------------------------------------------------\n");
			text.append(i18n.tr("Bankleitzahl      : ") + passport.getBLZ() + "\n");
			text.append(i18n.tr("Benutzerkennung   : ") + passport.getUserId() + "\n");

			Doc doc = new SimpleDoc(text.toString(),DOCFLAVOR,null);
			pj.print(doc,PRINTPROPS);
		}
		catch (Exception e)
		{
			throw new ApplicationException(i18n.tr("Fehler beim Drucken des Ini-Briefs"),e);
		}
	}

	/**
	 * Liefert eine Liste der verfuegbaren Drucker.
   * @return Liste der Drucker.
	 * @throws Exception
   */
  private Input getPrinterList() throws Exception
	{
		if (printerList != null)
			return printerList;

		ArrayList l = new ArrayList();

		PrintService[] service = PrintServiceLookup.lookupPrintServices(DOCFLAVOR,PRINTPROPS);
		for (int i=0;i<service.length;++i)
		{
			l.add(new Printer(service[i]));
		}

		if (l.size() == 0)
		{
			printerList = new LabelInput(i18n.tr("Kein Drucker verf�gbar"));
			return printerList;
		}

		Printer[] printers = (Printer[]) l.toArray(new Printer[l.size()]);
		printerList = new SelectInput(PseudoIterator.fromArray(printers),null);
		return printerList;
	}

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }

	/**
	 * Hilfsklasse zur Anzeige der Drucker.
   */
  private class Printer implements GenericObject
	{
		private PrintService service = null;
		
		private Printer(PrintService service)
		{
			this.service = service;
		}

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String arg0) throws RemoteException
    {
      return this.service.getName();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[] {"name"};
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return getClass().getName();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "name";
    }

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject arg0) throws RemoteException
    {
    	if (arg0 == null)
	      return false;
	    return this.getID().equals(arg0.getID());
    }
	}
}


/**********************************************************************
 * $Log$
 * Revision 1.6  2005-03-09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.5  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.4  2005/02/03 23:57:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2005/02/03 18:57:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2005/02/02 18:19:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/02/02 16:15:52  willuhn
 * @N Neue Dialoge fuer RDH
 *
 **********************************************************************/