package av1.utils;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.List;

public class Calculator {

    public double calculateAverage(ArrayList<double[]> arrays, int sensorNumber) {
        double[] data = getSensorData(arrays, sensorNumber);
        return StatUtils.mean(data);
    }

    public double calculatePrecision(ArrayList<double[]> arrays, int sensorNumber) {
        return calculateStandardDeviation(arrays, sensorNumber) * 2;
    }

    public double calculateBias(ArrayList<double[]> arrays, int sensorNumber, List<double[]> estimatedTimesArray) {
        double[] data = getSensorData(arrays, sensorNumber);
        double average = StatUtils.mean(data);
        double estimatedTime = estimatedTimesArray.get(sensorNumber)[sensorNumber];
        return estimatedTime - average;
    }

    public double calculateStandardDeviation(ArrayList<double[]> arrays, int sensorNumber) {
        double[] data = getSensorData(arrays, sensorNumber);
        DescriptiveStatistics stats = new DescriptiveStatistics(data);
        return stats.getStandardDeviation();
    }

    public double calculateUncertainty(ArrayList<double[]> arrays, int sensorNumber) {
        double standardDeviation = calculateStandardDeviation(arrays, sensorNumber);
        double precision = calculatePrecision(arrays, sensorNumber);
        return Math.sqrt(Math.pow(precision, 2) + Math.pow(standardDeviation, 2));
    }

    private double[] getSensorData(ArrayList<double[]> arrays, int sensorNumber) {
        ArrayList<Double> sensorData = new ArrayList<>();
        for (double[] array : arrays) {
            sensorData.add(array[sensorNumber]);
        }
        return sensorData.stream().mapToDouble(Double::doubleValue).toArray();
    }

    public static void main(String[] args) {
        // Same as before
    }
}
