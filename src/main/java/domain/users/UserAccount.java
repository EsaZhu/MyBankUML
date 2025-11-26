package domain.users;

import domain.bank.Branch;
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
    private Account[] accounts;
    private List<Transaction> transactionList;

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
        this.accounts = accounts;
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

        Transaction depositTransaction = new Transaction(
                "TXN_D" + System.currentTimeMillis(),
                this.userID,
                null,
                amount,
                "DEPOSIT",
                LocalDateTime.now(),
                TransactionStatus.PENDING
        );

        depositTransaction.execute();
        transactionList.add(depositTransaction);
    }

    public void withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("Amount must be positive");
            return;
        }

        // IMPORTANT!
        // TODO: When Account supports balances, check for the available funds here

        Transaction withdrawTransaction = new Transaction(
                "TXN_W" + System.currentTimeMillis(),
                this.userID,
                null,
                amount,
                "WITHDRAW",
                LocalDateTime.now(),
                TransactionStatus.PENDING
        );

        withdrawTransaction.execute();
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

        // IMPORTANT!
        // TODO: When Account supports balances, check for the available funds here

        Transaction transferTransaction = new Transaction(
                "TXN_T" + System.currentTimeMillis(),
                this.userID,
                target.getUserID(),
                amount,
                "TRANSFER",
                LocalDateTime.now(),
                TransactionStatus.PENDING
        );

        transferTransaction.execute();
        transactionList.add(transferTransaction);
    }
    
}
