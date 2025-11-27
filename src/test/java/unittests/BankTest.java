package unittests;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import domain.users.IUser;
import domain.users.UserAccount;
import domain.bank.Bank;
import domain.bank.Branch;

public class BankTest {

    private Bank bank;
    private FakeDatabase fakeDb;
    private ArrayList<IUser> resultList;

    @BeforeEach
    void setUp() {
        fakeDb = new FakeDatabase();
        resultList = new ArrayList<>();
        bank = new Bank("Test Bank", "B100", resultList, fakeDb);
    }

    // Test addBranch
    @Test
    void testAddBranch() {
        Branch b = new Branch("BR1", "Main Branch", "Test Bank");
        bank.addBranch(b);

        assertTrue(fakeDb.addedBranches.contains(b));
        assertTrue(fakeDb.updatedBanks.contains("B100"));
    }

    // Test getBranches
    @Test
    void testGetBranches() {
        Branch b1 = new Branch("BR1", "Main", "Test Bank");
        Branch b2 = new Branch("BR2", "West", "Test Bank");

        fakeDb.savedBranches.put("BR1", b1);
        fakeDb.savedBranches.put("BR2", b2);

        bank.addBranch(b1);
        bank.addBranch(b2);

        ArrayList<Branch> list = bank.getBranches();

        assertEquals(2, list.size());
        assertTrue(list.contains(b1));
        assertTrue(list.contains(b2));
    }

    // Test searchByID 
    @Test
    void testSearchByID() {
        IUser user = new UserAccount("U1", "John", null, null, null, null, null);
        fakeDb.userByID = user;

        IUser result = bank.searchByID("U1");

        assertEquals(user, result);
        assertEquals(1, bank.resultList.size());
        assertEquals(user, bank.resultList.get(0));
    }

    // Test searchByAttribute 
    @Test
    void testSearchByAttribute() {
        IUser u1 = new UserAccount("U1", "John", null, null, null, null, null);
        IUser u2 = new UserAccount("U2", "Tim", null, null, null, null, null);

        ArrayList<IUser> users = new ArrayList<>();
        users.add(u1);
        users.add(u2);
        fakeDb.attributeResults = users;

        ArrayList<IUser> results = bank.searchByAttribute("name", "John");

        assertEquals(2, results.size());
        assertEquals(results, bank.resultList);
    }

    // Database stub
    class FakeDatabase {
        public ArrayList<Branch> addedBranches = new ArrayList<>();
        public ArrayList<String> updatedBanks = new ArrayList<>();

        public HashMap<String, Branch> savedBranches = new HashMap<>();

        public IUser userByID;
        public ArrayList<IUser> attributeResults;

        public void addBranch(Branch b) {
            addedBranches.add(b);
        }

        public Branch retrieveBranch(String id) {
            return savedBranches.get(id);
        }

        public void updateBank(String id, Bank bank) {
            updatedBanks.add(id);
        }

        public IUser findUserByID(String id) {
            return userByID;
        }

        public ArrayList<IUser> searchAccountsByAttribute(String key, String value) {
            return attributeResults;
        }
    }

}
