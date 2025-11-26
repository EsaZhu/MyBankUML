package domain.users;

import database.Database;
import domain.bank.Branch;
import org.bson.Document;
import com.mongodb.client.model.Filters;

import domain.enums.UserRole;
import domain.enums.TransactionStatus;
import domain.transactions.Transaction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserAccount implements IUser {
    private String userID;
    private String username;
    private String firstName;
    private String lastName;
    private String passwordHash;
    private String branchId;
    private double balance;
    private Account[] accounts;
    private List<Transaction> transactionList;
    private final Database database = Database.getInstance();

    // ==Constructor==
    public UserAccount(String userID,
                       String username,
                       String firstName,
                       String LastName,
                       String passwordHash,
                       String branchId,
                       Account[] accounts) {
        this.userID = userID;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.passwordHash = passwordHash;
        this.branchId = branchId;
        this.balance = 0.0;
        this.accounts = accounts;
        this.transactionList = new ArrayList<>();
    }

    // Legacy constructor to support old signatures (name/email -> username/firstName)
    public UserAccount(String userID,
                       String name,
                       String email,
                       String passwordHash,
                       double balance,
                       Branch branch) {
        this.userID = userID;
        this.username = name;
        this.firstName = name;
        this.lastName = "";
        this.passwordHash = passwordHash;
        this.branchId = branch != null ? branch.getBranchID() : null;
        this.balance = balance;
        this.accounts = null;
        this.transactionList = new ArrayList<>();
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getPasswordHash() {
        return this.passwordHash;
    }

    @Override
    public UserRole getRole() {
        return UserRole.USER;
    }

    public String getUserID() {
        return userID;
    }

    public String getBranchId() {
        return branchId;
    }

    public double getBalance() {
        return getCurrentBalance(this.userID);
    }

    public void setBalance(double newBalance) {
        this.balance = newBalance;
        database.updateAccount(this.userID, new Document("balance", newBalance));
    }

    public Account[] getAccounts() {
        return accounts;
    }

    public List<Transaction> getTransactionHistory() {
        return transactionList;
    }

    public void deposit(double amount) {
        if (amount <= 0) {
            System.out.println("Amount must be positive");
            return;
        }

        double currentBalance = getCurrentBalance(this.userID);
        double newBalance = currentBalance + amount;
        database.updateAccount(this.userID, new Document("balance", newBalance));

        Transaction depositTransaction = new Transaction(
                "TXN_D" + System.currentTimeMillis(),
                this.userID,
                null,
                amount,
                "DEPOSIT",
                LocalDateTime.now(),
                TransactionStatus.PENDING
        );
        database.addTransaction(transactionToDocument(depositTransaction));
        transactionList.add(depositTransaction);
    }

    public void withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("Amount must be positive");
            return;
        }

        double currentBalance = getCurrentBalance(this.userID);
        if (currentBalance < amount) {
            System.out.println("Insufficient funds");
            return;
        }
        double newBalance = currentBalance - amount;
        database.updateAccount(this.userID, new Document("balance", newBalance));

        Transaction withdrawTransaction = new Transaction(
                "TXN_W" + System.currentTimeMillis(),
                this.userID,
                null,
                amount,
                "WITHDRAW",
                LocalDateTime.now(),
                TransactionStatus.PENDING
        );

        database.addTransaction(transactionToDocument(withdrawTransaction));
        transactionList.add(withdrawTransaction);
    }

    public void transferFunds(UserAccount target, double amount) {
        if (target == null) {
            System.out.println("Target account cannot be null");
            return;
        }

        if (amount <= 0) {
            System.out.println("Amount must be positive");
            return;
        }

        double sourceBalance = getCurrentBalance(this.userID);
        if (sourceBalance < amount) {
            System.out.println("Insufficient funds");
            return;
        }
        double targetBalance = target != null ? getCurrentBalance(target.getUserID()) : 0.0;

        database.updateAccount(this.userID, new Document("balance", sourceBalance - amount));
        if (target != null) {
            database.updateAccount(target.getUserID(), new Document("balance", targetBalance + amount));
        }

        Transaction transferTransaction = new Transaction(
                "TXN_T" + System.currentTimeMillis(),
                this.userID,
                target.getUserID(),
                amount,
                "TRANSFER",
                LocalDateTime.now(),
                TransactionStatus.PENDING
        );

        database.addTransaction(transactionToDocument(transferTransaction));
        transactionList.add(transferTransaction);
    }

    private double getCurrentBalance(String id) {
        Document doc = database.getAccountCollection().find(Filters.eq("userID", id)).first();
        if (doc == null) return 0.0;
        Double bal = doc.getDouble("balance");
        return bal != null ? bal : 0.0;
    }

    private Document transactionToDocument(Transaction txn) {
        return new Document()
                .append("transactionID", txn.getTransactionID())
                .append("sourceAccountID", txn.getSourceAccountID())
                .append("receiverAccountID", txn.getReceiverAccountID())
                .append("amount", txn.getAmount())
                .append("transactionType", txn.getTransactionType())
                .append("transactionDateTime", txn.getTransactionDateTime() != null ? txn.getTransactionDateTime().toString() : null)
                .append("status", txn.getStatus() != null ? txn.getStatus().toString() : null);
    }
    
}
