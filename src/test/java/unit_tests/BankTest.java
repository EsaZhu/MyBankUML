package unit_tests;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import domain.bank.Bank;
import domain.bank.Branch;
import database.Database;

public class BankTest {

    private Bank bank;
    private FakeDatabase fakeDb;

    @BeforeEach
    void setUp() {
        fakeDb = new FakeDatabase();
        bank = new Bank("Test Bank", "B100", fakeDb);
    }

    // Test getBranches
    @Test
    void testGetBranches() {
        Branch b1 = new Branch("BR1", "Main", "Test Bank");
        Branch b2 = new Branch("BR2", "West", "Test Bank");

        fakeDb.branches.add(b1);
        fakeDb.branches.add(b2);

        ArrayList<Branch> list = bank.getBranches();

        assertEquals(2, list.size());
        assertTrue(list.contains(b1));
        assertTrue(list.contains(b2));
    }

    // Test getBranches when empty
    @Test
    void testGetBranchesEmpty() {
        ArrayList<Branch> list = bank.getBranches();
        
        assertTrue(list.isEmpty());
    }

    // Database stub
    class FakeDatabase {
        public ArrayList<Branch> branches = new ArrayList<>();

        public ArrayList<Branch> getAllBranches() {
            return branches;
        }
    }
}