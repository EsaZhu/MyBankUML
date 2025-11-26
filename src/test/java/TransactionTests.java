
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import domain.enums.TransactionStatus;
import domain.transactions.Transaction;
import domain.users.UserAccount;

public class TransactionTests {
  
  @Test
  public void testTransactionWithInsufficientFunds(){
    UserAccount testUser = new UserAccount("UT01", "John Doe", "johndoe@gmail.com", "hashedpassword", 250.0, null);
    testUser.withdraw(1000.0);
    assertEquals(250.0, testUser.getBalance());
    assertEquals(TransactionStatus.FAILED, testUser.getTransactionHistory().get(0).getStatus());
  }

  @Test
  public void testTransactionHistoryForAccountWithNoTransactions(){
    UserAccount testUser = new UserAccount("UT01", "John Doe", "johndoe@gmail.com", "hashedpassword", 250.0, null);
    assertEquals(0 , testUser.getTransactionHistory().size());
  }

  @Test
  public void testReverseCompletedTransaction(){
    UserAccount testSourceUser = new UserAccount("UT01", "John Doe", "johndoe@gmail.com", "hashedpassword", 250.0, null);
    UserAccount testReceiverUser = new UserAccount("UT02", "Jane Doe", "janedoe@gmail.com", "hashedpassword", 250.0, null);
    testSourceUser.transferFunds(testReceiverUser, 100.0);
    Transaction completedTransaction = testSourceUser.getTransactionHistory().get(testSourceUser.getTransactionHistory().size() - 1);
    Transaction reversedTransaction = completedTransaction;
    reversedTransaction.reverseTransaction();
    assertEquals(TransactionStatus.COMPLETED, completedTransaction.getStatus());
  }
}