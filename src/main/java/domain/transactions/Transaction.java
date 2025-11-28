package domain.transactions;

import java.time.LocalDateTime;
import java.util.List;

import database.Database;
import domain.enums.TransactionStatus;
import domain.users.Account;
import domain.users.UserAccount;

public class Transaction {

    private String transactionID;
    private String sourceUserID;
    private String sourceAccountID;
  
    private String receiverAccountID;
    private String receiverUserID;
    private double amount;
    private String transactionType;
    private LocalDateTime transactionDateTime;
    private TransactionStatus status;

    // No-arg constructor for reflection/deserialization
    public Transaction() {}

    /**
     * @param transactionID
     * @param sourceAccountID
     * @param receiverAccountID
     * @param amount
     * @param transactionType
     * @param transactionDateTime
     * @param status
     */
    public Transaction(String transactionID, String sourceUserID, String sourceAccountID, String receiverUserID,
            String receiverAccountID,
            double amount, String transactionType, LocalDateTime transactionDateTime, TransactionStatus status) {
        this.transactionID = transactionID;
        this.sourceUserID = sourceUserID;
        this.sourceAccountID = sourceAccountID;
        this.receiverUserID = receiverUserID;
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
    public static List<Transaction> getTransactionHistory(String userID, String accountID) {
        Database db = Database.getInstance();
        UserAccount user = (UserAccount) db.retrieveUserAccount(accountID);
        Account account = user.getAccounts().stream()
                .filter(acc -> acc.getAccountID().equals(accountID))
                .findFirst()
                .orElse(null);
        return account.getTransactions();
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
        UserAccount sourceUser = findUserByAccountId(this.sourceAccountID);
        Account sourceAccount = findAccountById(sourceUser, this.sourceAccountID);
        if (sourceAccount == null) {
            this.status = TransactionStatus.FAILED;
            return false;
        }
        switch (transactionType != null ? transactionType.toUpperCase() : "") {
            case "DEPOSIT":
                sourceAccount.setBalance(sourceAccount.getBalance() + this.amount);
                this.status = TransactionStatus.COMPLETED;
                sourceAccount.getTransactions().add(this);
                db.updateUserAccount(sourceUser.getUserID(), sourceUser);
                // db.updateAccount(this.sourceAccountID, sourceUser);
                return true;
            case "WITHDRAW":
                sourceAccount.setBalance(sourceAccount.getBalance() - this.amount);
                this.status = TransactionStatus.COMPLETED;
                sourceAccount.getTransactions().add(this);
                db.updateUserAccount(sourceUser.getUserID(), sourceUser);
                // db.updateAccount(this.sourceAccountID, sourceUser);
                return true;
            case "TRANSFER":
                boolean sameUser = sourceUser != null && this.receiverAccountID != null
                        && this.receiverAccountID.startsWith(sourceUser.getUserID());
                UserAccount receiverUser = sameUser ? sourceUser : findUserByAccountId(this.receiverAccountID);
                Account receiverAccount = findAccountById(receiverUser, this.receiverAccountID);
                if (receiverAccount == null) {
                    this.status = TransactionStatus.FAILED;
                    return false;
                }
                sourceAccount.setBalance(sourceAccount.getBalance() - this.amount);
                receiverAccount.setBalance(receiverAccount.getBalance() + this.amount);
                this.status = TransactionStatus.COMPLETED;
                sourceAccount.getTransactions().add(this);
                receiverAccount.getTransactions().add(this);
                // If same user, update once; otherwise update both users
                db.updateUserAccount(sourceUser.getUserID(), sourceUser);
                if (!sameUser && receiverUser != null) {
                    db.updateUserAccount(receiverUser.getUserID(), receiverUser);
                }
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
        UserAccount sourceUser = findUserByAccountId(this.sourceAccountID);
        Account sourceAccount = findAccountById(sourceUser, this.sourceAccountID);
        switch (transactionType != null ? transactionType.toUpperCase() : "") {
            case "DEPOSIT":
                return sourceAccount != null;
            case "WITHDRAW":
                return sourceAccount != null && sourceAccount.getBalance() >= this.amount;
            case "TRANSFER":
                if (sourceAccount == null) return false;
                UserAccount receiverUser = findUserByAccountId(this.receiverAccountID);
                Account receiverAccount = findAccountById(receiverUser, this.receiverAccountID);
                if (receiverAccount == null) {
                    return false;
                }
                return sourceAccount.getBalance() >= this.amount;
            default:
                return false;
        }
    }

    /**
     * Function which reverses a transaction
     * Essentially performs the opposite operation of execute()
     * 
     */
    public boolean reverseTransaction() {
        if (!this.validateTransaction()) {
            this.status = TransactionStatus.FAILED;
            return false;
        }
        Database db = Database.getInstance();
        UserAccount sourceUser = findUserByAccountId(this.sourceAccountID);
        Account sourceAccount = findAccountById(sourceUser, this.sourceAccountID);
        if (sourceAccount == null) {
            this.status = TransactionStatus.FAILED;
            return false;
        }
        switch (transactionType) {
            case "DEPOSIT":
                sourceAccount.setBalance(sourceAccount.getBalance() - this.amount);
                this.status = TransactionStatus.REVERSED;
                sourceAccount.getTransactions().add(this);
                db.updateUserAccount(sourceUser.getUserID(), sourceUser);
                // db.updateAccount(this.sourceAccountID, sourceUser);
                return true;
            case "WITHDRAW":
                sourceAccount.setBalance(sourceAccount.getBalance() + this.amount);
                this.status = TransactionStatus.REVERSED;
                sourceAccount.getTransactions().add(this);
                db.updateUserAccount(sourceUser.getUserID(), sourceUser);
                // db.updateAccount(this.sourceAccountID, sourceUser);
                return true;
            case "TRANSFER":
                UserAccount receiverUser = findUserByAccountId(this.receiverAccountID);
                Account receiverAccount = findAccountById(receiverUser, this.receiverAccountID);
                if (receiverAccount == null) {
                    this.status = TransactionStatus.FAILED;
                    return false;
                }
                sourceAccount.setBalance(sourceAccount.getBalance() + this.amount);
                receiverAccount.setBalance(receiverAccount.getBalance() - this.amount);
                this.status = TransactionStatus.REVERSED;
                sourceAccount.getTransactions().add(this);
                receiverAccount.getTransactions().add(this);
                db.updateUserAccount(sourceUser.getUserID(), sourceUser);
                db.updateUserAccount(receiverUser.getUserID(), receiverUser);
                // db.updateAccount(this.sourceAccountID, sourceUser);
                // db.updateAccount(this.receiverAccountID, receiverUser);
                return true;
            default:
                this.status = TransactionStatus.FAILED;
                return false;
        }
    }

    /*-------------------- Getters and Setters --------------------*/

    public String getTransactionID() {
        return transactionID;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public LocalDateTime getTransactionDateTime() {
        return transactionDateTime;
    }

    public String getSourceUserID() {
        return sourceUserID;
    }

    public String getSourceAccountID() {
        return sourceAccountID;
    }

    public String getReceiverUserID() {
        return receiverUserID;
    }

    public String getReceiverAccountID() {
        return receiverAccountID;
    }

    public double getAmount() {
        return amount;
    }

    private UserAccount findUserByAccountId(String accountId) {
        if (accountId == null) return null;
        String userId = accountId.contains("-") ? accountId.substring(0, accountId.indexOf("-")) : accountId;
        Database db = Database.getInstance();
        return db.retrieveUserAccount(userId);
    }

    private Account findAccountById(UserAccount user, String accountId) {
        if (user == null || accountId == null) return null;
        if (user.getAccounts() == null) return null;
        return user.getAccounts().stream()
                .filter(acc -> acc.getAccountID().equals(accountId))
                .findFirst()
                .orElse(null);
    }
}
