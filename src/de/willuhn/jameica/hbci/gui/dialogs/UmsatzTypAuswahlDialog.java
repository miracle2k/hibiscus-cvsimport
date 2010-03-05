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

import org.eclipse.swt.widgets.Composite;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.input.UmsatzTypInput;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Fertig konfigurierter Dialog zur Auswahl einer Umsatz-Kategorie.
 */
public class UmsatzTypAuswahlDialog extends AbstractDialog
{
  private I18N i18n          = null;
  private UmsatzTyp choosen  = null;
  private int typ            = UmsatzTyp.TYP_EGAL;
  
  private UmsatzTypInput input = null;

  /**
   * ct.
   * @param position
   * @param typ Filter auf Kategorie-Typen.
   * Kategorien vom Typ "egal" werden grundsaetzlich angezeigt.
   * @see UmsatzTyp#TYP_AUSGABE
   * @see UmsatzTyp#TYP_EINNAHME
   * @throws RemoteException
   */
  public UmsatzTypAuswahlDialog(int position, int typ) throws RemoteException
  {
    this(position,null,typ);
  }

  /**
   * ct.
   * @param position
   * @param preselected der vorausgewaehlte Umsatztyp.
   * @throws RemoteException
   */
  public UmsatzTypAuswahlDialog(int position, UmsatzTyp preselected) throws RemoteException
  {
    this(position,preselected,UmsatzTyp.TYP_EGAL);
  }
  
  /**
   * ct.
   * @param position
   * @param preselected der vorausgewaehlte Umsatztyp.
   * @param typ Filter auf Kategorie-Typen.
   * Kategorien vom Typ "egal" werden grundsaetzlich angezeigt.
   * @see UmsatzTyp#TYP_AUSGABE
   * @see UmsatzTyp#TYP_EINNAHME
   * @throws RemoteException
   */
  private UmsatzTypAuswahlDialog(int position, UmsatzTyp preselected, int typ) throws RemoteException
  {
    super(position);
    this.choosen = preselected;
    this.typ = typ;
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    this.setTitle(i18n.tr("Umsatz-Kategorien"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return choosen;
  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    SimpleContainer group = new SimpleContainer(parent);
    
    group.addText(i18n.tr("Bitte w�hlen Sie die zu verwendende Kategorie aus."),true);

    DBIterator list = Settings.getDBService().createList(UmsatzTyp.class);
    list.setOrder("ORDER BY parent_id,nummer,name");
    
    if (this.choosen != null)
      this.input = new UmsatzTypInput(list,this.choosen);
    else
      this.input = new UmsatzTypInput(list,this.typ);
    
    input.setComment(null); // Hier keine Umsatz-Zahlen anzeigen. Das macht den Dialog haesslich
    
    group.addLabelPair(i18n.tr("Bezeichnung"),input);

    ButtonArea buttons = new ButtonArea(parent,2);
    buttons.addButton(i18n.tr("�bernehmen"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        choosen = (UmsatzTyp) input.getValue();
        close();
      }
    },null,true);
    buttons.addButton(i18n.tr("Abbrechen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    });
  }

}


/*********************************************************************
 * $Log$
 * Revision 1.10  2010-03-05 18:07:26  willuhn
 * @N Unterkategorien in Selectbox einruecken
 *
 * Revision 1.9  2010/03/05 18:00:27  willuhn
 * @C Umsatz-Kategorien nach Nummer und anschliessend nach Name sortieren
 *
 * Revision 1.8  2008/08/29 16:46:24  willuhn
 * @N BUGZILLA 616
 *
 * Revision 1.7  2008/08/08 08:57:14  willuhn
 * @N BUGZILLA 614
 *
 * Revision 1.6  2008/08/08 08:43:41  willuhn
 * @N BUGZILLA 614
 *
 * Revision 1.5  2008/08/08 08:30:35  willuhn
 * @B 544
 *
 * Revision 1.4  2007/12/03 10:00:27  willuhn
 * @N Umsatz-Kategorien nach Name sortieren, wenn keine Nummer angegeben
 *
 * Revision 1.3  2007/03/18 08:13:40  jost
 * Sortierte Anzeige der Umsatz-Kategorien.
 *
 * Revision 1.2  2006/12/29 14:28:47  willuhn
 * @B Bug 345
 * @B jede Menge Bugfixes bei SQL-Statements mit Valuta
 *
 * Revision 1.1  2006/11/30 23:48:40  willuhn
 * @N Erste Version der Umsatz-Kategorien drin
 *
 **********************************************************************/