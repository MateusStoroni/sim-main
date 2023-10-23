package av1.route;

import java.util.UUID;

import io.sim.Itinerary;

public class Route extends Itinerary {
    private UUID id = UUID.randomUUID();
    private double distance = 0;
    public String routeName = "";

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public Route(double distance, String routeName, String _uriRoutesXML, String _idRoute) {
        super(_uriRoutesXML, _idRoute);
        this.routeName = routeName;
        this.distance = distance;
        // System.out.println("Route: " + routeName + " created");
    }

    public Route(double distance, String routeName) {
        super("", "");
        this.routeName = routeName;
        this.distance = distance;
        // System.out.println("Route: " + routeName + " created");
    }

    public UUID getId() {
        return this.id;
    }

    public double getDistance() {
        return this.distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

}
