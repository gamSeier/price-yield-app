
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.JFrame;
import java.awt.BasicStroke;
import java.awt.Color;
import java.text.DecimalFormat;
import java.time.LocalDate;

public class PriceYieldChart extends JFrame {

	public PriceYieldChart(String title, Bond bond) {
    	super(title);

        XYSeriesCollection dataset = createDataset(bond);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Price-Yield Relationship",
                "Yield-to-Maturity (%)",
                "Price",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesShapesVisible(1, false);

        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesStroke(1, new BasicStroke(2.0f));

        renderer.setSeriesPaint(0, Color.GREEN);
        renderer.setSeriesPaint(1, Color.RED);
        
        renderer.setSeriesToolTipGenerator(0, new StandardXYToolTipGenerator(
                "{0}: ({1}, {2})", new DecimalFormat("0.00"), new DecimalFormat("0.00")
            ));
        
        renderer.setSeriesToolTipGenerator(1, new StandardXYToolTipGenerator(
                "{0}: ({1}, {2})", new DecimalFormat("0.00"), new DecimalFormat("0.00")
            ));

        chart.getXYPlot().setRenderer(renderer);
        
        NumberAxis yAxis = (NumberAxis) chart.getXYPlot().getRangeAxis();
        yAxis.setAutoRangeIncludesZero(false);

        ChartPanel panel = new ChartPanel(chart);
        
        chart.getPlot().setBackgroundPaint(Color.BLACK);
        setContentPane(panel);
    }

    private XYSeriesCollection createDataset(Bond bond) {
        XYSeries durationLine = new XYSeries("Duration Line");
        XYSeries convexityLine = new XYSeries("Convexity Line");
        
        double price = bond.getPrice();
        double yield = bond.getYieldToMaturity();
        double duration = bond.getModifiedDuration();
        double convexity = bond.getConvexity();
        
        for (double i = yield - (yield * .5); i <= yield + (yield * .5); i += yield * .005) {
        	double durationPrice = price - duration * (i - yield) * price;
            durationLine.add(i * 100, durationPrice);
            
            double convexityPrice = price - duration * (i - yield) * price + 0.5 * convexity * Math.pow(i - yield, 2) * price;;
            convexityLine.add(i * 100, convexityPrice);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(durationLine);
        dataset.addSeries(convexityLine);

        return dataset;
    }

    public static void main(String[] args) {
        
    	Bond bondUpdated = new Bond(1000, 0.08, LocalDate.of(2026, 01, 01), 2, LocalDate.of(2020, 1, 1), 911.37, true);
    	PriceYieldChart example = new PriceYieldChart("Line Chart Example", bondUpdated);
        example.setSize(800, 400);
        example.setLocationRelativeTo(null);
        example.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        example.setVisible(true);
        
    }
}