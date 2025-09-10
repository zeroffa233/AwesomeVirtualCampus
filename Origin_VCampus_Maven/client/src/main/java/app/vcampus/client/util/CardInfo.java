package app.vcampus.client.util;

/**
 * Represents the information of a campus card.
 */
public class CardInfo {
    private String cardNumber;
    private String status;
    private double balance;

    public CardInfo(String cardNumber, String status, double balance) {
        this.cardNumber = cardNumber;
        this.status = status;
        this.balance = balance;
    }

    // --- Getters and Setters ---
    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}