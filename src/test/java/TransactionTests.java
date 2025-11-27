
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import domain.accounts.Checking;
import domain.accounts.Savings;
import domain.enums.TransactionStatus;
import domain.transactions.Transaction;
import domain.users.Account;
import domain.users.UserAccount;

public class TransactionTests {

  @Test
  public void testTransactionWithInsufficientFunds() {
    // Arrange
    List<Account> accounts = new ArrayList<>();
    Account newAccount = new Savings("UT01", "A001", "SAV", 250.0, 0.03, 100.0);
    accounts.add(newAccount);
    UserAccount testUser = new UserAccount("UT01", "UFO_believer", "John", "Doe", "hashedpassword", "B01", accounts);

    // Act
    testUser.getAccounts().get(0).withdraw(1000.0);

    // Assert
    assertEquals(250.0, testUser.getAccounts().get(0).getBalance());
    assertEquals(TransactionStatus.FAILED, testUser.getAccounts().get(0).getTransactions().get(0).getStatus());
  }

  @Test
  public void testTransactionHistoryForAccountWithNoTransactions() {
    // Arrange
    List<Account> accounts = new ArrayList<>();
    Account newAccount = new Savings("UT01", "A001", "SAV", 250.0, 0.03, 100.0);
    accounts.add(newAccount);
    UserAccount testUser = new UserAccount("UT01", "UFO_believer", "John", "Doe", "hashedpassword", "B01", accounts);

    // Act & Assert
    assertEquals(0, testUser.getAccounts().get(0).getTransactions().size());
  }

  @Test
  public void testReverseCompletedTransaction() {
    // Arrange
    List<Account> sourceAccounts = new ArrayList<>();
    Account newSourceAccount = new Savings("UT01", "A001", "SAV", 250.0, 0.03, 100.0);
    sourceAccounts.add(newSourceAccount);
    UserAccount sourceUser = new UserAccount("UT01", "UFO_believer", "John", "Doe", "hashedpassword", "B01",
        sourceAccounts);

    List<Account> receiverAccounts = new ArrayList<>();
    Account newReceiverAccount = new Checking("UT02", "A002", "CHK", 500.0, 200.0, 50.0, 10.0);
    receiverAccounts.add(newReceiverAccount);
    UserAccount receiverUser = new UserAccount("UT02", "UFO_denier", "Jane", "Doe", "hashedpassword", "B02",
        receiverAccounts);

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
}