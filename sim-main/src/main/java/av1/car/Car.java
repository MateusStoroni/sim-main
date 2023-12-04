package av1.car;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.eclipse.sumo.libtraci.Simulation;
import org.jfree.ui.RefineryUtilities;

import au.com.bytecode.opencsv.CSVWriter;
import av1.reconciliation.ReconciliationDataRetrieval;
import av1.route.Route;
import av1.utils.RealTimeChartDemo;
import av1.utils.ReportGenerator;
import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;
import io.sim.Auto;
import io.sim.DrivingData;
import it.polito.appeal.traci.SumoTraciConnection;

public class Car extends Auto {
    private CarStatus carStatus = CarStatus.WAITING;

    public CarStatus getCarStatus() {
        return carStatus;
    }

    public void setCarStatus(CarStatus carStatus) {
        this.carStatus = carStatus;
    }

    private UUID carId;
    private Route runningRoute;
    private Auto auto;
    private double carSpeed = 0.0;
    private double distanceTraveled = 0.0;
    private double carFuelConsumption = 1.0;
    private int fuelType = 0;
    private double carFuelTank = 100.0;
    private double carCO2Emission = 0.0;
    private double carLongitude = 0.0;
    private double carLatitude = 0.0;
    private String carName;
    private Double carFuelTankMax = 45.0;
    private boolean isStopped = false;
    private boolean isStarted = false;
    private RealTimeChartDemo realTimeChartDemo;
    public ReconciliationDataRetrieval reconciliationDataRetrieval;

    public boolean isStarted() {
        return isStarted;
    }

    public Car(String carName, boolean _on_off, String _idAuto, SumoColor _colorAuto, String _driverID,
            SumoTraciConnection _sumo,
            long _acquisitionRate, int _fuelType, int _fuelPreferential, double _fuelPrice, int _personCapacity,
            int _personNumber)
            throws Exception {
        super(_on_off, _idAuto, _colorAuto, _driverID, _sumo, _acquisitionRate, _fuelType, _fuelPreferential,
                _fuelPrice, _personCapacity, _personNumber);
        this.fuelType = _fuelType;
        this.carName = carName;
        this.carId = UUID.randomUUID();
    }

    @Override
    public void run() {
        this.isStarted = true; // diz que a thread ja foi iniciada para que não seja iniciada novamente
        startChart(); // inicia o grafico
        while (true) { // loop infinito
            while (super.isOn_off()) { // enquanto o carro estiver ligado (vem da classe auto)

                setCarFuelConsumption(); // seta o consumo de combustivel do carro com o sumo
                checkCarMovement(); // checa se o carro esta parado para abastecer
                super.atualizaSensores(); // atualiza os sensores do carro (velocidade, distancia, etc)
                runRoute(); // roda a rota
                printCarStatus(); // imprime o status do carro
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public ArrayList<String> getCurrentEdges() {
        ArrayList<String> edge = new ArrayList<String>();
        edge.clear();
        String[] aux = this.runningRoute.getItinerary();

        for (String e : aux[1].split(" ")) {
            edge.add(e);
        }
        return edge;
    }

    private void checkCarMovement() {
        if (carStatus != CarStatus.RUNNING) {
            isStopped = true;
        } else {
            isStopped = false;
        }
    }

    private void startChart() {
        realTimeChartDemo = new RealTimeChartDemo("Car Speed", this);
        realTimeChartDemo.pack();
        RefineryUtilities.centerFrameOnScreen(realTimeChartDemo);
        realTimeChartDemo.setVisible(true);
    }

    private void runRoute() {

        if (isRouteNull() || isFuelLow()) {
            handleLowFuel();
            return;
        }

        if (isRouteInProgress()) {
            updateCarAndRouteStatus();
        }
    }

    private void printCarStatus() {

        if (isRouteInProgress()) {

            System.out.println("Car: " + carName + " is running Route: " + runningRoute.getRouteName()
                    + ", distance: " + this.distanceTraveled);
        }
    }

    private boolean isRouteNull() {
        return this.runningRoute == null;
    }

    public boolean isStopped() {
        return isStopped;
    }


    private boolean isFuelLow() {
        return this.carFuelTank <= 3.0;
    }

    private void handleLowFuel() {
        if (carStatus != CarStatus.REFUELING) {
            carStatus = CarStatus.EMPTY;
        }
    }

    private boolean isRouteInProgress() {
        return runningRoute.getDistance() > 0 && carFuelTank > 3.0;
    }

    private void updateCarAndRouteStatus() { // atualiza o status do carro e da rota
        carStatus = CarStatus.RUNNING; // seta o status do carro para rodando
        getPosition(); // pega a posição do carro
        distanceTraveled = super.getDistanceTraveled() / 1000; // pega a distancia percorrida pelo carro
        carFuelTank -= carFuelConsumption / 10; // diminui o combustivel do carro
        System.out.println(this.carName + " Fuel tank: " + carFuelTank); // imprime o combustivel do carro
    }

    public void finishRoute() {
        System.out.println("Car: " + carName + ": Route " + runningRoute.getRouteName() + " finished");
        carStatus = CarStatus.FINISHED;
    }

    public Double getLitersToRefuel() {
        return this.carFuelTankMax - this.carFuelTank;
    }

    public UUID getCarId() {
        return this.carId;
    }

    public Route getRunningRoute() {
        return this.runningRoute;
    }

    public Auto getAuto() {
        return this.auto;
    }

    public double getCarSpeed() {
        try {
            this.carSpeed = (Double) super.getSumo().do_job_get(Vehicle.getSpeed(this.getIdAuto()));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return this.carSpeed;
    }

    public double getDistanceTraveled() {
        return this.distanceTraveled;
    }

    public double getCarFuelConsumption() {
        return this.carFuelConsumption;
    }

    public int getFuelType() {
        return this.fuelType;
    }

    public double getCarFuelTank() {
        return this.carFuelTank;
    }

    public double getCarCO2Emission() {
        try {
            this.carCO2Emission = (Double) super.getSumo().do_job_get(Vehicle.getCO2Emission(this.getIdAuto()));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return this.carCO2Emission;
    }

    public double getCarLongitude() {
        return this.carLongitude;
    }

    public double getCarLatitude() {
        return this.carLatitude;
    }

    public void setRunningRoute(Route route) { // setter da rota rodando
        this.distanceTraveled = 0.0;
        this.runningRoute = route;
        System.out.println("Car: " + this.carName + " Route set to running");
    }

    public void setAuto(Auto auto) {
        this.auto = auto;
        System.out.println("Auto set");
    }

    public void setCarSpeed(double speed) {
        this.carSpeed = speed;
        System.out.println("Car speed set");
    }

    public void setDistanceTraveled(double distance) {
        this.distanceTraveled = distance;
        System.out.println("Distance traveled set");
    }

    public void setCarFuelConsumption() { // pega fuel comsumption do sumo
        if (this.getSumo().isClosed()) {
            return;
        }
        try {
            this.carFuelConsumption = ((Double) super.getSumo().do_job_get(Vehicle.getFuelConsumption(this.getIdAuto()))
                    / 1000); // consumo de combustivel vindo do sumo em L/s;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setFuelType(int fuelType) {
        this.fuelType = fuelType;
        System.out.println("Fuel type set");
    }

    public void setCarFuelTank(double fuelTank) {
        this.carFuelTank = fuelTank;
        System.out.println("Car fuel tank set");
    }

    public void setCarCO2Emission(double emission) {
        this.carCO2Emission = emission;
        System.out.println("Car CO2 emission set");
    }

    public void setCarLongitude(double longitude) {
        this.carLongitude = longitude;
        System.out.println("Car longitude set");
    }

    public void setCarLatitude(double latitude) {
        this.carLatitude = latitude;
        System.out.println("Car latitude set");
    }

    public void startRoute(Route route) {
        distanceTraveled = 0.0;
        this.runningRoute = route;
        System.out.println("Car: " + this.carName + " Route started");
    }

    public void getPosition() {
        double x = 0.0;
        double y = 0.0;
        SumoPosition2D sumoPosition2D;
        try {
            sumoPosition2D = (SumoPosition2D) super.getSumo().do_job_get(Vehicle.getPosition(this.getIdAuto()));
            x = sumoPosition2D.x;
            y = sumoPosition2D.y;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.carLongitude = x;
        this.carLatitude = y;
    }

    public void refuel(int liters) { // metodo para abastecer o carro
        this.carFuelTank += liters;
        System.out.println("Car: " + this.carName + " refueled " + liters + " liters");
        this.carStatus = CarStatus.RUNNING; // seta o status do carro para rodando
    }

    public void setSumo(SumoTraciConnection sumo) {
        super.setSumo(sumo);
    }

    public void writeDrivingReportToCSV(String filePath) {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(filePath), ';');

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

            // Escreve os dados de relatório para o carro
            for (DrivingData report : super.getDrivingRepport()) {
                String[] data = {
                        String.valueOf(System
                                .nanoTime()),
                        this.getIdAuto(),
                        report.getRouteIDSUMO(),
                        String.valueOf(report.getSpeed()),
                        String.valueOf(report.getOdometer()),
                        String.valueOf(report.getFuelConsumption()),
                        String.valueOf(fuelType),
                        String.valueOf(report.getCo2Emission()),
                        String.valueOf(getCarLatitude()),
                        String.valueOf(getCarLongitude())
                };
                writer.writeNext(data);
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

enum CarStatus { // enum para o status do carro
    WAITING, RUNNING, FINISHED, EMPTY, REFUELING
}
