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
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit Umsaetzen.
 */
public class UmsatzList extends TablePart
{

  private TextInput search      = null;
  
  private GenericIterator list  = null;
  private ArrayList umsaetze    = null;

  private I18N i18n;

  /**
   * @param konto
   * @param action
   * @throws RemoteException
   */
  public UmsatzList(Konto konto, Action action) throws RemoteException
  {
    this(konto,0,action);
  }

  /**
   * @param konto
   * @param days
   * @param action
   * @throws RemoteException
   */
  public UmsatzList(Konto konto, int days, Action action) throws RemoteException
  {
    this(konto.getUmsaetze(days), action);
  }

  /**
   * @param list
   * @param action
   */
  public UmsatzList(GenericIterator list, Action action)
  {
    super(list, action);
    
    this.list = list;
    
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    setMulti(true);
    setFormatter(new TableFormatter() {
      public void format(TableItem item) {
        Umsatz u = (Umsatz) item.getData();
        if (u == null) return;
        try {
          if (u.getBetrag() < 0.0)
          {
            item.setForeground(Settings.getBuchungSollForeground());
          }
          else
          {
            item.setForeground(Settings.getBuchungHabenForeground());
          }
          // Waehrung des Kontos dranpappen
          item.setText(3,item.getText(3) + " " + u.getKonto().getWaehrung());
          item.setText(4,item.getText(4) + " " + u.getKonto().getWaehrung());
        }
        catch (RemoteException e)
        {
        }
      }
    });

    // BUGZILLA 23 http://www.willuhn.de/bugzilla/show_bug.cgi?id=23
    // BUGZILLA 86 http://www.willuhn.de/bugzilla/show_bug.cgi?id=86
    addColumn(i18n.tr("Gegenkonto"),                "empfaenger");
    addColumn(i18n.tr("Verwendungszweck"),          "zweck");
    addColumn(i18n.tr("Valuta"),                    "valuta", new DateFormatter(HBCI.DATEFORMAT));
    addColumn(i18n.tr("Betrag"),                    "betrag", new CurrencyFormatter("",HBCI.DECIMALFORMAT));
    // BUGZILLA 66 http://www.willuhn.de/bugzilla/show_bug.cgi?id=66
    addColumn(i18n.tr("Saldo zu diesem Zeitpunkt"), "saldo",  new CurrencyFormatter("",HBCI.DECIMALFORMAT));

    // BUGZILLA 84 http://www.willuhn.de/bugzilla/show_bug.cgi?id=84
    setRememberOrder(true);

    setContextMenu(new de.willuhn.jameica.hbci.gui.menus.UmsatzList());
  }
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
    final Composite comp = new Composite(parent,SWT.NONE);
    final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 2;
    comp.setLayoutData(gd);
    comp.setBackground(Color.BACKGROUND.getSWTColor());
    comp.setLayout(new GridLayout(2,false));

    final Label label = GUI.getStyleFactory().createLabel(comp,SWT.NONE);
    label.setBackground(Color.BACKGROUND.getSWTColor());
    label.setText(i18n.tr("Zweck, Name oder Konto enth�lt"));
    label.setLayoutData(new GridData(GridData.BEGINNING));

    this.search = new TextInput("");
    this.search.paint(comp);

//    final Button b = GUI.getStyleFactory().createButton(comp);
//    b.setImage(SWTUtil.getImage("search.gif"));
//    b.addSelectionListener(new SelectionAdapter()
//    {
//      public void widgetSelected(SelectionEvent e)
//      {
//        Menu menu = new Menu(GUI.getShell(),SWT.POP_UP);
//        MenuItem item = new MenuItem(menu, SWT.PUSH);
//        item.setText(i18n.tr("Als Umsatz-Filter speichern"));
//        item.addListener(SWT.Selection, new Listener()
//        {
//          public void handleEvent (Event e)
//          {
//            try
//            {
//              String text = (String) search.getValue();
//              if (text == null || text.length() == 0)
//                return;
//              
//              UmsatzTyp typ = (UmsatzTyp) Settings.getDBService().createObject(UmsatzTyp.class,null);
//              typ.setName(i18n.tr("Zweck, Name oder Konto enth�lt \"{0}\"",text));
//              typ.setField("zweck");
//              typ.setPattern(text);
//              typ.setType(Pattern.TYPE_CONTAINS);
//              typ.store();
//              GUI.getStatusBar().setSuccessText(i18n.tr("Umsatz-Filter gespeichert"));
//            }
//            catch (Exception ex)
//            {
//              Logger.error("unable to store umsatz filter",ex);
//              GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern des Umsatz-Filters"));
//            }
//          }
//        });
//        
//        try
//        {
//          DBIterator i = Settings.getDBService().createList(UmsatzTyp.class);
//          if (i.size() > 0)
//          {
//            new MenuItem(menu, SWT.SEPARATOR);
//            while (i.hasNext())
//            {
//              final UmsatzTyp ut = (UmsatzTyp) i.next();
//              final String s = ut.getPattern();
//              final MenuItem mi = new MenuItem(menu, SWT.PUSH);
//              mi.setText(s);
//              mi.addListener(SWT.Selection, new Listener()
//              {
//                public void handleEvent(Event event)
//                {
//                  Logger.debug("applying filter " + s);
//                  search.setValue(s);
//                }
//              });
//              
//            }
//          }
//          
//        }
//        catch (Exception ex)
//        {
//          Logger.error("unable to load umsatz filter",ex);
//          GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Laden der Umsatz-Filters"));
//        }
//
//        menu.setLocation(GUI.getDisplay().getCursorLocation());
//        menu.setVisible(true);
//        while (!menu.isDisposed() && menu.isVisible())
//        {
//          if (!GUI.getDisplay().readAndDispatch()) GUI.getDisplay().sleep();
//        }
//        menu.dispose();
//      }
//    });
    
    super.paint(parent);

    // Wir kopieren den ganzen Kram in eine ArrayList, damit die
    // Objekte beim Filter geladen bleiben
    umsaetze = new ArrayList();
    list.begin();
    while (list.hasNext())
    {
      Umsatz u = (Umsatz) list.next();
      umsaetze.add(u);
    }
    
    KL kl = new KL();
    this.search.getControl().addKeyListener(kl);
  }

  // BUGZILLA 5
  private class KL extends KeyAdapter implements SelectionListener
  {
    private Thread timeout = null;
    private long count = 900l;
    
    /**
     * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
     */
    public void keyReleased(KeyEvent e)
    {
      // Mal schauen, ob schon ein Thread laeuft. Wenn ja, muessen wir den
      // erst killen
      if (timeout != null)
      {
        timeout.interrupt();
        timeout = null;
      }
      
      // Ein neuer Timer
      timeout = new Thread()
      {
        public void run()
        {
          try
          {
            // Wir warten 900ms. Vielleicht gibt der User inzwischen weitere
            // Sachen ein.
            sleep(count);
            
            // Ne, wir wurden nicht gekillt. Also machen wir uns ans Werk

            GUI.getDisplay().syncExec(new Runnable()
            {
              public void run()
              {
                try
                {
                  // Erstmal alle rausschmeissen
                  removeAll();

                  // Wir holen uns den aktuellen Text
                  String text = (String) search.getValue();
                  if (text.length() == 0) text = null;
                  if (text != null) text = text.toLowerCase();

                  Umsatz u    = null;
                  String vwz1 = null;
                  String vwz2 = null;
                  String name = null;
                  String kto  = null;

                  for (int i=0;i<umsaetze.size();++i)
                  {
                    u = (Umsatz) umsaetze.get(i);

                    vwz1 = u.getZweck();
                    vwz2 = u.getZweck2();
                    name = u.getEmpfaengerName();
                    kto  = u.getEmpfaengerKonto();
                    if (vwz1 == null) vwz1 = "";
                    if (vwz2 == null) vwz2 = "";
                    if (name == null) name = "";
                    if (kto == null) kto = "";

                    // Was zum Filtern da?
                    if (text == null)
                    {
                      // ne
                      addItem(u);
                      continue;
                    }
                    
                    boolean match = false;
                    match |= (vwz1.toLowerCase().indexOf(text) != -1);
                    match |= (vwz2.toLowerCase().indexOf(text) != -1);
                    match |= (name.toLowerCase().indexOf(text) != -1);
                    match |= (kto.toLowerCase().indexOf(text) != -1);

                    if (match)
                      addItem(u);
                    
                  }
                }
                catch (Exception e)
                {
                  Logger.error("error while loading umsatz",e);
                }
              }
            });
          }
          catch (InterruptedException e)
          {
            return;
          }
          finally
          {
            timeout = null;
            count = 900l;
          }
        }
      };
      timeout.start();
    }

    /**
     * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
     */
    public void widgetSelected(SelectionEvent e)
    {
      // Beim Klick auf die Checkbox muessen wir nichts warten
      count = 0l;
      keyReleased(null);
    }

    /**
     * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
     */
    public void widgetDefaultSelected(SelectionEvent e)
    {
    }
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.10  2005-11-18 17:39:12  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2005/11/14 21:41:02  willuhn
 * @B bug 5
 *
 * Revision 1.8  2005/06/27 15:35:27  web0
 * @B bug 84
 *
 * Revision 1.7  2005/06/23 17:36:33  web0
 * @B bug 84
 *
 * Revision 1.6  2005/06/21 20:15:33  web0
 * *** empty log message ***
 *
 * Revision 1.5  2005/06/21 20:11:10  web0
 * @C cvs merge
 *
 * Revision 1.3  2005/05/09 12:24:20  web0
 * @N Changelog
 * @N Support fuer Mehrfachmarkierungen
 * @N Mehere Adressen en bloc aus Umsatzliste uebernehmen
 *
 * Revision 1.2  2005/05/08 17:48:51  web0
 * @N Bug 56
 *
 * Revision 1.1  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 **********************************************************************/