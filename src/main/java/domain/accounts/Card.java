package domain.accounts;

import domain.bank.Branch;
import domain.users.UserAccount;

public class Card extends UserAccount {

    private double cardLimit;
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
     * Method to apply monthly fee based on interest and minimum payment
     */
    public void applyMonthlyFee() {
        System.out.println("Applying monthly fee for card account matched with credit interest");
        this.minimumPayment = 15.0 + (this.cardLimit - this.balance) * interest; // flat rate + interest on used credit
        this.withdraw(minimumPayment); // deduct new minimum payment from balance
    }

    /**
     * Method to check if the card limit is exceeded and displays available credit
     */
    public void checkCardLimit() {
        System.out.println("Checking Limit for card account");
        System.out.println("Current limit: " + this.cardLimit);
        System.out.println("Available credit: " + (this.cardLimit - this.balance));
        if (this.cardLimit - this.balance < 0) {
            System.out.println("Card limit exceeded!");
        } else {
            System.out.println("Card limit is within the allowed range.");
        }
    }
}