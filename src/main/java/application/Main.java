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
            
            // Test 2: Create a test user account
            System.out.println("Test 2: Creating a test user account...");
            Document branchDoc = new Document()
                .append("branchID", "BRANCH001")
                .append("branchName", "Downtown Branch");
            Document testUser = new Document()
                .append("userID", "TEST001")
                .append("name", "Test User")
                .append("passwordHash", "1234")
                .append("balance", 500.0)
                .append("branch", "B0001")
                .append("transactionHistory", Arrays.asList("T0001", "T0002"));
            
            db.addAccount(testUser);
            System.out.println("- User account created! -\n");
            
            // Test 3: Retrieve the user account
            System.out.println("Test 3: Retrieving user account...");
            Document retrieved = db.retrieveAccount("TEST001");
            if (retrieved != null) {
                System.out.println("- User found! -");
                System.out.println("  Name: " + retrieved.getString("name"));
                System.out.println("  Balance: $" + retrieved.getDouble("balance") + "\n");
            } else {
                System.out.println("- User not found! -\n");
            }
            
            // Test 4: Update user balance
            System.out.println("Test 4: Updating user balance...");
            Document updates = new Document("balance", 750.0);
            db.updateAccount("TEST001", updates);
            
            Document updated = db.retrieveAccount("TEST001");
            System.out.println("- Balance updated! -");
            System.out.println("  New Balance: $" + updated.getDouble("balance") + "\n");
            
            // Test 5: Create a transaction
            System.out.println("Test 5: Creating a transaction...");
            Document transaction = new Document()
                .append("transactionID", "TRANS001")
                .append("sourceAccountID", "TEST001")
                .append("amount", 100.0)
                .append("transactionType", "deposit")
                .append("status", "COMPLETED");
            
            db.addTransaction(transaction);
            System.out.println("- Transaction created! -\n");
            
            // Test 6: Get transaction history
            System.out.println("Test 6: Retrieving transaction history...");
            List<Document> history = db.getTransactionHistory("TEST001");
            System.out.println("- Found " + history.size() + " transaction(s) -\n");
            
            for (Document trans : history) {
                System.out.println("  Transaction ID: " + trans.getString("transactionID"));
                System.out.println("  Type: " + trans.getString("transactionType"));
                System.out.println("  Amount: $" + trans.getDouble("amount"));
                System.out.println("  Status: " + trans.getString("status"));
                System.out.println();
            }
            
            // Test 7: Clean up - remove test data 
            // ----CAN REMOVE THIS TO CHECK INSERTION----
            System.out.println("Test 7: Cleaning up test data...");
            db.removeAccount("TEST001");
            System.out.println("- Test data removed! -\n");
            
            System.out.println("=== All Tests Passed! ===");
            
            // Close connection
            db.close();
            
        } catch (Exception e) {
            System.err.println("✗ Error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
