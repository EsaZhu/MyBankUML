package domain.accounts;

import domain.bank.Branch;
import domain.enums.TransactionStatus;
import domain.transactions.Transaction;
import domain.users.Account;
import domain.users.UserAccount;

public class Card extends Account {

    private double cardLimit; // maximum credit limit
    private double interest;
    private double minimumPayment;

    public Card(String userID, String name, String email, String passwordHash, double balance, Branch branch,
            double cardLimit, double interest, double minimumPayment) {
        super(userID, name, email, passwordHash, balance, branch);
        this.cardLimit = cardLimit;
        this.interest = interest;
        this.minimumPayment = minimumPayment;
    }

    /**
     * Overridden deposit method to enforce card limit
     * 
     * @param amount
     */
    @Override
    public void deposit(double amount) {
        // invalid if amount is negative or if deposit exceeds card limit
        if (amount <= 0 || super.getBalance() + amount > this.cardLimit) {
            return;
        }
        Transaction depositTransaction = new Transaction(
                "TXN_D" + System.currentTimeMillis(),
                super.getUserID(),
                null,
                amount,
                "DEPOSIT",
                java.time.LocalDateTime.now(),
                TransactionStatus.PENDING);
        depositTransaction.execute();
    }

    /**
     * Method to apply monthly fee based on interest and minimum payment
     */
    public void applyMonthlyFee() {
        this.minimumPayment = (this.cardLimit - super.getBalance()) * interest; // interest on used credit
        this.withdraw(minimumPayment); // deduct new minimum payment from balance
    }

    /**
     * GUI display method to check if the card limit is exceeded and displays
     * available credit
     */
    public void checkCardLimit() {
        // TO BE REPLACED BY GUI
    }
}