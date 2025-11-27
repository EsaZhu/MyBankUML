package domain.users;

import domain.enums.UserRole;
import domain.transactions.Transaction;
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
            String lastName,
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
}