package av1.company;

import java.util.ArrayList;
import java.util.UUID;

import javax.crypto.SecretKey;

import av1.bank.Account;
import av1.car.Driver;
import av1.route.Route;
import av1.services.BotPayment;
import av1.services.MessageExchange;
import av1.utils.EncryptionUtil;
import av1.utils.JSONUtil;

public class Company extends Thread {

    private String companyName = ""; // nome da empresa
    private double companyBalance = 0.0;
    private Account companyAccount;// saldo da empresa
    private ArrayList<Route> routesQueue = new ArrayList<Route>(); // fila de rotas
    private ArrayList<Route> routesHistory = new ArrayList<Route>(); // histórico de rotas
    private ArrayList<Route> routesInProgress = new ArrayList<Route>(); // rotas em progresso
    private BotPayment paymentBot; // bot de pagamento
    private ArrayList<Driver> travelRequirements = new ArrayList<Driver>();
    private ArrayList<String> travelRequirementsNames = new ArrayList<String>();
    private MessageExchange messageExchange;
    private SecretKey secretKey;

    public Company(String name, double balance, BotPayment botPayment, SecretKey secretKey) { // construtor
        this.companyName = name;
        this.companyAccount = new Account(balance, name + " Account", "123456"); // cria uma conta para a empresa
        this.companyBalance = balance;
        this.paymentBot = botPayment;
        this.secretKey = secretKey;
        System.out.println("Company " + this.companyName + " created. Balance: " + this.companyBalance);
    }

    @Override
    public void run() { // método run da thread
        double routesToDo = this.routesQueue.size(); // pega o número de rotas na fila
        try {
            while (true) { // loop infinito
                if (this.routesQueue.size() > 0) { // se houver rotas na fila
                    if (travelRequirementsNames.size() > 0) { // se houver requerimentos de motoristas
                        String requirementDriverName = this.travelRequirementsNames.get(0); // pega o primeiro
                                                                                            // requerimento
                        this.travelRequirementsNames.remove(0); // remove o requerimento
                        Route route = this.routesQueue.get(0); // pega a primeira rota da fila
                        sendRouteToDriverJson(route, requirementDriverName); // adiciona a rota ao
                        // motorista
                        moveRouteToInProgress(); // move a rota da fila para as rotas em progresso
                        System.out.println(this.companyName + ": Driver " + requirementDriverName
                                + " is assigned to route: " + route.getRouteName()); // imprime que o motorista está
                                                                                     // rodando a rota
                    }
                }
                if (routesHistory.size() == routesToDo) { // se todas as rotas foram completadas
                    break;
                }
                Thread.sleep(100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Company " + this.companyName + " finished");
    }

    public void sendRouteToDriver(Route route, Driver driver) {
        messageExchange.sendRouteToDriver(route, driver);
    }

    public Account getCompanyAccount() {
        return companyAccount;
    }

    public String getCompanyName() { // getter do nome da empresa
        return this.companyName;
    }

    public double getCompanyBalance() { // getter do saldo da empresa
        return this.companyBalance;
    }

    public ArrayList<Route> getRoutesQueue() { // getter da fila de rotas
        return this.routesQueue;
    }

    public ArrayList<Route> getRoutesHistory() { // getter do histórico de rotas
        return this.routesHistory;
    }

    public ArrayList<Route> getRoutesInProgress() { // getter das rotas em progresso
        return this.routesInProgress;
    }

    public void addRouteToQueue(Route route) { // adiciona uma rota à fila
        this.routesQueue.add(route);
        System.out.println(companyName + ": Route added to queue");
    }

    public void addRouteToHistory(Route route) { // adiciona uma rota ao histórico
        this.routesHistory.add(route);
        System.out.println(companyName + ": Route added to history");
    }

    public void addRouteToInProgress(Route route) { // adiciona uma rota em progresso
        this.routesInProgress.add(route);
        // System.out.println("Route added to in progress");
    }

    public void removeRouteFromQueue(Route route) { // remove uma rota da fila
        this.routesQueue.remove(route);
        // System.out.println("Route removed from queue");
    }

    public void removeRouteFromInProgress(Route route) { // remove uma rota em progresso
        this.routesInProgress.remove(route);
        System.out.println(companyName + ": Route removed from in progress");
    }

    public void removeRouteFromHistory(Route route) { // remove uma rota do histórico
        this.routesHistory.remove(route);
        System.out.println(companyName + ": Route removed from history");
    }

    public void setCompanyBalance(double balance) { // setter do saldo da empresa
        this.companyBalance = balance;
        System.out.println(companyName + ": Company balance set to " + this.companyBalance);
    }

    public void openTravelRequirement(Driver driver) { // adiciona um motorista à lista de requerimentos de corrida
        this.travelRequirements.add(driver);
    }

    public void openTravelRequirementJson(String driverName) { // adiciona um motorista à lista de requerimentos de
                                                               // corrida
        this.travelRequirementsNames.add(driverName);

    }

    public void moveRouteToInProgress() { // move uma rota da fila para as rotas em progresso
        if (this.routesQueue.size() > 0) {
            Route route = this.routesQueue.get(0);
            this.removeRouteFromQueue(route);
            this.addRouteToInProgress(route);
            System.out.println(companyName + ": Route: " + route.routeName + " moved to in progress.");
        } else {
            System.out.println(companyName + ": No routes to dequeue");
        }
    }

    public void completeTravel(String routeName) { // completa uma corrida
        for (Route route : this.routesInProgress) { // percorre as rotas em progresso
            if (route.getRouteName().compareTo(routeName) == 0) { // se a rota for a mesma da corrida
                this.routesInProgress.remove(route); // remove a rota das rotas em progresso
                this.routesHistory.add(route); // adiciona a rota ao histórico
                System.out.println(companyName + ": Route completed"); // imprime que a rota foi completada
                return; // sai do método
            }
        }
        System.out.println(companyName + ": Route not found"); // imprime que a rota não foi encontrada
    }

    public void requestPayment(UUID driverID, double km) {
        this.paymentBot.payDriver(companyAccount.getAccountId(), driverID, km);
    }

    public void setMessageExchange(MessageExchange messageExchange) {
        this.messageExchange = messageExchange;
    }

    public void requestPaymentJson(String encryptedJson) {
        String decryptedJson = EncryptionUtil.decryptedMessage(encryptedJson, secretKey);
        JSONUtil.RequireDriverPaymentData requireDriverPaymentData = JSONUtil
                .parseRequireDriverPaymentJson(decryptedJson);
        this.paymentBot.payDriver(companyAccount.getAccountId(), requireDriverPaymentData.getAccountId(),
                requireDriverPaymentData.getKm());
    }

    public void requireTravelJson(String encryptedJson) {
        String decryptedJson = EncryptionUtil.decryptedMessage(encryptedJson, secretKey); // descriptografa o json
        JSONUtil.RequireTravelData requireTravelData = JSONUtil.parseRequireTravelJson(decryptedJson); // converte o
                                                                                                       // json para
                                                                                                       // objeto
        openTravelRequirementJson(requireTravelData.getDriver()); // adiciona o motorista à lista de requerimentos
                                                                  // de corrida
    }

    public void sendRouteToDriverJson(Route route, String driver) {
        String json = JSONUtil.createSendRouteToDriverJson(route, driver); // cria o json
        String encryptedJson = ""; // json criptografado

        try {
            encryptedJson = EncryptionUtil.encrypt(json, secretKey); // criptografa o json
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        messageExchange.sendRouteToDriverJson(encryptedJson); // envia o json criptografado
    }

}
