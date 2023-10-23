package av1.services;

import java.util.ArrayList;
import java.util.UUID;

import javax.crypto.SecretKey;

import av1.bank.AlphaBank;
import av1.car.Driver;
import av1.company.Company;
import av1.company.FuelStation;
import av1.route.Route;
import av1.utils.EncryptionUtil;
import av1.utils.JSONUtil;

public class MessageExchange {
    AlphaBank alphaBank;
    FuelStation fuelStation;
    Company company;
    ArrayList<Driver> drivers = new ArrayList<Driver>();
    BotPayment botPayment;
    SecretKey secretKey;

    public MessageExchange(SecretKey key) {
        this.secretKey = key;
    }

    public void setAlphaBank(AlphaBank alphaBank) {
        this.alphaBank = alphaBank;
    }

    public void setFuelStation(FuelStation fuelStation) {
        this.fuelStation = fuelStation;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public void setDrivers(ArrayList<Driver> drivers) {
        this.drivers = drivers;
    }

    public void setBotPayment(BotPayment botPayment) {
        this.botPayment = botPayment;
    }

    public void requireFuel(double valuePayed, String driverName) { // método para abrir requerimento de abastecimento
        fuelStation.requireFuel(valuePayed, driverName); // abre um requerimento de abastecimento na fuelStation
    }

    public void refuelDriver(String driverName, int liters) { // método para abastecer o motorista
        getDriver(driverName).refuelCar(liters); // abastece o carro do motorista
    }

    public void payDriver(UUID from, UUID to, double km) { // método para pagar o motorista
        botPayment.payDriver(from, to, km); // paga o motorista
    }

    public void payFuelStation(UUID from, int liters) { // método para pagar a fuelStation
        botPayment.payFuelStation(from, fuelStation.getFuelStationAccount().getAccountId(), liters); // paga a
                                                                                                     // fuelStation
    }

    public void requireTravel(Driver driver) { // método para abrir requerimento de viagem
        company.openTravelRequirement(driver); // abre um requerimento de viagem na company
    }

    public void sendRouteToDriver(Route route, Driver driver) {
        driver.addRouteToQueue(route); // adiciona a rota à fila do motorista
    }

    public void requireDriverPayment(UUID accountId, double km) {
        company.requestPayment(accountId, km);
    }

    public Driver getDriver(String driverName) {
        for (Driver driver : drivers) {
            if (driver.getDriverName().equals(driverName)) {
                return driver;
            }
        }
        return null;
    }

    public void completeTravel(String routeName) {
        company.completeTravel(routeName);
    }

    public void requireTravelJson(String encryptedRequireTravelJson) {
        company.requireTravelJson(encryptedRequireTravelJson); // repassa o requerimento de viagem criptografado para a
                                                               // company
    }

    public void sendRouteToDriverJson(String encryptedSendRouteToDriverJson) {
        String decryptedJson = EncryptionUtil.decryptedMessage(encryptedSendRouteToDriverJson, secretKey); // descriptografa
                                                                                                           // o json
        JSONUtil.SendRouteToDriverData sendRouteToDriverData = JSONUtil.parseSendRouteToDriverJson(decryptedJson); // converte
                                                                                                                   // o
                                                                                                                   // json
                                                                                                                   // para
                                                                                                                   // objeto
        sendRouteToDriver(sendRouteToDriverData.getRoute(), getDriver(sendRouteToDriverData.getDriver())); // repassa
                                                                                                           // a
                                                                                                           // rota
                                                                                                           // e o
                                                                                                           // motorista
                                                                                                           // para a
                                                                                                           // company
    }

    public void requireDriverPaymentJson(String encryptedRequirePaymentJson) {
        company.requestPaymentJson(encryptedRequirePaymentJson);
    }

    // public void completeTravelJson(String encryptedCompleteTravelJson) {
    // company.completeTravel(encryptedCompleteTravelJson);
    // }

    public void requireFuelJson(String encryptedRequireFuelJson) {
        fuelStation.requireFuelJson(encryptedRequireFuelJson);
    }

}
