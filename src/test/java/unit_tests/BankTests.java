package unit_tests;

import database.Database;
import domain.accounts.Account;
import domain.bank.*;
import domain.users.*;
import domain.transactions.Transaction;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

public class BankTests {

    // ================= BANK TESTS =================

    @Test
    static void test_AddBranches() {
        System.out.println("\n--- Bank.addBranch() ---");

        Bank bank = new Bank("MyBank", "B1", null);

        bank.addBranch(new Branch("BR1", "North Branch", "123 North St"));
        bank.addBranch(new Branch("BR2", "South Branch", "456 South St"));

        if (bank.getBranches().size() == 2)
            System.out.println("PASS");
        else
            System.out.println("FAIL");
    }

    @Test
    static void test_SearchByID() {
        System.out.println("\n--- Bank.searchByID() ---");

        Bank bank = new Bank("MyBank", "B2");
        Branch br = new Branch("BR1", "Main", "123 St");

        UserAccount u1 = new UserAccount("U500", "amy", "A", "Smith", "hash", "BR1", new Account[] {});
        br.addAccount(u1);
        bank.addBranch(br);

        IUser result = bank.searchByID("U500");

        if (result != null && result.getUsername().equals("amy"))
            System.out.println("PASS");
        else
            System.out.println("FAIL");
    }

    @Test
    static void test_SearchByAttribute() {
        System.out.println("\n--- Bank.searchByAttribute() ---");

        Bank bank = new Bank("BankX", "B3");
        Branch br = new Branch("BRZ", "ZetaBranch", "789 Road");

        br.addAccount(new UserAccount("1", "john", "f", "l", "p", "BRZ", new Account[] {}));
        br.addAccount(new UserAccount("2", "john", "f", "l", "p", "BRZ", new Account[] {}));
        br.addAccount(new UserAccount("3", "mark", "f", "l", "p", "BRZ", new Account[] {}));

        bank.addBranch(br);

        var users = bank.searchByAttribute("username", "john");

        if (users.size() == 2)
            System.out.println("PASS");
        else
            System.out.println("FAIL");
    }

    @Test
    static void test_FilterResults() {
        System.out.println("\n--- Bank.filterResults() ---");

        Bank bank = new Bank("BankFilt", "B4");

        ArrayList<IUser> list = new ArrayList<>();
        list.add(new UserAccount("101", "alice", "f", "l", "p", "BR", new Account[] {}));
        list.add(new UserAccount("102", "bob", "f", "l", "p", "BR", new Account[] {}));
        list.add(new UserAccount("103", "alice", "f", "l", "p", "BR", new Account[] {}));

        var filtered = bank.filterResults(list, "username", "alice");

        if (filtered.size() == 2)
            System.out.println("PASS");
        else
            System.out.println("FAIL");
    }

    // ================= BRANCH TESTS =================

    @Test
    static void test_AddAccountToBranch() {
        System.out.println("\n--- Branch.addAccount() ---");

        Branch branch = new Branch("BR10", "Main", "123 St");
        UserAccount acc = new UserAccount("200", "user", "f", "l", "p", "BR10", new Account[] {});

        branch.addAccount(acc);

        if (branch.getAccount("200") != null)
            System.out.println("PASS");
        else
            System.out.println("FAIL");
    }

    @Test
    static void test_RemoveAccountFromBranch() {
        System.out.println("\n--- Branch.removeAccount() ---");

        Branch branch = new Branch("BR11", "Main", "123 St");
        UserAccount acc = new UserAccount("300", "user2", "f", "l", "p", "BR11", new Account[] {});

        branch.addAccount(acc);
        branch.removeUserAccount("300");

        if (branch.getAccount("300") == null)
            System.out.println("PASS");
        else
            System.out.println("FAIL");
    }
}
