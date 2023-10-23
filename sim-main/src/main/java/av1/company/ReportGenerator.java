package av1.company;

import av1.car.Car;

public class ReportGenerator {
    public ReportGenerator() {
    }

    public void carReport(Car car) {
        System.out.println("Car report: id:" + car.getCarId() + " | RouteId: " + car.getRunningRoute() + " | Distance: "
                + car.getDistanceTraveled());
    }

    public void transactionReport(String transaction) {
        System.out.println("Transaction report: " + transaction);
    }
}
