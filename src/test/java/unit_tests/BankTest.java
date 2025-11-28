package unit_tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import database.Database;
import domain.bank.Bank;
import domain.bank.Branch;

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
        bank.getBranches().add(b1);
        bank.getBranches().add(b2);

        ArrayList<Branch> list = bank.getBranches();

        assertEquals(2, list.size());
        assertTrue(list.contains(b1));
        assertTrue(list.contains(b2));
    }

    // Database stub
    class FakeDatabase extends Database {
        public ArrayList<Branch> addedBranches = new ArrayList<>();
        public ArrayList<String> updatedBanks = new ArrayList<>();

        public HashMap<String, Branch> savedBranches = new HashMap<>();

        @Override
        public ArrayList<Branch> getAllBranches() {
            return this.addedBranches;
        }

    }
}

