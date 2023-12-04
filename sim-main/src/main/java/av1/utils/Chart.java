package av1.utils;

import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;

import java.util.ArrayList;
import java.util.List;

public class Chart {

    private final String chartName;
    private final String xAxisName;
    private final String yAxisName;

    public Chart(String chartName, String xAxisName, String yAxisName) {
        this.chartName = chartName;
        this.xAxisName = xAxisName;
        this.yAxisName = yAxisName;
    }

    public void createChart(List<double[]> arrays, int sensorNumber) {
        double[] xData = getXData(arrays);
        double[] yData = getYData(arrays, sensorNumber);

        // Create Chart
        XYChart chart = new XYChartBuilder()
                .width(800)
                .height(600)
                .title(chartName)
                .xAxisTitle(xAxisName)
                .yAxisTitle(yAxisName)
                .theme(Styler.ChartTheme.GGPlot2)
                .build();

        // Customize chart settings, e.g., axis labels, legends, etc.
        chart.getStyler().setLegendVisible(false);

        // Add data points to the chart
        chart.addSeries("Data", xData, yData);

        // Show the chart
        new SwingWrapper<>(chart).displayChart();
    }

    private double[] getXData(List<double[]> arrays) {
        double[] xData = new double[arrays.size()];
        for (int i = 0; i < arrays.size(); i++) {
            xData[i] = i + 1;
        }
        return xData;
    }

    private double[] getYData(List<double[]> arrays, int sensorNumber) {
        List<Double> yData = new ArrayList<>();
        for (double[] array : arrays) {
            yData.add(array[sensorNumber]);
        }
        return yData.stream().mapToDouble(Double::doubleValue).toArray();
    }
}
