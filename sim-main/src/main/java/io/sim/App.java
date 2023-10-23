package io.sim;

import java.util.ArrayList;

import javax.crypto.SecretKey;

import av1.bank.AlphaBank;
import av1.car.Driver;
import av1.company.Company;
import av1.company.FuelStation;
import av1.route.Route;
import av1.services.BotPayment;
import av1.services.MessageExchange;
import av1.utils.EncryptionUtil;
import av1.bank.Account;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) throws Exception {

        // EnvSimulator ev = new EnvSimulator();
        // ev.start();
        SecretKey generalKey = EncryptionUtil.generateSecretKey();

        MessageExchange messageExchange = new MessageExchange(generalKey);
        AlphaBank alphaBank = new AlphaBank(generalKey); // cria um banco
        ArrayList<Driver> drivers = new ArrayList<Driver>(); // motoristas
        BotPayment botPayment = new BotPayment(alphaBank, generalKey); // adiciona o motorista à empresa
        Company company = new Company("Company1", 1000000.0, botPayment, generalKey); // cria uma empresa
        FuelStation fuelStation = new FuelStation(generalKey);
        messageExchange.setAlphaBank(alphaBank);
        messageExchange.setCompany(company);
        messageExchange.setFuelStation(fuelStation);
        messageExchange.setDrivers(drivers);
        messageExchange.setBotPayment(botPayment);
        fuelStation.setMessageExchange(messageExchange);
        company.setMessageExchange(messageExchange);

        // cria um posto de gasolina
        alphaBank.addAccount(company.getCompanyAccount());
        alphaBank.addAccount(fuelStation.getFuelStationAccount());

        for (int i = 0; i < 900; i++) {
            String idRoute = Integer.toString(i);
            company.addRouteToQueue(new Route(100, "Route " + i, "C:\\Users\\Mateus\\Desktop\\sim-main\\sim-main\\data\\dados2.xml", idRoute)); // adiciona 10 rotas à
            // fila da empresa
        }

        for (int i = 0; i < 100; i++) {
            Account driverAccount = alphaBank.createAccount(1000, "Driver " + i + " Account", "123456");
            drivers.add(new Driver("Driver " + i, 1000, "123456", company, botPayment, driverAccount, generalKey));
        }

        company.start();
        fuelStation.start();
        // inicia a empresa
        for (int i = 0; i < 3; i++) {
            drivers.get(i).setMessageExchange(messageExchange); // adiciona o motorista à empresa
            drivers.get(i).start(); // inicia o motorista
            Thread.sleep(3000);
        }

        new Thread(() -> {
            try {
                company.join();
            } catch (Exception e) {
                // TODO: handle exception
            }

            for (Driver driver : drivers) {
                driver.getDriverCar().writeDrivingReportToCSV("car_" + driver.getDriverName() + "_report.csv");
            }
        }).start();
    }

}
