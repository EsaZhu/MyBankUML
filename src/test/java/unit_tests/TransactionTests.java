package unit_tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import database.Database;
import domain.accounts.Account;
import domain.accounts.Checking;
import domain.accounts.Savings;
import domain.enums.TransactionStatus;
import domain.transactions.Transaction;
import domain.users.UserAccount;

public class TransactionTests {

  private FakeDatabase fakeDb;
  private UserAccount testUser;

  @BeforeEach
  void setup() {
    fakeDb = new FakeDatabase();
    Account newAccount = new Savings("UT00", "A001", 250.0, 0.03, 100.0);
    testUser = new UserAccount("UT00", "UFO_believer", "John", "Doe", "hashedpassword", "B01", new ArrayList<>());
    testUser.getAccounts().add(newAccount);
    fakeDb.addAccount(testUser);
    Database.setMockInstance(fakeDb);
  }

  @Test
  void testTransactionWithInsufficientFunds() {
    // Arrange & Act
    testUser.getAccounts().get(0).withdraw(1000.0);

    // Assert
    assertEquals(250.0, testUser.getAccounts().get(0).getBalance());
  }

  @Test
  void testTransactionHistoryForAccountWithNoTransactions() {
    // Act & Assert
    assertEquals(0, testUser.getAccounts().get(0).getTransactions().size());
  }

  @Test
  void testReverseCompletedTransaction() {
    // Arrange
    Account newSourceAccount = new Savings("UT01", "A001", 250.0, 0.03, 100.0);
    UserAccount sourceUser = new UserAccount("UT01", "UFO_believer", "John", "Doe", "hashedpassword", "B01",
        new ArrayList<>());
    sourceUser.getAccounts().add(newSourceAccount);
    fakeDb.addAccount(sourceUser);

    Account newReceiverAccount = new Checking("UT02", "A002", 500.0, 200.0, 50.0, 10.0);
    UserAccount receiverUser = new UserAccount("UT02", "UFO_denier", "Jane", "Doe", "hashedpassword", "B02",
        new ArrayList<>());
    receiverUser.getAccounts().add(newReceiverAccount);
    fakeDb.addAccount(receiverUser);

    // Act
    sourceUser.getAccounts().get(0).transferFunds(receiverUser, receiverUser.getAccounts().get(0).getAccountID(),
        100.0);
    Transaction completedTransaction = sourceUser.getAccounts().get(0).getTransactions()
        .get(sourceUser.getAccounts().get(0).getTransactions().size() - 1);
    Transaction reversedTransaction = completedTransaction;
    reversedTransaction.reverseTransaction();

    // Assert
    assertEquals(TransactionStatus.COMPLETED, completedTransaction.getStatus());
    assertEquals(TransactionStatus.REVERSED, reversedTransaction.getStatus());
  }

  public class FakeDatabase extends Database {

    List<UserAccount> accounts = new ArrayList<>();

    @Override
    public UserAccount retrieveUserAccount(String sourceAccountID) {
      for (UserAccount user : accounts) {
        if (user.getUserID().equals(sourceAccountID)) {
          return user;
        }
      }
      return null;
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