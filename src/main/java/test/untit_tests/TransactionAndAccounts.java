package test.untit_tests;

import database.Database;
import domain.bank.*;
import domain.users.*;
import domain.accounts.*;
import domain.transactions.Transaction;
import java.util.ArrayList;

public class TransactionAndAccounts {

    public static void main(String[] args) {
        System.out.println("===== TRANSACTION & ACCOUNT TYPES TESTS =====");

        // TRANSACTION TESTS
        test_InsufficientFunds_Execute();
        test_EmptyHistory();
        test_ReverseTransaction();

        // CHECKING
        testChecking_applyMonthlyFee();
        testChecking_checkMinBalance();

        // SAVINGS
        testSavings_calculateInterest();
        testSavings_pay();
        testSavings_receipt();

        // CARD
        testCard_applyMonthlyFee();
        testCard_checkCardLimit();
    }

    // ---------------- TRANSACTION ----------------

    static void test_InsufficientFunds_Execute() {
        Transaction t = new Transaction("T100", "ACC1", null, 1000, "WITHDRAW",
                java.time.LocalDateTime.now(), TransactionStatus.PENDING);

        t.setSourceBalance(250);

        boolean valid = t.validateTransaction();
        boolean executed = t.execute();

        if (!valid && !executed && t.getStatus() == TransactionStatus.FAILED)
            System.out.println("PASS");
        else
            System.out.println("FAIL");
    }

    static void test_EmptyHistory() {
        Transaction t = new Transaction("TX", "X", null, 0, "INFO",
                java.time.LocalDateTime.now(), TransactionStatus.PENDING);

        var list = t.getTransactionHistory("NO_TRANSACTIONS");

        if (list.size() == 0)
            System.out.println("PASS");
        else
            System.out.println("FAIL");
    }

    static void test_ReverseTransaction() {
        Transaction t = new Transaction("TR1", "A1", "A2", 5000, "TRANSFER",
                java.time.LocalDateTime.now(), TransactionStatus.COMPLETED);

        t.setSourceBalance(10000);
        t.setReceiverBalance(1000);

        t.reverseTransaction();

        if (t.getStatus() == TransactionStatus.REVERSED &&
                t.getSourceBalance() == 15000 &&
                t.getReceiverBalance() == -4000)
            System.out.println("PASS");
        else
            System.out.println("FAIL");
    }

    // ---------------- CHECKING ----------------

    static void testChecking_applyMonthlyFee() {
        Checking c = new Checking("C1", "n", "e", "p", 50, null, 100, 0, 10);
        c.applyMonthlyFee();

        if (c.getBalance() == 40)
            System.out.println("PASS");
        else
            System.out.println("FAIL");
    }

    static void testChecking_checkMinBalance() {
        Checking c = new Checking("C2", "n", "e", "p", 500, null, 100, 50, 10);
        boolean ok = c.checkMinBalance();

        if (ok)
            System.out.println("PASS");
        else
            System.out.println("FAIL");
    }

    // ---------------- SAVINGS ----------------

    static void testSavings_calculateInterest() {
        Savings s = new Savings("S1", "n", "e", "p", 1000, null, 0.05, 0);
        double result = s.calculateInterest();

        if (result == 50)
            System.out.println("PASS");
        else
            System.out.println("FAIL");
    }

    static void testSavings_pay() {
        Savings s = new Savings("S2", "n", "e", "p", 500, null, 0.05, 0);
        s.pay(100);

        if (s.getBalance() == 400)
            System.out.println("PASS");
        else
            System.out.println("FAIL");
    }

    static void testSavings_receipt() {
        Savings s = new Savings("S3", "n", "e", "p", 500, null, 0.05, 0);
        s.pay(100);

        Receipt r = s.receipt();

        if (r != null && r.getAmount() == 100)
            System.out.println("PASS");
        else
            System.out.println("FAIL");
    }

    // ---------------- CARD ----------------

    static void testCard_applyMonthlyFee() {
        Card c = new Card("CRD", "n", "e", "p", 100, null, 500, 0.05, 0);
        c.applyMonthlyFee();

        double expected = (500 - 100) * 0.05;
        if (c.getMinimumPayment() == expected)
            System.out.println("PASS");
        else
            System.out.println("FAIL");
    }

    static void testCard_checkCardLimit() {
        Card c = new Card("CRD2", "n", "e", "p", 100, null, 500, 0.05, 0);

        if (c.checkCardLimit() == 500)
            System.out.println("PASS");
        else
            System.out.println("FAIL");
    }
}
