package domain.users;

import domain.bank.Branch;
import domain.enums.UserRole;
import domain.enums.TransactionStatus;
import domain.transactions.Transaction;

import java.util.ArrayList;
import java.util.List;

public class UserAccount implements IUser {
    private String userID;
    private String name;
    private String email;
    private String passwordHash;
    private double balance;
    private Branch branch;
    private List<Transaction> transactionList;

    // ==Constructor==
    public UserAccount(String userID, String name, String email, String passwordHash, double balance, Branch branch) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.balance = balance;
        this.branch = branch;
        this.transactionList = new ArrayList<>();
    }

    @Override
    public String getUsername() {
        return this.email;
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

    public void deposit(double amount) {
        if (amount <= 0) {
            System.out.println("Amount must be positive");
            return;
        }
        Transaction depositTransaction = new Transaction(
                "TXN_D" + System.currentTimeMillis(),
                this.userID,
                null,
                amount,
                "DEPOSIT",
                java.time.LocalDateTime.now(),
                TransactionStatus.PENDING);
        depositTransaction.execute();
    }

    public void withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("Amount must be positive");
            return;
        }

        if (amount > this.balance) {
            System.out.println("Insufficient balance");
            return;
        }
        Transaction withdrawTransaction = new Transaction(
                "TXN_W" + System.currentTimeMillis(),
                this.userID,
                null,
                amount,
                "WITHDRAW",
                java.time.LocalDateTime.now(),
                TransactionStatus.PENDING);
        withdrawTransaction.execute();
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

        if (amount > this.balance) {
            System.out.println("Insufficient balance for transfer");
            return;
        }

        Transaction transferTransaction = new Transaction(
                "TXN_T" + System.currentTimeMillis(),
                this.userID,
                target.getUserID(),
                amount,
                "TRANSFER",
                java.time.LocalDateTime.now(),
                TransactionStatus.PENDING);
        transferTransaction.execute();
    }

    public double getBalance() {
        return this.balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public List<Transaction> getTransactionHistory() {
        return this.transactionList;
    }
}
