//FinanceController.java
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

@Slf4j
public class FinanceController {

    private final Gson gson = new Gson();

    /**
     * Handles the request for card information from a staff client.
     * Corresponds to FinanceClient.findCardInfo
     *
     * @param request the request from the client
     * @param database the database session
     * @return a response containing the card info or an error
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

            // Use the label from the enum directly
            Map<String, Object> cardInfo = Map.of(
                    "cardNumber", card.getCardNumber().toString(),
                    "status", card.getStatus().getLabel(), // Optimized
                    "balance", card.getBalance() / 100.0 // Convert cents to yuan
            );

            return Response.Common.ok(cardInfo);
        } catch (NumberFormatException e) {
            return Response.Common.error("Invalid card number format");
        }
    }

    /**
     * Handles card recharge requests from a staff client.
     * Corresponds to FinanceClient.debit
     *
     * @param request the request from the client
     * @param database the database session
     * @return a success or error response
     */
    @RouteMapping(uri = "finance/debit", role = "finance_staff")
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
     * Handles card payment requests from a staff client.
     * Corresponds to FinanceClient.credit
     *
     * @param request the request from the client
     * @param database the database session
     * @return a success or error response
     */
    @RouteMapping(uri = "finance/credit", role = "finance_staff")
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
     * Updates the status of a finance card.
     * Corresponds to FinanceClient.updateCardStatus
     *
     * @param request the request from the client
     * @param database the database session
     * @return a success or error response
     */
    @RouteMapping(uri = "finance/updateStatus", role = "finance_staff")
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

            // Convert string status to CardStatus enum by matching label
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
     * Gets the balance for the current user's card.
     * Corresponds to FinanceClient.getBalance
     *
     * @param request the request from the client
     * @param database the database session
     * @return a response containing the balance
     */
    @RouteMapping(uri = "finance/balance", role = "finance_user")
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
     * Gets the transaction history for the current user's card.
     * Corresponds to FinanceClient.getTransactionHistory
     *
     * @param request the request from the client
     * @param database the database session
     * @return a response containing a list of transactions
     */
    @RouteMapping(uri = "finance/transactions", role = "finance_user")
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