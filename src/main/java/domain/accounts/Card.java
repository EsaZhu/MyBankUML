package domain.accounts;

import domain.bank.Branch;
import domain.users.UserAccount;

public class Card extends UserAccount {

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
     * @param amount
     */
    @Override
    public void deposit(double amount){
        // invalid if amount is negative or if deposit exceeds card limit
        if (amount <= 0 || this.balance + amount > this.cardLimit) {
            return;
        }
        this.balance += amount;
    }

    /**
     * Method to apply monthly fee based on interest and minimum payment
     */
    public void applyMonthlyFee() {
        System.out.println("Applying monthly fee for card account matched with credit interest");
        this.minimumPayment = (this.cardLimit - this.balance) * interest; // interest on used credit
        this.withdraw(minimumPayment); // deduct new minimum payment from balance (possible issues with balance being at most the card limit)
    }

    /**
     * GUI display method to check if the card limit is exceeded and displays available credit
     */
    public void checkCardLimit() {
        // TO BE REPLACED BY GUI
    }
}