package unit_tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import database.Database;
import domain.accounts.Card;
import domain.accounts.Checking;
import domain.accounts.Savings;
import domain.users.Account;
import domain.users.UserAccount;

public class AccountsTests {

  private FakeDatabase fakeDb;
  private UserAccount testUser;

  @BeforeEach
  void setup() throws Exception {
    fakeDb = new FakeDatabase();
    testUser = new UserAccount("UT01", "UFO_believer", "John", "Doe", "hashedpassword", "B01", new ArrayList<>());
    fakeDb.savedUserAccount = testUser;

    Field instance = Database.class.getDeclaredField("instance");
    instance.setAccessible(true);
    instance.set(null, fakeDb); // replace singleton
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
    Savings testSavings = new Savings("UT01", "SAV01", 1000, 0.05, 0);
    testUser.getAccounts().add(testSavings);

    // Act
    double expected = 1000 * 0.05;
    double result = testSavings.calculateInterest();

    // Assert
    assertEquals(expected, result);
  }

  // ---------------- CARD ----------------
  @Test
  void testCard_applyMonthlyFee() {
    // Arrange
    Card testCard = new Card("UT01", "CARD01", 100, 500, 0.05, 50);
    testUser.getAccounts().add(testCard);
    double initialBalance = testCard.getBalance();

    // Act
    testCard.applyMonthlyFee();
    double expected = initialBalance - (500 - initialBalance) * 0.05;

    // Assert
    assertEquals(expected, testCard.getBalance());
  }

  // ---------------- DEPOSIT ----------------
  @Test
  void testAccountDeposit() {
    // Arrange
    Account acc = new Savings("UT01", "ACC01", 0, 0.03, 0);
    testUser.getAccounts().add(acc);

    // Act
    acc.deposit(100);

    // Assert
    assertEquals(100, acc.getBalance());
  }

  // ---------------- WITHDRAW ----------------
  @Test
  void testAccount_Withdraw() {
    // Arrange
    Account acc = new Savings("UT01", "ACC02", 100, 0.03, 0);
    testUser.getAccounts().add(acc);

    // Act
    acc.withdraw(100);

    // Assert
    assertEquals(0, acc.getBalance());
  }

  // ---------------- TRANSFER FUNDS ----------------
  @Test
  void testTransferFunds() {
    // Arrange
    Account sourceAccount = new Savings("UT01", "ACC03", 100, 0.03, 0);
    Account receiverAccount = new Checking("UT02", "ACC04", 0, 100, 50, 10);

    UserAccount user2 = new UserAccount("UT02", "AnotherUser", "Funny", "Valentine",
        "pass", "B02", new ArrayList<>());

    testUser.getAccounts().add(sourceAccount);
    user2.getAccounts().add(receiverAccount);

    // Act
    sourceAccount.transferFunds(user2, receiverAccount.getAccountID(), 100);

    // Assert
    assertEquals(0, sourceAccount.getBalance());
    assertEquals(100, receiverAccount.getBalance());
  }

  // --------------------- Fake Database Mock ---------------------
  public class FakeDatabase extends Database {
    public UserAccount savedUserAccount;

    @Override
    public UserAccount retrieveUserAccount(String ID) {
      return this.savedUserAccount;
    }

    @Override
    public void updateUserAccount(String accountID, UserAccount updatedAccount) {
      this.savedUserAccount = updatedAccount;
    }
  }
}