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

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.PassportRegistry;
import de.willuhn.jameica.hbci.gui.controller.PassportObject;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit Sicherheitsmedien.
 */
public class PassportList extends TablePart implements Part
{
  private I18N i18n = null;

  /**
   * @param action
   * @throws RemoteException
   */
  public PassportList(final Action action) throws RemoteException
  {
    super(init(), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        if (context == null || !(context instanceof PassportObject))
          return;
        Passport p = ((PassportObject) context).getPassport();
        action.handleAction(p);
      }
    });
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    this.setMulti(false);
    this.setSummary(false);
    addColumn(i18n.tr("Bezeichnung"),"name");
  }

  private static GenericIterator init() throws RemoteException
  {

    Passport[] passports = null;
    try
    {
      passports = PassportRegistry.getPassports();
    }
    catch (Exception e)
    {
      Logger.error("error while loading the passport list",e);
      throw new RemoteException("unable to load the passport list",e);
    }

    GenericObject[] p = new GenericObject[passports.length];
    for (int i=0;i<passports.length;++i)
    {
      p[i] = new PassportObject(passports[i]);
    }
    return PseudoIterator.fromArray(p);
  }
  
}


/**********************************************************************
 * $Log$
 * Revision 1.3  2005-07-04 21:57:08  web0
 * @B bug 80
 *
 * Revision 1.2  2005/06/27 15:35:27  web0
 * @B bug 84
 *
 * Revision 1.1  2005/06/21 21:48:24  web0
 * @B bug 80
 *
 **********************************************************************/