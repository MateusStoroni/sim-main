package av1.bank;

import java.util.UUID;

public class Account extends Thread {
    private double balance;
    private double lastBalance;
    private UUID id;
    private String name;

    private String password;
    private boolean receivedPayment = false;

    public Account(double balance, String name, String password) {
        this.name = name;
        this.balance = balance;
        this.lastBalance = balance;
        this.id = UUID.randomUUID();
        this.password = password;
        System.out.println("Account created. Id: " + this.id + " | Name: " + this.name + " | Balance: " + this.balance
                + " | Password: " + this.password);
    }

    public UUID getAccountId() {
        return this.id;
    }

    public void deposit(double amount) {
        this.balance += amount;
        System.out.println("Account " + this.name + ": deposit " + amount + ". Balance: " + this.balance);
    }

    public void withdraw(double amount) {
        this.balance -= amount;
        System.out.println("Account " + this.name + ": withdraw " + amount + ". Balance: " + this.balance);
    }

    public double getBalance() {
        return this.balance;
    }

    public double getPayment() {
        return balance - this.lastBalance;
    }

    public double updateBalance() {
        this.lastBalance = this.balance;
        this.receivedPayment = false;
        return this.lastBalance;
    }

    public boolean shouldUpdateBalance() {
        return receivedPayment;
    }

    public String getAccountName() {
        return name;
    }

    public void run() {
        while (true) {
            try {
                if (this.balance != this.lastBalance) {
                    this.receivedPayment = true;
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
