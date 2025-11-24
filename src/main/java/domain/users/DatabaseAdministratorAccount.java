package domain.users;
import database.Database;

import domain.enums.UserRole;
import java.util.Scanner;

public class DatabaseAdministratorAccount implements IUser {

    private String adminID;
    private String username;
    private String passwordHash;

    public DatabaseAdministratorAccount(String adminID, String username, String passwordHash) {
        this.adminID = adminID;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public void manageTellerAccounts(){
        Scanner scanner = new Scanner(System.in);

        System.out.println("Managing teller accounts");
        System.out.println("Select one the following options:\n" +
                "1. Add a new bank teller account\n" +
                "2. Remove a bank teller account\n" +
                "3. Edit a bank teller username");

        int choice = scanner.nextInt();
        switch(choice){
            case 1:
                viewTellerList();
                System.out.println("Enter bank teller ID: ");
                String bankTellerID = scanner.next();
                System.out.println("Enter bank teller username: ");
                String username = scanner.next();
                System.out.println("Enter bank teller password: ");
                String password = scanner.next();
                createTeller(bankTellerID,username,password);
                break;
            case 2:
                viewTellerList();
                System.out.println("Enter bank teller ID: ");
                String bankTellerID2 = scanner.next();
                removeTellerAccount(bankTellerID2);
                break;
            case 3:
                viewTellerList();
                changeTellerUsername();
                break;
            default:
                System.out.println("Invalid choice");
                break;
        }

    }

    //manage teller additional private functions (only available within the database admin)
    private void viewTellerList(){

    }

    private void createTeller(String bankTellerID, String username, String passwordHash){
        BankTellerAccount bankTellerAccount = new BankTellerAccount(bankTellerID, username, passwordHash);
        Database.addTeller(Database.tellerAccountToDocument(bankTellerAccount));
    }

    private void changeTellerUsername(){


    }

    private void removeTellerAccount(String bankTellerID){

    }

   //...

    public void manageCustomerAccounts(){

    }

    //manage customer accounts additional private functions (only available within the database admin)
    private void deleteCustomer(){

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
