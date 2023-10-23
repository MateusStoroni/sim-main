package av1.utils;

import com.google.gson.Gson;

import av1.car.Driver;
import av1.route.Route;

import java.util.UUID;

public class JSONUtil {
    private static final Gson gson = new Gson();

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    public static TransferData parseTransferJson(String json) {
        TransferData data = fromJson(json, TransferData.class);
        return data;
    }

    public static String createTransferJson(UUID from, UUID to, double amount) {
        TransferData data = new TransferData(from, to, amount);
        return toJson(data);
    }

    public static String createRequireFuelJson(double valuePayed, String driverName) {
        RequireFuelData data = new RequireFuelData(valuePayed, driverName);
        return toJson(data);
    }

    public static RequireFuelData parseRequireFuelJson(String json) {
        return fromJson(json, RequireFuelData.class);
    }

    public static String createRefuelDriverJson(String driverName, int liters) {
        RefuelDriverData data = new RefuelDriverData(driverName, liters);
        return toJson(data);
    }

    public static RefuelDriverData parseRefuelDriverJson(String json) {
        return fromJson(json, RefuelDriverData.class);
    }

    public static String createPayDriverJson(UUID from, UUID to, double km) {
        PayDriverData data = new PayDriverData(from, to, km);
        return toJson(data);
    }

    public static PayDriverData parsePayDriverJson(String json) {
        return fromJson(json, PayDriverData.class);
    }

    public static String createPayFuelStationJson(UUID from, int liters) {
        PayFuelStationData data = new PayFuelStationData(from, liters);
        return toJson(data);
    }

    public static PayFuelStationData parsePayFuelStationJson(String json) {
        return fromJson(json, PayFuelStationData.class);
    }

    public static String createRequireTravelJson(String driver) {
        RequireTravelData data = new RequireTravelData(driver);
        return toJson(data);
    }

    public static RequireTravelData parseRequireTravelJson(String json) {
        return fromJson(json, RequireTravelData.class);
    }

    public static String createSendRouteToDriverJson(Route route, String driver) {
        SendRouteToDriverData data = new SendRouteToDriverData(route, driver);
        return toJson(data);
    }

    public static SendRouteToDriverData parseSendRouteToDriverJson(String json) {
        return fromJson(json, SendRouteToDriverData.class);
    }

    public static String createRequireDriverPaymentJson(UUID accountId, double km) {
        RequireDriverPaymentData data = new RequireDriverPaymentData(accountId, km);
        return toJson(data);
    }

    public static RequireDriverPaymentData parseRequireDriverPaymentJson(String json) {
        return fromJson(json, RequireDriverPaymentData.class);
    }

    public static String createCompleteTravelJson(UUID id) {
        CompleteTravelData data = new CompleteTravelData(id);
        return toJson(data);
    }

    public static CompleteTravelData parseCompleteTravelJson(String json) {
        return fromJson(json, CompleteTravelData.class);
    }

    public static class RequireFuelData {
        private double valuePayed;
        private String driverName;

        public RequireFuelData(double valuePayed, String driverName) {
            this.valuePayed = valuePayed;
            this.driverName = driverName;
        }

        public double getValuePayed() {
            return valuePayed;
        }

        public String getDriverName() {
            return driverName;
        }
    }

    private static class RefuelDriverData {
        private String driverName;
        private int liters;

        public RefuelDriverData(String driverName, int liters) {
            this.driverName = driverName;
            this.liters = liters;
        }

        public String getDriverName() {
            return driverName;
        }

        public int getLiters() {
            return liters;
        }
    }

    private static class PayDriverData {
        private UUID from;
        private UUID to;
        private double km;

        public PayDriverData(UUID from, UUID to, double km) {
            this.from = from;
            this.to = to;
            this.km = km;
        }

        public UUID getFrom() {
            return from;
        }

        public UUID getTo() {
            return to;
        }

        public double getKm() {
            return km;
        }
    }

    private static class PayFuelStationData {
        private UUID from;
        private int liters;

        public PayFuelStationData(UUID from, int liters) {
            this.from = from;
            this.liters = liters;
        }

        public UUID getFrom() {
            return from;
        }

        public int getLiters() {
            return liters;
        }
    }

    public static class RequireTravelData {
        private String driver;

        public RequireTravelData(String driver) {
            this.driver = driver;
        }

        public String getDriver() {
            return driver;
        }
    }

    public static class SendRouteToDriverData {
        private Route route;
        private String driver;

        public SendRouteToDriverData(Route route, String driver) {
            this.route = route;
            this.driver = driver;
        }

        public Route getRoute() {
            return route;
        }

        public String getDriver() {
            return driver;
        }
    }

    public static class RequireDriverPaymentData {
        private UUID accountId;
        private double km;

        public RequireDriverPaymentData(UUID accountId, double km) {
            this.accountId = accountId;
            this.km = km;
        }

        public UUID getAccountId() {
            return accountId;
        }

        public double getKm() {
            return km;
        }
    }

    public static class CompleteTravelData {
        private UUID id;

        public CompleteTravelData(UUID id) {
            this.id = id;
        }

        public UUID getId() {
            return id;
        }
    }

    public static class TransferData {
        private UUID from;
        private UUID to;
        private double amount;

        public TransferData(UUID from, UUID to, double amount) {
            this.from = from;
            this.to = to;
            this.amount = amount;
        }

        public UUID getFrom() {
            return from;
        }

        public UUID getTo() {
            return to;
        }

        public double getAmount() {
            return amount;
        }
    }

}
