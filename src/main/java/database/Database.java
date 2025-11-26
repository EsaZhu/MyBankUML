package database;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import domain.accounts.Card;
import domain.accounts.Checking;
import domain.accounts.Savings;
import domain.bank.Bank;
import domain.bank.Branch;
import domain.transactions.Transaction;
import domain.users.Account;
import domain.users.BankTellerAccount;
import domain.users.DatabaseAdministratorAccount;
import domain.users.IUser;
import domain.users.UserAccount;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class Database {
    private static Database instance;
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    
    private MongoCollection<Document> accountCollection;
    private MongoCollection<Document> tellerCollection;
    private MongoCollection<Document> adminCollection;
    private MongoCollection<Document> transactionCollection;
    private MongoCollection<Document> branchCollection;
    private MongoCollection<Document> bankCollection;
    
    private static final String CONNECTION_STRING = "mongodb+srv://AdminUser:Test1234@cluster.leyizej.mongodb.net/?appName=Cluster";
    private static final String DATABASE_NAME = "Bank";
    
    // Private constructor for singleton pattern
    private Database() {
        try {
            mongoClient = MongoClients.create(CONNECTION_STRING);
            mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
            initializeCollections();
            System.out.println("MongoDB connection established successfully!");
        } catch (Exception e) {
            System.err.println("Failed to connect to MongoDB: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    // Get singleton instance
    public static synchronized Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }
    
    private void initializeCollections() {
        accountCollection = mongoDatabase.getCollection("UserAccount");
        tellerCollection = mongoDatabase.getCollection("BankTellerAccount");
        adminCollection = mongoDatabase.getCollection("DatabaseAdministratorAccount");
        transactionCollection = mongoDatabase.getCollection("Transaction");
        branchCollection = mongoDatabase.getCollection("Branch");
        bankCollection = mongoDatabase.getCollection("Bank");
    }
    
    public boolean connect() {
        try {
            mongoDatabase.listCollectionNames().first();
            return true;
        } catch (Exception e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }
    
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("MongoDB connection closed.");
        }
    }
    
    // ----User Account Operations----
    public void addAccount(UserAccount account) {
        Document accountDoc = userAccountToDocument(account);
        accountCollection.insertOne(accountDoc);
    }
    
    public UserAccount retrieveAccount(String accountID) {
        Document doc = accountCollection.find(Filters.eq("userID", accountID)).first();
        if (doc != null) {
            return documentToUserAccount(doc, UserAccount.class);
        }
        return null;
    }
    
    public void updateAccount(String accountID, UserAccount updatedAccount) {
        Document updates = userAccountToDocument(updatedAccount);
        accountCollection.updateOne(Filters.eq("userID", accountID), new Document("$set", updates));
    }
    
    public void removeAccount(String accountID) {
        accountCollection.deleteOne(Filters.eq("userID", accountID));
    }
    
    public ArrayList<UserAccount> getAllAccounts() {
        ArrayList<UserAccount> accounts = new ArrayList<>();
        ArrayList<Document> accountDocs = new ArrayList<>();
        accountCollection.find().into(accountDocs);
        
        for (Document doc : accountDocs) {
            UserAccount account = documentToUserAccount(doc, UserAccount.class);
            accounts.add(account);
        }
        
        return accounts;
    }
    
    // ----BankTellerAccount Operations----
    public void addBankTeller(BankTellerAccount teller) {
        Document tellerDoc = tellerAccountToDocument(teller);
        tellerCollection.insertOne(tellerDoc);
    }
    
    public BankTellerAccount retrieveBankTeller(String tellerID) {
        Document doc = tellerCollection.find(Filters.eq("bankTellerID", tellerID)).first();
        if (doc != null) {
            return documentToBankTellerAccount(doc, BankTellerAccount.class);
        }
        return null;
    }
    
    public void updateBankTeller(String tellerID, BankTellerAccount updatedTeller) {
        Document updates = tellerAccountToDocument(updatedTeller);
        tellerCollection.updateOne(Filters.eq("bankTellerID", tellerID), new Document("$set", updates));
    }
    
    public void removeBankTeller(String tellerID) {
        tellerCollection.deleteOne(Filters.eq("bankTellerID", tellerID));
    }
    
    public ArrayList<BankTellerAccount> getAllBankTellers() {
        ArrayList<BankTellerAccount> tellers = new ArrayList<>();
        ArrayList<Document> tellerDocs = new ArrayList<>();
        tellerCollection.find().into(tellerDocs);
        
        for (Document doc : tellerDocs) {
            BankTellerAccount teller = documentToBankTellerAccount(doc, BankTellerAccount.class);
            tellers.add(teller);
        }
        
        return tellers;
    }
    
    // ----DatabaseAdminAccount Operations----
    public void addAdmin(DatabaseAdministratorAccount admin) {
        Document adminDoc = adminAccountToDocument(admin);
        adminCollection.insertOne(adminDoc);
    }
    
    public DatabaseAdministratorAccount retrieveAdmin(String adminID) {
        Document doc = adminCollection.find(Filters.eq("adminID", adminID)).first();
        if (doc != null) {
            return documentToAdminAccount(doc, DatabaseAdministratorAccount.class);
        }
        return null;
    }

    public void updateAdmin(String adminID, DatabaseAdministratorAccount updatedAdmin) {
        Document updates = adminAccountToDocument(updatedAdmin);
        adminCollection.updateOne(Filters.eq("adminID", adminID), new Document("$set", updates));
    }

    public void removeAdmin(String adminID) {
        adminCollection.deleteOne(Filters.eq("adminID", adminID));
    }
    
    public ArrayList<DatabaseAdministratorAccount> getAllAdmins() {
        ArrayList<DatabaseAdministratorAccount> admins = new ArrayList<>();
        ArrayList<Document> adminDocs = new ArrayList<>();
        adminCollection.find().into(adminDocs);
        
        for (Document doc : adminDocs) {
            DatabaseAdministratorAccount admin = documentToAdminAccount(doc, DatabaseAdministratorAccount.class);
            admins.add(admin);
        }
        
        return admins;
    }
    
    // ----Transaction Operations----
    public void addTransaction(Transaction transaction) {
        // Add transaction to database
        Document transactionDoc = transactionToDocument(transaction);
        transactionCollection.insertOne(transactionDoc);
        
        // Update user transaction history
        // Assuming receiver and sender account are the same
        String userID = transactionDoc.getString("sourceAccountID");
        updateUserTransactionHistory(userID, transactionDoc.getString("transactionID"));
    }

    private void updateUserTransactionHistory(String userID, String transactionID) {
        // Use $addToSet to add transaction ID to the user's transactionHistory array
        // This prevents duplicates automatically
        accountCollection.updateOne(
            Filters.eq("userID", userID),
            new Document("$addToSet", new Document("transactionHistory", transactionID))
        );
    }
    
    public Transaction retrieveTransaction(String transactionID) {
        Document doc = transactionCollection.find(Filters.eq("transactionID", transactionID)).first();
        if (doc != null) {
            return documentToTransaction(doc, Transaction.class);
        }
        return null;
    }
    
    public ArrayList<Transaction> getTransactionHistory(String accountID) {
        ArrayList<Transaction> transactions = new ArrayList<>();
        ArrayList<Document> transactionDocs = new ArrayList<>();
        transactionCollection.find(
            Filters.or(
                Filters.eq("sourceAccountID", accountID),
                Filters.eq("receiverAccountID", accountID)
            )
        ).into(transactionDocs);
        
        for (Document doc : transactionDocs) {
            Transaction transaction = documentToTransaction(doc, Transaction.class);
            transactions.add(transaction);
        }
        
        return transactions;
    }
    
    public void updateTransaction(String transactionID, Transaction updatedTransaction) {
        Document updates = transactionToDocument(updatedTransaction);
        transactionCollection.updateOne(Filters.eq("transactionID", transactionID), new Document("$set", updates));
    }
    
    public ArrayList<Transaction> getAllTransactions() {
        ArrayList<Transaction> transactions = new ArrayList<>();
        ArrayList<Document> transactionDocs = new ArrayList<>();
        transactionCollection.find().into(transactionDocs);
        
        for (Document doc : transactionDocs) {
            Transaction transaction = documentToTransaction(doc, Transaction.class);
            transactions.add(transaction);
        }
        
        return transactions;
    }
    
    // ----Branch Operations----
    public void addBranch(Branch branch) {
        Document branchDoc = branchToDocument(branch);
        branchCollection.insertOne(branchDoc);
    }

    public Branch retrieveBranch(String branchID) {
        Document doc = branchCollection.find(Filters.eq("branchID", branchID)).first();
        if (doc != null) {
            return documentToBranch(doc, Branch.class);
        }
        // If not found
        return null;
    }
    
    public void updateBranch(String branchID, Branch updatedBranch) {
        Document updates = branchToDocument(updatedBranch);
        branchCollection.updateOne(Filters.eq("branchID", branchID), new Document("$set", updates));
    }
    
    public ArrayList<Branch> getAllBranches() {
        ArrayList<Branch> branches = new ArrayList<>();
        ArrayList<Document> branchDocs = new ArrayList<>();
        branchCollection.find().into(branchDocs);
        
        for (Document doc : branchDocs) {
            Branch branch = documentToBranch(doc, Branch.class);
            branches.add(branch);
        }
        
        return branches;
    }

    // ----Bank Operations----
    public void addBank(Bank bank) {
        Document bankDoc = bankToDocument(bank);
        bankCollection.insertOne(bankDoc);
    }
    
    public Bank retrieveBank(String bankID) {
        Document doc = bankCollection.find(Filters.eq("bankID", bankID)).first();
        if (doc != null) {
            return documentToBank(doc, Bank.class);
        }
        return null;
    }
    
    public void removeBank(String bankID) {
        bankCollection.deleteOne(Filters.eq("bankID", bankID));
    }

    public void updateBank(String bankID, Bank updatedBank) {
        Document updates = bankToDocument(updatedBank);
        bankCollection.updateOne(Filters.eq("bankID", bankID), new Document("$set", updates));
    }
    
    public ArrayList<Bank> getAllBanks() {
        ArrayList<Bank> banks = new ArrayList<>();
        ArrayList<Document> bankDocs = new ArrayList<>();
        bankCollection.find().into(bankDocs);
        
        for (Document doc : bankDocs) {
            Bank bank = documentToBank(doc, Bank.class);
            banks.add(bank);
        }
        
        return banks;
    }
    
    // ----SEARCH METHODS----
    // This is for database administrators since you can search for banktellers as well
    public ArrayList<IUser> searchAccountsByAttribute(String fieldName, Object value) {
        ArrayList<IUser> results = new ArrayList<>();
        
        // Search in UserAccount collection
        ArrayList<Document> userDocs = new ArrayList<>();
        accountCollection.find(Filters.eq(fieldName, value)).into(userDocs);
        for (Document doc : userDocs) {
            UserAccount user = documentToUserAccount(doc, UserAccount.class);
            results.add(user);
        }
        
        // Search in BankTellerAccount collection
        ArrayList<Document> tellerDocs = new ArrayList<>();
        tellerCollection.find(Filters.eq(fieldName, value)).into(tellerDocs);
        for (Document doc : tellerDocs) {
            BankTellerAccount teller = documentToBankTellerAccount(doc, BankTellerAccount.class);
            results.add(teller);
        }
        return results;
    }

    // This is for bank tellers since they shouldn't be able to search for banktellers
    public ArrayList<IUser> searchCustomersByAttribute(String fieldName, Object value) {
        ArrayList<IUser> results = new ArrayList<>();
        // Search in UserAccount collection only (customers)
        ArrayList<Document> userDocs = new ArrayList<>();
        accountCollection.find(Filters.eq(fieldName, value)).into(userDocs);
        for (Document doc : userDocs) {
            UserAccount user = documentToUserAccount(doc, UserAccount.class);
            results.add(user);
        }
        return results;
    }

    // This is for bank tellers since they shouldn't be able to search for admins
    public IUser findUserByID(String id) {
        Document user = accountCollection.find(Filters.eq("userID", id)).first();
        if (user != null) {
            return documentToUserAccount(user, UserAccount.class);
        }
        
        user = tellerCollection.find(Filters.eq("bankTellerID", id)).first();
        if (user != null) {
            return documentToBankTellerAccount(user, BankTellerAccount.class);
        }
        
        return null;
    }

    // For login and database admins to get any user (UserAccount, BankTellerAccount, DatabaseAdminAccount)
    public IUser retrieveUser(String id) {
        // For UserAccount 
        Document doc = accountCollection.find(Filters.eq("userID", id)).first();
        if (doc != null) {
            return documentToIUser(doc);
        }
    
        // For BankTellerAccount
        doc = tellerCollection.find(Filters.eq("bankTellerID", id)).first();
        if (doc != null) {
            return documentToIUser(doc);
        }
        
        // For DatabaseAdministratorAccount
        doc = adminCollection.find(Filters.eq("adminID", id)).first();
        if (doc != null) {
            return documentToIUser(doc);
        }

        // User not found
        return null; 
    }

    // ==================== CONVERSION HELPER METHODS ====================
    // ----CLASS TO DOCUMENT METHODS----
    public Document userAccountToDocument(Object userAccount) {
        try {
            return new Document()
                .append("userID", getField(userAccount, "userID"))
                .append("passwordHash", getField(userAccount, "passwordHash"))
                .append("branch", getField(userAccount, "branchId"))
                .append("transactionHistory", getField(userAccount, "transactionHistory"))
                .append("first_name", getField(userAccount, "firstName"))
                .append("last_name", getField(userAccount, "lastName"))
                .append("username", getField(userAccount, "username"))
                .append("accounts", accountsToDocumentList((Account[]) getField(userAccount, "accounts")));
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert UserAccount to Document", e);
        }
    }

    // Helper method to convert Account[] to List<Document>
    private List<Document> accountsToDocumentList(Account[] accounts) {
        List<Document> accountDocs = new ArrayList<>();
        if (accounts != null) {
            for (Account account : accounts) {
                Document accountDoc = new Document();
                
                // !!!!!TO DO: come back to this later!!!!!
                // Check which type of Account instance it is
                if (account instanceof Savings) {
                    //accountDoc.append("Savings", account.getBalance());
                } else if (account instanceof Checking) {
                    //accountDoc.append("Checking", account.getBalance());
                } else if (account instanceof Card) {
                    //accountDoc.append("Card", account.getBalance());
                }
                
                accountDocs.add(accountDoc);
            }
        }
        return accountDocs;
    }
    
    public Document tellerAccountToDocument(Object teller) {
        try {
            return new Document()
                .append("bankTellerID", getField(teller, "bankTellerID"))
                .append("username", getField(teller, "username"))
                .append("passwordHash", getField(teller, "passwordHash"))
                .append("firstname", getField(teller, "firstname"))  // lowercase to match field
                .append("lastname", getField(teller, "lastname"))    // lowercase to match field
                .append("branchID", getField(teller, "branchID"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert BankTellerAccount to Document", e);
        }
    }
    
    public Document adminAccountToDocument(Object admin) {
        try {
            return new Document()
                .append("adminID", getField(admin, "adminID"))
                .append("username", getField(admin, "username"))
                .append("passwordHash", getField(admin, "passwordHash"))
                .append("firstname", getField(admin, "firstname"))  // lowercase to match field
                .append("lastname", getField(admin, "lastname"));    // lowercase to match field
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert DatabaseAdministratorAccount to Document", e);
        }
    }
    
    public Document branchToDocument(Object branch) {
        try {
            return new Document()
                .append("branchID", getField(branch, "branchID"))
                .append("branchName", getField(branch, "branchName"))
                .append("address", getField(branch, "address"))
                .append("accounts", getField(branch, "accounts"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Branch to Document", e);
        }
    }

    public Document bankToDocument(Object bank) {
        try {
            return new Document()
                .append("name", getField(bank, "name"))
                .append("bankID", getField(bank, "bankID"))
                .append("branches", getField(bank, "branches"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Bank to Document", e);
        }
    }
    
    public Document transactionToDocument(Object transaction) {
        try {
            return new Document()
                .append("transactionID", getField(transaction, "transactionID"))
                .append("sourceAccountID", getField(transaction, "sourceAccountID"))
                .append("receiverAccountID", getField(transaction, "receiverAccountID"))
                .append("amount", getField(transaction, "amount"))
                .append("transactionType", getField(transaction, "transactionType"))
                .append("transactionDateTime", getField(transaction, "transactionDateTime"))
                .append("status", getField(transaction, "status"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Transaction to Document", e);
        }
    }
    
    // Helper method to get field using reflection
    private Object getField(Object obj, String fieldName) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            // Try getter method
            try {
                String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                java.lang.reflect.Method method = obj.getClass().getMethod(methodName);
                return method.invoke(obj);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to get field: " + fieldName, ex);
            }
        }
    }

    // Helper method to set field using reflection
    private void setField(Object obj, String fieldName, Object value) {
        try {
            // Try to access field directly
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            // Try setter method
            try {
                String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                Class<?> paramType = value != null ? value.getClass() : Object.class;
                
                // Handle primitive types
                if (value instanceof Double) paramType = double.class;
                else if (value instanceof Integer) paramType = int.class;
                else if (value instanceof Boolean) paramType = boolean.class;
                
                java.lang.reflect.Method method = obj.getClass().getMethod(methodName, paramType);
                method.invoke(obj, value);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to set field: " + fieldName, ex);
            }
        }
    }

    // ----DOCUMENT TO CLASS METHODS----
    // Document to UserAccount
    public <T> T documentToUserAccount(Document doc, Class<T> userAccountClass) {
        try {
            T userAccount = userAccountClass.getDeclaredConstructor().newInstance();
            setField(userAccount, "userID", doc.getString("userID"));
            setField(userAccount, "passwordHash", doc.getString("passwordHash"));
            setField(userAccount, "branchId", doc.getString("branch"));
            setField(userAccount, "firstName", doc.getString("first_name"));
            setField(userAccount, "lastName", doc.getString("last_name"));
            setField(userAccount, "username", doc.getString("username"));
            
            List<String> transactionHistory = (List<String>) doc.get("transactionHistory");
            if (transactionHistory != null) {
                setField(userAccount, "transactionHistory", transactionHistory);
            }
            
            // Convert accounts from List<Document> to Account[]
            List<Document> accountDocs = (List<Document>) doc.get("accounts");
            if (accountDocs != null) {
                Account[] accountArray = new Account[accountDocs.size()];
                for (int i = 0; i < accountDocs.size(); i++) {
                    accountArray[i] = documentToAccount(accountDocs.get(i));
                }
                setField(userAccount, "accounts", accountArray);
            }
            
            return userAccount;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Document to UserAccount", e);
        }
    }

    // Helper method to convert a Document to an Account object
    private Account documentToAccount(Document doc) {
        // The document has keys like "Savings", "Checking", "Card"
        // You need to determine which type and create the appropriate Account subclass
        //!!!!!TO DO: come back when Savings, Checking and Card classes are fixed!!!!!
        if (doc.containsKey("Savings")) {
            //return new Savings(doc.getDouble("Savings"));
        } else if (doc.containsKey("Checking")) {
            //return new Checking(doc.getDouble("Checking"));
        } else if (doc.containsKey("Card")) {
            //return new Card(doc.getDouble("Card"));
        }
        
        throw new IllegalArgumentException("Unknown account type in document");
    }

    // Document to BankTellerAccount
    public <T> T documentToBankTellerAccount(Document doc, Class<T> tellerClass) {
        try {
            T teller = tellerClass.getDeclaredConstructor().newInstance();
            setField(teller, "bankTellerID", doc.getString("bankTellerID"));
            setField(teller, "username", doc.getString("username"));
            setField(teller, "passwordHash", doc.getString("passwordHash"));
            setField(teller, "firstname", doc.getString("firstname"));  // lowercase to match field
            setField(teller, "lastname", doc.getString("lastname"));    // lowercase to match field
            setField(teller, "branchID", doc.getString("branchID"));
            return teller;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Document to BankTellerAccount", e);
        }
    }

    // Document to DatabaseAdministratorAccount
    public <T> T documentToAdminAccount(Document doc, Class<T> adminClass) {
        try {
            T admin = adminClass.getDeclaredConstructor().newInstance();
            setField(admin, "adminID", doc.getString("adminID"));
            setField(admin, "username", doc.getString("username"));
            setField(admin, "passwordHash", doc.getString("passwordHash"));
            setField(admin, "firstname", doc.getString("firstname"));  // lowercase to match field
            setField(admin, "lastname", doc.getString("lastname"));    // lowercase to match field
            return admin;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Document to DatabaseAdministratorAccount", e);
        }
    }

    // Document to Branch
    public <T> T documentToBranch(Document doc, Class<T> branchClass) {
        try {
            T branch = branchClass.getDeclaredConstructor().newInstance();
            setField(branch, "branchID", doc.getString("branchID"));
            setField(branch, "branchName", doc.getString("branchName"));
            setField(branch, "address", doc.getString("address"));
            
            List<String> accounts = (List<String>) doc.get("accounts");
            if (accounts != null) {
                setField(branch, "accounts", accounts);
            }
            
            return branch;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Document to Branch", e);
        }
    }

    // Document to Bank
    public <T> T documentToBank(Document doc, Class<T> bankClass) {
        try {
            T bank = bankClass.getDeclaredConstructor().newInstance();
            setField(bank, "name", doc.getString("name"));
            setField(bank, "bankID", doc.getString("bankID"));
            
            List<String> branches = (List<String>) doc.get("branches");
            if (branches != null) {
                setField(bank, "branches", branches);
            }
            
            return bank;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Document to Bank", e);
        }
    }

    // Document to Transaction
    public <T> T documentToTransaction(Document doc, Class<T> transactionClass) {
        try {
            T transaction = transactionClass.getDeclaredConstructor().newInstance();
            setField(transaction, "transactionID", doc.getString("transactionID"));
            setField(transaction, "sourceAccountID", doc.getString("sourceAccountID"));
            setField(transaction, "receiverAccountID", doc.getString("receiverAccountID"));
            setField(transaction, "amount", doc.getDouble("amount"));
            setField(transaction, "transactionType", doc.getString("transactionType"));
            setField(transaction, "transactionDateTime", doc.getString("transactionDateTime"));
            setField(transaction, "status", doc.getString("status"));
            return transaction;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Document to Transaction", e);
        }
    }

    // Document to IUser (works for any account type)
    public IUser documentToIUser(Document doc) {
        // Check which type of account it is based on the fields in the document
        if (doc.containsKey("userID")) {
            // It's UserAccount
            return documentToUserAccount(doc, UserAccount.class);
        } else if (doc.containsKey("bankTellerID")) {
            // It's BankTellerAccount
            return documentToBankTellerAccount(doc, BankTellerAccount.class);
        } else if (doc.containsKey("adminID")) {
            // It's DatabaseAdministratorAccount
            return documentToAdminAccount(doc, DatabaseAdministratorAccount.class);
        } else {
            throw new IllegalArgumentException("Unknown account type in document");
        }
    }
}