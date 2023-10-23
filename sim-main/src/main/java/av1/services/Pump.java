package av1.services;

public class Pump extends Thread {
    private final String driverName;
    private final double fuelAmount;
    private final MessageExchange messageExchange;

    public Pump(String driverName, double fuelAmount, MessageExchange messageExchange) {
        this.driverName = driverName;
        this.fuelAmount = fuelAmount;
        this.messageExchange = messageExchange;
    }

    @Override
    public void run() {
        try {
            System.out.println("Refueling " + driverName);
            Thread.sleep(5000); // Simula o tempo de abastecimento
            messageExchange.refuelDriver(driverName, (int) fuelAmount);
            System.out.println(driverName + " refueled");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
