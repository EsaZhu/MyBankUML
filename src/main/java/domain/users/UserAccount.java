package domain.users;

import domain.bank.Branch;
import domain.enums.UserRole;
import domain.transactions.Transaction;

import java.util.ArrayList;
import java.util.List;

public class UserAccount implements IUser{
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
        this.balance += amount;

        // TODO:Add transaction object
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

        this.balance -= amount;

        // TODO:Add transaction object
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

        this.withdraw(amount);

        target.deposit(amount);

        // TODO:Add transaction object
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

}
