package application;

import domain.users.Customer;

import java.util.Arrays;
import java.util.List;

import org.bson.Document;

import database.Database;
import domain.accounts.Card;
import domain.accounts.Checking;
import domain.accounts.Savings;
import domain.bank.Branch;
import domain.transactions.Transaction;

public class Main {
    public static void main(String[] args) {
        /* Customer customer = new Customer("Shayan Aminaei");
        customer.printCustomerInfo();

        Card card = new Card(customer);
        Checking check = new Checking(customer);
        Savings saving = new Savings(customer);

        Transaction t1 = new Transaction();
        Transaction t2 = new Transaction();
        Transaction t3 = new Transaction();

        card.addTransaction(t1);
        check.addTransaction(t2);
        saving.addTransaction(t3);

        System.out.println("Card   transactions count:   " + card.getTransactions().size());
        System.out.println("Check  transactions count:   " + check.getTransactions().size());
        System.out.println("Saving transactions count:   " + saving.getTransactions().size()); */
        
        System.out.println("=== Testing MongoDB Connection ===\n");
        try {
            // Get database instance
            Database db = Database.getInstance();
            
            // Test 1: Check connection
            System.out.println("Test 1: Testing connection...");
            if (db.connect()) {
                System.out.println("- Connection successful! -\n");
            } else {
                System.out.println("- Connection failed! -\n");
                return;
            }
            // Close connection
            db.close();     
        } catch (Exception e) {
            System.err.println("✗ Error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
