package av1.utils;

import au.com.bytecode.opencsv.CSVWriter;
import io.sim.Auto;
import io.sim.DrivingData;

import java.io.FileWriter;
import java.io.IOException;
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
}
