package av1.company;

import java.util.concurrent.Semaphore;

import javax.crypto.SecretKey;

import java.util.HashMap;
import av1.bank.Account;
import av1.services.MessageExchange;
import av1.utils.EncryptionUtil;
import av1.utils.JSONUtil;

public class FuelStation extends Thread {
    MessageExchange messageExchange;
    Double averageTankMax = 45.0; // litros
    Account fuelStationAccount;
    HashMap<String, Double> driversToRefuel = new HashMap<>();
    Semaphore semaphore = new Semaphore(2);
    SecretKey secretKey;

    public FuelStation(SecretKey secretKey) {
        this.secretKey = secretKey;
        this.fuelStationAccount = new Account(0, "FuelStation Account", "123456");
        System.out.println("FuelStation created");
    }

    public void setMessageExchange(MessageExchange messageExchange) {
        this.messageExchange = messageExchange;
    }

    public Account getFuelStationAccount() {
        return fuelStationAccount;
    }

    public void requireFuel(double valuePayed, String driverName) {
        double totalGas = valuePayed / 4.87;
        // totalGas = Math.min(totalGas, averageTankMax);
        driversToRefuel.put(driverName, totalGas);
    }

    public void requireFuelJson(String encryptedJson) {
        String decryptedJson = EncryptionUtil.decryptedMessage(encryptedJson, secretKey);
        JSONUtil.RequireFuelData requireFuelData = JSONUtil.parseRequireFuelJson(decryptedJson);
        requireFuel(requireFuelData.getValuePayed(), requireFuelData.getDriverName());
    }

    private void refuelDriver(String driverName, double fuelAmount) {
        messageExchange.refuelDriver(driverName, (int) fuelAmount);
    }

    @Override
    public void run() {
        while (true) { // loop infinito
            if (!driversToRefuel.isEmpty()) { // se houver motoristas para abastecer
                try {
                    semaphore.acquire(); // uso de semaforos para controlar as bombas de combustivel
                    String driverName = driversToRefuel.keySet().iterator().next(); // pega o primeiro motorista
                    double fuelAmount = driversToRefuel.remove(driverName); // pega a quantidade de combustivel

                    new Thread(() -> {
                        try {
                            System.out.println("Refueling " + driverName);
                            Thread.sleep(5000); // mudar para 2 minutos = 120000
                            refuelDriver(driverName, fuelAmount); // abastece o motorista
                            System.out.println(driverName + " refueled");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            semaphore.release();
                        }
                    }).start();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
