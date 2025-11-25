package domain.users;
import database.Database;



import domain.enums.UserRole;
import java.util.Scanner;

public class DatabaseAdministratorAccount implements IUser {

    private String adminID;
    private String username;
    private String passwordHash;
    Database database;
    Scanner scanner = new Scanner(System.in);

    public DatabaseAdministratorAccount(String adminID, String username, String passwordHash) {
        this.adminID = adminID;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public String getAdminID() {
        return adminID;
    }

    // Interface getters
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
        return UserRole.ADMIN;
    }


    public void manageTellerAccounts() {

        System.out.println("Managing teller accounts");
        System.out.println("Select one the following options:\n" +
                "1. Add a new bank teller account\n" +
                "2. Remove a bank teller account\n" +
                "3. Edit a bank teller id");

        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                //use a gui form
                viewTellerList();
                System.out.println("Enter ID: ");
                String bankTellerID = scanner.nextLine();
                System.out.println("Enter  username: ");
                String username = scanner.nextLine();
                System.out.println("Enter password: ");
                String password = scanner.nextLine();
                System.out.println("Enter branch: ");
                String branchId = scanner.nextLine();
                createTeller(bankTellerID, username, password, branchId);
                break;
            case 2:
                viewTellerList();
                System.out.println("Enter ID: ");
                String bankTellerID2 = scanner.nextLine();
                removeTellerAccount(bankTellerID2);
                break;
            case 3:
                viewTellerList();
                System.out.println("Enter ID to change: ");
                String currentId = scanner.nextLine();
                System.out.println("Enter new ID: ");
                String newId = scanner.nextLine();
                changeTellerUsername(currentId, newId);
                break;
            default:
                System.out.println("Invalid choice");
                break;
        }

    }

    //manage teller additional private functions (only available within the database admin)
    private void viewTellerList() {
        System.out.println(database.getAllBankTellers());

    }

    private void createTeller(String bankTellerID, String username, String passwordHash, String branch) {
        if (database.retrieveBankTeller(bankTellerID) == null) {
            System.out.println("Bank Teller with this ID already exists");
        } else {
            BankTellerAccount bankTellerAccount = new BankTellerAccount(bankTellerID, username, passwordHash, branch);
            database.addBankTeller(bankTellerAccount);
        }
    }

    private void changeTellerUsername(String currentTellerID, String newTellerID) {
        if (database.retrieveBankTeller(currentTellerID) == null) {
            System.out.println("Teller does not exist");
        } else {
            BankTellerAccount currentTeller = database.retrieveBankTeller(currentTellerID);
            BankTellerAccount newTeller = new BankTellerAccount(newTellerID, currentTeller.getUsername(), currentTeller.getPasswordHash(), currentTeller.getBranchID());
            database.updateBankTeller(currentTellerID, newTeller);
        }

    }

    private void removeTellerAccount(String bankTellerID) {
        if (database.retrieveBankTeller(bankTellerID) == null) {
            System.out.println("Teller does not exist");
        } else {
            database.removeBankTeller(bankTellerID);
        }

    }

    //...

    public void manageCustomerAccounts() {
        System.out.println("Managing Customer accounts");
        System.out.println("Select one the following options:\n" +
                "1. Add a new customer account\n" +
                "2. Remove a customer account\n" +
                "3. Edit a customer account's id");

        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                //use a gui form
                viewCustomerAccounts();
                System.out.println("Enter ID: ");
                String id = scanner.nextLine();
                System.out.println("Enter username: ");
                String username = scanner.nextLine();
                System.out.println("Enter password: ");
                String password = scanner.nextLine();
                System.out.println("Enter balance");
                double balance = scanner.nextDouble();
                System.out.println("Enter branch: ");
                String branchId = scanner.nextLine();
                createCustomerAccounts(id, username, password, balance, branchId);
                break;
            case 2:
                viewCustomerAccounts();
                System.out.println("Enter customer ID: ");
                String customerID = scanner.nextLine();
                removeCustomer(customerID);
                break;
            case 3:
                viewCustomerAccounts();
                System.out.println("Enter customer ID to change: ");
                String currentId = scanner.nextLine();
                System.out.println("Enter new customer ID: ");
                String newId = scanner.nextLine();
                changeCustomerID(currentId, newId);
                break;
            default:
                System.out.println("Invalid choice");
                break;
        }

    }

    //manage customer accounts additional private functions (only available within the database admin)
    private void removeCustomer(String customerID) {
        if (database.retrieveAccount(customerID) == null) {
            System.out.println("Customer does not exist");
        } else {
            database.removeAccount(customerID);
        }

    }

    private void createCustomerAccounts(String userID, String name, String passwordHash, double balance, String branch) {
        if (database.retrieveAccount(userID) != null) {
            System.out.println("Customer already exists");
        } else {
            UserAccount newUser = new UserAccount(userID, name, passwordHash, balance, branch);
            database.addAccount(newUser);
        }

    }

    private void viewCustomerAccounts() {
        System.out.println(database.getAllAccounts());
    }


    private void changeCustomerID(String currentCustomerID, String newCustomerID) {
        if (database.retrieveAccount(currentCustomerID) == null) {
            System.out.println("Customer does not exist");
        } else {
            UserAccount currentUser = database.retrieveAccount(currentCustomerID);
            UserAccount newUser = new UserAccount(newCustomerID, currentUser.getUsername(), currentUser.getPasswordHash(), currentUser.getBalance(), currentUser.getBranchID);
            database.updateAccount(currentCustomerID, newUser);
        }

    }


    //...

    public void accessDatabase() {
        //maybe remove if the database is being accessed by other methods regardless.

    }

    public void generateReports() {
        int totalBankTellers = database.getAllBankTellers().size();
        int totalCustomers = database.getAllAccounts().size();
        int totalAdmins = database.getAllAdmins().size();
        int totalBranches = database.getAllBranches().size();
        int totalTransactions = database.getAllTransactions().size();
        int totalBanks = database.getAllBanks().size();

        String report = "-----BANK SYSTEM REPORT-----\n" +
                "Total User Accounts: " + totalCustomers +
                "Total Bank Teller Accounts: " + totalBankTellers +
                "Total Admin Accounts: " + totalAdmins +
                "Total Banks: " + totalBanks +
                "Total Branches: " + totalBranches +
                "Total Transactions: " + totalTransactions;
    }

    //changed this to boolean
    public boolean searchAccounts(String id) {
        return database.retrieveAccount(id) != null && database.retrieveBankTeller(id) != null;
    }


    public void reverseTransactions(String id) {
        database.retrieveTransaction(id).reverseTransaction();

    }


}
