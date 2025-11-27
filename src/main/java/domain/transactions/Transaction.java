package domain.transactions;

import java.time.LocalDateTime;
import java.util.List;

import database.Database;
import domain.enums.TransactionStatus;
import domain.users.Account;

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
        Database db = Database.getInstance();
        return db.retrieveTransaction(transactionID);
    }

    /**
     * Static method to get transaction history for a given account
     *
     * @param accountID
     * @return
     */
    public static List<Transaction> getTransactionHistory(String accountID) {
        Database db = Database.getInstance();
        Account user = (Account) db.retrieveAccount(accountID);
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
        Database db = Database.getInstance();
        Account sourceUser = (Account) db.retrieveAccount(this.sourceAccountID);
        switch (transactionType) {
            case "DEPOSIT":
                sourceUser.setBalance(sourceUser.getBalance() + this.amount);
                this.status = TransactionStatus.COMPLETED;
                sourceUser.getTransactionHistory().add(this);
                db.updateAccount(this.sourceAccountID, sourceUser);
                return true;
            case "WITHDRAW":
                sourceUser.setBalance(sourceUser.getBalance() - this.amount);
                this.status = TransactionStatus.COMPLETED;
                sourceUser.getTransactionHistory().add(this);
                db.updateAccount(this.sourceAccountID, sourceUser);
                return true;
            case "TRANSFER":
                Account receiverUser = (Account) db.retrieveAccount(this.receiverAccountID);
                sourceUser.setBalance(sourceUser.getBalance() - this.amount);
                receiverUser.setBalance(receiverUser.getBalance() + this.amount);
                this.status = TransactionStatus.COMPLETED;
                sourceUser.getTransactionHistory().add(this);
                receiverUser.getTransactionHistory().add(this);
                db.updateAccount(this.sourceAccountID, sourceUser);
                db.updateAccount(this.receiverAccountID, receiverUser);
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
        Database db = Database.getInstance();
        Account sourceUser = (Account) db.retrieveAccount(this.sourceAccountID);
        switch (transactionType) {
            case "WITHDRAW":
                return sourceUser.getBalance() >= this.amount;
            case "TRANSFER":
                Account receiverUser = (Account) db.retrieveAccount(this.receiverAccountID);
                if (receiverUser == null) {
                    return false;
                }
                return sourceUser.getBalance() >= this.amount;
            default:
                return false;
        }
    }

    /**
     * Function which reverses a transaction
     * Essentially performs the opposite operation of execute()
     * 
     */
    public void reverseTransaction() {
        if (!this.validateTransaction()) {
            this.status = TransactionStatus.FAILED;
        }
        Database db = Database.getInstance();
        Account sourceUser = (Account) db.retrieveAccount(this.sourceAccountID);
        switch (transactionType) {
            case "DEPOSIT":
                sourceUser.setBalance(sourceUser.getBalance() - this.amount);
                this.status = TransactionStatus.REVERSED;
                sourceUser.getTransactionHistory().add(this);
                db.updateAccount(this.sourceAccountID, sourceUser);
            case "WITHDRAW":
                sourceUser.setBalance(sourceUser.getBalance() + this.amount);
                this.status = TransactionStatus.REVERSED;
                sourceUser.getTransactionHistory().add(this);
                db.updateAccount(this.sourceAccountID, sourceUser);
            case "TRANSFER":
                Account receiverUser = (Account) db.retrieveAccount(this.receiverAccountID);
                sourceUser.setBalance(sourceUser.getBalance() + this.amount);
                receiverUser.setBalance(receiverUser.getBalance() - this.amount);
                this.status = TransactionStatus.REVERSED;
                sourceUser.getTransactionHistory().add(this);
                receiverUser.getTransactionHistory().add(this);
                db.updateAccount(this.sourceAccountID, sourceUser);
                db.updateAccount(this.receiverAccountID, receiverUser);
            default:
                this.status = TransactionStatus.FAILED;
        }
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

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
}