package unit_tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import domain.accounts.Card;
import domain.accounts.Checking;
import domain.accounts.Savings;

public class AccountsTests {

  // ---------------- CHECKING ----------------

  @Test
  static void testChecking_applyMonthlyFee() {
    // Arrange
    Checking testChecking = new Checking("UT01", "CHK01", 200, 100, 50, 10);

    // Act
    testChecking.applyMonthlyFee();

    // Assert
    assertEquals(190, testChecking.getBalance());
  }

  // ---------------- SAVINGS ----------------

  @Test
  static void testSavings_calculateInterest() {
    // Arrange
    Savings testSavings = new Savings("UT02", "SAV01", 1000, 0.05, 0);

    // Act
    double result = testSavings.calculateInterest();

    // Assert
    assertEquals(50, result);
  }

  // ---------------- CARD ----------------

  @Test
  static void testCard_applyMonthlyFee() {
    // Arrange
    Card testCard = new Card("UT03", "CARD01", 100, 500, 0.05, 50);

    // Act
    testCard.applyMonthlyFee();
    double expected = (500 - 100) * 0.05;

    // Assert
    assertEquals(expected, testCard.getBalance());
  }
}
