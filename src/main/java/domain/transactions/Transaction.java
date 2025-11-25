package domain.transactions;

import org.bson.Document;

import java.time.LocalDateTime;
import java.util.List;

import database.Database;
import domain.users.UserAccount;

public class Transaction {

    private String transactionID;
    private String sourceAccountID;
    private String receiverAccountID;
    private double amount;
    private String transactionType;
    private LocalDateTime transactionDateTime;
    private TransactionStatus status;

    /**
     * @param transactionID
     * @param sourceAccountID
     * @param receiverAccountID
     * @param amount
     * @param transactionType
     * @param transactionDateTime
     * @param status
     */
    public Transaction(String transactionID, String sourceAccountID, String receiverAccountID,
            double amount, String transactionType, LocalDateTime transactionDateTime, TransactionStatus status) {
        this.transactionID = transactionID;
        this.sourceAccountID = sourceAccountID;
        this.receiverAccountID = receiverAccountID;
        this.amount = amount;
        this.transactionType = transactionType;
        this.transactionDateTime = transactionDateTime;
        this.status = TransactionStatus.PENDING;
    }

    /*-------------------- Static methods --------------------*/
    /**
     * Static method to get a transaction by its ID
     *
     * @param transactionID
     * @return
     */
    public static Transaction getTransaction(String transactionID) {
        Document documentTransaction = Database.retrieveTransaction(transactionID);
        return Database.documentToTransaction(documentTransaction);
    }

    /**
     * Static method to get transaction history for a given account
     *
     * @param accountID
     * @return
     */
    public static List<Transaction> getTransactionHistory(String accountID) {
        Document documentAccount = Database.retrieveAccount(accountID);
        UserAccount user = Database.documentToUserAccount(documentAccount);
        return user.getTransactionHistory();
    }

    /*-------------------- Instance methods --------------------*/
    /**
     * Executes the transaction, adds to users transaction history
     *
     * @return
     */
    public boolean execute() {
        if (!this.validateTransaction()) {
            this.status = TransactionStatus.FAILED;
            return false;
        }
        Document documentSourceAccount = Database.retrieveAccount(this.sourceAccountID);
        UserAccount sourceUser = Database.documentToUserAccount(documentSourceAccount);
        switch (transactionType) {
            case "WITHDRAW":
                sourceUser.withdraw(this.amount);
                this.status = TransactionStatus.COMPLETED;
                sourceUser.getTransactionHistory().add(this);
                Database.updateAccount(this.sourceAccountID, new Document("transactionHistory", sourceUser.getTransactionHistory()));
                return true;
            case "DEPOSIT":
                sourceUser.deposit(this.amount); 
                this.status = TransactionStatus.COMPLETED;
                sourceUser.getTransactionHistory().add(this);
                Database.updateAccount(this.sourceAccountID, new Document("transactionHistory", sourceUser.getTransactionHistory()));
                return true;
            case "TRANSFER":
                Document documentReceiverAccount = Database.retrieveAccount(this.receiverAccountID);
                UserAccount receiverUser = Database.documentToUserAccount(documentReceiverAccount);
                sourceUser.withdraw(this.amount);
                receiverUser.deposit(this.amount);
                this.status = TransactionStatus.COMPLETED;
                sourceUser.getTransactionHistory().add(this);
                receiverUser.getTransactionHistory().add(this);
                Database.updateAccount(this.sourceAccountID, new Document("transactionHistory", sourceUser.getTransactionHistory()));
                Database.updateAccount(this.receiverAccountID, new Document("transactionHistory", receiverUser.getTransactionHistory()));
                return true;
            default:
                this.status = TransactionStatus.FAILED;
                return false;
        }
    }

    /**
     * Function which validates if transaction can be executed
     *
     * @return
     */
    public boolean validateTransaction() {
        Document documentSourceAccount = Database.retrieveAccount(this.sourceAccountID);
        // WITHDRAW/TRANSFER: check if source account has sufficient balance
        if (documentSourceAccount.getDouble("balance") < this.amount) {
            return false;
        }
        return true;
    }

    /**
     * Function which reverses a transaction
     * Essentially performs the opposite operation of execute()
     * 
     */
    public void reverseTransaction() {
        if (!this.validateTransaction()) {
            this.status = TransactionStatus.FAILED;
            return false;
        }
        Document documentSourceAccount = Database.retrieveAccount(this.sourceAccountID);
        UserAccount sourceUser = Database.documentToUserAccount(documentSourceAccount);
        switch (transactionType) {
            case "WITHDRAW":
                sourceUser.deposit(this.amount);
                this.status = TransactionStatus.REVERSED;
                sourceUser.getTransactionHistory().add(this);
                Database.updateAccount(this.sourceAccountID, new Document("transactionHistory", sourceUser.getTransactionHistory()));
                return true;
            case "DEPOSIT":
                sourceUser.withdraw(this.amount);
                this.status = TransactionStatus.REVERSED;
                sourceUser.getTransactionHistory().add(this);
                Database.updateAccount(this.sourceAccountID, new Document("transactionHistory", sourceUser.getTransactionHistory()));
                return true;
            case "TRANSFER":
                Document documentReceiverAccount = Database.retrieveAccount(this.receiverAccountID);
                UserAccount receiverUser = Database.documentToUserAccount(documentReceiverAccount);
                sourceUser.deposit(this.amount);
                receiverUser.withdraw(this.amount);
                this.status = TransactionStatus.REVERSED;
                sourceUser.getTransactionHistory().add(this);
                receiverUser.getTransactionHistory().add(this);
                Database.updateAccount(this.sourceAccountID, new Document("transactionHistory", sourceUser.getTransactionHistory()));
                Database.updateAccount(this.receiverAccountID, new Document("transactionHistory", receiverUserUser.getTransactionHistory()));
                return true;
            default:
                this.status = TransactionStatus.FAILED;
                return false;
        }
    }


    /**
     * Calculates and directly modifies new balance for receiver after transaction in the db
     * @return
     */
    public double newBalance() {
        Document documentReceiverAccount = Database.retrieveAccount(this.receiverAccountID);
        if (documentReceiverAccount == null) {
            return -1;
        }
        double oldBalance = documentReceiverAccount.getDouble("balance");
        double newBalance = oldBalance + this.amount;
        Database.updateAccount(this.receiverAccountID, new Document("balance", newBalance);
        return newBalance;
    }

    /*-------------------- Getters and Setters --------------------*/
    public String getTransactionID() {
        return transactionID;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
}