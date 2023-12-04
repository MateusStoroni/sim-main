package av1.utils;

import au.com.bytecode.opencsv.CSVWriter;
import io.sim.Auto;
import io.sim.DrivingData;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVReportGenerator {
    public void generateCSVReport(List<Auto> carList, String filePath) {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(filePath));

            // Escreve o cabeçalho da planilha
            String[] header = {
                    "TimeStamp (nanoseconds)",
                    "ID car",
                    "ID Route",
                    "Speed",
                    "Distance",
                    "Fuel Consumption",
                    "Fuel Type",
                    "CO2 Emission",
                    "Longitude (lon)",
                    "Latitude (lat)"
            };
            writer.writeNext(header);

            // Escreve os dados de relatório para cada carro na lista
            for (Auto car : carList) {
                for (DrivingData report : car.getDrivingRepport()) {
                    String[] data = {
                            String.valueOf(report.getTimeStamp()),
                            car.getIdAuto(),
                            report.getRouteIDSUMO(),
                            String.valueOf(report.getSpeed()),
                            String.valueOf(report.getOdometer()),
                            String.valueOf(report.getFuelConsumption()),
                            String.valueOf(car.getFuelType()),
                            String.valueOf(report.getCo2Emission()),
                            String.valueOf(report.getX_Position()),
                            String.valueOf(report.getY_Position())
                    };
                    writer.writeNext(data);
                }
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void generateReconciliationCSV(String filename, CharSequence title, ArrayList<double[]> data) {
        try {
            FileWriter csvWriter = new FileWriter(filename + ".csv");

            // Write title
            csvWriter.append(title);
            csvWriter.append("\n");

            // Write column names
            for (int i = 1; i < data.get(0).length - 1; i++) {
                csvWriter.append("F" + (i + 1));
                csvWriter.append(";");
            }
            csvWriter.append("\n");

            for (double[] row : data) {
                for (int i = 1; i < row.length - 1; i++) {
                    csvWriter.append(String.valueOf(row[i]));
                    csvWriter.append(";");
                }
                csvWriter.append("\n");
            }

            csvWriter.flush();
            csvWriter.close();

            System.out.println("CSV file created successfully!");
        } catch (IOException e) {
            System.out.println("Error while generating the CSV file.");
            e.printStackTrace();
        }
    }
}
