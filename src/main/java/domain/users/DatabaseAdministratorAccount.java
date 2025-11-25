package domain.users;
import database.Database;
import domain.bank.Branch;
import org.bson.Document;


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

    public void manageTellerAccounts(){

        System.out.println("Managing teller accounts");
        System.out.println("Select one the following options:\n" +
                "1. Add a new bank teller account\n" +
                "2. Remove a bank teller account\n" +
                "3. Edit a bank teller id");

        int choice = scanner.nextInt();
        switch(choice){
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
                createTeller(bankTellerID,username,password,branchId);
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
    private void viewTellerList(){
        System.out.println(database.getAllTellers());

    }

    private void createTeller(String bankTellerID, String username, String passwordHash, String branch){
        BankTellerAccount bankTellerAccount = new BankTellerAccount(bankTellerID, username, passwordHash, branch);
        database.addTeller(database.tellerAccountToDocument(bankTellerAccount));
    }

    private void changeTellerUsername(String currentTellerID, String newTellerID){
        if(database.retrieveTeller(currentTellerID) == null){
            System.out.println("Teller does not exist");
        }else{
            Document idUpdate = new Document("bankTellerID", newTellerID);
            database.updateTeller(currentTellerID, idUpdate);
        }

    }

    private void removeTellerAccount(String bankTellerID){
        if(database.retrieveTeller(bankTellerID) == null){
            System.out.println("Teller does not exist");
        } else {
            database.removeTeller(bankTellerID);
        }

    }

   //...

    public void manageCustomerAccounts(){
        System.out.println("Managing Customer accounts");
        System.out.println("Select one the following options:\n" +
                "1. Add a new customer account\n" +
                "2. Remove a customer account\n" +
                "3. Edit a customer account's id");

        int choice = scanner.nextInt();
        switch(choice){
            case 1:
                //use a gui form
                viewTellerList();
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

                break;
            case 2:
                viewTellerList();
                System.out.println("Enter bank teller ID: ");
                String bankTellerID2 = scanner.nextLine();
                removeTellerAccount(bankTellerID2);
                break;
            case 3:
                viewTellerList();
                System.out.println("Enter bank teller ID to change: ");
                String currentId = scanner.nextLine();
                System.out.println("Enter new bank teller ID: ");
                String newId = scanner.nextLine();
                changeTellerUsername(currentId, newId);
                break;
            default:
                System.out.println("Invalid choice");
                break;
        }

    }

    //manage customer accounts additional private functions (only available within the database admin)
    private void removeCustomer(String customerID){

    }

    private void createCustomerAccounts(){

    }

    private void viewCustomerAccounts(){

    }

    private void changeCustomerID(String currentCustomerID, String newCustomerID){

    }


    //...

    public void accessDatabase(){
        //maybe remove if the database is being accessed by other methods regardless.

    }

    public void generateReports(){

    }

    public void searchAccounts(){

    }

    public void reverseTransactions(){

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

}
