package unit_tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import domain.users.UserAccount;
import domain.users.BankTellerAccount;
import domain.users.DatabaseAdministratorAccount;
import domain.bank.Bank;
import domain.bank.Branch;

class DatabaseTest {

    private FakeDatabase db;

    @BeforeEach
    void setUp() {
        db = new FakeDatabase();
    }

    @Test
    void test_DB_Connect() {
        boolean connected = db.connect();
        assertEquals(true, connected);
    }

    @Test
    void testAddAndRetrieveAccount() {
        UserAccount u1 = new UserAccount("U1", "John", null, null, null, null, null);
        db.addAccount(u1);

        UserAccount retrieved = db.retrieveAccount("U1");
        assertEquals(u1, retrieved);
    }

    @Test
    void testUpdateAccount() {
        UserAccount u1 = new UserAccount("U1", "John", null, null, null, null, null);
        db.addAccount(u1);

        u1 = new UserAccount("U1", "Johnny", null, null, null, null, null);
        db.updateAccount("U1", u1);

        UserAccount retrieved = db.retrieveAccount("U1");
        assertEquals("Johnny", retrieved.getUsername());
    }

    @Test
    void testRemoveAccount() {
        UserAccount u1 = new UserAccount("U1", "John", null, null, null, null, null);
        db.addAccount(u1);

        db.removeAccount("U1");
        assertNull(db.retrieveAccount("U1"));
    }

    @Test
    void testAddAndRetrieveBranch() {
        Branch b = new Branch("B1", "Main Branch", "Addr");
        db.addBranch(b);

        Branch retrieved = db.retrieveBranch("B1");
        assertEquals(b, retrieved);
    }

    @Test
    void testAddAndRetrieveBank() {
        Bank bank = new Bank("Test Bank", "Bank1", null);
        db.addBank(bank);

        Bank retrieved = db.retrieveBank("Bank1");
        assertEquals(bank, retrieved);
    }

    @Test
    void testAddAndRetrieveTeller() {
        BankTellerAccount teller = new BankTellerAccount("T01", "cashier", "JC", "Denton", "hashedpassword", "BR01", null);
        db.addTeller(teller);

        BankTellerAccount retrieved = db.retrieveTeller("T1");
        assertEquals(teller, retrieved);
    }

    @Test
    void testAddAndRetrieveAdmin() {
        DatabaseAdministratorAccount admin = new DatabaseAdministratorAccount("A1", "admin", "pw", "John", "Doe");
        db.addAdmin(admin);

        DatabaseAdministratorAccount retrieved = db.retrieveAdmin("A1");
        assertEquals(admin, retrieved);
    }

    @Test
    void testSearchByAttribute() {
        UserAccount u1 = new UserAccount("U1", "John", null, null, null, null, null);
        UserAccount u2 = new UserAccount("U2", "John", null, null, null, null, null);
        db.addAccount(u1);
        db.addAccount(u2);

        ArrayList<UserAccount> results = db.searchAccountsByAttribute("firstName", "John");
        assertEquals(2, results.size());
        assertTrue(results.contains(u1));
        assertTrue(results.contains(u2));
    }

    // Database mock up
    static class FakeDatabase {
        HashMap<String, UserAccount> accounts = new HashMap<>();
        HashMap<String, BankTellerAccount> tellers = new HashMap<>();
        HashMap<String, DatabaseAdministratorAccount> admins = new HashMap<>();
        HashMap<String, Branch> branches = new HashMap<>();
        HashMap<String, Bank> banks = new HashMap<>();

        public boolean connect() {
            return true;
        }

        public void addAccount(UserAccount account) {
            accounts.put(account.getUserID(), account);
        }

        public UserAccount retrieveAccount(String accountID) {
            return accounts.get(accountID);
        }

        public void updateAccount(String accountID, UserAccount updatedAccount) {
            accounts.put(accountID, updatedAccount);
        }

        public void removeAccount(String accountID) {
            accounts.remove(accountID);
        }

        public void addBranch(Branch branch) {
            branches.put(branch.getBranchID(), branch);
        }

        public Branch retrieveBranch(String branchID) {
            return branches.get(branchID);
        }

        public void addBank(Bank bank) {
            banks.put(bank.getBankID(), bank);
        }

        public Bank retrieveBank(String bankID) {
            return banks.get(bankID);
        }

        public void addTeller(BankTellerAccount teller) {
            tellers.put(teller.getUsername(), teller);
        }

        public BankTellerAccount retrieveTeller(String tellerID) {
            return tellers.get(tellerID);
        }

        public void addAdmin(DatabaseAdministratorAccount admin) {
            admins.put(admin.getAdminID(), admin);
        }

        public DatabaseAdministratorAccount retrieveAdmin(String adminID) {
            return admins.get(adminID);
        }

        public ArrayList<UserAccount> searchAccountsByAttribute(String fieldName, Object value) {
            ArrayList<UserAccount> result = new ArrayList<>();
            for (UserAccount u : accounts.values()) {
                if ("username".equals(fieldName) && value.equals(u.getUsername())) {
                    result.add(u);
                }
            }
            return result;
        }
    }
}
