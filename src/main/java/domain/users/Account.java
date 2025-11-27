package domain.users;

import domain.enums.TransactionStatus;
import domain.transactions.Transaction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class Account {
    protected String userID;
    protected String accountStrHeader;
    protected double balance;
    protected List<Transaction> transactions;

    public Account(String userID, String accountStrHeader, double balance) {
        this.userID = userID;
        this.accountStrHeader = accountStrHeader;
        this.balance = balance;
        this.transactions = new ArrayList<>();
    }

    /**
     * Method to deposit amount
     * @param amount
     */
    public void deposit(double amount) {
        if (amount <= 0) {
            System.out.println("Amount must be positive");
            return;
        }

        Transaction depositTransaction = new Transaction(
                "TXN_" + this.accountStrHeader + "_D" + System.currentTimeMillis(),
                this.userID,
                null,
                amount,
                "DEPOSIT",
                LocalDateTime.now(),
                TransactionStatus.PENDING
        );

        depositTransaction.execute();
        transactions.add(depositTransaction);
    }

    /**
     * Method to withdraw amount
     * @param amount
     */
    public void withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("Amount must be positive");
            return;
        }

        Transaction withdrawTransaction = new Transaction(
                "TXN_" + this.accountStrHeader + "_W" + System.currentTimeMillis(),
                this.userID,
                null,
                amount,
                "WITHDRAW",
                LocalDateTime.now(),
                TransactionStatus.PENDING
        );

        withdrawTransaction.execute();
        transactions.add(withdrawTransaction);
    }

    /**
     * Method to transfer funds to another account
     * @param target
     * @param amount
     */
    public void transferFunds(UserAccount target, double amount) {
        if (target == null) {
            System.out.println("Target account cannot be null");
            return;
        }

        if (amount <= 0) {
            System.out.println("Amount must be positive");
            return;
        }

        Transaction transferTransaction = new Transaction(
                "TXN_" + this.accountStrHeader + "_T" + System.currentTimeMillis(),
                this.userID,
                target.getUserID(),
                amount,
                "TRANSFER",
                LocalDateTime.now(),
                TransactionStatus.PENDING
        );
        transferTransaction.execute();
    }

    /*-------------------- Getters and Setters --------------------*/

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public double getBalance(){
        return this.balance;
    }

    public void setBalance(double amount){
        this.balance = amount;
    }
}

