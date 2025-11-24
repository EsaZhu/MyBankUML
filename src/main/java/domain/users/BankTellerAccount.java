package domain.users;

import domain.bank.Branch;
import domain.enums.UserRole;

public class BankTellerAccount implements IUser {

    private String bankTellerID;
    private String username;
    private String passwordHash;
    private Branch branch;

    public BankTellerAccount(String bankTellerID, String username, String passwordHash, Branch branch ) {
        this.bankTellerID = bankTellerID;
        this.username = username;
        this.passwordHash = passwordHash;
        this.branch = branch;
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

    // should add getters for other fields

    // ========== Teller-specific methods ==========

    public void manageCustomerAccounts() {
        // placeholder – interacts with Branch.accounts later
        System.out.println("Managing customer accounts...");
    }

    public void generateReports() {
        // placeholder – generate account summaries later
        System.out.println("Generating teller reports...");
    }

    public void searchAccounts() {
        // placeholder – search logic added when branch is integrated
        System.out.println("Searching accounts...");
    }

    public void processTransactions() {
        // placeholder – will coordinate with Transaction later
        System.out.println("Processing transactions...");
    }
}
