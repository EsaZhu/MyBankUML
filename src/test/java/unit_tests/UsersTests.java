package unit_tests;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import database.Database;
import domain.accounts.Savings;
import domain.enums.TransactionStatus;
import domain.transactions.Transaction;
import domain.users.*;

public class UsersTests {

  private FakeDatabase db;

  @BeforeEach
  void setUp() throws Exception{
    db = new FakeDatabase();
    Field instance = Database.class.getDeclaredField("instance");
    instance.setAccessible(true);
    instance.set(null, db); // replace singleton
  }

  // ---------------- DATABASE ADMINISTRATOR ----------------

  @Test
  void test_Admin_ReverseTransaction() throws NoSuchFieldException, IllegalAccessException {
    System.out.println("\n--- Admin.reverseTransactions() ---");

    DatabaseAdministratorAccount admin = new DatabaseAdministratorAccount("AD1", "Admin", "Lief", "Erikson",
        "hashedpassword");

    Field field = DatabaseAdministratorAccount.class.getDeclaredField("database");
    field.setAccessible(true);
    field.set(admin, db);

    Transaction t = new Transaction(
        "TXN_CRD_T001",
        "U100",
        "A100",
        null,
        null,
        100.0,
        "DEPOSIT",
        LocalDateTime.now(),
        TransactionStatus.PENDING);

    db.savedTransaction = t;
    db.savedUserAccount = new UserAccount("U100", "hotdog123", "Ash", "Ketchup", "hashedpassword", "BR01", new ArrayList<Account>());
    db.savedUserAccount.getAccounts().add(new Savings("U100", "A100", 100, 0.06, 10));
    
    t.execute();

    admin.reverseTransactions("TXN_CRD_T001");

    assertEquals(t.getStatus(), TransactionStatus.REVERSED);
  }

  // --------------------- Fake Database Mock ---------------------
  static class FakeDatabase extends Database {
    public Transaction savedTransaction;
    public UserAccount savedUserAccount;

    @Override
    public Transaction retrieveTransaction(String id) {
      return savedTransaction;
    }

    @Override
    public UserAccount retrieveUserAccount(String id) {
      return savedUserAccount;
    }

    @Override
    public void updateUserAccount(String id, UserAccount user) {
      savedUserAccount = user;
    }
  }

}