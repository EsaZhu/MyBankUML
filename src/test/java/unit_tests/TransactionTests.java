package unit_tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import database.Database;
import domain.accounts.Checking;
import domain.accounts.Savings;
import domain.enums.TransactionStatus;
import domain.transactions.Transaction;
import domain.users.Account;
import domain.users.UserAccount;

public class TransactionTests {

    private FakeDatabase fakeDb;
    private UserAccount testUser;

    @BeforeEach
    void setup() throws Exception {
        fakeDb = new FakeDatabase();
        Account newAccount = new Savings("UT01", "A001", 250.0, 0.03, 100.0);
        testUser = new UserAccount("UT00", "UFO_believer", "John", "Doe", "hashedpassword", "B01", new ArrayList<>());
        testUser.getAccounts().add(newAccount);
        fakeDb.addAccount(testUser);
        Field instance = Database.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, fakeDb); // replace singleton
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
        // Arrange & Act & Assert
        assertEquals(0, testUser.getAccounts().get(0).getTransactions().size());
    }

    @Test
    void testReverseCompletedTransaction() {
        // Arrange
        Account sourceAccount = testUser.getAccounts().get(0);
        Account receiverAccount = new Checking("UT02", "A002", 500.0, 200.0, 50.0, 10.0);
        UserAccount receiverUser = new UserAccount("UT02", "UFO_denier", "Jane", "Doe", "hashedpassword", "B02",
                new ArrayList<>());
        receiverUser.getAccounts().add(receiverAccount);
        fakeDb.addAccount(receiverUser);

        // Act
        sourceAccount.transferFunds(receiverUser, receiverAccount.getAccountID(), 100.0);

        // Assert
        Transaction completedTransaction = sourceAccount.getTransactions().get(0);
        assertEquals(TransactionStatus.COMPLETED, completedTransaction.getStatus());
        completedTransaction.reverseTransaction();
        assertEquals(TransactionStatus.REVERSED, completedTransaction.getStatus());
        assertEquals(250.0, sourceAccount.getBalance());
        assertEquals(500.0, receiverAccount.getBalance());
    }

    public class FakeDatabase extends Database {

        List<UserAccount> accounts = new ArrayList<>();

        @Override
        public UserAccount retrieveUserAccount(String ID) {
            for (UserAccount user : accounts) {
                if (user.getUserID().equals(ID)) {
                    return user;
                }
                if (user.getAccounts() != null) {
                    for (Account acc : user.getAccounts()) {
                        if (acc.getAccountID().equals(ID)) {
                            return user;
                        }
                    }
                }
            }
            return null;
        }

        @Override
        public void addAccount(UserAccount account) {
            accounts.add(account);
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
