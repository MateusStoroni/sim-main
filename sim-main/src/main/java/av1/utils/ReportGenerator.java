package av1.utils;

import av1.car.Car;

public class ReportGenerator {
    public ReportGenerator() {
    }

    public void carReport(Car car) {
        System.out.println("Car report: TimeStamp (nanoseconds): " + System.nanoTime() +
                " | ID car: " + car.getCarId() +
                " | ID Route: " + car.getRunningRoute().getId() +
                " | Speed: " + car.getCarSpeed() +
                " | Distance: " + car.getDistanceTraveled() +
                " | Fuel Consumption: " + car.getCarFuelConsumption() +
                " | Fuel Type: " + car.getFuelType() +
                " | CO2 Emission: " + car.getCarCO2Emission() +
                " | Longitude (lon): " + car.getCarLongitude() +
                " | Latitude (lat): " + car.getCarLatitude());
    }

    public void transactionReport(String transaction) {
        System.out.println("Transaction report: " + transaction);
    }
}
