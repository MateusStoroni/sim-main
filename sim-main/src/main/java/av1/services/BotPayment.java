package av1.services;

import java.util.UUID;

import javax.crypto.SecretKey;

import av1.bank.AlphaBank;
import av1.utils.EncryptionUtil;
import av1.utils.JSONUtil;

public class BotPayment extends Thread {
    private double kmPrice = 3.25;
    private double gasPrice = 4.87;
    private AlphaBank alphaBank;
    private SecretKey secretKey;

    public BotPayment(AlphaBank alphaBank, SecretKey secretKey) {
        this.secretKey = secretKey;
        this.alphaBank = alphaBank;
    }

    public void payDriver(UUID from, UUID to, double km) {
        double amount = km * kmPrice;
        String json = JSONUtil.createTransferJson(from, to, amount);
        try {
            String encryptedJson = EncryptionUtil.encrypt(json, secretKey);
            alphaBank.transferJson(encryptedJson);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void payFuelStation(UUID from, UUID to, double liters) {
        double amount = liters * gasPrice;
        String json = JSONUtil.createTransferJson(from, to, amount);
        try {
            String encryptedJson = EncryptionUtil.encrypt(json, secretKey);
            alphaBank.transferJson(encryptedJson);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
