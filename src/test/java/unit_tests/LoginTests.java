package unit_tests;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import application.Login;
import database.Database;
import domain.enums.UserRole;
import domain.users.IUser;
import domain.users.UserAccount;

public class LoginTests {

  private FakeDatabase fakeDb;
  private Login login;
  private UserAccount testUser;

  @BeforeEach
  void setup() throws Exception {
    fakeDb = new FakeDatabase();
    testUser = new UserAccount(
        "U001",
        "dragonreborn",
        "Rand",
        "al'Thor",
        "hashedpassword",
        "BR01",
        null);

    fakeDb.addUser(testUser);
    Field instance = Database.class.getDeclaredField("instance");
    instance.setAccessible(true);
    instance.set(null, fakeDb); // replace singleton

    login = new Login(fakeDb);
  }

  @Test
  void testSuccessfulLogin() {
    // Arrange & Act
    boolean result = login.login("customer1", "Password123");

    // Assert
    assertTrue(result);
    assertEquals(UserRole.USER, login.getRole());
  }

  @Test
  void testFailedLoginBadPassword() {
    // Arrange & Act
    boolean result = login.login("customer1", "WrongPassword");

    // Assert
    assertFalse(result);
    assertEquals(UserRole.NONE, login.getRole());
  }

  @Test
  void testLoginFailsWithEmptyUsername() {
    // Arrange & Act
    boolean result = login.login("", "Password123");

    // Assert
    assertFalse(result);
    assertEquals(UserRole.NONE, login.getRole());
  }

  @Test
  void testLoginFailsWithEmptyPassword() {
    // Arrange & Act
    boolean result = login.login("customer1", "");

    // Assert
    assertFalse(result);
    assertEquals(UserRole.NONE, login.getRole());
  }

  @Test
  void testLoginFailsIfDatabaseUnavailable() throws Exception {
    // Arrange
    fakeDb.throwError = true;

    // Act
    boolean result = login.login("customer1", "Password123");

    // Assert
    assertFalse(result);
    assertEquals(UserRole.NONE, login.getRole());
  }

  // --------------------- Fake Database Mock ---------------------
  public class FakeDatabase extends Database {

    private UserAccount storedUser = null;
    public boolean throwError = false;

    @Override
    public IUser retrieveUser(String ID) {
      if (throwError) {
        throw new RuntimeException("DATABASE OFFLINE");
      }
      if (storedUser != null && storedUser.getUsername().equals(ID)) {
        return storedUser;
      }
      return null;
    }

    public void addUser(UserAccount user) {
      this.storedUser = user;
    }
  }
}
