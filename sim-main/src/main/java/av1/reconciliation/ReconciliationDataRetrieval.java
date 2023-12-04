package av1.reconciliation;

import java.util.ArrayList;



import av1.car.Car;
import de.tudresden.sumo.cmd.Edge;
import de.tudresden.sumo.cmd.Simulation;
import de.tudresden.sumo.cmd.Vehicle;
import it.polito.appeal.traci.SumoTraciConnection;

public class ReconciliationDataRetrieval extends Thread {
    private SumoTraciConnection sumo;
    private String currentEdge = "";
    private String lastEdge = "";
    private ArrayList<String> currentEdges = new ArrayList<String>();
    private double simulationTime = 0.0;
    private double totalDistance = 0.0;
    private Car _car;
    private double previousTime = 0.0;
    private ArrayList<Double> timeMeasurements = new ArrayList<Double>();
    private ArrayList<Double> edgesDistances = new ArrayList<Double>();
    private ArrayList<Double> edgesEstimatedTimes = new ArrayList<Double>();

    public ReconciliationDataRetrieval(SumoTraciConnection _sumo, Car _car, ArrayList<String> _currentEdges) {
        this.sumo = _sumo;
        this._car = _car;
        this.currentEdges = _currentEdges;
    }

    @Override
    public void run() {
        while (!this.sumo.isClosed()) {
            try {
                currentEdge = (String) this.sumo.do_job_get(Vehicle.getRoadID(_car.getIdAuto()));
                simulationTime = (double) this.sumo.do_job_get(Simulation.getTime());
                totalDistance = (double) this.sumo.do_job_get(Vehicle.getDistance(_car.getIdAuto()));

                if (!currentEdge.equals(lastEdge) && currentEdges.contains(currentEdge)) {
                    try {
                        double time = simulationTime - previousTime;
                        System.out.println("Time: " + time + " Edge: " + lastEdge);
                        previousTime = simulationTime;
                        lastEdge = currentEdge;
                        timeMeasurements.add(time);
                        edgesEstimatedTimes.add((double) sumo.do_job_get(Edge.getTraveltime(currentEdge)));
                        getDistanceTraveledAtEdge();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Thread.sleep(50);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (this.sumo.isClosed()) {
            timeMeasurements.add(simulationTime - previousTime);
            getDistanceTraveledAtEdge();
        }
        printTimeMeasurements();
        printDistancesMeasurements();
    }

    public void getDistanceTraveledAtEdge() {
        double distanceSum = 0.0;
        for (Double distance : edgesDistances) {
            distanceSum += distance;
        }

        edgesDistances.add(totalDistance - distanceSum);
    }

    public void printTimeMeasurements() {
        System.out.println("Car " + _car.getIdAuto() + " total time:" + getTotalTime() + " time measurements: ");
        timeMeasurements.set(0, getTotalTime());
        System.out.println("---------------------------------------------");
        for (Double time : timeMeasurements) {
            System.out.println(time);
        }
    }

    public void printDistancesMeasurements() {
        System.out.println(
                "Car " + _car.getIdAuto() + " total distance:" + getTotalDistance() + " distances measurements: ");
        edgesDistances.set(0, getTotalDistance());
        System.out.println("---------------------------------------------");
        for (Double distance : edgesDistances) {
            System.out.println(distance);
        }
    }

    public ArrayList<Double> getTimeMeasurements() {
        return timeMeasurements;
    }

    public ArrayList<Double> getEdgesDistances() {
        return edgesDistances;
    }

    public ArrayList<Double> getEdgesEstimatedTimes() {
        return edgesEstimatedTimes;
    }

    public double getTotalDistance() {
        return totalDistance - edgesDistances.get(0);
    }

    public double getTotalTime() {
        return simulationTime - timeMeasurements.get(0);
    }

}
