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
import av1.utils.CSVReportGenerator;
import av1.utils.Calculator;
import av1.utils.Chart;
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

        

        for (int i = 0; company.getRoutesQueue().size() < 100; i++) {
            String idRoute = Integer.toString(0);
            Route route = new Route(100, "Route " + i, "C:\\Users\\Mateus\\Desktop\\sim-main\\sim-main\\data\\dados2.xml", idRoute);
            if (route.getItinerary() != null) {
                company.addRouteToQueue(route); // adiciona rota a empresa
            }
        }

        for (int i = 0; i < 1; i++) {
            Account driverAccount = alphaBank.createAccount(1000, "Driver " + i + " Account", "123456");
            drivers.add(new Driver("Driver " + i, 1000, "123456", company, botPayment, driverAccount, generalKey));
        }

        company.start();
        fuelStation.start();
        // inicia a empresa
        for (int i = 0; i < 1; i++) {
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
                CSVReportGenerator.generateReconciliationCSV("timeReconCSV", "timeReconCSV",
                        driver.getReconciliationTimeArrays());
                CSVReportGenerator.generateReconciliationCSV("distanceReconCSV", "distanceReconCSV",
                        driver.getReconciliationDistanceArrays());
                CSVReportGenerator.generateReconciliationCSV("speedsReconCSV", "speedsReconCSV",
                        driver.getReconciliationSpeedsArray());

                Calculator calculator = new Calculator();
                double mediaTempoF1 = calculator.calculateAverage(driver.getReconciliationTimeArrays(), 1);
                System.out.println("Media Tempo F1: " + mediaTempoF1);
                double desvioPadraoTempoF1 = calculator.calculateStandardDeviation(driver.getReconciliationTimeArrays(),
                        1);
                System.out.println("Desvio Padrao Tempo F1: " + desvioPadraoTempoF1);
                double precisaoTempoF1 = calculator.calculatePrecision(driver.getReconciliationTimeArrays(), 1);
                System.out.println("Precisao Tempo F1: " + precisaoTempoF1);
                double incertezaTempoF1 = calculator.calculateUncertainty(driver.getReconciliationTimeArrays(), 1);
                System.out.println("Incerteza Tempo F1: " + incertezaTempoF1);
                double biasF1 = calculator.calculateBias(driver.getReconciliationTimeArrays(), 1,
                        driver.getEstimatedTimesArray());
                System.out.println("Bias F1: " + biasF1);

                Chart chart = new Chart("F1 time Measurements", "Measurement", "Sensor Value");
                chart.createChart(driver.getReconciliationTimeArrays(), 1); // Passing sensor number 0

                Chart chart2 = new Chart("F1 distance Measurements", "Measurement", "Sensor Value");
                chart2.createChart(driver.getReconciliationDistanceArrays(), 1); // Passing sensor number 0

                Chart chart3 = new Chart("F1 speed Measurements", "Measurement", "Sensor Value");
                chart3.createChart(driver.getReconciliationSpeedsArray(), 1); // Passing sensor number 0
            }
        }).start();
    }

}
