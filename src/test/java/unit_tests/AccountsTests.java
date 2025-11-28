package unit_tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import database.Database;
import domain.accounts.*;
import domain.users.UserAccount;

public class AccountsTests {

  private FakeDatabase fakeDb;
  private UserAccount testUser;

  @BeforeEach
  void setup() {
    fakeDb = new FakeDatabase();
    testUser = new UserAccount("UT01", "testuser", "Test", "User", "hash", "BR01", new ArrayList<Account>());

    fakeDb.userToReturn = testUser;
    Database.setMockInstance(fakeDb);
  }

  // ---------------- CHECKING ----------------

  @Test
  void testChecking_applyMonthlyFee() {
    // Arrange
    Checking testChecking = new Checking("UT01", "CHK01", 200, 100, 50, 10);
    testUser.getAccounts().add(testChecking);

    // Act
    testChecking.applyMonthlyFee();

    // Assert
    assertEquals(190, testChecking.getBalance());
  }

  // ---------------- SAVINGS ----------------

  @Test
  void testSavings_calculateInterest() {
    // Arrange
    Savings testSavings = new Savings("UT02", "SAV01", 1000, 0.05, 0);
    testUser.getAccounts().add(testSavings);

    // Act
    double result = testSavings.calculateInterest();

    // Assert
    assertEquals(50, result);
  }

  // ---------------- CARD ----------------

  @Test
  void testCard_applyMonthlyFee() {
    // Arrange
    Card testCard = new Card("UT03", "CARD01", 100, 500, 0.05, 50);
    testUser.getAccounts().add(testCard);

    // Act
    testCard.applyMonthlyFee();
    double expected = (500 - 100) * 0.05;

    // Assert
    assertEquals(100 - expected, testCard.getBalance());
  }

  public class FakeDatabase extends Database {

    List<UserAccount> accounts = new ArrayList<>();

    public UserAccount userToReturn;

    @Override
    public UserAccount retrieveUserAccount(String id) {
      return userToReturn;
    }

    @Override
    public void addAccount(UserAccount account) {
      this.accounts.add(account);
    }

    @Override
    public void updateUserAccount(String accountID, UserAccount updatedAccount) {
      for (int i = 0; i < accounts.size(); i++) {
        if (accounts.get(i).getUserID().equals(accountID)) {
          accounts.set(i, updatedAccount);
          return;
        }
      }
    }

  }

}
