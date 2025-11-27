package domain.bank;

import java.util.ArrayList;

import database.Database;

public class Bank {

    public String name;
    public String bankID;

    private Database database;

    public Bank(String name, String bankID, Database database) {
        this.name = name;
        this.bankID = bankID;
        this.database = database;
    }

    public ArrayList<Branch> getBranches() {
        return database.getAllBranches();
    }

    public void printBankInfo() {
        System.out.println("BANK INFORMATION");
        System.out.println("Bank Name: " + name);
        System.out.println("Bank ID: " + bankID);

        ArrayList<Branch> branches = getBranches();

        if (branches.isEmpty()) {
            System.out.println("No branches available.");
        } else {
            for (Branch b : branches) {
                System.out.println("----");
                b.printBranchInfo();
            }
        }
    }
}
