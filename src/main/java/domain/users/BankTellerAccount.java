package domain.users;

import database.Database;
import domain.enums.UserRole;
import domain.users.UserAccount;
import java.util.List;

public class BankTellerAccount implements IUser {

    private String bankTellerID;
    private String username;
    private String passwordHash;
    private String branchID;

    public BankTellerAccount(String bankTellerID, String username, String passwordHash, String branchID) {
        this.bankTellerID = bankTellerID;
        this.username = username;
        this.passwordHash = passwordHash;
        this.branchID = branchID;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPasswordHash() {
        return passwordHash;
    }

    @Override
    public UserRole getRole() {
        return UserRole.TELLER;
    }

    public String getBranchID() {
        return branchID;
    }

    // ===========================
    // Teller Operations
    // ===========================

    // Search only customers in teller’s own branch
    public List<IUser> searchCustomers(Database db, String key, String value) {
        List<IUser> all = db.searchCustomersByAttribute(key, value);
        return all.stream()
                .filter(u -> u instanceof UserAccount)
                .filter(u -> ((UserAccount) u).getBranchID().equals(branchID))
                .toList();
    }

    // Search one customer by ID (must match branch)
    public UserAccount searchCustomerByID(Database db, String id) {
        IUser result = db.findUserByID(id);

        if (result instanceof UserAccount acc &&
                acc.getBranchID().equals(branchID)) {
            return acc;
        }
        return null;
    }

    // Simple management placeholder
    public void manageCustomerAccounts(Database db) {
        List<IUser> customers = db.searchCustomersByAttribute("branchID", branchID);

        System.out.println("Customers in your branch (" + branchID + "):");
        for (IUser u : customers) {
            UserAccount acc = (UserAccount) u;
            System.out.println("  - " + acc.getUserID() + " | " + acc.getUsername());
        }
    }

    // Reports for customers in this branch
    public void generateReports(Database db) {
        System.out.println("Generating branch report for " + branchID);

        List<IUser> customers = db.searchCustomersByAttribute("branchID", branchID);

        int count = 0;
        double totalBalance = 0.0;

        for (IUser u : customers) {
            UserAccount acc = (UserAccount) u;
            count++;
            totalBalance += acc.getBalance();
        }

        System.out.println("Customers: " + count);
        System.out.println("Total Balance: " + totalBalance);
    }

    
    public void processTransactions() {
        System.out.println("Processing transactions for branch " + branchID + "...");
    }
}
