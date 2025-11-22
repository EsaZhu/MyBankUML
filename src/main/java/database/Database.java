package database;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

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
    
    // Generic CRUD operations - work with any collection
    public void insertDocument(String collectionName, Document document) {
        mongoDatabase.getCollection(collectionName).insertOne(document);
    }
    
    public Document findDocument(String collectionName, Bson filter) {
        return mongoDatabase.getCollection(collectionName).find(filter).first();
    }
    
    public List<Document> findDocuments(String collectionName, Bson filter) {
        List<Document> results = new ArrayList<>();
        mongoDatabase.getCollection(collectionName).find(filter).into(results);
        return results;
    }
    
    public void updateDocument(String collectionName, Bson filter, Document updates) {
        mongoDatabase.getCollection(collectionName).updateOne(filter, new Document("$set", updates));
    }
    
    public void deleteDocument(String collectionName, Bson filter) {
        mongoDatabase.getCollection(collectionName).deleteOne(filter);
    }
    
    // User Account Operations
    public void addAccount(Document accountDoc) {
        accountCollection.insertOne(accountDoc);
    }
    
    public Document retrieveAccount(String accountID) {
        return accountCollection.find(Filters.eq("userID", accountID)).first();
    }
    
    public void updateAccount(String accountID, Document updates) {
        accountCollection.updateOne(Filters.eq("userID", accountID), new Document("$set", updates));
    }
    
    public void removeAccount(String accountID) {
        accountCollection.deleteOne(Filters.eq("userID", accountID));
    }
    
    public List<Document> getAllAccounts() {
        List<Document> accounts = new ArrayList<>();
        accountCollection.find().into(accounts);
        return accounts;
    }
    
    // Teller Account Operations
    public void addTeller(Document tellerDoc) {
        tellerCollection.insertOne(tellerDoc);
    }
    
    public Document retrieveTeller(String tellerID) {
        return tellerCollection.find(Filters.eq("bankTellerID", tellerID)).first();
    }
    
    public void updateTeller(String tellerID, Document updates) {
        tellerCollection.updateOne(Filters.eq("bankTellerID", tellerID), new Document("$set", updates));
    }
    
    public void removeTeller(String tellerID) {
        tellerCollection.deleteOne(Filters.eq("bankTellerID", tellerID));
    }
    
    public List<Document> getAllTellers() {
        List<Document> tellers = new ArrayList<>();
        tellerCollection.find().into(tellers);
        return tellers;
    }
    
    // Admin Account Operations
    public void addAdmin(Document adminDoc) {
        adminCollection.insertOne(adminDoc);
    }
    
    public Document retrieveAdmin(String adminID) {
        return adminCollection.find(Filters.eq("adminID", adminID)).first();
    }
    
    public void updateAdmin(String adminID, Document updates) {
        adminCollection.updateOne(Filters.eq("adminID", adminID), new Document("$set", updates));
    }
    
    public List<Document> getAllAdmins() {
        List<Document> admins = new ArrayList<>();
        adminCollection.find().into(admins);
        return admins;
    }
    
    // Transaction Operations
    public void addTransaction(Document transactionDoc) {
        transactionCollection.insertOne(transactionDoc);
    }
    
    public Document retrieveTransaction(String transactionID) {
        return transactionCollection.find(Filters.eq("transactionID", transactionID)).first();
    }
    
    public List<Document> getTransactionHistory(String accountID) {
        List<Document> transactions = new ArrayList<>();
        transactionCollection.find(
            Filters.or(
                Filters.eq("sourceAccountID", accountID),
                Filters.eq("receiverAccountID", accountID)
            )
        ).into(transactions);
        return transactions;
    }
    
    public void updateTransaction(String transactionID, Document updates) {
        transactionCollection.updateOne(Filters.eq("transactionID", transactionID), new Document("$set", updates));
    }
    
    public List<Document> getAllTransactions() {
        List<Document> transactions = new ArrayList<>();
        transactionCollection.find().into(transactions);
        return transactions;
    }
    
    // Branch Operations
    public void addBranch(Document branchDoc) {
        branchCollection.insertOne(branchDoc);
    }
    
    public Document retrieveBranch(String branchID) {
        return branchCollection.find(Filters.eq("branchID", branchID)).first();
    }
    
    public void updateBranch(String branchID, Document updates) {
        branchCollection.updateOne(Filters.eq("branchID", branchID), new Document("$set", updates));
    }
    
    public List<Document> getAllBranches() {
        List<Document> branches = new ArrayList<>();
        branchCollection.find().into(branches);
        return branches;
    }
    
    // Search Operations
    public List<Document> searchAccountsByAttribute(String fieldName, Object value) {
        List<Document> results = new ArrayList<>();
        accountCollection.find(Filters.eq(fieldName, value)).into(results);
        return results;
    }
    
    public Document findUserByUsername(String username) {
        Document user = accountCollection.find(Filters.eq("username", username)).first();
        if (user == null) {
            user = tellerCollection.find(Filters.eq("username", username)).first();
        }
        if (user == null) {
            user = adminCollection.find(Filters.eq("username", username)).first();
        }
        return user;
    }
    
    // Getters for direct collection access if needed
    public MongoCollection<Document> getAccountCollection() {
        return accountCollection;
    }
    
    public MongoCollection<Document> getTellerCollection() {
        return tellerCollection;
    }
    
    public MongoCollection<Document> getAdminCollection() {
        return adminCollection;
    }
    
    public MongoCollection<Document> getTransactionCollection() {
        return transactionCollection;
    }
    
    public MongoCollection<Document> getBranchCollection() {
        return branchCollection;
    }

    // ==================== CONVERSION HELPER METHODS ====================
    
    private Document userAccountToDocument(Object userAccount) {
        try {
            return new Document()
                .append("userID", getField(userAccount, "userID"))
                .append("name", getField(userAccount, "name"))
                .append("passwordHash", getField(userAccount, "passwordHash"))
                .append("balance", getField(userAccount, "balance"))
                .append("branch", getField(userAccount, "branch"))
                .append("transactionHistory", getField(userAccount, "transactionHistory"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert UserAccount to Document", e);
        }
    }
    
    private Document tellerAccountToDocument(Object teller) {
        try {
            return new Document()
                .append("bankTellerID", getField(teller, "bankTellerID"))
                .append("username", getField(teller, "username"))
                .append("passwordHash", getField(teller, "passwordHash"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert BankTellerAccount to Document", e);
        }
    }
    
    private Document adminAccountToDocument(Object admin) {
        try {
            return new Document()
                .append("adminID", getField(admin, "adminID"))
                .append("username", getField(admin, "username"))
                .append("passwordHash", getField(admin, "passwordHash"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert DatabaseAdministratorAccount to Document", e);
        }
    }
    
    private Document branchToDocument(Object branch) {
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
    
    private Document transactionToDocument(Object transaction) {
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

    // Document to UserAccount
    public Object documentToUserAccount(Document doc, Class<?> userAccountClass) {
        try {
            Object userAccount = userAccountClass.getDeclaredConstructor().newInstance();
            setField(userAccount, "userID", doc.getString("userID"));
            setField(userAccount, "name", doc.getString("name"));
            setField(userAccount, "passwordHash", doc.getString("passwordHash"));
            setField(userAccount, "balance", doc.getDouble("balance"));
            setField(userAccount, "branch", doc.getString("branch"));
            
            List<String> transactionHistory = (List<String>) doc.get("transactionHistory");
            if (transactionHistory != null) {
                setField(userAccount, "transactionHistory", transactionHistory);
            }
            
            return userAccount;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Document to UserAccount", e);
        }
    }
    
    // Document to BankTellerAccount
    public Object documentToBankTellerAccount(Document doc, Class<?> tellerClass) {
        try {
            Object teller = tellerClass.getDeclaredConstructor().newInstance();
            setField(teller, "bankTellerID", doc.getString("bankTellerID"));
            setField(teller, "username", doc.getString("username"));
            setField(teller, "passwordHash", doc.getString("passwordHash"));
            return teller;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Document to BankTellerAccount", e);
        }
    }
    
    // Document to DatabaseAdministratorAccount
    public Object documentToAdminAccount(Document doc, Class<?> adminClass) {
        try {
            Object admin = adminClass.getDeclaredConstructor().newInstance();
            setField(admin, "adminID", doc.getString("adminID"));
            setField(admin, "username", doc.getString("username"));
            setField(admin, "passwordHash", doc.getString("passwordHash"));
            return admin;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Document to DatabaseAdministratorAccount", e);
        }
    }
    
    // Document to Branch
    public Object documentToBranch(Document doc, Class<?> branchClass) {
        try {
            Object branch = branchClass.getDeclaredConstructor().newInstance();
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
    
    // Document to Transaction
    public Object documentToTransaction(Document doc, Class<?> transactionClass) {
        try {
            Object transaction = transactionClass.getDeclaredConstructor().newInstance();
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
}