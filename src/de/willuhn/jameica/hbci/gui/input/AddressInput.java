/**********************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 * $Locker$
 * $State$
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.gui.input.SearchInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.filter.AddressFilter;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.AddressbookService;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Autosuggest-Feld zur Eingabe/Auswahl einer Adresse.
 */
public class AddressInput extends SearchInput
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private AddressFilter filter   = null;

  /**
   * ct.
   * @param name Anzuzeigender Name.
   */
  public AddressInput(String name)
  {
    this(name,null);
  }

  /**
   * ct.
   * @param name Anzuzeigender Name.
   * @param filter optionaler Adressfilter.
   */
  public AddressInput(String name, AddressFilter filter)
  {
    super();
    this.filter = filter;
    this.setValue(name);
    this.setValidChars(HBCIProperties.HBCI_DTAUS_VALIDCHARS);
    this.setMaxLength(HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH);
    this.addListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        try
        {
          Address a = (Address) event.data;
          setText(a.getName());
        }
        catch (Exception e)
        {
          Logger.error("unable to apply name",e);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim �bernehmen des Namens"),StatusBarMessage.TYPE_ERROR));
        }
      }
    });
  }

  /**
   * @see de.willuhn.jameica.gui.input.SearchInput#format(java.lang.Object)
   */
  protected String format(Object bean)
  {
    if (bean == null)
      return null;
    
    if (!(bean instanceof Address))
      return bean.toString();
    
    try
    {
      Address a = (Address) bean;
      StringBuffer sb = new StringBuffer(a.getName());
      
      String blz = a.getBlz();
      if (blz != null && blz.length() > 0)
      {
        sb.append(" - ");
        String bankName = HBCIUtils.getNameForBLZ(blz);
        if (bankName != null && bankName.length() > 0)
        {
          sb.append(bankName);
        }
        else
        {
          sb.append("BLZ ");
          sb.append(blz);
        }
      }
      String comment = a.getKommentar();
      if (comment != null && comment.length() > 0)
      {
        sb.append(" (");
        sb.append(comment);
        sb.append(")");
      }
      return sb.toString();
    }
    catch (RemoteException re)
    {
      Logger.error("unable to format address",re);
      return null;
    }
  }


  /**
   * @see de.willuhn.jameica.gui.input.SearchInput#startSearch(java.lang.String)
   */
  public List startSearch(String text)
  {
    try
    {
      AddressbookService service = (AddressbookService) Application.getServiceFactory().lookup(HBCI.class,"addressbook");
      List l = service.findAddresses(text);
      if (l == null || l.size() == 0)
        return l;
      
      List result = new ArrayList();
      for (int i=0;i<l.size();++i)
      {
        Address a = (Address) l.get(i);
        if (filter == null || filter.accept(a))
          result.add(a);
      }
      return result;
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(), StatusBarMessage.TYPE_ERROR));
    }
    catch (Exception e)
    {
      Logger.error("error while searching in address book",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Suchen im Adressbuch: {0}",e.getMessage()), StatusBarMessage.TYPE_ERROR));
    }
    return new ArrayList();
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.4  2009-10-20 23:12:58  willuhn
 * @N Support fuer SEPA-Ueberweisungen
 * @N Konten um IBAN und BIC erweitert
 *
 * Revision 1.3  2009/03/13 00:25:12  willuhn
 * @N Code fuer Auslandsueberweisungen fast fertig
 *
 * Revision 1.2  2009/02/24 23:51:01  willuhn
 * @N Auswahl der Empfaenger/Zahlungspflichtigen jetzt ueber Auto-Suggest-Felder
 *
 * Revision 1.1  2009/01/04 01:25:47  willuhn
 * @N Checksumme von Umsaetzen wird nun generell beim Anlegen des Datensatzes gespeichert. Damit koennen Umsaetze nun problemlos geaendert werden, ohne mit "hasChangedByUser" checken zu muessen. Die Checksumme bleibt immer erhalten, weil sie in UmsatzImpl#insert() sofort zu Beginn angelegt wird
 * @N Umsaetze sind nun vollstaendig editierbar
 *
 **********************************************************************/
