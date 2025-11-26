package domain.users;

import database.Database;
import domain.bank.Branch;
import domain.enums.UserRole;
import domain.transactions.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BankTellerAccount implements IUser {

    private String bankTellerID;
    private String username;
    private String firstname;
    private String lastname;
    private String passwordHash;
    private String branchID;

    private final Database database = Database.getInstance();
    private final Scanner scanner = new Scanner(System.in);

    public BankTellerAccount(String bankTellerID,
            String username,
            String firstname,
            String lastname,
            String passwordHash,
            String branchID) {

        this.bankTellerID = bankTellerID;
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
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

    public String getFirstName() {
        return firstname;
    }

    public String getLastName() {
        return lastname;
    }
  
    public void manageCustomerAccounts() {

        System.out.println("\n=== MANAGE CUSTOMER ACCOUNTS ===");
        System.out.println("1. View customers in my branch");
        System.out.println("2. Deposit into customer account");
        System.out.println("3. Withdraw from customer account");
        System.out.println("4. Transfer between customers");
        System.out.println("5. Edit customer balance");
        System.out.println("Select option: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                listBranchCustomers();
                break;

            case 2:
                System.out.print("Customer ID: ");
                depositForCustomer(scanner.nextLine());
                break;

            case 3:
                System.out.print("Customer ID: ");
                withdrawForCustomer(scanner.nextLine());
                break;

            case 4:
                System.out.print("Source ID: ");
                String src = scanner.nextLine();
                System.out.print("Target ID: ");
                String dst = scanner.nextLine();
                System.out.print("Amount: ");
                double a = scanner.nextDouble();
                transferForCustomer(src, dst, a);
                break;

            case 5:
                System.out.print("Customer ID: ");
                editCustomerBalance(scanner.nextLine());
                break;

            default:
                System.out.println("Invalid option.");
        }
    }


    public void generateReports() {

        ArrayList<IUser> customers = database.searchCustomersByAttribute("branchID", branchID);

        System.out.println("\n=== BRANCH REPORT ===");
        System.out.println("Branch ID: " + branchID);
        System.out.println("Total customers: " + customers.size());

        double totalBalance = 0;
        int totalTransactions = 0;

        for (IUser user : customers) {
            UserAccount acc = (UserAccount) user;
            totalBalance += acc.getBalance();
            totalTransactions += acc.getTransactionHistory().size();
        }

        System.out.println("Total balance across branch: " + totalBalance);
        System.out.println("Total transactions recorded: " + totalTransactions);
        System.out.println("=== END REPORT ===\n");
    }


    public void searchAccounts() {

        System.out.print("Search field (name / userID / email): ");
        String field = scanner.nextLine();

        System.out.print("Search value: ");
        String value = scanner.nextLine();

        ArrayList<IUser> results = database.searchCustomersByAttribute(field, value);

        System.out.println("\n=== SEARCH RESULTS ===");

        if (results.isEmpty()) {
            System.out.println("No matching customers found.");
            return;
        }

        for (IUser user : results) {
            UserAccount u = (UserAccount) user;
            System.out.println("Customer: " + u.getUserID() + " | " + u.getUsername());
        }
    }


    public void processTransactions() {

        System.out.println("\n=== PROCESS TRANSACTION ===");
        System.out.print("Source account ID: ");
        String src = scanner.nextLine();

        System.out.print("Transaction type (DEPOSIT / WITHDRAW / TRANSFER): ");
        String type = scanner.nextLine().toUpperCase();

        System.out.print("Amount: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        String receiver = null;

        if (type.equals("TRANSFER")) {
            System.out.print("Receiver account ID: ");
            receiver = scanner.nextLine();
        }

        Transaction txn = new Transaction(
                "TXN_" + System.currentTimeMillis(),
                src,
                receiver,
                amount,
                type,
                java.time.LocalDateTime.now(),
                domain.enums.TransactionStatus.PENDING);

        if (txn.execute())
            System.out.println("Transaction completed.");
        else
            System.out.println("Transaction failed.");
    }


    private void listBranchCustomers() {
        ArrayList<IUser> users = database.searchCustomersByAttribute("branchID", branchID);

        System.out.println("\n=== CUSTOMERS IN BRANCH " + branchID + " ===");

        if (users.isEmpty()) {
            System.out.println("No customers found.");
            return;
        }

        for (IUser u : users) {
            UserAccount acc = (UserAccount) u;
            System.out.println(acc.getUserID() + " | " + acc.getUsername());
        }
    }

    private void depositForCustomer(String id) {
        UserAccount acc = database.retrieveAccount(id);
        if (acc == null) {
            System.out.println("Customer not found.");
            return;
        }
        System.out.print("Amount: ");
        double amt = scanner.nextDouble();
        scanner.nextLine();
        acc.deposit(amt);
        database.updateAccount(id, acc);
    }

    private void withdrawForCustomer(String id) {
        UserAccount acc = database.retrieveAccount(id);
        if (acc == null) {
            System.out.println("Customer not found.");
            return;
        }
        System.out.print("Amount: ");
        double amt = scanner.nextDouble();
        scanner.nextLine();
        acc.withdraw(amt);
        database.updateAccount(id, acc);
    }

    private void transferForCustomer(String src, String dst, double amount) {
        UserAccount s = database.retrieveAccount(src);
        UserAccount t = database.retrieveAccount(dst);
        if (s == null || t == null) {
            System.out.println("Invalid accounts.");
            return;
        }
        s.transferFunds(t, amount);
        database.updateAccount(src, s);
        database.updateAccount(dst, t);
    }

    private void editCustomerBalance(String id) {
        UserAccount acc = database.retrieveAccount(id);
        if (acc == null) {
            System.out.println("Customer not found.");
            return;
        }
        System.out.print("New balance: ");
        double b = scanner.nextDouble();
        scanner.nextLine();
        acc.setBalance(b);
        database.updateAccount(id, acc);
    }
}
