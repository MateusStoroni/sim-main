package av1.bank;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import av1.utils.EncryptionUtil;
import av1.utils.JSONUtil;
import av1.utils.ReportGenerator;

public class AlphaBank {

    private ArrayList<Account> accounts = new ArrayList<Account>();
    private SecretKey secretKey;

    public AlphaBank(SecretKey secretKey) {
        this.secretKey = secretKey;
        System.out.println("AlphaBank created");
    }

    public void addAccount(Account account) {
        accounts.add(account);
        System.out.println("Account " + account.getAccountId() + " added to AlphaBank");
    }

    public Account createAccount(double balance, String name, String password) {
        Account account = new Account(balance, name, password);
        accounts.add(account);
        System.out.println("Account " + account.getAccountId() + " created");
        return account;
    }

    public Account getAccount(UUID id) {
        for (Account account : accounts) {
            if (account.getAccountId().equals(id)) {
                return account;
            }
        }
        return null;
    }

    public void transferJson(String encryptedJson) {
        String json = EncryptionUtil.decryptedMessage(encryptedJson, secretKey); // descriptografa o json
        JSONUtil.TransferData transferData = JSONUtil.parseTransferJson(json); // converte o json para um objeto
        Account fromAccount = getAccount(transferData.getFrom()); // pega a conta de origem
        Account toAccount = getAccount(transferData.getTo()); // pega a conta de destino
        if (fromAccount != null && toAccount != null) { // se as contas existirem
            fromAccount.withdraw(transferData.getAmount()); // retira o dinheiro da conta de origem
            toAccount.deposit(transferData.getAmount()); // adiciona o dinheiro na conta de destino
        }
        registerTransaction(fromAccount.getAccountName(), toAccount.getAccountName(), transferData.getAmount(),
                "payment"); // registra a transação
    }

    public void registerTransaction(String from, String to, double amount, String transactionType) {
        ReportGenerator reportGenerator = new ReportGenerator();
        long timestampInNanoSeconds = System.nanoTime();
        reportGenerator.transactionReport("Timestamp: " + timestampInNanoSeconds + " | Transaction type: "
                + transactionType + " | From: " + from + " | To: " + to
                + " | Amount: " + amount);
    }

}
