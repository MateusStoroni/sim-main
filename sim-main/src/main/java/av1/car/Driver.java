package av1.car;

import java.io.IOException;
import java.util.ArrayList;

import javax.crypto.SecretKey;
import av1.reconciliation.Reconciliation;
import av1.reconciliation.ReconciliationDataRetrieval;

import av1.bank.Account;
import av1.company.Company;
import av1.route.Route;
import av1.services.BotPayment;
import av1.services.MessageExchange;
import av1.utils.EncryptionUtil;
import av1.utils.JSONUtil;
import de.tudresden.sumo.objects.SumoColor;
import io.sim.TransportService;
import it.polito.appeal.traci.SumoTraciConnection;

public class Driver extends Thread {
    private Account driverAccount; // conta do motorista
    private String driverName; // nome do motorista
    private Car driverCar; // carro do motorista
    private ArrayList<Route> driverRoutesQueue = new ArrayList<Route>(); // fila de rotas do motorista
    private ArrayList<Route> driverRoutesHistory = new ArrayList<Route>(); // histórico de rotas do motorista
    private ArrayList<Route> driverRoutesInProgress = new ArrayList<Route>(); // rotas em progresso do motorista
    private boolean driverIsAvailable = true; // motorista disponível
    private boolean driverIsWaiting = false; // motorista esperando
    private MessageExchange messageExchange; // troca de mensagens
    private SumoTraciConnection sumo;
    private SecretKey encryptionKey;
    private ReconciliationDataRetrieval reconciliationDataRetrieval;
    private ArrayList<double[]> reconciliationDistanceArrays = new ArrayList<double[]>();
    private ArrayList<double[]> reconciliationTimeArrays = new ArrayList<double[]>();
    private ArrayList<double[]> estimatedTimesArray = new ArrayList<double[]>();
    EncryptionUtil encryptionUtil = new EncryptionUtil();

    public Driver(String name, int balance, String password, Company company, BotPayment botPayment, Account account,
            SecretKey encryptionKey)
            throws Exception { // construtor
        this.driverName = name;
        this.driverAccount = account;
        this.encryptionKey = encryptionKey;
        // roda a simulação do sumo
        driverAccount.start(); // inicia a conta do motorista
        System.out.println("Driver: " + this.driverName + " created. Balance: " + this.driverAccount.getBalance());
        int fuelType = 2;
        int fuelPreferential = 2;
        double fuelPrice = 4.87;
        int personCapacity = 1;
        int personNumber = 1;
        SumoColor green = new SumoColor(0, 255, 0, 126);
        this.driverCar = new Car(name + " Car", true, name, green, "D1", this.sumo, 100, fuelType, fuelPreferential,
                fuelPrice,
                personCapacity, personNumber); // cria o carro do motorista
    }

    @Override
    public void run() { // método run
        while (true) { // loop infinito
            try {
                requireTravelJson(); // Metodo para abrir requerimento de uma viagem se possível na empresa
                startTravel(); // Metodo para iniciar uma viagem se possível
                checkTravel(); // Metodo para verificar se a viagem foi concluída
                checkPayment(); // Metodo para verificar se o motorista recebeu pagamento
                Thread.sleep(1000); // Espera 1 segundo
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void runSumoSim() { // copiada de env simulator
        String sumo_bin = "sumo-gui";
        String config_file = "C:\\Users\\Mateus\\Desktop\\sim-main\\sim-main\\map\\map.sumo.cfg";
        // Sumo connection
        this.sumo = new SumoTraciConnection(sumo_bin, config_file);
        this.sumo.addOption("start", "1"); // auto-run on GUI show
        sumo.addOption("quit-on-end", "1"); // auto-close on end
        try {
            this.sumo.runServer();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void checkPayment() {
        if (this.driverAccount.shouldUpdateBalance()) { // se a conta do motorista deve ser atualizada
            System.out.println("Driver " + driverName + ": received payment: " + this.driverAccount.getPayment());
            this.driverAccount.updateBalance(); // atualiza o saldo do motorista
            System.out.println("Driver " + driverName + ": Balance updated to " + this.driverAccount.getBalance());
        }
    }

    private void requireTravelJson() {

        if (!driverIsAvailable || driverIsWaiting) // se o motorista não estiver disponível
            return; // sai da função
        driverIsWaiting = true; // o motorista está esperando uma viagem
        String jsonMessage = JSONUtil.createRequireTravelJson(this.driverName); // cria um json com o nome do motorista
                                                                                // para pedir uma viagem a company
        try {
            String encryptedJson = EncryptionUtil.encrypt(jsonMessage, encryptionKey); // criptografa o json
            messageExchange.requireTravelJson(encryptedJson); // envia o json criptografado
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } // abre um requerimento de viagem na empresa

    }

    private void requirePayment(double km) {

        String requirePaymentJson = JSONUtil.createRequireDriverPaymentJson(this.driverAccount.getAccountId(), km);
        String encryptedRequirePaymentJson = "";
        try {
            encryptedRequirePaymentJson = EncryptionUtil.encrypt(requirePaymentJson, encryptionKey);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        messageExchange.requireDriverPaymentJson(encryptedRequirePaymentJson);
        // messageExchange.requireDriverPayment(this.driverAccount.getAccountId(), km);
        // abre um requerimento de
        // pagamento
        // na empresa
    }

    private void checkTravel() {
        if (this.driverCar.getRunningRoute() != null) { // se o motorista estiver rodando uma rota
            checkCarStatus(); // verifica o status do carro
        }
    }

    private void checkCarStatus() { // verifica o status do carro
        if (this.driverCar.getCarStatus() == CarStatus.FINISHED) {
            handleFinishedStatus();
        } else if (this.driverCar.getCarStatus() == CarStatus.EMPTY) {
            handleEmptyStatus();
        }
    }

    private void handleFinishedStatus() { // trata o status de finalizado
        Route route = this.driverRoutesInProgress.get(0); // pega a primeira rota em progresso
        try {
            Thread.sleep(1000);
            reconciliateMeasuresAndAdd();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        completeRoute(route); // completa a rota
        requirePayment(driverCar.getDistanceTraveled()); // abre um requerimento de pagamento
        resetCarStatus(); // reseta o status do carro
        System.out.println("Driver " + driverName + ": Route " + route.getRouteName() + " finished"); //
    }

    private void completeRoute(Route route) { // completa uma rota
        removeRouteFromInProgress(route); // remove a rota das rotas em progresso
        addRouteToHistory(route); // adiciona a rota ao histórico
        this.driverIsAvailable = true; // o motorista está disponível
        messageExchange.completeTravel(this.driverRoutesHistory.get(driverRoutesHistory.size() - 1).getRouteName()); // completa
                                                                                                                     // a
                                                                                                                     // viagem
    }

    private void resetCarStatus() {
        this.driverCar.setRunningRoute(null);
        this.driverCar.setCarStatus(CarStatus.WAITING);
    }

    private void handleEmptyStatus() { // trata o status de vazio
        this.driverCar.setCarStatus(CarStatus.REFUELING); // muda o status do carro para abastecendo
        System.out.println("Driver " + this.driverName + " awaiting to refuel.");
        double fullTankPrice = this.driverCar.getLitersToRefuel() * this.driverCar.getFuelPrice();
        System.out.println("Driver " + this.driverName + " full tank price: " + fullTankPrice + " driver balance: "
                + this.driverAccount.getBalance());
        double value;
        if (this.driverAccount.getBalance() < fullTankPrice) {
            value = this.driverAccount.getBalance();
        } else {
            value = fullTankPrice;
        } // calcula o valor do abastecimento
        messageExchange.requireFuel(value, driverName); // abre um requerimento de
                                                        // abastecimento
    }

    public void startTravel() {
        if (isTravelNotPossible()) {
            return;
        }

        if (isRouteQueueEmpty()) {
            System.out.println("Driver " + driverName + ": No routes to run");
            return;
        }

        initiateTravel();
    }

    private boolean isTravelNotPossible() {
        return !driverIsAvailable || driverCar.getRunningRoute() != null
                || driverCar.getCarStatus() == CarStatus.RUNNING;
    }

    private boolean isRouteQueueEmpty() {
        return driverRoutesQueue.isEmpty();
    }

    private void initiateTravel() {
        runSumoSim(); // roda a simulação do sumo
        driverIsAvailable = false; // o motorista não está disponível
        Route route = driverRoutesQueue.remove(0); // pega a primeira rota da fila
        driverCar.setRunningRoute(route); // seta a rota como rota em progresso
        driverRoutesInProgress.add(route); // adiciona a rota em progresso
        driverIsWaiting = false; // o motorista não está esperando
        TransportService transportService = new TransportService(true, // cria um serviço de transporte que vai fazer os
                                                                       // passos no sumo
                this.driverName,
                this.driverCar.getRunningRoute(),
                this.driverCar, this.sumo);
        transportService.start(); // inicia o serviço de transporte
        driverCar.setSumo(this.sumo); // passa a instancia atualizada para o carro
        try {
            Thread.sleep(5000); // espera 3 segundos para o sumo carregar
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        driverCar.setOn_off(true); // fala para a auto que ja está ligado
        reconciliationDataRetrieval = new ReconciliationDataRetrieval(sumo, driverCar,
                driverCar.getCurrentEdges());
        reconciliationDataRetrieval.start();
        if (!driverCar.isStarted()) { // Checa se a Thread carro não está ativa
            driverCar.start(); // inicia a thread do carro
        }
    }

    public String getDriverName() { // getter do nome do motorista
        return this.driverName;
    }

    public boolean isDriverIsAvailable() { // getter do status de disponibilidade do motorista
        return driverIsAvailable;
    }

    public void setDriverIsAvailable(boolean driverIsAvailable) { // setter do status de disponibilidade do motorista
        this.driverIsAvailable = driverIsAvailable;
    }

    public Account getDriverAccount() { // getter da conta do motorista
        return this.driverAccount;
    }

    public Car getDriverCar() { // getter do carro do motorista
        return this.driverCar;
    }

    public ArrayList<Route> getDriverRoutesQueue() { // getter da fila de rotas do motorista
        return this.driverRoutesQueue;
    }

    public ArrayList<Route> getDriverRoutesHistory() { // getter do histórico de rotas do motorista
        return this.driverRoutesHistory;
    }

    public ArrayList<Route> getDriverRoutesInProgress() { // getter das rotas em progresso do motorista
        return this.driverRoutesInProgress;
    }

    public void addRouteToQueue(Route route) { // adiciona uma rota à fila
        this.driverRoutesQueue.add(route);
        System.out.println("Driver " + this.driverName + ": Route " + route.getId() + " added to Driver queue ");
    }

    public void addRouteToHistory(Route route) { // adiciona uma rota ao histórico
        this.driverRoutesHistory.add(route);
        System.out.println("Driver " + this.driverName + ":Route " + route.getId() + " added to Driver history ");
    }

    public void addRouteToInProgress(Route route) { // adiciona uma rota em progresso
        this.driverRoutesInProgress.add(route);
        System.out.println("Driver " + this.driverName + ": Route " + route.getId() + " added to Driver in progress");
    }

    public void removeRouteFromQueue(Route route) { // remove uma rota da fila
        this.driverRoutesQueue.remove(route);
        System.out.println("Driver " + this.driverName + ": Route " + route.getId() + " removed from Driver queue");
    }

    public void removeRouteFromInProgress(Route route) { // remove uma rota em progresso
        this.driverRoutesInProgress.remove(route);
        System.out.println(
                "Driver " + this.driverName + ": Route " + route.getId() + " removed from Driver in progress ");
    }

    public void removeRouteFromHistory(Route route) { // remove uma rota do histórico
        this.driverRoutesHistory.remove(route);
        System.out.println("Driver " + this.driverName + ": Route " + route.getId() + " removed from Driver history");
    }

    public void refuelCar(int liters) {
        this.driverCar.refuel(liters);
        payFuelStation(liters);
    }

    public void payFuelStation(int liters) {
        System.out.println("Driver " + driverName + ": Paying fuel station for " + liters + " liters");
        messageExchange.payFuelStation(this.driverAccount.getAccountId(), liters); // abastece o carro
    }

    public void setMessageExchange(MessageExchange messageExchange) {
        this.messageExchange = messageExchange;
    }
    public ArrayList<double[]> getReconciliationDistanceArrays() {
        return reconciliationDistanceArrays;
    }

    public ArrayList<double[]> getReconciliationTimeArrays() {
        return reconciliationTimeArrays;
    }

    public ArrayList<double[]> getEstimatedTimesArray() {
        return estimatedTimesArray;
    }

    public ArrayList<double[]> getReconciliationSpeedsArray() {
        ArrayList<double[]> reconciliatedSpeeds = new ArrayList<double[]>();
        for (int i = 0; i < reconciliationDistanceArrays.size(); i++) {
            double[] speed = new double[reconciliationDistanceArrays.get(i).length];
            for (int j = 0; j < reconciliationDistanceArrays.get(i).length; j++) {
                speed[j] = reconciliationDistanceArrays.get(i)[j] / reconciliationTimeArrays.get(i)[j];
            }
            reconciliatedSpeeds.add(speed);
        }
        return reconciliatedSpeeds;
    }

    public void reconciliateMeasuresAndAdd() {
        ArrayList<Double> timeMeasurements = reconciliationDataRetrieval.getTimeMeasurements();
        ArrayList<Double> edgesDistances = reconciliationDataRetrieval.getEdgesDistances();
        double[] timeMeasurementsArray = Reconciliation.arrayListToDouble(timeMeasurements);
        double[] edgesDistancesArray = Reconciliation.arrayListToDouble(edgesDistances);
        double[] v1 = Reconciliation.varianceArray(timeMeasurements.size());
        double[] a1 = Reconciliation.incidenceMatrix(timeMeasurements.size());
        double[] v2 = Reconciliation.varianceArray(edgesDistances.size());
        double[] a2 = Reconciliation.incidenceMatrix(edgesDistances.size());

        System.out.println();
        System.out.println("timeMeasurementsArray (y1): ");
        Reconciliation.printMatrix(timeMeasurementsArray);
        System.out.println("time variance (v1): ");
        Reconciliation.printMatrix(v1);
        System.out.println("time incidence matrix (a1): ");
        Reconciliation.printMatrix(a1);
        System.out.println();
        System.out.println("edgesDistancesArray (y2): ");
        Reconciliation.printMatrix(edgesDistancesArray);
        System.out.println("distance variance (v2): ");
        Reconciliation.printMatrix(v2);
        System.out.println("distance incidence matrix (a2): ");
        Reconciliation.printMatrix(a2);

        Reconciliation timeReconciliation = new Reconciliation(timeMeasurementsArray, v1, a1);
        Reconciliation distanceReconciliation = new Reconciliation(edgesDistancesArray, v2, a2);
        double[] timeReconciledFlow = timeReconciliation.getReconciledFlow();
        System.out.println();
        System.out.println("time reconciled flow: ");
        Reconciliation.printMatrix(timeReconciledFlow);
        System.out.println();
        System.out.println("distance reconciled flow: ");
        double[] distanceReconciledFlow = distanceReconciliation.getReconciledFlow();
        Reconciliation.printMatrix(distanceReconciledFlow);
        double totaltimereconciliated = 0.0;

        for (int i = 1; i < timeReconciledFlow.length - 1; i++) {
            totaltimereconciliated += timeReconciledFlow[i];
        }
        System.out.println("total time reconciliated: " + totaltimereconciliated);

        double totaldistancereconciliated = 0.0;
        for (int i = 1; i < distanceReconciledFlow.length - 1; i++) {
            totaldistancereconciliated += distanceReconciledFlow[i];
        }
        System.out.println("total distance reconciliated: " + totaldistancereconciliated);

        estimatedTimesArray.add(Reconciliation.arrayListToDouble(reconciliationDataRetrieval.getEdgesEstimatedTimes()));
        reconciliationDistanceArrays.add(distanceReconciledFlow);
        reconciliationTimeArrays.add(timeReconciledFlow);
    }

}
