package app.vcampus.server.utility;

/**
 * 校园卡信息类。
 * 用于封装校园卡的卡号、状态和余额信息。
 */
public class CardInfo {
    /**
     * 校园卡的卡号。
     */
    private String cardNumber;
    /**
     * 校园卡的状态 (例如, "正常", "挂失")。
     */
    private String status;
    /**
     * 校园卡的余额。
     */
    private double balance;

    /**
     * 构造一个新的校园卡信息对象。
     *
     * @param cardNumber 卡号。
     * @param status     状态。
     * @param balance    余额。
     */
    public CardInfo(String cardNumber, String status, double balance) {
        this.cardNumber = cardNumber;
        this.status = status;
        this.balance = balance;
    }

    /**
     * 获取卡号。
     *
     * @return 卡号。
     */
    public String getCardNumber() {
        return cardNumber;
    }

    /**
     * 设置卡号。
     *
     * @param cardNumber 新的卡号。
     */
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    /**
     * 获取状态。
     *
     * @return 状态。
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置状态。
     *
     * @param status 新的状态。
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取余额。
     *
     * @return 余额。
     */
    public double getBalance() {
        return balance;
    }

    /**
     * 设置余额。
     *
     * @param balance 新的余额。
     */
    public void setBalance(double balance) {
        this.balance = balance;
    }
}