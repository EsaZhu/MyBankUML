package unittests;


import domain.transactions.Transaction;
import domain.users.*;

public class UsersTests {

    public static void main(String[] args) {
        System.out.println("===== USERS + ADMIN TESTS =====");

        // USERACCOUNT basic operations
        testDeposit();
        testWithdraw();
        testTransfer();

        // DATABASE ADMIN tests
        test_Admin_CreateAccounts();
        test_Admin_ReverseTransaction();
        test_Admin_EmptyAuditReport();
    }

    // ---------------- USERACCOUNT ----------------

    static void testDeposit() {
        UserAccount u = new UserAccount("U1", "user", "f", "l", "hash", "BR1", new Account[] {});
        u.setBalance(0);
        u.deposit(100);

        if (u.getBalance() == 100)
            System.out.println("PASS");
        else
            System.out.println("FAIL");
    }

    static void testWithdraw() {
        UserAccount u = new UserAccount("U2", "user", "f", "l", "hash", "BR1", new Account[] {});
        u.setBalance(100);
        u.withdraw(100);

        if (u.getBalance() == 0)
            System.out.println("PASS");
        else
            System.out.println("FAIL");
    }

    static void testTransfer() {
        UserAccount u1 = new UserAccount("A", "u", "f", "l", "p", "BR1", new Account[] {});
        UserAccount u2 = new UserAccount("B", "u", "f", "l", "p", "BR1", new Account[] {});

        u1.setBalance(100);
        u2.setBalance(0);

        u1.transferFunds(u2, 100);

        if (u1.getBalance() == 0 && u2.getBalance() == 100)
            System.out.println("PASS");
        else
            System.out.println("FAIL");
    }

    // ---------------- DATABASE ADMINISTRATOR ----------------

    static void test_Admin_CreateAccounts() {
        System.out.println("\n--- Admin.manageTellerAccounts + manageCustomerAccounts ---");

        DatabaseAdministratorAccount admin = new DatabaseAdministratorAccount("A1", "admin", "F", "L", "hash");

        admin.manageTellerAccounts("CREATE", "BT1", "pass", "BR1");
        admin.manageCustomerAccounts("CREATE", "U1", "John", "Doe", "pass", "BR1");

        var tellers = admin.searchAccounts("teller");
        var customers = admin.searchAccounts("customer");

        if (tellers.size() >= 1 && customers.size() >= 1)
            System.out.println("PASS");
        else
            System.out.println("FAIL");
    }

    static void test_Admin_ReverseTransaction() {
        System.out.println("\n--- Admin.reverseTransactions() ---");

        DatabaseAdministratorAccount admin = new DatabaseAdministratorAccount("A1", "ad", "F", "L", "p");

        Transaction t = new Transaction(
                "TRX", "A1", "A2", 200, "TRANSFER",
                java.time.LocalDateTime.now(),
                TransactionStatus.COMPLETED);

        admin.reverseTransactions(t);

        if (t.getStatus() == TransactionStatus.REVERSED)
            System.out.println("PASS");
        else
            System.out.println("FAIL");
    }

    static void test_Admin_EmptyAuditReport() {
        System.out.println("\n--- Admin.generateReports() empty ---");

        DatabaseAdministratorAccount admin = new DatabaseAdministratorAccount("A2", "ad2", "F", "L", "p");

        Report r = admin.generateReports();

        if (r.isEmpty())
            System.out.println("PASS");
        else
            System.out.println("FAIL");
    }
}
