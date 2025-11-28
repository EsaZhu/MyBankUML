package unit_tests;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import domain.enums.TransactionStatus;
import domain.transactions.Transaction;
import domain.users.*;

public class UsersTests {

  // ---------------- DATABASE ADMINISTRATOR ----------------

  @Test
  void test_Admin_ReverseTransaction() {
    System.out.println("\n--- Admin.reverseTransactions() ---");

    DatabaseAdministratorAccount admin = new DatabaseAdministratorAccount("AD1", "Admin", "Lief", "Erikson", "hashedpassword");

    Transaction t = new Transaction(
        "TXN_CRD_T001",
        "U100",
        "A100",
        "U200",
        "A200",
        100.0,
        "TRANSFER",
        LocalDateTime.now(),
        TransactionStatus.PENDING);
    t.execute();

    admin.reverseTransactions("TXN_CRD_T001");

    assertEquals(t.getStatus(), TransactionStatus.REVERSED);
  }
}