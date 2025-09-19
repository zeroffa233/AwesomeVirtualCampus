package app.vcampus.server.controller;

import app.vcampus.server.entity.CardTransaction;
import app.vcampus.server.entity.FinanceCard;
import app.vcampus.server.enums.CardStatus;
import app.vcampus.server.enums.TransactionType;
import app.vcampus.server.utility.Request;
import app.vcampus.server.utility.Response;
import app.vcampus.server.utility.router.RouteMapping;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 财务控制器。
 * 处理与一卡通金融相关的操作，如查询、充值、消费、状态更新和交易记录查询。
 */
@Slf4j
public class FinanceController {

    /**
     * 财务工作人员获取指定卡号的金融信息。
     *
     * @param request  包含卡号的请求。
     * @param database 数据库会话。
     * @return 包含卡信息的响应或错误信息。
     */
    @RouteMapping(uri = "finance/info", role = "finance_staff")
    public Response getCardInfo(Request request, org.hibernate.Session database) {
        String cardNumberStr = request.getParams().get("cardNumber");
        if (cardNumberStr == null) {
            return Response.Common.error("Card number is required");
        }

        try {
            Integer cardNumber = Integer.parseInt(cardNumberStr);
            FinanceCard card = database.get(FinanceCard.class, cardNumber);

            if (card == null) {
                return Response.Common.error("Card not found");
            }

            Map<String, Object> cardInfo = Map.of(
                    "cardNumber", card.getCardNumber().toString(),
                    "status", card.getStatus().getLabel(),
                    "balance", card.getBalance() / 100.0
            );

            return Response.Common.ok(cardInfo);
        } catch (NumberFormatException e) {
            return Response.Common.error("Invalid card number format");
        }
    }

    /**
     * 财务工作人员为指定一卡通充值。
     *
     * @param request  包含卡号、金额和描述的请求。
     * @param database 数据库会话。
     * @return 操作成功或失败的响应。
     */
    @RouteMapping(uri = "finance/debit")
    public Response debitCard(Request request, org.hibernate.Session database) {
        String cardNumberStr = request.getParams().get("cardNumber");
        String amountStr = request.getParams().get("amount");
        String description = request.getParams().get("description");

        if (cardNumberStr == null || amountStr == null) {
            return Response.Common.error("Card number and amount are required");
        }

        try {
            Integer cardNumber = Integer.parseInt(cardNumberStr);
            double amount = Double.parseDouble(amountStr);

            if (amount <= 0) {
                return Response.Common.error("Recharge amount must be positive");
            }

            FinanceCard card = database.get(FinanceCard.class, cardNumber);
            if (card == null) {
                return Response.Common.error("Card not found");
            }
            if (card.getStatus() == CardStatus.frozen) {
                return Response.Common.error("Cannot recharge a frozen card");
            }

            Transaction tx = database.beginTransaction();
            try {
                int amountInCents = (int) Math.round(amount * 100);
                card.setBalance(card.getBalance() + amountInCents);
                database.merge(card);

                CardTransaction transaction = new CardTransaction();
                transaction.setCardNumber(cardNumber);
                transaction.setAmount(amountInCents);
                transaction.setTime(new Date());
                transaction.setType(TransactionType.deposit);
                transaction.setDescription(description);
                database.persist(transaction);
                tx.commit();
                return Response.Common.ok();
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                log.error("Recharge failed for card: " + cardNumber, e);
                return Response.Common.error("Recharge failed due to a server error");
            }

        } catch (NumberFormatException e) {
            return Response.Common.error("Invalid number format for card number or amount");
        }
    }

    /**
     * 财务工作人员为指定一卡通进行消费操作。
     *
     * @param request  包含卡号、金额和描述的请求。
     * @param database 数据库会话。
     * @return 操作成功或失败的响应。
     */
    @RouteMapping(uri = "finance/credit")
    public Response creditCard(Request request, org.hibernate.Session database) {
        String cardNumberStr = request.getParams().get("cardNumber");
        String amountStr = request.getParams().get("amount");
        String description = request.getParams().get("description");

        if (cardNumberStr == null || amountStr == null || description == null) {
            return Response.Common.error("Card number, amount, and description are required");
        }

        try {
            Integer cardNumber = Integer.parseInt(cardNumberStr);
            double amount = Double.parseDouble(amountStr);

            if (amount <= 0) {
                return Response.Common.error("Payment amount must be positive");
            }

            FinanceCard card = database.get(FinanceCard.class, cardNumber);
            if (card == null) {
                return Response.Common.error("Card not found");
            }
            if (card.getStatus() == CardStatus.frozen) {
                return Response.Common.error("Cannot process payment for a frozen card");
            }

            int amountInCents = (int) Math.round(amount * 100);
            if (card.getBalance() < amountInCents) {
                return Response.Common.error("Insufficient balance");
            }

            Transaction tx = database.beginTransaction();
            try {
                card.setBalance(card.getBalance() - amountInCents);
                database.merge(card);

                CardTransaction transaction = new CardTransaction();
                transaction.setCardNumber(cardNumber);
                transaction.setAmount(amountInCents);
                transaction.setTime(new Date());
                transaction.setType(TransactionType.payment);
                transaction.setDescription(description);
                database.persist(transaction);

                tx.commit();
                return Response.Common.ok();
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                log.error("Payment failed for card: " + cardNumber, e);
                return Response.Common.error("Payment failed due to a server error");
            }

        } catch (NumberFormatException e) {
            return Response.Common.error("Invalid number format for card number or amount");
        }
    }


    /**
     * 财务工作人员更新一卡通的状态。
     *
     * @param request  包含卡号和新状态的请求。
     * @param database 数据库会话。
     * @return 操作成功或失败的响应。
     */
    @RouteMapping(uri = "finance/updateStatus")
    public Response updateCardStatus(Request request, org.hibernate.Session database) {
        String cardNumberStr = request.getParams().get("cardNumber");
        String newStatusStr = request.getParams().get("newStatus");

        if (cardNumberStr == null || newStatusStr == null) {
            return Response.Common.error("Card number and new status are required");
        }

        try {
            Integer cardNumber = Integer.parseInt(cardNumberStr);
            FinanceCard card = database.get(FinanceCard.class, cardNumber);
            if (card == null) {
                return Response.Common.error("Card not found");
            }

            CardStatus newStatus = Arrays.stream(CardStatus.values())
                    .filter(status -> status.getLabel().equals(newStatusStr))
                    .findFirst()
                    .orElse(null);

            if (newStatus == null) {
                return Response.Common.error("Invalid status value: " + newStatusStr);
            }

            Transaction tx = database.beginTransaction();
            try {
                card.setStatus(newStatus);
                database.merge(card);
                tx.commit();
                return Response.Common.ok();
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                log.error("Failed to update status for card: " + cardNumber, e);
                return Response.Common.error("Status update failed due to a server error");
            }

        } catch (NumberFormatException e) {
            return Response.Common.error("Invalid card number format");
        }
    }

    /**
     * 获取当前用户的余额。
     *
     * @param request  请求对象。
     * @param database 数据库会话。
     * @return 包含余额信息的响应。
     */
    @RouteMapping(uri = "finance/balance")
    public Response getBalance(Request request, org.hibernate.Session database) {
        Integer cardNumber = request.getSession().getCardNum();
        FinanceCard card = database.get(FinanceCard.class, cardNumber);

        if (card == null) {
            Transaction tx = database.beginTransaction();
            card = new FinanceCard();
            card.setCardNumber(cardNumber);
            card.setBalance(0);
            card.setStatus(CardStatus.normal);
            database.persist(card);
            tx.commit();
        }

        return Response.Common.ok(Map.of("balance", card.getBalance() / 100.0));
    }

    /**
     * 获取当前用户的交易记录。
     *
     * @param request  请求对象。
     * @param database 数据库会话。
     * @return 包含交易记录列表的响应。
     */
    @RouteMapping(uri = "finance/transactions")
    public Response getTransactionHistory(Request request, org.hibernate.Session database) {
        int cardNumber = request.getSession().getCardNum();

        String hql = "FROM CardTransaction WHERE cardNumber = :cardNumber ORDER BY time DESC";
        Query<CardTransaction> query = database.createQuery(hql, CardTransaction.class);
        query.setParameter("cardNumber", cardNumber);
        List<CardTransaction> transactions = query.list();

        List<Map<String, Object>> displayableTransactions = transactions.stream().map(tx -> {
            return Map.<String, Object>of(
                    "time", tx.getTime().getTime(),
                    "type", tx.getType().toString(),
                    "amount", tx.getAmount() / 100.0,
                    "description", tx.getDescription()
            );
        }).collect(Collectors.toList());

        return Response.Common.ok(displayableTransactions);
    }
}