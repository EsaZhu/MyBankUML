package unit_tests;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import database.Database;
import domain.accounts.Savings;
import domain.enums.TransactionStatus;
import domain.transactions.Transaction;
import domain.users.*;

public class BankTellerTests {

  private FakeDatabase db;
  private BankTellerAccount teller;

  @BeforeEach
  void setup() throws Exception {
    db = new FakeDatabase();

    Field instance = Database.class.getDeclaredField("instance");
    instance.setAccessible(true);
    instance.set(null, db); // replace singleton

    teller = new BankTellerAccount(
        "T001",
        "tellerbingo",
        "Bingo",
        "Bongo",
        "hashedpassword",
        "BR01",
        db);
  }

  @Test
  void test_Search_And_Manage_CustomerAccount() {
    // Arrange
    UserAccount u1 = new UserAccount("UT01", "aaa", "aaa", "One", "p1", "BR01", new ArrayList<>());
    UserAccount u2 = new UserAccount("UT02", "bbb", "bbb", "Two", "p2", "BR01", new ArrayList<>());
    UserAccount u3 = new UserAccount("UT03", "ccc", "ccc", "Three", "p3", "BR01", new ArrayList<>());

    u1.getAccounts().add(new Savings("UT01", "A1", 50, 0.03, 0));
    u2.getAccounts().add(new Savings("UT02", "A2", 75, 0.03, 0));
    u3.getAccounts().add(new Savings("UT03", "A3", 100, 0.03, 0));

    db.savedUsers.add(u1);
    db.savedUsers.add(u2);
    db.savedUsers.add(u3);

    // Act
    ArrayList<IUser> result = db.searchCustomersByAttribute("userID", "UT02");

    // Assert
    assertEquals(1, result.size());
    assertEquals("UT02", ((UserAccount) result.get(0)).getUserID());

    UserAccount target = (UserAccount) result.get(0);
    target.setTransactionHistory(new ArrayList<>());

    assertNotEquals(null, target.getTransactionHistory());
  }

  @Test
  void test_ProcessTransaction_And_GenerateReport() throws Exception {
    // Arrange
    UserAccount user = new UserAccount(
        "U200",
        "newcustomer",
        "Agent",
        "Cooper",
        "twinpeaks",
        "BR01",
        new ArrayList<>());

    Savings account = new Savings("U200", "SAV200", 300.0, 0.03, 0);
    user.getAccounts().add(account);

    db.savedUsers.add(user);
    db.savedAccounts.add(account);

    // Act
    boolean ok = teller.depositForCustomer("U200", 100);

    // Assert
    assertTrue(ok);
    assertEquals(400.0, account.getBalance());

    // ---- report catching ----
    // Arrange
    Transaction txn = new Transaction(
        "TXN_SAV_W_TEST",
        "U200",
        "SAV200",
        null,
        null,
        100,
        "DEPOSIT",
        LocalDateTime.now(),
        TransactionStatus.PENDING);

    // Act
    account.getTransactions().add(txn);

    TestOutputCapture capture = new TestOutputCapture();
    capture.start();
    teller.generateReports();
    String report = capture.stop();

    // Assert
    assertTrue(report.contains("Total customers: 1"));
    assertTrue(report.contains("Total balance across branch: 400.0"));
  }

  // --------------------- Fake Database Mock ---------------------
  static class FakeDatabase extends Database {

    public ArrayList<UserAccount> savedUsers = new ArrayList<>();
    public ArrayList<Account> savedAccounts = new ArrayList<>();

    @Override
    public UserAccount retrieveUserAccount(String id) {
      for (UserAccount user : savedUsers) {
        if (user.getUserID().equals(id))
          return user;
        for (Account acc : user.getAccounts()) {
          if (acc.getAccountID().equals(id))
            return user;
        }
      }
      return null;
    }

    @Override
    public Account retrieveAccountByAccountID(String id) {
      for (UserAccount user : savedUsers) {
        for (Account acc : user.getAccounts()) {
          if (acc.getAccountID().equals(id))
            return acc;
        }
      }
      return null;
    }

    @Override
    public ArrayList<IUser> searchCustomersByAttribute(String fieldName, Object value) {
      ArrayList<IUser> result = new ArrayList<>();

      for (UserAccount user : savedUsers) {
        switch (fieldName) {
          case "userID":
            if (user.getUserID().equals(value))
              result.add(user);
            break;
          case "branchId":
            if (user.getBranchID().equals(value)) {
              result.add(user);
            }
            break;
        }
      }
      return result;
    }

    @Override
    public void addAccount(UserAccount account) {
      savedUsers.add(account);
    }

    @Override
    public void updateUserAccount(String accountID, UserAccount updatedAccount) {
      for (int i = 0; i < savedUsers.size(); i++) {
        if (savedUsers.get(i).getUserID().equals(accountID)) {
          savedUsers.set(i, updatedAccount);
          return;
        }
      }
    }
  }

  // --------------------- Capture System.out ---------------------
  static class TestOutputCapture {
    private java.io.PrintStream original;
    private java.io.ByteArrayOutputStream stream;

    void start() {
      original = System.out;
      stream = new java.io.ByteArrayOutputStream();
      System.setOut(new java.io.PrintStream(stream));
    }

    String stop() {
      System.setOut(original);
      return stream.toString();
    }
  }
}
