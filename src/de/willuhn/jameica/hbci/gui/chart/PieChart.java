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

package de.willuhn.jameica.hbci.gui.chart;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Vector;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithoutAxes;
import org.eclipse.birt.chart.model.attribute.Anchor;
import org.eclipse.birt.chart.model.attribute.ChartDimension;
import org.eclipse.birt.chart.model.attribute.Position;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.data.impl.TextDataSetImpl;
import org.eclipse.birt.chart.model.impl.ChartWithoutAxesImpl;
import org.eclipse.birt.chart.model.layout.Legend;
import org.eclipse.birt.chart.model.type.PieSeries;
import org.eclipse.birt.chart.model.type.impl.PieSeriesImpl;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.logging.Logger;

/**
 * Implementierung eines Torten-Diagramms.
 */
public class PieChart extends AbstractChart
{
  
  /**
   * ct.
   * @throws Exception
   */
  public PieChart() throws Exception
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.AbstractChart#createChart()
   */
  public Chart createChart() throws RemoteException
  {
    // Wir erzeugen ein Chart mit Axen.
    ChartWithoutAxes chart = ChartWithoutAxesImpl.create();
    chart.getBlock().setBackground(ColorDefinitionImpl.WHITE()); // Hintergrundfarbe
    chart.getBlock().getOutline().setVisible(true); // Rahmen um alles
    chart.setDimension(ChartDimension.TWO_DIMENSIONAL_LITERAL);
    chart.getTitle().setVisible(false);
  
    // CUSTOMIZE THE LEGEND 
    Legend lg = chart.getLegend();
    lg.getText().getFont().setSize(Font.DEFAULT.getSWTFont().getFontData()[0].getHeight());
    lg.getInsets().set(20, 5, 0, 0);
    lg.setAnchor(Anchor.NORTH_LITERAL);
  
    Vector data = getData();
    for (int i=0;i<data.size();++i)
    {
      final Vector labelLine = new Vector();
      final Vector dataLine  = new Vector();
      
      ChartData cd          = (ChartData) data.get(i);
      List list             = cd.getData();
      Formatter format      = cd.getLabelFormatter();
      String dataAttribute  = cd.getDataAttribute();
      String labelAttribute = cd.getLabelAttribute();

      if (list == null || list.size() == 0 || dataAttribute == null || labelAttribute == null)
      {
        Logger.info("skipping data line, contains no data");
        dataLine.add(new Double(0));
        labelLine.add(i18n.tr("Nicht definiert"));
      }
      else
      {
        for (Object o:list)
        {
          Object ovalue = BeanUtil.get(o,dataAttribute);
          Object olabel = BeanUtil.get(o,labelAttribute);
          
          if (olabel == null || ovalue == null || !(ovalue instanceof Number))
            continue;

          double d = ((Number) ovalue).doubleValue();

          // Negative Werte versteht der PieChart nicht. Daher muessen wir hier
          // den Betrag bilden.
          d = Math.abs(d);

          // Ausserdem runden wir auf 2 Stellen nach dem Komma
          d = ((int)(d * 100)) / 100d;
          dataLine.add(new Double(d));
          labelLine.add(format == null ? olabel : format.format(olabel));
        }
      }

      SeriesDefinition sd = SeriesDefinitionImpl.create();
      sd.getSeriesPalette().update(1);
      chart.getSeriesDefinitions().add(sd);
      
      Series seCategory = (Series) SeriesImpl.create();
      seCategory.setDataSet(TextDataSetImpl.create(labelLine));
      sd.getSeries().add(seCategory);

      PieSeries sePie = (PieSeries) PieSeriesImpl.create();
      sePie.setDataSet(NumberDataSetImpl.create(dataLine));
      sePie.setLabelPosition(Position.INSIDE_LITERAL);
      sePie.setSeriesIdentifier(getTitle());
      sePie.setExplosion(7);
      
      // sePie.setExplosionExpression("orthogonalValue<20 || orthogonalValue>50");
      
      SeriesDefinition sdValues = SeriesDefinitionImpl.create();
      sd.getSeriesDefinitions().add(sdValues);
      sdValues.getSeries().add(sePie);

      //Min Slice
      chart.setMinSlice(10);
      chart.setMinSlicePercent(false);
      chart.setMinSliceLabel(i18n.tr("Sonstige"));
    }
    return chart;
  }
}


/*********************************************************************
 * $Log$
 * Revision 1.8  2010-08-12 17:12:32  willuhn
 * @N Saldo-Chart komplett ueberarbeitet (Daten wurden vorher mehrmals geladen, Summen-Funktion, Anzeige mehrerer Konten, Durchschnitt ueber mehrere Konten, Bugfixing, echte "Homogenisierung" der Salden via SaldoFinder)
 *
 * Revision 1.7  2008/02/26 01:01:16  willuhn
 * @N Update auf Birt 2 (bessere Zeichen-Qualitaet, u.a. durch Anti-Aliasing)
 * @N Neuer Chart "Umsatz-Kategorien im Verlauf"
 * @N Charts erst beim ersten Paint-Event zeichnen. Dadurch laesst sich z.Bsp. die Konto-View schneller oeffnen, da der Saldo-Verlauf nicht berechnet werden muss
 *
 * Revision 1.6  2006/08/05 22:00:51  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2006/08/01 21:29:12  willuhn
 * @N Geaenderte LineCharts
 *
 * Revision 1.4  2005/12/30 00:28:14  willuhn
 * @C piechart layout
 *
 * Revision 1.3  2005/12/30 00:14:45  willuhn
 * @N first working pie charts
 *
 * Revision 1.2  2005/12/29 01:22:11  willuhn
 * @R UmsatzZuordnung entfernt
 * @B Debugging am Pie-Chart
 *
 * Revision 1.1  2005/12/20 00:03:26  willuhn
 * @N Test-Code fuer Tortendiagramm-Auswertungen
 *
 **********************************************************************/