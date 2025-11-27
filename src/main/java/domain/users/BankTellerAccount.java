package domain.users;

import database.Database;
import domain.accounts.Account;
import domain.enums.UserRole;
import domain.transactions.Transaction;
import domain.users.UserAccount;

import java.time.LocalDateTime;
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

    private Database database;
    private final Scanner scanner = new Scanner(System.in);

    public BankTellerAccount(String bankTellerID,
            String username,
            String firstname,
            String lastname,
            String passwordHash,
            String branchID, Database database) {

        this.bankTellerID = bankTellerID;
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.passwordHash = passwordHash;
        this.branchID = branchID;
        this.database = database;
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
        System.out.print("Select option: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                listBranchCustomers();
                break;

            case 2:
                System.out.print("Customer USER ID: ");
                depositForCustomer(scanner.nextLine());
                break;

            case 3:
                System.out.print("Customer USER ID: ");
                withdrawForCustomer(scanner.nextLine());
                break;

            case 4:
                System.out.print("Source USER ID: ");
                String src = scanner.nextLine();
                System.out.print("Target USER ID: ");
                String dst = scanner.nextLine();
                System.out.print("Amount: ");
                double a = scanner.nextDouble();
                scanner.nextLine();
                transferForCustomer(src, dst, a);
                break;

            default:
                System.out.println("Invalid option.");
        }
    }


    public void generateReports() {

        ArrayList<IUser> customers = database.searchCustomersByAttribute("branchId", branchID);

        System.out.println("\n=== BRANCH REPORT ===");
        System.out.println("Branch ID: " + branchID);
        System.out.println("Total customers: " + customers.size());

        double totalBalance = 0;
        int totalTransactions = 0;

        for (IUser user : customers) {
            UserAccount acc = (UserAccount) user;

            // SUM OVER ALL ACCOUNTS OF THE USER
            for (Account a : acc.getAccounts()) {
                totalBalance += a.getBalance();
                totalTransactions += a.getTransactions().size();
            }
        }

        System.out.println("Total balance across branch: " + totalBalance);
        System.out.println("Total transactions recorded: " + totalTransactions);
        System.out.println("=== END REPORT ===\n");
    }


    public void searchAccounts() {

        System.out.print("Search field (username / userID / firstName / lastName / branchId): ");
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
        System.out.print("Source ACCOUNT ID: ");
        String srcAccountID = scanner.nextLine();

        System.out.print("Transaction type (DEPOSIT / WITHDRAW / TRANSFER): ");
        String type = scanner.nextLine().toUpperCase();

        System.out.print("Amount: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        String receiverAccountID = null;

        if (type.equals("TRANSFER")) {
            System.out.print("Receiver ACCOUNT ID: ");
            receiverAccountID = scanner.nextLine();
        }

        // Retrieve source Account + User
        Account srcAccount = database.retrieveAccountByAccountID(srcAccountID);
        if (srcAccount == null) {
            System.out.println("Invalid source account.");
            return;
        }

        UserAccount srcUser = database.retrieveUserAccount(srcAccount.getUserID());

        UserAccount receiverUser = null;
        if (receiverAccountID != null) {
            Account receiverAccount = database.retrieveAccountByAccountID(receiverAccountID);
            if (receiverAccount == null) {
                System.out.println("Invalid receiver account.");
                return;
            }

            receiverUser = database.retrieveUserAccount(receiverAccount.getUserID());
        }

        Transaction txn = new Transaction(
                "TXN_" + System.currentTimeMillis(),
                srcUser.getUserID(),
                srcAccountID,
                (receiverUser != null ? receiverUser.getUserID() : null),
                receiverAccountID,
                amount,
                type,
                LocalDateTime.now(),
                domain.enums.TransactionStatus.PENDING);

        if (txn.execute())
            System.out.println("Transaction completed.");
        else
            System.out.println("Transaction failed.");
    }



    private void listBranchCustomers() {
        ArrayList<IUser> users = database.searchCustomersByAttribute("branchId", branchID);

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

    private UserAccount getUserByID(String userID) {
        ArrayList<IUser> list = database.searchCustomersByAttribute("userID", userID);
        if (list.isEmpty())
            return null;
        return (UserAccount) list.get(0);
    }

    private Account getFirstAccount(UserAccount user) {
        return user.getAccounts().isEmpty() ? null : user.getAccounts().get(0);
    }

    private void depositForCustomer(String userID) {

        UserAccount acc = getUserByID(userID);
        if (acc == null) {
            System.out.println("Customer not found.");
            return;
        }

        Account account = getFirstAccount(acc);
        if (account == null) {
            System.out.println("Customer has no accounts.");
            return;
        }

        System.out.print("Amount: ");
        double amt = scanner.nextDouble();
        scanner.nextLine();

        account.deposit(amt);
        System.out.println("Deposit completed.");
    }

    private void withdrawForCustomer(String userID) {

        UserAccount acc = getUserByID(userID);
        if (acc == null) {
            System.out.println("Customer not found.");
            return;
        }

        Account account = getFirstAccount(acc);
        if (account == null) {
            System.out.println("Customer has no accounts.");
            return;
        }

        System.out.print("Amount: ");
        double amt = scanner.nextDouble();
        scanner.nextLine();

        account.withdraw(amt);
        System.out.println("Withdrawal completed.");
    }

    private void transferForCustomer(String srcUserID, String dstUserID, double amount) {

        UserAccount s = database.retrieveUserAccount(srcUserID);
        UserAccount t = database.retrieveUserAccount(dstUserID);

        Account srcAcc = getFirstAccount(s);
        Account dstAcc = getFirstAccount(t);

        srcAcc.transferFunds(t, dstAcc.getAccountID(), amount);

        System.out.println("Transfer completed.");
    }
}
