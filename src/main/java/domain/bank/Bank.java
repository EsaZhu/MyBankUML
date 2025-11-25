package domain.bank;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import database.Database;
import domain.users.BankTellerAccount;
import domain.users.IUser;
import domain.users.UserAccount;

// removed lombok usage to avoid external dependency
public class Bank {
    public String name;
    public String bankID;
    public String searchID;
    public ArrayList<String> criteriaList;
    public ArrayList<IUser> resultList;
    public Database database;

    public Bank(String name, String bankID, ArrayList<Branch> branches, ArrayList<IUser> resultList, Database database) {
        this.name = name;
        this.bankID = bankID;
        this.resultList = resultList;

        this.database = database;
        database.connect();
    }

    public void addBranch(Branch branch) {
        database.addBranch(branch);
    }

    public ArrayList<Branch> getBranches() {
        ArrayList<Branch> branches = database.getAllBranches();
        return branches;
    }

    public void printBankInfo() {
        System.out.println("BANK INFORMATION");
        System.out.println("Bank Name: " + name);
        System.out.println("Bank ID: " + bankID);
        System.out.println("\n--- BRANCHES ---");
        
        ArrayList<Branch> branches = getBranches();
        if (branches == null || branches.isEmpty()) {
            System.out.println("No branches available.");
        } else {
            for (int i = 0; i < branches.size(); i++) {
                Branch branch = branches.get(i);
                System.out.println("\nBranch " + (i + 1) + ":");
                branch.printBranchInfo();
            }
        }
    }

    public IUser searchByID(String id) {
        IUser user = database.findUserByID(id);
        this.resultList = new ArrayList<>();
        this.resultList.add(user);
        return user;
    }

    public ArrayList<IUser> searchByAttribute(String key, String value) {
        ArrayList<IUser> users = database.searchAccountsByAttribute(key, value);
        this.resultList = users;
        return users;
    }

    public void displayResults() {
        System.out.println("Total Results: " + resultList.size());
        for (int i = 0; i < resultList.size(); i++) {
            IUser user = resultList.get(i);
            System.out.println("\nResult " + (i + 1) + ":");
            
            if (user instanceof UserAccount) {
                UserAccount userAccount = (UserAccount) user;
                System.out.println("  Type: User Account");
                // -------TO DO: UPDATE THIS AFTER------
                // System.out.println("  User ID: " + userAccount.getUserID());
                // System.out.println("  Name: " + userAccount.getName());
                // System.out.println("  Balance: $" + String.format("%.2f", userAccount.getBalance()));
                // System.out.println("  Branch: " + userAccount.getBranch());
            } else if (user instanceof BankTellerAccount) {
                BankTellerAccount teller = (BankTellerAccount) user;
                System.out.println("  Type: Bank Teller Account");
                // -------TO DO: UPDATE THIS AFTER------
                System.out.println("  Teller Username: " + teller.getUsername());
                System.out.println("  Role: " + teller.getRole());
            }
        }
    }
}
