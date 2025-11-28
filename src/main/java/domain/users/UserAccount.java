package domain.users;

import java.util.List;
import domain.enums.UserRole;

public class UserAccount implements IUser {
    private String userID;
    private String username;
    private String firstName;
    private String lastName;
    private String passwordHash;
    private String branchId;
    private List<Account> accounts;
    private List<String> transactionHistory;

    // No-arg constructor for reflection/deserialization
    public UserAccount() {
    }

    public UserAccount(String userID,
            String username,
            String firstName,
            String lastName,
            String passwordHash,
            String branchId,
            List<Account> accounts) {
        this.userID = userID;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.passwordHash = passwordHash;
        this.branchId = branchId;
        this.accounts = accounts;
        this.transactionHistory = null;
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
        return this.userID;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getBranchID() {
        return this.branchId;
    }

    public List<Account> getAccounts() {
        return this.accounts;
    }

    public List<String> getTransactionHistory() {
        return transactionHistory;
    }

    public void setTransactionHistory(List<String> transactionHistory) {
        this.transactionHistory = transactionHistory;
    }
}
