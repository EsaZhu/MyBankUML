package domain.accounts;

import domain.users.UserAccount;

public class Card extends UserAccount {

    private double cardLimit; // maximum credit limit
    private double interest;
    private double minimumPayment;

    public Card(String userID, String name, String email, String passwordHash, double balance, domain.bank.Branch branch,
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
        super.deposit(amount);
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
