package av1.utils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import av1.car.Car;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class RealTimeChartDemo extends ApplicationFrame {
    private XYSeries distanceDataSeries;
    private long initialTime;
    private ChartPanel chartPanel;

    public RealTimeChartDemo(final String title, final Car car) {
        super(title);
        distanceDataSeries = new XYSeries("Distance vs Time");
        initialTime = System.currentTimeMillis();
        XYSeriesCollection dataset = new XYSeriesCollection(distanceDataSeries);
        JFreeChart chart = createChart(dataset);

        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 400));
        chartPanel.setMouseWheelEnabled(true);

        final JPanel content = new JPanel(new BorderLayout());
        content.add(chartPanel);
        setContentPane(content);

        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - initialTime;
                double distance = car.getDistanceTraveled();
                distanceDataSeries.add(elapsedTime / 1000, distance);
            }
        });
        timer.start();
    }

    private JFreeChart createChart(XYSeriesCollection dataset) {
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Real-Time Car Speed",
                "Time (s)",
                "Distance (km)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);

        XYPlot plot = chart.getXYPlot();
        NumberAxis domain = (NumberAxis) plot.getDomainAxis();
        // domain.setRange(0, 100); // Adjust the range as needed

        NumberAxis range = (NumberAxis) plot.getRangeAxis();
        range.setRange(0, 3); // Adjust the range as needed

        return chart;
    }
}
