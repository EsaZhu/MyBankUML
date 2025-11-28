package domain.users;

import domain.enums.TransactionStatus;
import domain.transactions.Transaction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import database.Database;


public abstract class Account {
    protected String userID;
    protected String accountID;
    protected String accountHeader;
    protected double balance;
    protected List<Transaction> transactions;

    public Account(String userID, String accountID, String accountHeader, double balance) {
        this.userID = userID;
        this.accountID = accountID;
        this.accountHeader = accountHeader;
        this.balance = balance;
        this.transactions = new ArrayList<>();
    }

    /**
     * Method to deposit amount
     * 
     * @param amount
     */
    public void deposit(double amount) {
        if (amount <= 0) {
            System.out.println("Amount must be positive");
            return;
        }

        Transaction depositTransaction = new Transaction(
                "TXN_" + this.accountHeader + "_D" + System.currentTimeMillis(),
                this.userID,
                this.accountID,
                null,
                null,
                amount,
                "DEPOSIT",
                LocalDateTime.now(),
                TransactionStatus.PENDING);

        if (depositTransaction.execute()) {
            // persist updated balance and transaction history
            Database db = Database.getInstance();
            UserAccount owner = db.retrieveUserAccount(this.userID);
            if (owner != null) {
                if (owner.getAccounts() != null) {
                    owner.getAccounts().stream()
                            .filter(acc -> acc.getAccountID().equals(this.accountID))
                            .findFirst()
                            .ifPresent(acc -> acc.setBalance(this.balance));
                }
                db.updateUserAccount(owner.getUserID(), owner);
                db.addTransaction(depositTransaction);
            }
            transactions.add(depositTransaction);
        }
    }

    /**
     * Method to withdraw amount
     * 
     * @param amount
     */
    public void withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("Amount must be positive");
            return;
        }

        Transaction withdrawTransaction = new Transaction(
                "TXN_" + this.accountHeader + "_W" + System.currentTimeMillis(),
                this.userID,
                this.accountID,
                null,
                null,
                amount,
                "WITHDRAW",
                LocalDateTime.now(),
                TransactionStatus.PENDING);

        if (withdrawTransaction.execute()) {
            Database db = Database.getInstance();
            UserAccount owner = db.retrieveUserAccount(this.userID);
            if (owner != null) {
                if (owner.getAccounts() != null) {
                    owner.getAccounts().stream()
                            .filter(acc -> acc.getAccountID().equals(this.accountID))
                            .findFirst()
                            .ifPresent(acc -> acc.setBalance(this.balance));
                }
                db.updateUserAccount(owner.getUserID(), owner);
                db.addTransaction(withdrawTransaction);
            }
            transactions.add(withdrawTransaction);
        }
    }

    /**
     * Method to transfer funds to another account
     * 
     * @param target
     * @param amount
     */
    public void transferFunds(UserAccount receiver, String receiverAccountID, double amount) {
        if (receiver == null) {
            System.out.println("Target account cannot be null");
            return;
        }

        if (amount <= 0) {
            System.out.println("Amount must be positive");
            return;
        }

        Transaction transferTransaction = new Transaction(
                "TXN_" + this.accountHeader + "_T" + System.currentTimeMillis(),
                this.userID,
                this.accountID,
                receiver.getUserID(),
                receiverAccountID,
                amount,
                "TRANSFER",
                LocalDateTime.now(),
                TransactionStatus.PENDING);
        if (transferTransaction.execute()) {
            Database db = Database.getInstance();
            UserAccount owner = db.retrieveUserAccount(this.userID);
            UserAccount recv = db.retrieveUserAccount(receiver.getUserID());
            if (owner != null) {
                if (owner.getAccounts() != null) {
                    owner.getAccounts().stream()
                            .filter(acc -> acc.getAccountID().equals(this.accountID))
                            .findFirst()
                            .ifPresent(acc -> acc.setBalance(this.balance));
                }
                db.updateUserAccount(owner.getUserID(), owner);
            }
            if (recv != null) {
                if (recv.getAccounts() != null) {
                    recv.getAccounts().stream()
                            .filter(acc -> acc.getAccountID().equals(receiverAccountID))
                            .findFirst()
                            .ifPresent(acc -> acc.setBalance(acc.getBalance()));
                }
                db.updateUserAccount(recv.getUserID(), recv);
            }
            db.addTransaction(transferTransaction);
        }
    }

    /*-------------------- Getters and Setters --------------------*/

    public String getUserID(){
        return this.userID;
    }

    public String getAccountID(){
        return this.accountID;
    }

    public String getAccountHeader(){
        return this.accountHeader;
    }

    public double getBalance() {
        return this.balance;
    }

    public void setBalance(double amount) {
        this.balance = amount;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
}
