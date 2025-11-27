package unit_tests;

import database.Database;
import domain.users.*;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

public class DatabaseAndLoginTests {

    // ================= DATABASE TESTS =================

    @Test
    static void test_DB_Connect() {
        System.out.println("\n--- Database.connect() ---");

        Database db = Database.getInstance();
        boolean connected = db.connect();

        if (connected)
            System.out.println("PASS");
        else
            System.out.println("FAIL");
    }

    @Test
    static void test_DB_AddRetrieve() {
        System.out.println("\n--- addAccount + retrieveAccount ---");

        Database db = Database.getInstance();
        UserAccount u = new UserAccount("1234", "alice", "f", "l", "p", "BR1", new Account[] {});

        db.addAccount(u);

        UserAccount retrieved = db.retrieveAccount("1234");

        if (retrieved != null && retrieved.getUserID().equals("1234"))
            System.out.println("PASS");
        else
            System.out.println("FAIL");
    }

    @Test
    static void test_DB_AddRemove() {
        System.out.println("\n--- addAccount + removeAccount ---");

        Database db = Database.getInstance();
        UserAccount u = new UserAccount("999", "bob", "f", "l", "p", "BR3", new Account[] {});

        db.addAccount(u);
        db.removeAccount("999");

        if (db.retrieveAccount("999") == null)
            System.out.println("PASS");
        else
            System.out.println("FAIL");
    }

    @Test
    static void test_DB_AddUpdate() {
        System.out.println("\n--- addAccount + updateAccount ---");

        Database db = Database.getInstance();
        UserAccount u = new UserAccount("777", "mary", "f", "l", "p", "BR2", new Account[] {});

        db.addAccount(u);

        u.setLastName("UPDATED");
        db.updateAccount("777", u);

        UserAccount updated = db.retrieveAccount("777");

        if (updated != null && updated.getLastName().equals("UPDATED"))
            System.out.println("PASS");
        else
            System.out.println("FAIL");
    }
}
