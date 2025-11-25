package domain.transactions;

import java.time.LocalDateTime;
import java.util.List;

public class Transaction {

    private String transactionID;
    private String sourceAccountID;
    private String receiverAccountID;
    private double amount;
    private String transactionType;
    private LocalDateTime transactionDateTime;
    private TransactionStatus status;

    public Transaction(String transactionID, String sourceAccountID, String receiverAccountID,
            double amount, String transactionType, LocalDateTime transactionDateTime, TransactionStatus status) {
        this.transactionID = transactionID;
        this.sourceAccountID = sourceAccountID;
        this.receiverAccountID = receiverAccountID;
        this.amount = amount;
        this.transactionType = transactionType;
        this.transactionDateTime = transactionDateTime;
        this.status = status;
    }

    public boolean execute() {
        if (this.validateTransaction()) {
            // withdraw amount from source account
            // deposit amount to receiver account
            this.status = TransactionStatus.COMPLETED;
            return true;
        }
        this.status = TransactionStatus.FAILED;
        return false;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public Transaction getTransaction(String transactionID) {
        // Placeholder for fetching transaction by ID
        return null;
    }

    public boolean validateTransaction() {
        // check if source account has sufficient funds
        // check if accounts are active
        return true;
    }

    public List<Transaction> getTransactionHistory(String accountID) {
        // Placeholder for fetching transaction history for an account
        return null;
    }

    public void reverseTransaction() {
        // deposit amount to source account
        // withdraw amount from receiver account
        this.status = TransactionStatus.REVERSED;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public double newBalance() {
        return 0.0;

    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
